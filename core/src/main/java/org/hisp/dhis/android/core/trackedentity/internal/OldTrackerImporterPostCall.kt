/*
 *  Copyright (c) 2004-2023, University of Oslo
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.android.core.trackedentity.internal

import dagger.Reusable
import java.net.HttpURLConnection.HTTP_CONFLICT
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.hisp.dhis.android.core.arch.api.executors.internal.CoroutineAPICallExecutor
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.arch.call.internal.D2ProgressManager
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.internal.EventImportHandler
import org.hisp.dhis.android.core.event.internal.EventPayload
import org.hisp.dhis.android.core.event.internal.EventService
import org.hisp.dhis.android.core.imports.internal.EventWebResponse
import org.hisp.dhis.android.core.imports.internal.TEIWebResponse
import org.hisp.dhis.android.core.imports.internal.TEIWebResponseHandler
import org.hisp.dhis.android.core.imports.internal.TEIWebResponseHandlerSummary
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.relationship.internal.RelationshipPostCall
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.tracker.importer.internal.TrackerImporterBreakTheGlassHelper
import org.hisp.dhis.android.core.tracker.importer.internal.TrackerImporterProgramOwnerPostCall

@Reusable
@Suppress("LongParameterList")
internal class OldTrackerImporterPostCall @Inject internal constructor(
    private val trackerImporterPayloadGenerator: OldTrackerImporterPayloadGenerator,
    private val trackerStateManager: TrackerPostStateManager,
    private val trackedEntityInstanceService: TrackedEntityInstanceService,
    private val eventService: EventService,
    private val teiWebResponseHandler: TEIWebResponseHandler,
    private val eventImportHandler: EventImportHandler,
    private val coroutineAPICallExecutor: CoroutineAPICallExecutor,
    private val relationshipPostCall: RelationshipPostCall,
    private val fileResourcePostCall: OldTrackerImporterFileResourcesPostCall,
    private val programOwnerPostCall: TrackerImporterProgramOwnerPostCall,
    private val breakTheGlassHelper: TrackerImporterBreakTheGlassHelper
) {

    fun uploadTrackedEntityInstances(
        trackedEntityInstances: List<TrackedEntityInstance>
    ): Flow<D2Progress> {
        val payload = trackerImporterPayloadGenerator.getTrackedEntityInstancePayload(trackedEntityInstances)
        return uploadPayload(payload)
    }

    fun uploadEvents(
        events: List<Event>
    ): Flow<D2Progress> {
        val payload = trackerImporterPayloadGenerator.getEventPayload(events)
        return uploadPayload(payload)
    }

    private fun uploadPayload(
        payload: OldTrackerImporterPayload
    ): Flow<D2Progress> = flow {
        val partitionedRelationships = payload.relationships.partition { it.deleted()!! }

        emitAll(programOwnerPostCall.uploadProgramOwners(payload.programOwners, onlyExistingTeis = true))
        emitAll(relationshipPostCall.deleteRelationships(partitionedRelationships.first))
        emitAll(postTrackedEntityInstances(payload.trackedEntityInstances))
        emitAll(postEvents(payload.events))
        emitAll(relationshipPostCall.postRelationships(partitionedRelationships.second))
        emitAll(programOwnerPostCall.uploadProgramOwners(payload.programOwners, onlyExistingTeis = false))
    }

    @Suppress("TooGenericExceptionCaught")
    private fun postTrackedEntityInstances(
        trackedEntityInstances: List<TrackedEntityInstance>
    ): Flow<D2Progress> = flow {
        val progressManager = D2ProgressManager(null)
        val teiPartitions = trackedEntityInstances
            .chunked(TrackedEntityInstanceService.DEFAULT_PAGE_SIZE)
            .map { partition -> fileResourcePostCall.uploadTrackedEntityFileResources(partition) }
            .filter { it.items.isNotEmpty() }

        for (partition in teiPartitions) {
            try {
                val summary = postPartition(partition.items)
                val glassErrors = breakTheGlassHelper.getGlassErrors(summary, partition.items)

                if (glassErrors.isNotEmpty()) {
                    breakTheGlassHelper.fakeBreakGlass(glassErrors)
                    val breakGlassSummary = postPartition(glassErrors)

                    summary.update(breakGlassSummary)
                }

                fileResourcePostCall.updateFileResourceStates(partition.fileResources)

                emit(progressManager.increaseProgress(TrackedEntityInstance::class.java, false))
            } catch (e: Exception) {
                trackerStateManager.restorePayloadStates(
                    trackedEntityInstances = partition.items,
                    fileResources = partition.fileResources
                )
                if (e is D2Error && e.isOffline) {
                    throw e
                } else {
                    emit(progressManager.increaseProgress(TrackedEntityInstance::class.java, false))
                }
            }
        }
    }

    private suspend fun postPartition(
        trackedEntityInstances: List<TrackedEntityInstance>
    ): TEIWebResponseHandlerSummary {
        trackerStateManager.setPayloadStates(
            trackedEntityInstances = trackedEntityInstances,
            forcedState = State.UPLOADING
        )
        val trackedEntityInstancePayload = TrackedEntityInstancePayload.create(trackedEntityInstances)

        val response = coroutineAPICallExecutor.wrap(
            storeError = true,
            acceptedErrorCodes = listOf(HTTP_CONFLICT),
            errorClass = TEIWebResponse::class.java
        ) {
            trackedEntityInstanceService.postTrackedEntityInstances(trackedEntityInstancePayload, "SYNC")
        }

        return response.getOrThrow().let { webResponse ->
            teiWebResponseHandler.handleWebResponse(webResponse, trackedEntityInstances)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun postEvents(
        events: List<Event>
    ): Flow<D2Progress> = flow {
        val progressManager = D2ProgressManager(null)

        if (events.isEmpty()) {
            emit(progressManager.increaseProgress(Event::class.java, true))
        } else {
            val validEvents = fileResourcePostCall.uploadEventsFileResources(events)

            val payload = EventPayload()
            payload.events = validEvents.items

            trackerStateManager.setPayloadStates(
                events = payload.events,
                forcedState = State.UPLOADING
            )

            val strategy = "SYNC"
            try {
                val webResponse = coroutineAPICallExecutor.wrap(
                    storeError = true,
                    acceptedErrorCodes = listOf(HTTP_CONFLICT),
                    errorClass = EventWebResponse::class.java
                ) {
                    eventService.postEvents(payload, strategy)
                }.getOrThrow()

                eventImportHandler.handleEventImportSummaries(
                    eventImportSummaries = webResponse.response()?.importSummaries(),
                    events = payload.events
                )

                fileResourcePostCall.updateFileResourceStates(validEvents.fileResources)

                emit(progressManager.increaseProgress(Event::class.java, true))
            } catch (e: Exception) {
                trackerStateManager.restorePayloadStates(
                    events = payload.events,
                    fileResources = validEvents.fileResources
                )
                throw e
            }
        }
    }
}
