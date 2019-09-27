/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.core.trackedentity.internal;

import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor;
import org.hisp.dhis.android.core.arch.call.D2Progress;
import org.hisp.dhis.android.core.arch.call.internal.D2ProgressManager;
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder;
import org.hisp.dhis.android.core.arch.db.stores.internal.ObjectWithoutUidStore;
import org.hisp.dhis.android.core.arch.helpers.CollectionsHelper;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.BaseDataModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.internal.EnrollmentStore;
import org.hisp.dhis.android.core.enrollment.note.Note;
import org.hisp.dhis.android.core.enrollment.note.internal.NoteToPostTransformer;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.internal.EventStore;
import org.hisp.dhis.android.core.imports.internal.TEIWebResponse;
import org.hisp.dhis.android.core.imports.internal.TEIWebResponseHandler;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.Relationship229Compatible;
import org.hisp.dhis.android.core.relationship.RelationshipCollectionRepository;
import org.hisp.dhis.android.core.relationship.RelationshipHelper;
import org.hisp.dhis.android.core.relationship.internal.RelationshipDHISVersionManager;
import org.hisp.dhis.android.core.relationship.internal.RelationshipItemStore;
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.systeminfo.internal.SystemInfoModuleDownloader;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.Reusable;
import io.reactivex.Observable;
import io.reactivex.Single;

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.ExcessiveImports"})
@Reusable
public final class TrackedEntityInstancePostCall {
    // internal modules
    private final DHISVersionManager versionManager;
    private final RelationshipDHISVersionManager relationshipDHISVersionManager;
    private final RelationshipCollectionRepository relationshipRepository;

    // service
    private final TrackedEntityInstanceService trackedEntityInstanceService;

    // stores
    private final TrackedEntityInstanceStore trackedEntityInstanceStore;
    private final EnrollmentStore enrollmentStore;
    private final EventStore eventStore;
    private final TrackedEntityDataValueStore trackedEntityDataValueStore;
    private final TrackedEntityAttributeValueStore trackedEntityAttributeValueStore;
    private final RelationshipItemStore relationshipItemStore;
    private final ObjectWithoutUidStore<Note> noteStore;

    private final TEIWebResponseHandler teiWebResponseHandler;

    private final APICallExecutor apiCallExecutor;
    private final SystemInfoModuleDownloader systemInfoDownloader;

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Inject
    TrackedEntityInstancePostCall(@NonNull DHISVersionManager versionManager,
                                  @NonNull RelationshipDHISVersionManager relationshipDHISVersionManager,
                                  @NonNull RelationshipCollectionRepository relationshipRepository,
                                  @NonNull TrackedEntityInstanceService trackedEntityInstanceService,
                                  @NonNull TrackedEntityInstanceStore trackedEntityInstanceStore,
                                  @NonNull EnrollmentStore enrollmentStore,
                                  @NonNull EventStore eventStore,
                                  @NonNull TrackedEntityDataValueStore trackedEntityDataValueStore,
                                  @NonNull TrackedEntityAttributeValueStore trackedEntityAttributeValueStore,
                                  @NonNull RelationshipItemStore relationshipItemStore,
                                  @NonNull ObjectWithoutUidStore<Note> noteStore,
                                  @NonNull TEIWebResponseHandler teiWebResponseHandler,
                                  @NonNull APICallExecutor apiCallExecutor, SystemInfoModuleDownloader systemInfoDownloader) {
        this.versionManager = versionManager;
        this.relationshipDHISVersionManager = relationshipDHISVersionManager;
        this.relationshipRepository = relationshipRepository;
        this.trackedEntityInstanceService = trackedEntityInstanceService;
        this.trackedEntityInstanceStore = trackedEntityInstanceStore;
        this.enrollmentStore = enrollmentStore;
        this.eventStore = eventStore;
        this.trackedEntityDataValueStore = trackedEntityDataValueStore;
        this.trackedEntityAttributeValueStore = trackedEntityAttributeValueStore;
        this.relationshipItemStore = relationshipItemStore;
        this.noteStore = noteStore;
        this.teiWebResponseHandler = teiWebResponseHandler;
        this.apiCallExecutor = apiCallExecutor;
        this.systemInfoDownloader = systemInfoDownloader;
    }

    public Observable<D2Progress> uploadTrackedEntityInstances(
            List<TrackedEntityInstance> filteredTrackedEntityInstances) {
        return Observable.defer(() -> {
            List<List<TrackedEntityInstance>> trackedEntityInstancesToPost =
                getPartitionsToSync(filteredTrackedEntityInstances);

            // if size is 0, then no need to do network request
            if (trackedEntityInstancesToPost.isEmpty()) {
                return Observable.empty();
            } else {
                D2ProgressManager progressManager = new D2ProgressManager(2);

                Single<D2Progress> systemInfoDownload = systemInfoDownloader.downloadMetadata().toSingle(() ->
                        progressManager.increaseProgress(SystemInfo.class, false));

                return systemInfoDownload.flatMapObservable(systemInfoProgress -> Observable.create(emitter -> {
                    emitter.onNext(systemInfoProgress);

                    String strategy;
                    if (versionManager.is2_29()) {
                        strategy = "CREATE_AND_UPDATE";
                    } else {
                        strategy = "SYNC";
                    }

                    for (List<TrackedEntityInstance> partition : trackedEntityInstancesToPost) {
                        TrackedEntityInstancePayload trackedEntityInstancePayload =
                                TrackedEntityInstancePayload.create(partition);

                        TEIWebResponse webResponse = apiCallExecutor.executeObjectCallWithAcceptedErrorCodes(
                                trackedEntityInstanceService.postTrackedEntityInstances(
                                        trackedEntityInstancePayload, strategy),
                                Collections.singletonList(409), TEIWebResponse.class);
                        teiWebResponseHandler.handleWebResponse(webResponse);
                    }

                    emitter.onNext(progressManager.increaseProgress(TrackedEntityInstance.class, true));
                    emitter.onComplete();
                }));
            }
        });
    }

    @NonNull
    List<List<TrackedEntityInstance>> getPartitionsToSync(List<TrackedEntityInstance> filteredTrackedEntityInstances) {
        Map<String, List<TrackedEntityDataValue>> dataValueMap =
                trackedEntityDataValueStore.queryTrackerTrackedEntityDataValues();
        Map<String, List<Event>> eventMap = eventStore.queryEventsAttachedToEnrollmentToPost();
        Map<String, List<Enrollment>> enrollmentMap = enrollmentStore.queryEnrollmentsToPost();
        Map<String, List<TrackedEntityAttributeValue>> attributeValueMap =
                trackedEntityAttributeValueStore.queryTrackedEntityAttributeValueToPost();
        String whereNotesClause = new WhereClauseBuilder()
                .appendKeyStringValue(BaseDataModel.Columns.STATE, State.TO_POST).build();
        List<Note> notes = noteStore.selectWhere(whereNotesClause);

        List<TrackedEntityInstance> targetTrackedEntityInstances;
        if (filteredTrackedEntityInstances == null) {
            targetTrackedEntityInstances = trackedEntityInstanceStore.queryTrackedEntityInstancesToSync();
        } else {
            targetTrackedEntityInstances = filteredTrackedEntityInstances;
        }

        List<List<TrackedEntityInstance>> trackedEntityInstancesToSync
                = getPagedTrackedEntityInstances(targetTrackedEntityInstances);

        List<List<TrackedEntityInstance>> trackedEntityInstancesRecreated = new ArrayList<>();

        for (List<TrackedEntityInstance> partition : trackedEntityInstancesToSync) {
            List<TrackedEntityInstance> partitionRecreated = new ArrayList<>();
            for (TrackedEntityInstance trackedEntityInstance : partition) {
                TrackedEntityInstance recreatedTrackedEntityInstance = recreateTrackedEntityInstance(
                        trackedEntityInstance, dataValueMap, eventMap, enrollmentMap, attributeValueMap, notes);

                partitionRecreated.add(recreatedTrackedEntityInstance);
            }
            trackedEntityInstancesRecreated.add(partitionRecreated);
        }

        markPartitionsAsUploading(trackedEntityInstancesRecreated);

        return trackedEntityInstancesRecreated;
    }

    private List<List<TrackedEntityInstance>> getPagedTrackedEntityInstances(
            List<TrackedEntityInstance> filteredTrackedEntityInstances) {
        List<String> includedUids = new ArrayList<>();

        List<Set<TrackedEntityInstance>> partitions =
                CollectionsHelper.setPartition(filteredTrackedEntityInstances, DEFAULT_PAGE_SIZE);

        List<List<TrackedEntityInstance>> partitionsWithRelationships = new ArrayList<>();

        for (Set<TrackedEntityInstance> partition : partitions) {
            List<TrackedEntityInstance> partitionWithoutDuplicates = UidsHelper.excludeUids(partition, includedUids);
            List<TrackedEntityInstance> partitionWithRelationships =
                    getTrackedEntityInstancesWithRelationships(partitionWithoutDuplicates, includedUids);

            partitionsWithRelationships.add(partitionWithRelationships);
            includedUids.addAll(UidsHelper.getUidsList(partitionWithRelationships));
        }

        return partitionsWithRelationships;
    }

    private List<TrackedEntityInstance> getTrackedEntityInstancesWithRelationships(
            List<TrackedEntityInstance> filteredTrackedEntityInstances, List<String> excludedUids) {
        List<TrackedEntityInstance> trackedEntityInstancesInDBToSync =
                trackedEntityInstanceStore.queryTrackedEntityInstancesToSync();

        List<String> filteredUids = UidsHelper.getUidsList(filteredTrackedEntityInstances);
        List<String> teiUidsToPost =
                UidsHelper.getUidsList(trackedEntityInstanceStore.queryTrackedEntityInstancesToPost());
        List<String> relatedTeisToPost = new ArrayList<>();
        List<String> internalRelatedTeis = filteredUids;

        do {
            List<String> relatedTeiUids = relationshipItemStore.getRelatedTeiUids(internalRelatedTeis);

            relatedTeiUids.retainAll(teiUidsToPost);

            relatedTeiUids.removeAll(filteredUids);
            relatedTeiUids.removeAll(relatedTeisToPost);
            relatedTeiUids.removeAll(excludedUids);

            relatedTeisToPost.addAll(relatedTeiUids);
            internalRelatedTeis = relatedTeiUids;
        }
        while (!internalRelatedTeis.isEmpty());

        for (TrackedEntityInstance trackedEntityInstanceInDB : trackedEntityInstancesInDBToSync) {
            if (relatedTeisToPost.contains(trackedEntityInstanceInDB.uid())) {
                filteredTrackedEntityInstances.add(trackedEntityInstanceInDB);
            }
        }
        return filteredTrackedEntityInstances;
    }

    @NonNull
    private TrackedEntityInstance recreateTrackedEntityInstance(
            TrackedEntityInstance trackedEntityInstance,
            Map<String, List<TrackedEntityDataValue>> dataValueMap,
            Map<String, List<Event>> eventMap,
            Map<String, List<Enrollment>> enrollmentMap,
            Map<String, List<TrackedEntityAttributeValue>> attributeValueMap,
            List<Note> notes) {

        String trackedEntityInstanceUid = trackedEntityInstance.uid();
        List<Enrollment> enrollmentsRecreated = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentMap.get(trackedEntityInstanceUid);
        List<TrackedEntityAttributeValue> emptyAttributeValueList = new ArrayList<>();

        if (enrollments != null) {
            for (Enrollment enrollment : enrollments) {
                List<Event> eventRecreated = new ArrayList<>();
                List<Event> eventsForEnrollment = eventMap.get(enrollment.uid());
                if (eventsForEnrollment != null) {
                    for (Event event : eventsForEnrollment) {
                        List<TrackedEntityDataValue> dataValuesForEvent = dataValueMap.get(event.uid());
                        if (versionManager.is2_30()) {
                            eventRecreated.add(event.toBuilder()
                                    .trackedEntityDataValues(dataValuesForEvent)
                                    .geometry(null)
                                    .build());
                        } else {
                            eventRecreated.add(event.toBuilder().trackedEntityDataValues(dataValuesForEvent).build());
                        }
                    }
                }

                List<Note> notesForEnrollment = new ArrayList<>();
                NoteToPostTransformer transformer = new NoteToPostTransformer(versionManager);
                for (Note note : notes) {
                    if (enrollment.uid().equals(note.enrollment())) {
                        notesForEnrollment.add(transformer.transform(note));
                    }
                }

                enrollmentsRecreated.add(enrollment.toBuilder()
                        .events(eventRecreated)
                        .notes(notesForEnrollment)
                        .build());
            }
        }

        List<TrackedEntityAttributeValue> attributeValues = attributeValueMap.get(trackedEntityInstanceUid);

        List<Relationship> dbRelationships =
                relationshipRepository.getByItem(RelationshipHelper.teiItem(trackedEntityInstance.uid()));
        List<Relationship229Compatible> versionAwareRelationships =
                relationshipDHISVersionManager.to229Compatible(dbRelationships, trackedEntityInstance.uid());

        return trackedEntityInstance.toBuilder()
                .trackedEntityAttributeValues(attributeValues == null ? emptyAttributeValueList : attributeValues)
                .relationships(versionAwareRelationships)
                .enrollments(enrollmentsRecreated)
                .build();
    }

    private void markPartitionsAsUploading(List<List<TrackedEntityInstance>> partitions) {
        List<String> trackedEntityInstancesUids = new ArrayList<>();
        List<String> enrollmentUids = new ArrayList<>();
        List<String> eventUids = new ArrayList<>();

        for (List<TrackedEntityInstance> partition : partitions) {
            for (TrackedEntityInstance instance : partition) {
                trackedEntityInstancesUids.add(instance.uid());
                for (Enrollment enrollment : instance.enrollments()) {
                    enrollmentUids.add(enrollment.uid());
                    for (Event event : enrollment.events()) {
                        eventUids.add(event.uid());
                    }
                }
            }
        }

        trackedEntityInstanceStore.setState(trackedEntityInstancesUids, State.UPLOADING);
        enrollmentStore.setState(enrollmentUids, State.UPLOADING);
        eventStore.setState(eventUids, State.UPLOADING);
    }
}