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

package org.hisp.dhis.android.core.maintenance;

public enum D2ErrorCode {
    ALREADY_AUTHENTICATED,
    @Deprecated ALREADY_EXECUTED,
    API_UNSUCCESSFUL_RESPONSE,
    @Deprecated API_INVALID_QUERY,
    API_RESPONSE_PROCESS_ERROR,
    APP_NAME_NOT_SET,
    APP_VERSION_NOT_SET,
    BAD_CREDENTIALS,
    CANT_ACCESS_KEYSTORE,
    CANT_CREATE_EXISTING_OBJECT,
    CANT_DELETE_NON_EXISTING_OBJECT,
    CANT_INSTANTIATE_KEYSTORE,
    COULD_NOT_RESERVE_VALUE_ON_SERVER,
    DATABASE_EXPORT_LOGIN_FIRST,
    DATABASE_EXPORT_ENCRYPTED_NOT_SUPPORTED,
    DATABASE_IMPORT_ALREADY_EXISTS,
    DATABASE_IMPORT_LOGOUT_FIRST,
    DATABASE_IMPORT_VERSION_HIGHER_THAN_SUPPORTED,
    FILE_NOT_FOUND,
    FAIL_RESIZING_IMAGE,
    IMPOSSIBLE_TO_GENERATE_COORDINATES,
    JOB_REPORT_NOT_AVAILABLE,
    LOGIN_USERNAME_NULL,
    LOGIN_PASSWORD_NULL,
    MAX_TEI_COUNT_REACHED,
    MIGHT_BE_RUNNING_LOW_ON_AVAILABLE_VALUES,
    MIN_SEARCH_ATTRIBUTES_REQUIRED,
    NO_AUTHENTICATED_USER,
    NO_AUTHENTICATED_USER_OFFLINE,
    NOT_ENOUGH_VALUES_LEFT_TO_RESERVE_ON_SERVER,
    @Deprecated DIFFERENT_AUTHENTICATED_USER_OFFLINE,
    INVALID_DHIS_VERSION,
    INVALID_GEOMETRY_VALUE,
    NO_DHIS2_SERVER,
    NO_RESERVED_VALUES,
    OBJECT_CANT_BE_UPDATED,
    OBJECT_CANT_BE_INSERTED,
    ORGUNIT_NOT_IN_SEARCH_SCOPE,
    OWNERSHIP_ACCESS_DENIED,
    PROGRAM_ACCESS_CLOSED,
    SEARCH_GRID_PARSE,
    SERVER_URL_NULL,
    SERVER_URL_MALFORMED,
    SETTINGS_APP_NOT_SUPPORTED,
    SETTINGS_APP_NOT_INSTALLED,
    SOCKET_TIMEOUT,
    RELATIONSHIPS_CANT_BE_UPDATED,
    TOO_MANY_ORG_UNITS,
    @Deprecated TOO_MANY_PERIODS,
    TOO_MANY_REQUESTS,
    UNEXPECTED,
    UNKNOWN_HOST,
    URL_NOT_FOUND,
    USER_ACCOUNT_DISABLED,
    USER_ACCOUNT_LOCKED,
    VALUE_CANT_BE_SET,
    VALUES_RESERVATION_TOOK_TOO_LONG,
    SSL_ERROR,
    SMS_NOT_SUPPORTED,
    INVALID_CHARACTERS
}