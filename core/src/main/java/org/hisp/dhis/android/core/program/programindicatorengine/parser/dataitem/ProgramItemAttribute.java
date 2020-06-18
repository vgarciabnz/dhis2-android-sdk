package org.hisp.dhis.android.core.program.programindicatorengine.parser.dataitem;

/*
 * Copyright (c) 2004-2020, University of Oslo
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

import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder;
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo;
import org.hisp.dhis.android.core.parser.expression.CommonExpressionVisitor;
import org.hisp.dhis.android.core.program.programindicatorengine.parser.ProgramExpressionItem;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.antlr.ParserExceptionWithoutContext;

import static org.hisp.dhis.android.core.common.BaseIdentifiableObject.UID;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo.Columns.TRACKED_ENTITY_ATTRIBUTE;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo.Columns.TRACKED_ENTITY_INSTANCE;
import static org.hisp.dhis.parser.expression.antlr.ExpressionParser.ExprContext;

/**
 * Program indicator expression data item ProgramAttribute
 *
 * @author Jim Grace
 */
public class ProgramItemAttribute
        extends ProgramExpressionItem {

    @Override
    public Object evaluate(ExprContext ctx, CommonExpressionVisitor visitor) {
        String attributeUid = getProgramAttributeId(ctx);
        String teiQuery = visitor.programIndicatorContext.enrollmentUid();

        WhereClauseBuilder clauseBuilder = new WhereClauseBuilder()
                .appendKeyStringValue(TRACKED_ENTITY_ATTRIBUTE, attributeUid)
                .appendInSubQuery(TRACKED_ENTITY_INSTANCE, getTrackedEntityInstanceQuery(teiQuery));

        TrackedEntityAttributeValue attributeValue =
                visitor.getTrackedEntityAttributeValueStore().selectOneWhere(clauseBuilder.build());

        String value = attributeValue == null ? null : attributeValue.value();
        return visitor.handleNulls(value);
    }


    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * Makes sure that the parsed A{...} has a syntax that could be used
     * be used in an program expression for A{attributeUid}
     *
     * @param ctx the item context
     * @return the attribute UID.
     */
    private String getProgramAttributeId(ExprContext ctx) {
        if (ctx.uid1 != null) {
            throw new ParserExceptionWithoutContext(
                    "Program attribute must have one UID: " + ctx.getText());
        }

        return ctx.uid0.getText();
    }

    private String getTrackedEntityInstanceQuery(String enrollmentUid) {
        return String.format("SELECT %s FROM %s WHERE %s = %s",
                EnrollmentTableInfo.Columns.TRACKED_ENTITY_INSTANCE,
                EnrollmentTableInfo.TABLE_INFO.name(),
                UID,
                enrollmentUid);
    }
}
