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
package org.hisp.dhis.android.core.enrollment

import org.hisp.dhis.android.core.arch.handlers.internal.HandleAction
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender
import org.hisp.dhis.android.core.arch.repositories.`object`.internal.ObjectRepositoryFactory
import org.hisp.dhis.android.core.arch.repositories.`object`.internal.ReadWriteWithUidDataObjectRepositoryImpl
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.Unit
import org.hisp.dhis.android.core.common.internal.TrackerDataManager
import org.hisp.dhis.android.core.enrollment.internal.EnrollmentStore
import org.hisp.dhis.android.core.maintenance.D2Error
import java.util.Date

class EnrollmentObjectRepository internal constructor(
    store: EnrollmentStore,
    uid: String?,
    childrenAppenders: Map<String, ChildrenAppender<Enrollment>>,
    scope: RepositoryScope,
    private val trackerDataManager: TrackerDataManager,
) : ReadWriteWithUidDataObjectRepositoryImpl<Enrollment, EnrollmentObjectRepository>(
    store,
    childrenAppenders,
    scope,
    ObjectRepositoryFactory { s: RepositoryScope ->
        EnrollmentObjectRepository(
            store,
            uid,
            childrenAppenders,
            s,
            trackerDataManager,
        )
    },
) {

    @Throws(D2Error::class)
    fun setOrganisationUnitUid(organisationUnitUid: String?): Unit {
        return updateObject(updateBuilder().organisationUnit(organisationUnitUid).build())
    }

    @Throws(D2Error::class)
    fun setEnrollmentDate(enrollmentDate: Date?): Unit {
        return updateObject(updateBuilder().enrollmentDate(enrollmentDate).build())
    }

    @Throws(D2Error::class)
    fun setIncidentDate(incidentDate: Date?): Unit {
        return updateObject(updateBuilder().incidentDate(incidentDate).build())
    }

    @Throws(D2Error::class)
    fun setCompletedDate(completedDate: Date?): Unit {
        return updateObject(updateBuilder().completedDate(completedDate).build())
    }

    @Throws(D2Error::class)
    fun setFollowUp(followUp: Boolean?): Unit {
        return updateObject(updateBuilder().followUp(followUp).build())
    }

    @Throws(D2Error::class)
    fun setStatus(enrollmentStatus: EnrollmentStatus): Unit {
        val completedDate = if (enrollmentStatus == EnrollmentStatus.COMPLETED) Date() else null
        return updateObject(updateBuilder().status(enrollmentStatus).completedDate(completedDate).build())
    }

    @Throws(D2Error::class)
    fun setGeometry(geometry: Geometry?): Unit {
        GeometryHelper.validateGeometry(geometry)
        return updateObject(updateBuilder().geometry(geometry).build())
    }

    private fun updateBuilder(): Enrollment.Builder {
        val enrollment = blockingGetWithoutChildren()!!
        val updateDate = Date()
        var state = enrollment.aggregatedSyncState()
        state = if (state === State.TO_POST) state else State.TO_UPDATE
        return enrollment.toBuilder()
            .syncState(state)
            .aggregatedSyncState(state)
            .lastUpdated(updateDate)
            .lastUpdatedAtClient(updateDate)
    }

    override fun propagateState(m: Enrollment, action: HandleAction) {
        trackerDataManager.propagateEnrollmentUpdate(m, action)
    }

    override fun deleteObject(m: Enrollment) {
        trackerDataManager.deleteEnrollment(m)
    }
}
