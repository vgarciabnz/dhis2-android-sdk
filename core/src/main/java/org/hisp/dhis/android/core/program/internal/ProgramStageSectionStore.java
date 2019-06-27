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

package org.hisp.dhis.android.core.program.internal;

import android.database.sqlite.SQLiteStatement;

import org.hisp.dhis.android.core.arch.db.stores.binders.internal.IdentifiableStatementBinder;
import org.hisp.dhis.android.core.arch.db.stores.binders.internal.StatementBinder;
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStore;
import org.hisp.dhis.android.core.arch.db.stores.internal.StoreFactory;
import org.hisp.dhis.android.core.arch.db.stores.projections.internal.SingleParentChildProjection;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.program.ProgramStageSectionTableInfo;

import androidx.annotation.NonNull;

import static org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.sqLiteBind;

final class ProgramStageSectionStore {

    private static StatementBinder<ProgramStageSection> BINDER =
            new IdentifiableStatementBinder<ProgramStageSection>() {
        @Override
        public void bindToStatement(@NonNull ProgramStageSection o, @NonNull SQLiteStatement sqLiteStatement) {
            super.bindToStatement(o, sqLiteStatement);
            sqLiteBind(sqLiteStatement, 7, o.sortOrder());
            sqLiteBind(sqLiteStatement, 8, UidsHelper.getUidOrNull(o.programStage()));
            sqLiteBind(sqLiteStatement, 9, ProgramStageSectionRenderingHelper.desktopRenderType(o.renderType()));
            sqLiteBind(sqLiteStatement, 10, ProgramStageSectionRenderingHelper.mobileRenderType(o.renderType()));
        }
    };

    static final SingleParentChildProjection CHILD_PROJECTION = new SingleParentChildProjection(
            ProgramStageSectionTableInfo.TABLE_INFO, ProgramStageSectionTableInfo.Columns.PROGRAM_STAGE);

    private ProgramStageSectionStore() {}

    static IdentifiableObjectStore<ProgramStageSection> create(DatabaseAdapter databaseAdapter) {
        return StoreFactory.objectWithUidStore(databaseAdapter, ProgramStageSectionTableInfo.TABLE_INFO,
                BINDER, ProgramStageSection::create);
    }
}