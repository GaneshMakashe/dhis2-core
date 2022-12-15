/*
 * Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.tracker.validation.hooks;

import static org.hisp.dhis.tracker.validation.hooks.AssertTrackerValidationReport.assertHasError;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hisp.dhis.tracker.TrackerType;
import org.hisp.dhis.tracker.validation.ValidationCode;
import org.hisp.dhis.tracker.validation.ValidationErrorReporter;

public class AssertValidationErrorReporter
{
    private AssertValidationErrorReporter()
    {
        throw new IllegalStateException( "utility class" );
    }

    public static void assertMissingProperty( ValidationErrorReporter reporter, TrackerType type, String entity,
        String uid,
        String property,
        ValidationCode errorCode )
    {
        assertHasError( reporter.getErrors(), errorCode, type, uid,
            "Missing required " + entity + " property: `" + property + "`." );
    }

    public static void hasTrackerError( ValidationErrorReporter reporter, ValidationCode code, TrackerType type,
        String uid )
    {
        assertTrue( reporter.hasErrors(), "error not found since reporter has no errors" );
        assertTrue( reporter.hasErrorReport( err -> code == err.getErrorCode() &&
            type == err.getTrackerType() &&
            uid.equals( err.getUid() ) ),
            String.format( "error with code %s, type %s, uid %s not found in reporter with %d error(s)", code, type,
                uid, reporter.getErrors().size() ) );
    }
}
