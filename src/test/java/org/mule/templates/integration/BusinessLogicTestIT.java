/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.construct.Flow;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.NullPayload;

import com.mulesoft.module.batch.BatchTestHelper;
import com.mulesoft.module.batch.api.BatchManager;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Anypoint Template that make calls to external systems.
 * 
 */
public class BusinessLogicTestIT extends AbstractTemplateTestCase {
	private static final Logger log = LogManager.getLogger(BusinessLogicTestIT.class);
	private static final String TEST_CUSTOMER_MASTER_FILE = "./src/test/resources/debmas01.xml";
	private static final int TIMEOUT_SEC = 120;

	private SubflowInterceptingChainLifecycleWrapper retrieveAccountFromSFDCFlow;

	private BatchTestHelper helper;
	private List<Map<String, Object>> accountsToDeleteFromSFDC = new ArrayList<Map<String, Object>>();
	private Flow mainFlow;

	@Before
	public void setUp() throws Exception {
		muleContext.getRegistry().lookupObject(BatchManager.class).cancelAllRunningInstances();

		helper = new BatchTestHelper(muleContext);
		
		retrieveAccountFromSFDCFlow = getSubFlow("retrieveAccountFromSFDCFlow");
		retrieveAccountFromSFDCFlow.initialise();
		
		mainFlow =  (Flow) muleContext.getRegistry().lookupObject("mainFlow");
	}

	@After
	public void tearDown() throws Exception {
		deleteTestDataFromSandBox();
	}

	@Test
	public void testMainFlow() throws Exception {		
		String originalXML = getFileString(TEST_CUSTOMER_MASTER_FILE);
		SapPayloadGenerator generator = new SapPayloadGenerator(originalXML);
		generator.setTemplateName(TEMPLATE_NAME);
		String xmlPayload = generator.generateXML();

		final MuleEvent testEvent = getTestEvent(null, mainFlow);
		testEvent.getMessage().setPayload(xmlPayload, DataTypeFactory.create(InputStream.class, "application/xml"));
		
		mainFlow.process(testEvent);
		
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();
		
		log.info("DONE");

		for (String id : generator.getUniqueIdList()) {
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("sap_external_id__c", id);
			Map<String, Object> result = invokeRetrieveFlow(retrieveAccountFromSFDCFlow, payload);
			Assert.assertNotNull("The SAP Material wit id " + id + " should have been sync", result);
			accountsToDeleteFromSFDC.add(result);
		}

	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> invokeRetrieveFlow(SubflowInterceptingChainLifecycleWrapper flow, Map<String, Object> payload)
			throws Exception {
		MuleEvent event = flow.process(getTestEvent(payload, MessageExchangePattern.REQUEST_RESPONSE));
		Object resultPayload = event.getMessage().getPayload();

		return resultPayload instanceof NullPayload ? null : (Map<String, Object>) resultPayload;
	}

	private void deleteTestDataFromSandBox() throws MuleException, Exception {
		deleteTestAccountsFromSFDC(accountsToDeleteFromSFDC);
	}

	protected void deleteTestAccountsFromSFDC(List<Map<String, Object>> createdAccountsInSFDC)
			throws InitialisationException, MuleException, Exception {

		SubflowInterceptingChainLifecycleWrapper deleteAccountsFromSFDCFlow = getSubFlow("deleteAccountsFromSFDCFlow");
		deleteAccountsFromSFDCFlow.initialise();
		deleteTestEntityFromSandBox(deleteAccountsFromSFDCFlow, createdAccountsInSFDC);
	}

	protected void deleteTestEntityFromSandBox(SubflowInterceptingChainLifecycleWrapper deleteFlow, List<Map<String, Object>> entitities)
			throws MuleException, Exception {
		List<String> idList = new ArrayList<String>();
		for (Map<String, Object> c : entitities) {
			idList.add(c.get("Id").toString());
		}
		deleteFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}
}
