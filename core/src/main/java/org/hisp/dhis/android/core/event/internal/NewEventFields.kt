/*
 *  Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.android.core.event.internal

import org.hisp.dhis.android.core.arch.api.fields.internal.Fields
import org.hisp.dhis.android.core.arch.fields.internal.FieldsHelper
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.event.NewTrackerImporterEvent
import org.hisp.dhis.android.core.note.NewTrackerImporterNote
import org.hisp.dhis.android.core.note.internal.NewNoteFields
import org.hisp.dhis.android.core.relationship.NewTrackerImporterRelationship
import org.hisp.dhis.android.core.relationship.internal.NewRelationshipFields
import org.hisp.dhis.android.core.trackedentity.NewTrackerImporterTrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.internal.NewTrackedEntityDataValueFields

internal object NewEventFields {
    private const val UID = "event"
    private const val ENROLLMENT = "enrollment"
    private const val CREATED_AT = "createdAt"
    private const val UPDATED_AT = "updatedAt"
    private const val PROGRAM = "program"
    private const val PROGRAM_STAGE = "programStage"
    private const val ORGANISATION_UNIT = "orgUnit"
    private const val OCCURRED_AT = "occurredAt"
    private const val COMPLETED_AT = "completedAt"
    private const val SCHEDULED_AT = "scheduledAt"
    private const val STATUS = "status"
    private const val TRACKED_ENTITY_DATA_VALUES = "dataValues"
    private const val GEOMETRY = "geometry"
    private const val NOTES = "notes"
    private const val RELATIONSHIPS = "relationships"
    private const val ATTRIBUTE_OPTION_COMBO = "attributeOptionCombo"
    private const val ASSIGNED_USER = "assignedUser"
    private const val DELETED = "deleted"

    private val fh = FieldsHelper<NewTrackerImporterEvent>()

    val allFields: Fields<NewTrackerImporterEvent> = commonFields()
        .fields(
            fh.nestedField<NewTrackerImporterNote>(NOTES).with(NewNoteFields.all),
            fh.nestedField<NewTrackerImporterRelationship>(RELATIONSHIPS).with(NewRelationshipFields.allFields),
            fh.nestedField<NewTrackerImporterTrackedEntityDataValue>(TRACKED_ENTITY_DATA_VALUES)
                .with(NewTrackedEntityDataValueFields.allFields)
        ).build()

    val asRelationshipFields: Fields<NewTrackerImporterEvent> = commonFields().build()

    private fun commonFields(): Fields.Builder<NewTrackerImporterEvent> {
        return Fields.builder<NewTrackerImporterEvent>().fields(
            fh.field<String>(UID),
            fh.field<String>(ENROLLMENT),
            fh.field<String>(CREATED_AT),
            fh.field<String>(UPDATED_AT),
            fh.field<EventStatus>(STATUS),
            fh.field<Geometry>(GEOMETRY),
            fh.field<String>(PROGRAM),
            fh.field<String>(PROGRAM_STAGE),
            fh.field<String>(ORGANISATION_UNIT),
            fh.field<String>(OCCURRED_AT),
            fh.field<String>(COMPLETED_AT),
            fh.field<String>(SCHEDULED_AT),
            fh.field<Boolean>(DELETED),
            fh.field<String>(ATTRIBUTE_OPTION_COMBO),
            fh.field<String>(ASSIGNED_USER)
        )
    }
}
