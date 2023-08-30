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
package org.hisp.dhis.android.core.trackedentity

import org.hisp.dhis.android.core.enrollment.NewTrackerImporterEnrollmentTransformer
import org.hisp.dhis.android.core.relationship.NewTrackerImporterRelationshipTransformer
import org.hisp.dhis.android.core.trackedentity.ownership.NewTrackerImporterProgramOwnerTransformer

internal object NewTrackerImporterTrackedEntityTransformer {
    fun transform(
        o: TrackedEntityInstance,
        tetAttributeMap: Map<String, List<String>>,
    ): NewTrackerImporterTrackedEntity {
        val teiAttributes = o.trackedEntityAttributeValues() ?: emptyList()
        val typeAttributes = tetAttributeMap[o.trackedEntityType()] ?: emptyList()
        val teiTypeAttributes = teiAttributes
            .filter { typeAttributes.contains(it.trackedEntityAttribute()) }
            .map { NewTrackerImporterTrackedEntityAttributeValueTransformer.transform(it) }
        val programOwners = o.programOwners()?.map { NewTrackerImporterProgramOwnerTransformer.transform(it) }

        return NewTrackerImporterTrackedEntity.builder()
            .id(o.id())
            .uid(o.uid())
            .deleted(o.deleted())
            .createdAt(o.created())
            .updatedAt(o.lastUpdated())
            .createdAtClient(o.createdAtClient())
            .updatedAtClient(o.lastUpdatedAtClient())
            .organisationUnit(o.organisationUnit())
            .trackedEntityType(o.trackedEntityType())
            .geometry(o.geometry())
            .syncState(o.syncState())
            .aggregatedSyncState(o.aggregatedSyncState())
            .trackedEntityAttributeValues(teiTypeAttributes)
            .programOwners(programOwners)
            .build()
    }

    fun deTransform(
        o: NewTrackerImporterTrackedEntity,
    ): TrackedEntityInstance {
        val enrollments = o.enrollments()?.map {
            NewTrackerImporterEnrollmentTransformer.deTransform(it)
        }

        val teiAttributeValues = o.trackedEntityAttributeValues()?.map { a ->
            NewTrackerImporterTrackedEntityAttributeValueTransformer.deTransform(a)
        } ?: emptyList()

        val enrollmentAttributeValues = o.enrollments()?.flatMap {
            it.attributes()?.map { a ->
                NewTrackerImporterTrackedEntityAttributeValueTransformer.deTransform(a)
            } ?: emptyList()
        } ?: emptyList()

        val attributes = (teiAttributeValues + enrollmentAttributeValues).distinctBy { it.trackedEntityAttribute() }

        val relationships = o.relationships()?.map { NewTrackerImporterRelationshipTransformer.deTransform(it) }
        val programOwners = o.programOwners()?.map { NewTrackerImporterProgramOwnerTransformer.deTransform(it) }

        return TrackedEntityInstance.builder()
            .id(o.id())
            .uid(o.uid())
            .deleted(o.deleted())
            .created(o.createdAt())
            .lastUpdated(o.updatedAt())
            .createdAtClient(o.createdAtClient())
            .lastUpdatedAtClient(o.updatedAtClient())
            .organisationUnit(o.organisationUnit())
            .trackedEntityType(o.trackedEntityType())
            .geometry(o.geometry())
            .syncState(o.syncState())
            .aggregatedSyncState(o.aggregatedSyncState())
            .enrollments(enrollments)
            .trackedEntityAttributeValues(attributes)
            .programOwners(programOwners)
            .relationships(relationships)
            .build()
    }
}
