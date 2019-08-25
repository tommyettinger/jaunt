/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.xml.jaxb;

import org.javolution.io.AppendableWriter;
import org.javolution.io.CharSequenceReader;
import org.javolution.text.TextBuilder;
import org.javolution.xml.internal.jaxb.JAXBAnnotatedObjectWriterImpl;
import org.javolution.xml.jaxb.common.test.schema.TestCommonRoot;
import org.javolution.xml.jaxb.test.schema.TestRoot;
import org.junit.Test;

import javax.xml.bind.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class JAXBAnnotatedObjectWriterTest {

	@Test
	public void testSmall() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-small.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testBoundedElementWithMixedBoundedNesting() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-bounded-element-with-mixed-bounded-nesting.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testBoundedElementWithNestedAttributeOnly() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-bounded-element-with-nested-attribute-only.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testCommonElement() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-common-element.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testLargeNestedMixedObject() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-large-nested-mixed-object.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testLargeValidNestedMixedObject() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-large-valid-nested-mixed-object.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, true);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testMoreThanOneOfSameElementWithMixedData() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-more-than-one-of-same-element-with-mixed-data.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testWithBasicDatatypes() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-basic-datatypes.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testWithCDataString() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-cdata-string.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testWithChoiceElement() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-choice-element.xml");
		final TestCommonRoot testCommonRoot = readJAXBObjectWithJDK(TestCommonRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testCommonRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testCommonRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testWithEnumAndDate() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-enum-and-date.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testWithSkippedElements() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-skipped-elements.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test(expected=MarshalException.class)
	public void testWithMissingRequiredAttribute() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-missing-required-attribute.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		writeJAXBObjectWithJavolution(testRoot, true);
	}

	@Test(expected=MarshalException.class)
	public void testWithMissingRequiredElement() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-missing-required-element.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		writeJAXBObjectWithJavolution(testRoot, true);
	}

	@Test
	public void testWithQNameElement() throws JAXBException, IOException, URISyntaxException {
		final String xml = readXMLResourceToString("/test-with-namespace-element.xml");
		final TestCommonRoot testCommonRoot = readJAXBObjectWithJDK(TestCommonRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testCommonRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testCommonRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testWithBinaryElement() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-binary-element.xml");
		final TestCommonRoot testCommonRoot = readJAXBObjectWithJDK(TestCommonRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testCommonRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testCommonRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testWithValueElement() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-value-element.xml");
		final TestCommonRoot testCommonRoot = readJAXBObjectWithJDK(TestCommonRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testCommonRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testCommonRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testWithEmptyElements() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-empty-elements.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	@Test
	public void testWithDecimalAndInteger() throws JAXBException, IOException, URISyntaxException{
		final String xml = readXMLResourceToString("/test-with-decimal-and-integer.xml");
		final TestRoot testRoot = readJAXBObjectWithJDK(TestRoot.class, xml);
		final String javolutionXml = writeJAXBObjectWithJavolution(testRoot, false);
		final String jdkXml = writeJAXBObjectWithJDK(testRoot);
		assertEquals("XML Equals", jdkXml, javolutionXml);
	}

	private String readXMLResourceToString(final String resource) throws IOException, URISyntaxException{
		final URL xmlUrl = JAXBAnnotatedObjectWriterTest.class.getResource(resource);
		final File xmlFile = new File(xmlUrl.toURI());

		Scanner scanner = null;

		try {
			scanner = new Scanner( xmlFile, "UTF-8" );
			return scanner.useDelimiter("\\A").next();
		} finally {
			scanner.close();
		}
	}

	private <T> T readJAXBObjectWithJDK(final Class<T> jaxbClass, final String xml) throws JAXBException{
		final JAXBContext context = JAXBContext.newInstance(jaxbClass);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		return (T) unmarshaller.unmarshal(new CharSequenceReader(xml));
	}

	private <T> String writeJAXBObjectWithJDK(final T jaxbClass) throws JAXBException{
		final JAXBContext context = JAXBContext.newInstance(jaxbClass.getClass());
		final Marshaller marshaller = context.createMarshaller();
		final AppendableWriter writer = new AppendableWriter(new TextBuilder());
		marshaller.marshal(jaxbClass, writer);
		return writer.getOutput().toString();
	}

	private <T> String writeJAXBObjectWithJavolution(final T jaxbClass, final boolean validate) throws JAXBException{
		final JAXBAnnotatedObjectWriter jaxbWriter = new JAXBAnnotatedObjectWriterImpl(jaxbClass.getClass());
		jaxbWriter.setValidating(validate);
		final AppendableWriter writer = new AppendableWriter(new TextBuilder());
		jaxbWriter.write(jaxbClass, writer);
		return writer.getOutput().toString();
	}
}
