/*
 *  Copyright (c) 2004-2024, University of Oslo
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

package org.hisp.dhis.android.core.analytics.trackerlinelist.internal.evaluator

import org.hisp.dhis.android.core.analytics.AnalyticsException
import org.hisp.dhis.android.core.analytics.aggregated.MetadataItem
import org.hisp.dhis.android.core.analytics.trackerlinelist.TrackerLineListItem
import org.hisp.dhis.android.core.analytics.trackerlinelist.internal.DataFilterHelper
import org.hisp.dhis.android.core.analytics.trackerlinelist.internal.evaluator.TrackerLineListSQLLabel.EventAlias
import org.hisp.dhis.android.core.event.EventTableInfo
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueTableInfo
import org.hisp.dhis.android.core.util.SqlUtils.getColumnValueCast

internal class ProgramDataElementEvaluator(
    private val item: TrackerLineListItem.ProgramDataElement,
    private val metadata: Map<String, MetadataItem>,
) : TrackerLineListEvaluator() {
    override fun getSelectSQLForEvent(): String {
        return "SELECT ${getColumnSql()} " +
                "FROM ${TrackedEntityDataValueTableInfo.TABLE_INFO.name()} " +
                "WHERE ${TrackedEntityDataValueTableInfo.Columns.EVENT} = " +
                "$EventAlias.${EventTableInfo.Columns.UID} " +
                "AND ${TrackedEntityDataValueTableInfo.Columns.DATA_ELEMENT} = '${item.uid}'"
    }

    override fun getSelectSQLForEnrollment(): String {
        return TODO()
    }

    override fun getWhereSQLForEvent(): String {
        return DataFilterHelper.getWhereClause(item.id, item.filters)
    }

    override fun getWhereSQLForEnrollment(): String {
        return TODO()
    }

    private fun getColumnSql(): String {
        val dataElementMetadata = metadata[item.id] ?: throw AnalyticsException.InvalidDataElement(item.id)
        val dataElement = ((dataElementMetadata) as MetadataItem.DataElementItem).item

        return getColumnValueCast(
            column = TrackedEntityDataValueTableInfo.Columns.VALUE,
            valueType = dataElement.valueType(),
        )
    }
}
