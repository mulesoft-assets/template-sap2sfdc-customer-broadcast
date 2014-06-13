/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.mule.api.config.MuleProperties;
import org.mule.tck.junit4.FunctionalTestCase;

/**
 * This is the base test class for Anypoint Templates integration tests.
 * 
 * @author damiansima
 */
public abstract class AbstractTemplateTestCase extends FunctionalTestCase {
	private static final String MAPPINGS_FOLDER_PATH = "./mappings";
	private static final String TEST_FLOWS_FOLDER_PATH = "./src/test/resources/flows/";
	private static final String MULE_DEPLOY_PROPERTIES_PATH = "./src/main/app/mule-deploy.properties";

	protected static final String TEMPLATE_NAME = "sap2sfdc-account-broadcast";

	@Override
	protected String getConfigResources() {
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(MULE_DEPLOY_PROPERTIES_PATH));
			return props.getProperty("config.resources") + getTestFlows();
		} catch (IOException e) {
			throw new IllegalStateException("Could not find mule-deploy.properties file on classpath. "
					+ "Please add any of those files or override the getConfigResources() method to provide the resources by your own.");
		}

	}

	protected String getTestFlows() {
		File testFlowsFolder = new File(TEST_FLOWS_FOLDER_PATH);
		File[] listOfFiles = testFlowsFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isFile() && f.getName().endsWith("xml");
			}
		});
		
		if (listOfFiles == null) {
			return "";
		}
		
		StringBuilder resources = new StringBuilder();
		for (File f : listOfFiles) {
			resources.append(",").append(TEST_FLOWS_FOLDER_PATH).append(f.getName());
		}
		return resources.toString();
	}

	@Override
	protected Properties getStartUpProperties() {
		Properties properties = new Properties(super.getStartUpProperties());
		properties.put(MuleProperties.APP_HOME_DIRECTORY_PROPERTY, new File(MAPPINGS_FOLDER_PATH).getAbsolutePath());
		return properties;
	}

	protected String buildUniqueName(String templateName, String name) {
		return new StringBuilder()
				.append(name)
				.append(templateName)
				.append(new Long(new Date().getTime()).toString())
				.toString();
	}
	
	protected static String getFileString(String filePath) throws IOException {
		return FileUtils.readFileToString(new File(filePath));
	}

}
