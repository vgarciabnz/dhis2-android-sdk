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
package org.hisp.dhis.android.core.trackedentity.search;

import org.hisp.dhis.android.core.arch.repositories.scope.internal.FilterItemOperator;
import org.hisp.dhis.android.core.arch.repositories.scope.internal.RepositoryScopeFilterItem;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;
import org.hisp.dhis.android.core.trackedentity.search.internal.TrackedEntityInstanceQueryRepositoryScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(JUnit4.class)
public class TrackedEntityInstanceLocalQueryHelperShould {

    private TrackedEntityInstanceQueryRepositoryScope.Builder queryBuilder;

    @Before
    public void setUp() {
        queryBuilder = TrackedEntityInstanceQueryRepositoryScope.builder();
    }

    @Test
    public void build_sql_query_with_programs() {
        TrackedEntityInstanceQueryRepositoryScope scope = queryBuilder
                .program("IpHINAT79UW")
                .orgUnits(Collections.singletonList("DiszpKrYNg8"))
                .orgUnitMode(OrganisationUnitMode.DESCENDANTS)
                .query(RepositoryScopeFilterItem.builder().key("").operator(FilterItemOperator.LIKE).value("female").build())
                .build();

        String sqlQuery = TrackedEntityInstanceLocalQueryHelper.getSqlQuery(scope, Collections.emptyList(), 50);
        assertThat(sqlQuery).contains("program");
    }

    @Test
    public void build_sql_query_with_enrollment_date() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        TrackedEntityInstanceQueryRepositoryScope scope  = queryBuilder
                .program("IpHINAT79UW")
                .programStartDate(format.parse("2019-04-15"))
                .programEndDate(format.parse("2019-05-19"))
                .query(RepositoryScopeFilterItem.builder().key("").operator(FilterItemOperator.LIKE).value("female").build())
                .build();

        String sqlQuery = TrackedEntityInstanceLocalQueryHelper.getSqlQuery(scope, Collections.emptyList(), 50);
        assertThat(sqlQuery).contains("enrollmentDate >= '2019-04-15'");
        assertThat(sqlQuery).contains("enrollmentDate <= '2019-05-19'");
    }

    @Test
    public void build_sql_query_with_states() {
        TrackedEntityInstanceQueryRepositoryScope scope  = queryBuilder
                .states(Arrays.asList(State.SYNCED, State.TO_POST, State.TO_UPDATE))
                .program("IpHINAT79UW")
                .query(RepositoryScopeFilterItem.builder().key("").operator(FilterItemOperator.LIKE).value("female").build())
                .build();

        String sqlQuery = TrackedEntityInstanceLocalQueryHelper.getSqlQuery(scope, Collections.emptyList(), 50);
        assertThat(sqlQuery).contains("state IN ('SYNCED', 'TO_POST', 'TO_UPDATE')");
    }

}
