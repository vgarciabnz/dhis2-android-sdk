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

package org.hisp.dhis.android.core.common;

import org.hisp.dhis.android.core.resource.ResourceModel;
import org.hisp.dhis.android.core.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;

public abstract class UidPayloadCall<P> extends AbstractEndpointListCall<P, UidsQuery, Payload<P>> {

    private final int uidLimit;

    public UidPayloadCall(GenericCallData data,
                          ResourceModel.Type resourceType,
                          UidsQuery query,
                          int uidLimit,
                          ListPersistor<P> persistor) {

        super(data, resourceType, query, persistor);
        this.uidLimit = uidLimit;
    }

    @Override
    protected List<P> getObjects(UidsQuery query, String lastUpdated) throws D2CallException {
        List<Set<String>> partitions = Utils.setPartition(query.uids(), uidLimit);

        List<P> result = new ArrayList<>();
        APICallExecutor apiCallExecutor = new APICallExecutor();
        for (Set<String> partitionUids : partitions) {
            UidsQuery uidQuery = UidsQuery.create(partitionUids);
            List<P> callResult = apiCallExecutor.executePayloadCall(getCall(uidQuery, lastUpdated));
            result.addAll(callResult);
        }

        return result;
    }

    protected abstract Call<Payload<P>> getCall(UidsQuery query, String lastUpdated);
}
