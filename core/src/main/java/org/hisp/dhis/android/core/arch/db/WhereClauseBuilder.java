/*
 * Copyright (c) 2017, University of Oslo
 *
 * All rights reserved.
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
package org.hisp.dhis.android.core.arch.db;

import org.hisp.dhis.android.core.utils.Utils;

import java.util.List;

public class WhereClauseBuilder {

    private static final String EQ_STR = " = '";
    private static final String LIKE_STR = " LIKE '";
    private static final String END_STR = "'";
    private static final String PARENTHESES_END = ")";

    private static final String EQ_NUMBER = " = ";

    private static final String AND = " AND ";
    private static final String IN = " IN (";
    private static final String NOT_IN = " NOT IN (";

    @SuppressWarnings("PMD.AvoidStringBufferField")
    private final StringBuilder whereClause = new StringBuilder();
    private boolean isFirst = true;

    public WhereClauseBuilder appendKeyStringValue(String column, Object value) {
        return appendKeyValue(column, value, EQ_STR, END_STR);
    }

    public WhereClauseBuilder appendKeyLikeStringValue(String column, Object value) {
        return appendKeyValue(column, value, LIKE_STR, END_STR);
    }

    public WhereClauseBuilder appendKeyNumberValue(String column, double value) {
        return appendKeyValue(column, value, EQ_NUMBER, "");
    }

    public WhereClauseBuilder appendKeyNumberValue(String column, int value) {
        return appendKeyValue(column, value, EQ_NUMBER, "");
    }

    public WhereClauseBuilder appendNotInKeyStringValues(String column, List<String> values) {
        String valuesArray = Utils.commaAndSpaceSeparatedArrayValues(Utils.withSingleQuotationMarksArray(values));
        return appendKeyValue(column, valuesArray, NOT_IN, PARENTHESES_END);
    }

    public WhereClauseBuilder appendInKeyStringValues(String column, List<String> values) {
        String valuesArray = Utils.commaAndSpaceSeparatedArrayValues(Utils.withSingleQuotationMarksArray(values));
        return appendKeyValue(column, valuesArray, IN, PARENTHESES_END);
    }

    private WhereClauseBuilder appendKeyValue(String column, Object value, String eq, String end) {
        String andOpt = isFirst ? "" : AND;
        isFirst = false;
        whereClause.append(andOpt).append(column).append(eq).append(value).append(end);
        return this;
    }

    public String build() {
        if (whereClause.length() == 0) {
            throw new RuntimeException("No columns added");
        } else {
            return whereClause.toString();
        }
    }
}
