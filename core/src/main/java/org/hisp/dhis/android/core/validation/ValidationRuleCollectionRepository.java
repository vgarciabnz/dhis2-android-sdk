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
package org.hisp.dhis.android.core.validation;

import static org.hisp.dhis.android.core.validation.ValidationRuleTableInfo.Columns;

import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender;
import org.hisp.dhis.android.core.arch.repositories.collection.internal.ReadOnlyNameableCollectionRepositoryImpl;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.BooleanFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.EnumFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.FilterConnectorFactory;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.IdentifiableColumns;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.validation.internal.ValidationRuleStore;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public final class ValidationRuleCollectionRepository
        extends ReadOnlyNameableCollectionRepositoryImpl<ValidationRule, ValidationRuleCollectionRepository> {

    @Inject
    ValidationRuleCollectionRepository(final ValidationRuleStore store,
                                       final Map<String, ChildrenAppender<ValidationRule>> childrenAppenders,
                                       final RepositoryScope scope) {
        super(store, childrenAppenders, scope, new FilterConnectorFactory<>(scope,
                s -> new ValidationRuleCollectionRepository(store, childrenAppenders, s)));
    }

    public StringFilterConnector<ValidationRuleCollectionRepository> byInstruction() {
        return cf.string(Columns.INSTRUCTION);
    }

    public EnumFilterConnector<ValidationRuleCollectionRepository, ValidationRuleImportance> byImportance() {
        return cf.enumC(Columns.IMPORTANCE);
    }

    public EnumFilterConnector<ValidationRuleCollectionRepository, ValidationRuleOperator> byOperator() {
        return cf.enumC(Columns.OPERATOR);
    }

    public EnumFilterConnector<ValidationRuleCollectionRepository, PeriodType> byPeriodType() {
        return cf.enumC(Columns.PERIOD_TYPE);
    }

    public BooleanFilterConnector<ValidationRuleCollectionRepository> bySkipFormValidation() {
        return cf.bool(Columns.SKIP_FORM_VALIDATION);
    }

    public StringFilterConnector<ValidationRuleCollectionRepository> byLeftSideExpression() {
        return cf.string(Columns.LEFT_SIDE_EXPRESSION);
    }

    public StringFilterConnector<ValidationRuleCollectionRepository> byLeftSideDescription() {
        return cf.string(Columns.LEFT_SIDE_DESCRIPTION);
    }

    public EnumFilterConnector<ValidationRuleCollectionRepository, MissingValueStrategy>
    byLeftSideMissingValueStrategy() {
        return cf.enumC(Columns.LEFT_SIDE_MISSING_VALUE_STRATEGY);
    }

    public StringFilterConnector<ValidationRuleCollectionRepository> byRightSideExpression() {
        return cf.string(Columns.RIGHT_SIDE_EXPRESSION);
    }

    public StringFilterConnector<ValidationRuleCollectionRepository> byRightSideDescription() {
        return cf.string(Columns.RIGHT_SIDE_DESCRIPTION);
    }

    public EnumFilterConnector<ValidationRuleCollectionRepository, MissingValueStrategy>
    byRightSideMissingValueStrategy() {
        return cf.enumC(Columns.RIGHT_SIDE_MISSING_VALUE_STRATEGY);
    }

    public StringFilterConnector<ValidationRuleCollectionRepository> byOrganisationUnitLevels() {
        return cf.string(Columns.ORGANISATION_UNIT_LEVELS);
    }

    public ValidationRuleCollectionRepository byDataSetUids(List<String> dataSetUids) {
        return cf.subQuery(IdentifiableColumns.UID).inLinkTable(
                DataSetValidationRuleLinkTableInfo.TABLE_INFO.name(),
                DataSetValidationRuleLinkTableInfo.Columns.VALIDATION_RULE,
                DataSetValidationRuleLinkTableInfo.Columns.DATA_SET,
                dataSetUids);
    }
}
