//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.17 at 09:13:55 PM EDT 
//


package org.javolution.xml.jaxb.test.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for testBoundedWrapperElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="testBoundedWrapperElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="testElement" type="{http://javolution.org/xml/schema/javolution}testElement" minOccurs="0"/>
 *         &lt;element name="testValidationElement" type="{http://javolution.org/xml/schema/javolution}testValidationElement" minOccurs="0"/>
 *         &lt;element name="testAttributeElement" type="{http://javolution.org/xml/schema/javolution}testAttributeElement" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testBoundedWrapperElement", propOrder = {
    "testElement",
    "testValidationElement",
    "testAttributeElement"
})
public class TestBoundedWrapperElement {

    protected TestElement testElement;
    protected TestValidationElement testValidationElement;
    protected TestAttributeElement testAttributeElement;

    /**
     * Gets the value of the testElement property.
     * 
     * @return
     *     possible object is
     *     {@link TestElement }
     *     
     */
    public TestElement getTestElement() {
        return testElement;
    }

    /**
     * Sets the value of the testElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link TestElement }
     *     
     */
    public void setTestElement(TestElement value) {
        this.testElement = value;
    }

    /**
     * Gets the value of the testValidationElement property.
     * 
     * @return
     *     possible object is
     *     {@link TestValidationElement }
     *     
     */
    public TestValidationElement getTestValidationElement() {
        return testValidationElement;
    }

    /**
     * Sets the value of the testValidationElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link TestValidationElement }
     *     
     */
    public void setTestValidationElement(TestValidationElement value) {
        this.testValidationElement = value;
    }

    /**
     * Gets the value of the testAttributeElement property.
     * 
     * @return
     *     possible object is
     *     {@link TestAttributeElement }
     *     
     */
    public TestAttributeElement getTestAttributeElement() {
        return testAttributeElement;
    }

    /**
     * Sets the value of the testAttributeElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link TestAttributeElement }
     *     
     */
    public void setTestAttributeElement(TestAttributeElement value) {
        this.testAttributeElement = value;
    }

}
