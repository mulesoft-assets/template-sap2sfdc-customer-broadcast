/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.unit;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.registry.RegistrationException;
import org.mule.munit.common.mocking.SpyProcess;

import com.mulesoft.module.batch.api.BatchManager;

/**
 * The purpose of this test is to verify the correct behavior of the business
 * logic file. The test would validate that the payload that arrived the
 * connector call is correct and that a call to it is being done.
 * 
 * @author damiansima
 */
public class BusinessLogicTests extends AbstractTemplateFunctionalMunitSuite {

	private static final Integer AMOUNTS_OF_PRODUCTS_IN_THE_TEST_FILE = 1;
	private static final String TEST_MAT_MASTER_FILE = "./src/test/resources/debmas01.xml";

	@Before
	public void setUp() throws RegistrationException, MuleException {
		muleContext.getRegistry().lookupObject(BatchManager.class).cancelAllRunningInstances();
	}

	@Test
	public void testSuccessCall() throws MuleException, Exception {
		String xmlPayload = getFileString(TEST_MAT_MASTER_FILE);

		whenMessageProcessor("upsert").ofNamespace("sfdc").thenReturn(testEvent("").getMessage());
		spyMessageProcessor("upsert").ofNamespace("sfdc").before(new ProductItemSpayValidator());

		runFlow("callBatchFlow", testEvent(xmlPayload));
		Thread.sleep(5000);
		verifyCallOfMessageProcessor("upsert").ofNamespace("sfdc").times(1);
	}

	private class ProductItemSpayValidator implements SpyProcess {

		@Override
		public void spy(MuleEvent event) throws MuleException {
			List<?> payload = (List<?>) event.getMessage().getPayload();

			Assert.assertTrue(AMOUNTS_OF_PRODUCTS_IN_THE_TEST_FILE.equals(payload.size()));

			for (Object item : payload) {
				Map<?, ?> account = (Map<?, ?>) item;
				Assert.assertTrue(StringUtils.isNotEmpty((String) account.get("sap_external_id__c")));
				Assert.assertTrue(StringUtils.isNotEmpty((String) account.get("Name")));
			}
		}
	}
}
