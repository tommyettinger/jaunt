/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.service;

import javolution.util.function.Equality;
import javolution.util.function.Splittable;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * The set of related map functionalities required to implement fast maps.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see javolution.util.FastMap#FastMap()
 */
public interface MapService<K, V> extends 
        Map<K, V>, Splittable<MapService<K, V>>, Serializable {
    
    /**
     * Returns a set view over the entries of this map. The set 
     * supports adding/removing entries. Two entries are considered 
     * equals if they have the same key regardless of their values.
     */
    @Override
    SetService<Entry<K, V>> entrySet();

    /**
     *  Returns an iterator over this map entries.
     */
    Iterator<Entry<K, V>> iterator();

    /** 
    * Returns the key comparator used for key equality or order if the 
    * map is sorted.
    */
    Equality<? super K> keyComparator();

    /**
     * Returns a set view over the key of this map, the set supports
     * adding new keys, for which the value is automatically {@code null}.
     */
    @Override
    SetService<K> keySet();

    /** 
    * Returns the value comparator used for value equality.
    */
    Equality<? super V> valueComparator();

    /**
     * Returns a collection view over the values of this map, the collection 
     * support value/entry removal but not adding new values.
     */
    @Override
    CollectionService<V> values(); 
}