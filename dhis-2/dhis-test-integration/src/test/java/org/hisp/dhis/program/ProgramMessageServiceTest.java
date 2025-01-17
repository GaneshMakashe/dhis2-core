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
package org.hisp.dhis.program;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.common.DeliveryChannel;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.message.ProgramMessage;
import org.hisp.dhis.program.message.ProgramMessageQueryParams;
import org.hisp.dhis.program.message.ProgramMessageRecipients;
import org.hisp.dhis.program.message.ProgramMessageService;
import org.hisp.dhis.program.message.ProgramMessageStatus;
import org.hisp.dhis.sms.config.BulkSmsGatewayConfig;
import org.hisp.dhis.sms.config.SmsConfiguration;
import org.hisp.dhis.sms.config.SmsConfigurationManager;
import org.hisp.dhis.test.integration.PostgresIntegrationTestBase;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zubair <rajazubair.asghar@gmail.com>
 */
@Transactional
class ProgramMessageServiceTest extends PostgresIntegrationTestBase {
  private OrganisationUnit ouA;

  private OrganisationUnit ouB;

  private Enrollment enrollmentA;

  private TrackedEntity trackedEntityA;

  private BulkSmsGatewayConfig bulkSmsConfig;

  private ProgramMessageStatus messageStatus = ProgramMessageStatus.OUTBOUND;

  private Set<DeliveryChannel> channels = new HashSet<>();

  private ProgramMessageQueryParams params;

  private ProgramMessage pmsgA;

  private ProgramMessage pmsgB;

  private ProgramMessage pmsgC;

  private ProgramMessage pmsgD;

  private ProgramMessageRecipients recipientsA;

  private ProgramMessageRecipients recipientsB;

  private ProgramMessageRecipients recipientsC;

  private ProgramMessageRecipients recipientsD;

  private String uidA;

  private String uidB;

  private String uidC;

  private String text = "Hi";

  private String msisdn = "4742312555";

  private String subject = "subjectText";

  // -------------------------------------------------------------------------
  // Dependencies
  // -------------------------------------------------------------------------
  @Autowired private ProgramMessageService programMessageService;

  @Autowired private OrganisationUnitService orgUnitService;

  @Autowired private TrackedEntityService trackedEntityService;

  @Autowired private IdentifiableObjectManager manager;

  @Autowired private ProgramService programService;

  @Autowired private SmsConfigurationManager smsConfigurationManager;

  @BeforeEach
  void setUp() {
    ouA = createOrganisationUnit('A');
    ouA.setPhoneNumber(msisdn);
    ouB = createOrganisationUnit('B');
    orgUnitService.addOrganisationUnit(ouA);
    orgUnitService.addOrganisationUnit(ouB);
    Program program = createProgram('A');
    program.setAutoFields();
    program.setOrganisationUnits(Sets.newHashSet(ouA, ouB));
    program.setName("programA");
    program.setShortName("programAshortname");
    program.setProgramType(ProgramType.WITHOUT_REGISTRATION);
    programService.addProgram(program);
    enrollmentA = new Enrollment();
    enrollmentA.setProgram(program);
    enrollmentA.setOrganisationUnit(ouA);
    enrollmentA.setName("enrollmentA");
    enrollmentA.setEnrollmentDate(new Date());
    enrollmentA.setAutoFields();
    manager.save(enrollmentA);
    Set<OrganisationUnit> ouSet = new HashSet<>();
    ouSet.add(ouA);
    Set<String> ouUids = new HashSet<>();
    ouUids.add(ouA.getUid());
    // ouSet.add( ouB );
    trackedEntityA = createTrackedEntity(ouA);
    manager.save(trackedEntityA);
    recipientsA = new ProgramMessageRecipients();
    recipientsA.setOrganisationUnit(ouA);
    recipientsA.setTrackedEntity(trackedEntityA);
    recipientsB = new ProgramMessageRecipients();
    recipientsB.setOrganisationUnit(ouA);
    recipientsB.setTrackedEntity(trackedEntityA);
    recipientsC = new ProgramMessageRecipients();
    recipientsC.setOrganisationUnit(ouA);
    recipientsC.setTrackedEntity(trackedEntityA);
    recipientsD = new ProgramMessageRecipients();
    recipientsD.setOrganisationUnit(ouA);
    recipientsD.setTrackedEntity(null);
    Set<String> phoneNumberListA = new HashSet<>();
    phoneNumberListA.add(msisdn);
    recipientsA.setPhoneNumbers(phoneNumberListA);
    Set<String> phoneNumberListB = new HashSet<>();
    phoneNumberListB.add(msisdn);
    recipientsB.setPhoneNumbers(phoneNumberListB);
    Set<String> phoneNumberListC = new HashSet<>();
    phoneNumberListC.add(msisdn);
    recipientsC.setPhoneNumbers(phoneNumberListC);
    channels.add(DeliveryChannel.SMS);
    pmsgA = createProgramMessage(text, subject, recipientsA, messageStatus, channels);
    pmsgA.setEnrollment(enrollmentA);
    pmsgA.setStoreCopy(false);
    pmsgB = createProgramMessage(text, subject, recipientsB, messageStatus, channels);
    pmsgB.setEnrollment(enrollmentA);
    pmsgC = createProgramMessage(text, subject, recipientsC, messageStatus, channels);
    pmsgD = createProgramMessage(text, subject, recipientsD, messageStatus, channels);
    pmsgD.setEnrollment(enrollmentA);
    pmsgD.setStoreCopy(false);
    uidA = CodeGenerator.generateCode(10);
    uidB = CodeGenerator.generateCode(10);
    uidC = CodeGenerator.generateCode(10);
    pmsgA.setUid(uidA);
    pmsgB.setUid(uidB);
    pmsgC.setUid(uidC);
    params = new ProgramMessageQueryParams();
    params.setOrganisationUnit(ouUids);
    params.setEnrollment(enrollmentA);
    params.setMessageStatus(messageStatus);
    bulkSmsConfig = new BulkSmsGatewayConfig();
    bulkSmsConfig.setDefault(true);
    bulkSmsConfig.setName("bulk");
    bulkSmsConfig.setUsername("user_uio");
    bulkSmsConfig.setPassword("5cKMMQTGNMkD");
    SmsConfiguration smsConfig = new SmsConfiguration();
    smsConfig.getGateways().add(bulkSmsConfig);
    smsConfigurationManager.updateSmsConfiguration(smsConfig);
  }

  // -------------------------------------------------------------------------
  // Tests
  // -------------------------------------------------------------------------
  @Test
  void testDeleteProgramMessage() {
    Long pmsgAId = null;
    pmsgAId = programMessageService.saveProgramMessage(pmsgA);
    assertNotNull(pmsgAId);
    programMessageService.deleteProgramMessage(pmsgA);
    ProgramMessage programMessage = programMessageService.getProgramMessage(pmsgAId.intValue());
    assertNull(programMessage);
  }

  @Test
  void testGetAllProgramMessages() {
    programMessageService.saveProgramMessage(pmsgA);
    programMessageService.saveProgramMessage(pmsgB);
    programMessageService.saveProgramMessage(pmsgC);
    List<ProgramMessage> programMessages = programMessageService.getAllProgramMessages();
    assertNotNull(programMessages);
    assertTrue(!programMessages.isEmpty());
    assertTrue(equals(programMessages, pmsgA, pmsgB, pmsgC));
  }

  @Test
  void testGetProgramMessageById() {
    long pmsgAId = programMessageService.saveProgramMessage(pmsgA);
    ProgramMessage programMessage = programMessageService.getProgramMessage(pmsgAId);
    assertNotNull(programMessage);
    assertTrue(pmsgA.equals(programMessage));
  }

  @Test
  void testGetProgramMessageByUid() {
    programMessageService.saveProgramMessage(pmsgA);
    ProgramMessage programMessage = programMessageService.getProgramMessage(uidA);
    assertNotNull(programMessage);
    assertTrue(pmsgA.equals(programMessage));
  }

  @Test
  void testGetProgramMessageByQuery() {
    programMessageService.saveProgramMessage(pmsgA);
    programMessageService.saveProgramMessage(pmsgB);
    List<ProgramMessage> list = programMessageService.getProgramMessages(params);
    assertNotNull(list);
    assertTrue(equals(list, pmsgA, pmsgB));
    assertTrue(channels.equals(list.get(0).getDeliveryChannels()));
  }

  @Test
  void testSaveProgramMessage() {
    Long pmsgAId = null;
    pmsgAId = programMessageService.saveProgramMessage(pmsgA);
    assertNotNull(pmsgAId);
    ProgramMessage programMessage = programMessageService.getProgramMessage(pmsgAId.intValue());
    assertTrue(programMessage.equals(pmsgA));
  }

  @Test
  void testUpdateProgramMessage() {
    Long pmsgAId = programMessageService.saveProgramMessage(pmsgA);
    ProgramMessage programMessage = programMessageService.getProgramMessage(pmsgAId.intValue());
    programMessage.setText("hello");
    programMessageService.updateProgramMessage(programMessage);
    ProgramMessage programMessageUpdated =
        programMessageService.getProgramMessage(pmsgAId.intValue());
    assertNotNull(programMessageUpdated);
    assertTrue(programMessageUpdated.getText().equals("hello"));
  }
}
