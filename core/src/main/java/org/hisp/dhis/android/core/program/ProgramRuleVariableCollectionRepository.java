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
package org.hisp.dhis.android.core.program;

import org.hisp.dhis.android.core.arch.repositories.children.ChildrenAppender;
import org.hisp.dhis.android.core.arch.repositories.children.ChildrenSelection;
import org.hisp.dhis.android.core.arch.repositories.collection.ReadOnlyIdentifiableCollectionRepositoryImpl;
import org.hisp.dhis.android.core.arch.repositories.filters.BooleanFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.EnumFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.FilterConnectorFactory;
import org.hisp.dhis.android.core.arch.repositories.filters.StringFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScopeItem;
import org.hisp.dhis.android.core.common.IdentifiableObjectStore;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public final class ProgramRuleVariableCollectionRepository extends ReadOnlyIdentifiableCollectionRepositoryImpl
        <ProgramRuleVariable, ProgramRuleVariableCollectionRepository> {

    @Inject
    ProgramRuleVariableCollectionRepository(final IdentifiableObjectStore<ProgramRuleVariable> store,
                                            final Collection<ChildrenAppender<ProgramRuleVariable>> childrenAppenders,
                                            final ChildrenSelection childrenSelection,
                                            final List<RepositoryScopeItem> scope) {
        super(store, childrenAppenders, childrenSelection, scope, new FilterConnectorFactory<>(scope,
                updatedScope -> new ProgramRuleVariableCollectionRepository(store, childrenAppenders,
                        childrenSelection, updatedScope)));
    }


    public BooleanFilterConnector<ProgramRuleVariableCollectionRepository> byUseCodeForOptionSet() {
        return cf.bool(ProgramRuleVariableFields.USE_CODE_FOR_OPTION_SET);
    }

    public StringFilterConnector<ProgramRuleVariableCollectionRepository> byProgramUid() {
        return cf.string(ProgramRuleVariableFields.PROGRAM);
    }

    public StringFilterConnector<ProgramRuleVariableCollectionRepository> byProgramStageUid() {
        return cf.string(ProgramRuleVariableFields.PROGRAM_STAGE);
    }

    public StringFilterConnector<ProgramRuleVariableCollectionRepository> byDataElementUid() {
        return cf.string(ProgramRuleVariableFields.DATA_ELEMENT);
    }

    public StringFilterConnector<ProgramRuleVariableCollectionRepository> byTrackedEntityAttributeUid() {
        return cf.string(ProgramRuleVariableFields.TRACKED_ENTITY_ATTRIBUTE);
    }

    public EnumFilterConnector<ProgramRuleVariableCollectionRepository,
            ProgramRuleVariableSourceType> byProgramRuleVariableSourceType() {

        return cf.enumC(ProgramRuleVariableFields.PROGRAM_RULE_VARIABLE_SOURCE_TYPE);
    }
}