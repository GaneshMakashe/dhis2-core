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
package org.hisp.dhis.webapi.controller.event;

import static org.hisp.dhis.security.Authorities.F_MOBILE_SENDSMS;
import static org.hisp.dhis.security.Authorities.F_SEND_EMAIL;
import static org.hisp.dhis.webapi.controller.tracker.export.RequestParamsValidator.validateDeprecatedParameter;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hisp.dhis.common.DhisApiVersion;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.feedback.BadRequestException;
import org.hisp.dhis.feedback.ConflictException;
import org.hisp.dhis.outboundmessage.BatchResponseStatus;
import org.hisp.dhis.program.Enrollment;
import org.hisp.dhis.program.Event;
import org.hisp.dhis.program.message.ProgramMessage;
import org.hisp.dhis.program.message.ProgramMessageBatch;
import org.hisp.dhis.program.message.ProgramMessageQueryParams;
import org.hisp.dhis.program.message.ProgramMessageService;
import org.hisp.dhis.program.message.ProgramMessageStatus;
import org.hisp.dhis.security.RequiresAuthority;
import org.hisp.dhis.webapi.controller.AbstractCrudController;
import org.hisp.dhis.webapi.mvc.annotation.ApiVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Zubair <rajazubair.asghar@gmail.com> */
@RestController
@RequestMapping("/api/messages")
@ApiVersion({DhisApiVersion.DEFAULT, DhisApiVersion.ALL})
public class ProgramMessageController extends AbstractCrudController<ProgramMessage> {
  @Autowired private ProgramMessageService programMessageService;

  // -------------------------------------------------------------------------
  // GET
  // -------------------------------------------------------------------------

  @RequiresAuthority(anyOf = F_MOBILE_SENDSMS)
  @GetMapping(produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<ProgramMessage> getProgramMessages(
      @RequestParam(required = false) Set<String> ou,
      @Deprecated(since = "2.41") @RequestParam(required = false) UID programInstance,
      @RequestParam(required = false) UID enrollment,
      @Deprecated(since = "2.41") @RequestParam(required = false) UID programStageInstance,
      @RequestParam(required = false) UID event,
      @RequestParam(required = false) ProgramMessageStatus messageStatus,
      @RequestParam(required = false) Date afterDate,
      @RequestParam(required = false) Date beforeDate,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize)
      throws BadRequestException, ConflictException {
    UID enrollmentUid =
        validateDeprecatedParameter("programInstance", programInstance, "enrollment", enrollment);
    UID eventUid =
        validateDeprecatedParameter("programStageInstance", programStageInstance, "event", event);

    if (enrollmentUid == null && eventUid == null) {
      throw new ConflictException("Enrollment or Event must be specified.");
    }

    ProgramMessageQueryParams params =
        programMessageService.getFromUrl(
            ou,
            enrollmentUid == null ? null : enrollmentUid.getValue(),
            eventUid == null ? null : eventUid.getValue(),
            messageStatus,
            page,
            pageSize,
            afterDate,
            beforeDate);

    return programMessageService.getProgramMessages(params);
  }

  @RequiresAuthority(anyOf = F_MOBILE_SENDSMS)
  @GetMapping(value = "/scheduled/sent", produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<ProgramMessage> getScheduledSentMessage(
      @OpenApi.Param(value = Enrollment.class)
          @Deprecated(since = "2.41")
          @RequestParam(required = false)
          UID programInstance,
      @OpenApi.Param({UID.class, Enrollment.class}) @RequestParam(required = false) UID enrollment,
      @OpenApi.Param(value = Event.class)
          @Deprecated(since = "2.41")
          @RequestParam(required = false)
          UID programStageInstance,
      @OpenApi.Param({UID.class, Event.class}) @RequestParam(required = false) UID event,
      @RequestParam(required = false) Date afterDate,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize)
      throws BadRequestException {
    UID enrollmentUid =
        validateDeprecatedParameter("programInstance", programInstance, "enrollment", enrollment);
    UID eventUid =
        validateDeprecatedParameter("programStageInstance", programStageInstance, "event", event);

    ProgramMessageQueryParams params =
        programMessageService.getFromUrl(
            null,
            enrollmentUid == null ? null : enrollmentUid.getValue(),
            eventUid == null ? null : eventUid.getValue(),
            null,
            page,
            pageSize,
            afterDate,
            null);

    return programMessageService.getProgramMessages(params);
  }

  // -------------------------------------------------------------------------
  // POST
  // -------------------------------------------------------------------------

  @RequiresAuthority(anyOf = {F_MOBILE_SENDSMS, F_SEND_EMAIL})
  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public BatchResponseStatus sendMessages(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    ProgramMessageBatch batch =
        renderService.fromJson(request.getInputStream(), ProgramMessageBatch.class);

    for (ProgramMessage programMessage : batch.getProgramMessages()) {
      programMessageService.validatePayload(programMessage);
    }

    return programMessageService.sendMessages(batch.getProgramMessages());
  }
}
