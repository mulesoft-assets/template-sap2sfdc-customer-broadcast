/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SapPayloadGenerator {
	private static final String DEFAULT_TEMPLATE_NAME = "SAP_TEMPLATE";
	private static final String MATERIAL_ID_XPATH = "/DEBMAS01/IDOC/E1KNA1M/KUNNR";
	private static final Logger log = LogManager.getLogger(SapPayloadGenerator.class);

	private XPath xpath;
	private Document doc;
	private String templateName;

	private List<String> uniqueIdList = new ArrayList<String>();

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		String xml = loadFile("./src/test/resources/debmas01.xml");
		SapPayloadGenerator generator = new SapPayloadGenerator(xml);
		log.info(generator.generateXML());
		log.info(generator.getUniqueIdList());
	}

	private static String loadFile(String filePath) throws IOException {
		InputStream in = new FileInputStream(filePath);
		return IOUtils.toString(in);
	}

	private static Document buildDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

		return doc;
	}

	public SapPayloadGenerator(String xmlFile) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		doc = buildDocument(xmlFile);
		xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	 * Generates an SAP XML based on an example XML provided. The returned XML
	 * has its respective IDs created in a unique way for this particular run.
	 * 
	 * @return
	 */
	public String generateXML() {
		uniqueIdList.clear();

		try {
			return generateUniqueIds();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateName() {
		if (StringUtils.isEmpty(templateName)) {
			return DEFAULT_TEMPLATE_NAME;
		}
		return templateName;
	}

	public List<String> getUniqueIdList() {
		return uniqueIdList;
	}

	private String generateUniqueIds()
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException {

		NodeList nodeList = (NodeList) xpath.compile(MATERIAL_ID_XPATH).evaluate(doc, javax.xml.xpath.XPathConstants.NODESET);
		makeIdsUnique(nodeList);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		transformer.transform(new DOMSource(doc), new StreamResult(os));
		return new String(os.toByteArray());

	}

	private void makeIdsUnique(NodeList nodeList) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		String waterMark = "_" + getTemplateName() + System.currentTimeMillis();

		int index = 0;
		while (index < nodeList.getLength()) {
			Node node = nodeList.item(index);
			String uniqueId = node.getTextContent() + waterMark;
			node.setTextContent(uniqueId);
			index++;

			uniqueIdList.add(uniqueId);
		}
	}
}
