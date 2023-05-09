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

package org.hisp.dhis.android.core.note;

import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStore;
import org.hisp.dhis.android.core.arch.handlers.internal.HandleAction;
import org.hisp.dhis.android.core.arch.handlers.internal.Transformer;
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender;
import org.hisp.dhis.android.core.arch.repositories.collection.internal.ReadWriteWithUidCollectionRepositoryImpl;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.EnumFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.FilterConnectorFactory;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.object.ReadOnlyObjectRepository;
import org.hisp.dhis.android.core.arch.repositories.object.ReadOnlyOneObjectRepositoryFinalImpl;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.arch.repositories.scope.internal.RepositoryScopeHelper;
import org.hisp.dhis.android.core.common.IdentifiableColumns;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.internal.DataStatePropagator;
import org.hisp.dhis.android.core.note.NoteTableInfo.Columns;

import java.util.Map;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public final class NoteCollectionRepository
        extends ReadWriteWithUidCollectionRepositoryImpl<Note, NoteCreateProjection, NoteCollectionRepository> {

    private final DataStatePropagator dataStatePropagator;

    @Inject
    NoteCollectionRepository(final IdentifiableObjectStore<Note> store,
                             final Map<String, ChildrenAppender<Note>> childrenAppenders,
                             final RepositoryScope scope,
                             final Transformer<NoteCreateProjection, Note> transformer,
                             final DataStatePropagator dataStatePropagator) {
        super(store, childrenAppenders, scope, transformer, new FilterConnectorFactory<>(scope,
                s -> new NoteCollectionRepository(store, childrenAppenders, s, transformer, dataStatePropagator)));
        this.dataStatePropagator = dataStatePropagator;
    }

    public StringFilterConnector<NoteCollectionRepository> byUid() {
        return cf.string(IdentifiableColumns.UID);
    }


    public EnumFilterConnector<NoteCollectionRepository, Note.NoteType> byNoteType() {
        return cf.enumC(Columns.NOTE_TYPE);
    }

    public StringFilterConnector<NoteCollectionRepository> byEventUid() {
        return cf.string(Columns.EVENT);
    }

    public StringFilterConnector<NoteCollectionRepository> byEnrollmentUid() {
        return cf.string(Columns.ENROLLMENT);
    }

    public StringFilterConnector<NoteCollectionRepository> byValue() {
        return cf.string(Columns.VALUE);
    }

    public StringFilterConnector<NoteCollectionRepository> byStoredBy() {
        return cf.string(Columns.STORED_BY);
    }

    public StringFilterConnector<NoteCollectionRepository> byStoredDate() {
        return cf.string(Columns.STORED_DATE);
    }

    public EnumFilterConnector<NoteCollectionRepository, State> bySyncState() {
        return cf.enumC(Columns.SYNC_STATE);
    }

    @Override
    public ReadOnlyObjectRepository<Note> uid(String uid) {
        RepositoryScope updatedScope = RepositoryScopeHelper.withUidFilterItem(scope, uid);
        return new ReadOnlyOneObjectRepositoryFinalImpl<>(store, childrenAppenders, updatedScope);
    }

    @Override
    protected void propagateState(Note note, HandleAction action) {
        dataStatePropagator.propagateNoteCreation(note);
    }
}