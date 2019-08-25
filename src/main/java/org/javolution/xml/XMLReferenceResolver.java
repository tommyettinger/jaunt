/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.xml;

import org.javolution.lang.Index;
import org.javolution.text.CharArray;
import org.javolution.text.TextBuilder;
import org.javolution.util.FastMap;
import org.javolution.util.FastTable;
import org.javolution.util.function.Order;
import org.javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents a resolver for XML cross references during 
 *     the marshalling/unmarshalling process.</p>
 *     
 * <p> Instances of this class may only be shared by {@link XMLObjectReader}/ 
 *     {@link XMLObjectWriter} running sequentially (for cross references 
 *     spawning multiple documents).</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public class XMLReferenceResolver {

    /**
     * Holds object to identifier (FastTable.Index) mapping.
     */
    private FastMap<Object, Index> _objectToId = new FastMap<Object, Index>(Order.identity());

    /**
     * Holds the objects (index to object mapping).
     */
    private FastTable<Object> _idToObject = new FastTable<Object>();

    /**
     * Holds the id counter.
     */
    private int _counter;

    /**
     * Holds the identifier attribute name.
     */
    private String _idName = "id";

    /**
     * Holds the identifier attribute URI if any.
     */
    private String _idURI = null;

    /**
     * Holds the reference attribute name.
     */
    private String _refName = "ref";

    /**
     * Holds the reference attribute URI if any.
     */
    private String _refURI = null;

    /**
     * Default constructor.
     */
    public XMLReferenceResolver() {}

    /**
     * Sets the name of the identifier attribute (by default<code>"id"</code>).
     * If the name is <code>null</code> then the identifier attribute
     * is never read/written (which may prevent unmarshalling).
     * 
     * @param name the name of the attribute or <code>null</code>.
     */
    public void setIdentifierAttribute(String name) {
        setIdentifierAttribute(name, null);
    }

    /**
     * Sets the local name and namespace URI of the identifier attribute.
     * 
     * @param localName the local name of the attribute or <code>null</code>.
     * @param uri the URI of the attribute or <code>null</code> if the attribute
     *        has no namespace URI.
     */
    public void setIdentifierAttribute(String localName, String uri) {
        _idName = localName;
        _idURI = uri;
    }

    /**
     * Sets the name of the reference attribute (by default<code>"ref"</code>).
     * If the name is <code>null</code> then the reference attribute
     * is never read/written (which may prevent unmarshalling).
     * 
     * @param name the name of the attribute or <code>null</code>.
     */
    public void setReferenceAttribute(String name) {
        setReferenceAttribute(name, null);
    }

    /**
     * Sets the local name and namespace URI of the identifier attribute.
     * 
     * @param localName the local name of the attribute or <code>null</code>.
     * @param uri the URI of the attribute or <code>null</code> if the attribute
     *        has no namespace URI.
     */
    public void setReferenceAttribute(String localName, String uri) {
        _refName = localName;
        _refURI = uri;
    }

    /**
     * Writes a reference to the specified object into the specified XML
     * element. The default implementation writes the reference into the 
     * reference attribute and for the first occurences an identifier 
     * (counter starting at 1) is written into the identifier attribute. 
     * 
     * @param  obj the object for which the reference is written.
     * @param  xml the output XML element.
     * @throws org.javolution.xml.stream.XMLStreamException if an exception occurs while writing the reference
     * @return <code>true</code> if a reference is written;
     *         <code>false</code> if a new identifier is written.
     */
    public boolean writeReference(Object obj, XMLFormat.OutputElement xml)
            throws XMLStreamException {
        Index id = (Index) _objectToId.get(obj);
        if (id == null) { // New identifier.
            id = Index.of(_counter++);
            _objectToId.put(obj, id);
            _tmp.clear().append(id.intValue());
            if (_idURI == null) {
                xml.getStreamWriter().writeAttribute(_idName, _tmp);
            } else {
                xml.getStreamWriter().writeAttribute(_idURI, _idName, _tmp);
            }
            return false;
        }
        _tmp.clear().append(id.intValue());
        if (_refURI == null) {
            xml._writer.writeAttribute(_refName, _tmp);
        } else {
            xml._writer.writeAttribute(_refURI, _refName, _tmp);
        }
        return true;
    }

    private TextBuilder _tmp = new TextBuilder();

    /**
     * Reads the object referenced by the specified xml input element if any.
     * The default implementation reads the reference attribute to retrieve 
     * the object. 
     * 
     * @param  xml the input XML element.
     * @throws org.javolution.xml.stream.XMLStreamException if an exception occurs while reading the reference
     * @return the referenced object or <code>null</code> if the specified 
     *         XML input does not have a reference attribute.
     */
    public Object readReference(XMLFormat.InputElement xml)
            throws XMLStreamException {
        CharArray value = xml._reader.getAttributeValue(_refURI, _refName);
        if (value == null)
            return null;
        int ref = value.toInt();
        if (ref >= _idToObject.size())
            throw new XMLStreamException("Reference: " + value + " not found");
        return _idToObject.get(ref);
    }

    /**
     * Creates a reference for the specified object (the identifier
     * being specified by the input XML element).
     * The default implementation reads the identifier attribute (if any)
     * and associates it to the specified object. 
     * 
     * @param  obj the object being referenced.
     * @param  xml the input XML element holding the reference identifier.
     * @throws org.javolution.xml.stream.XMLStreamException if an exception occurs while creating the reference
     */
    public void createReference(Object obj, XMLFormat.InputElement xml)
            throws XMLStreamException {
        CharArray value = xml._reader.getAttributeValue(_idURI, _idName);
        if (value == null)
            return;
        int i = value.toInt();
        if (_idToObject.size() != i)
            throw new XMLStreamException("Identifier discontinuity detected "
                    + "(expected " + _idToObject.size() + " found " + i + ")");
        _idToObject.add(obj);
    }

    public void reset() {
        _idName = "id";
        _idURI = null;
        _refName = "ref";
        _refURI = null;
        _idToObject.clear();
        _objectToId.clear();
        _counter = 0;
    }

}
