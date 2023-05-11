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
package org.hisp.dhis.android.core.settings.internal

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import dagger.Reusable
import io.reactivex.Single
import java.net.HttpURLConnection
import javax.inject.Inject
import org.hisp.dhis.android.core.arch.api.executors.internal.RxAPICallExecutor
import org.hisp.dhis.android.core.maintenance.D2Error

@Reusable
internal class SettingsAppInfoCall @Inject constructor(
    private val settingAppService: SettingAppService,
    private val apiCallExecutor: RxAPICallExecutor
) {
    companion object {
        const val unknown = "unknown"
    }

    fun fetch(storeError: Boolean): Single<SettingsAppVersion> {
        return fetchAppVersion(storeError)
    }

    private fun fetchAppVersion(storeError: Boolean): Single<SettingsAppVersion> {
        return apiCallExecutor.wrapSingle(settingAppService.info(), storeError)
            .map<SettingsAppVersion> {
                SettingsAppVersion.Valid(it.dataStoreVersion(), it.androidSettingsVersion() ?: unknown)
            }
            .onErrorResumeNext { throwable: Throwable ->
                return@onErrorResumeNext when {
                    throwable is D2Error && throwable.httpErrorCode() == HttpURLConnection.HTTP_NOT_FOUND ->
                        fetchV1GeneralSettings(storeError)
                    throwable is D2Error && throwable.originalException() is InvalidFormatException ->
                        Single.just(SettingsAppVersion.DataStoreEmpty)
                    else ->
                        Single.error(throwable)
                }
            }
    }

    private fun fetchV1GeneralSettings(storeError: Boolean): Single<SettingsAppVersion> {
        return apiCallExecutor.wrapSingle(
            settingAppService.generalSettings(SettingsAppDataStoreVersion.V1_1), storeError
        )
            .map<SettingsAppVersion> {
                SettingsAppVersion.Valid(SettingsAppDataStoreVersion.V1_1, unknown)
            }
            .onErrorResumeNext { throwable: Throwable ->
                return@onErrorResumeNext when {
                    throwable is D2Error && throwable.httpErrorCode() == HttpURLConnection.HTTP_NOT_FOUND ->
                        Single.just(SettingsAppVersion.DataStoreEmpty)
                    throwable is D2Error && throwable.originalException() is InvalidFormatException ->
                        Single.just(SettingsAppVersion.DataStoreEmpty)
                    else ->
                        Single.error(throwable)
                }
            }
    }
}
