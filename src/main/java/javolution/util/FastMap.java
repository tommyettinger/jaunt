/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import javolution.lang.Immutable;
import javolution.lang.Realtime;
import javolution.util.function.Consumer;
import javolution.util.function.Equalities;
import javolution.util.function.Equality;
import javolution.util.internal.map.FastMapImpl;
import javolution.util.internal.map.UnmodifiableMapImpl;
import javolution.util.service.CollectionService;
import javolution.util.service.MapService;
import org.javolution.text.TextBuilder;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import static javolution.lang.Realtime.Limit.CONSTANT;
import static javolution.lang.Realtime.Limit.LINEAR;

/**
 * <p> A high-performance hash map with {@link Realtime real-time} behavior. 
 *     Related to {@link FastCollection}, fast map supports various views.
 * <ul>
 *    <li>{@link #unmodifiable} - View which does not allow any modifications.</li>
 *    <li>{@link #entrySet} - {@link FastSet} view over the map entries allowing 
 *                            entries to be added/removed.</li>
 *    <li>{@link #keySet} - {@link FastSet} view over the map keys allowing keys 
 *                           to be added (map entry with {@code null} value).</li>
 *    <li>{@link #values} - {@link FastCollection} view over the map values (add not supported).</li>
 * </ul>      
 * <p> The iteration order over the map keys, values or entries is deterministic 
 *     (unlike {@link java.util.HashMap}). It is either the insertion order (default) 
 *     or the key order for the {@link FastSortedMap} subclass. 
 *     This class permits {@code null} keys.</p> 
 *     
 * <p> Fast maps can advantageously replace any of the standard <code>java.util</code> maps.</p> 
 * <p>[code]
 *  FastMap<Foo, Bar> hashMap = new FastMap<Foo, Bar>(); 
 *  FastMap<Foo, Bar> linkedHashMap = new FastMap<Foo, Bar>(); // Deterministic iteration order (insertion order).
 *  FastMap<Foo, Bar> treeMap = new FastSortedMap<Foo, Bar>(); 
 *  FastMap<Foo, Bar> identityHashMap = new FastMap<Foo, Bar>(Equalities.IDENTITY);
 *  [/code]</p>
 *  <p> and adds more ... 
 * <p>[code]
 *  FastMap<String, Bar> lexicalHashMap = new FastMap<String, Bar>(Equalities.LEXICAL);  // Allows for value retrieval using any CharSequence key.
 *  FastMap<String, Bar> fastStringHashMap = new FastMap<String, Bar>(Equalities.LEXICAL_FAST);  // Same with faster hashcode calculations.
 *  ...
 *  [/code]</p>
 *  
 *  <p> Of course all views (entry, key, values) over a fast map are fast collections 
 *      and allow parallel processing.
 * [code]
 * Consumer<Collection<String>> removeNull = new Consumer<Collection<String>>() {  
 *     public void accept(Collection<String> view) {
 *          Iterator<String> it = view.iterator();
 *          while (it.hasNext()) {
 *             if (it.next() == null) it.remove();
 *         }
 *     }
 * };
 * FastMap<Person, String> names = ...
 * names.values().update(removeNull); // Remove all entries with null values.
 * [/code]</p> 
 *             
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 6.0, July 21, 2013
 */
@Realtime
public class FastMap<K, V> implements Map<K, V>,
        Serializable {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Holds the actual map service implementation.
     */
    private final MapService<K, V> service;

    /**
     * Creates an empty fast map.
     */
    public FastMap() {
        this(Equalities.STANDARD);
    }

    /**
     * Creates an empty fast map using the specified comparator for keys 
     * equality.
     */
    public FastMap(Equality<? super K> keyEquality) {
        this(keyEquality, Equalities.STANDARD);
    }

    /**
     * Creates an empty fast map using the specified comparators for keys 
     * equality and values equality.
     */
    public FastMap(Equality<? super K> keyEquality,
            Equality<? super V> valueEquality) {
        service = new FastMapImpl<K, V>(keyEquality, valueEquality);
    }

    /**
     * Creates a map backed up by the specified service implementation.
     */
    protected FastMap(MapService<K, V> service) {
        this.service = service;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    /**
     * Returns an unmodifiable view over this map. Any attempt to 
     * modify the map through this view will result into 
     * a {@link UnsupportedOperationException} being raised.
     */
    public FastMap<K, V> unmodifiable() {
        return new FastMap<K, V>(new UnmodifiableMapImpl<K, V>(service));
    }

    /**
     * Returns a set view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  The set supports 
     * adding new keys for which the corresponding entry value 
     * is always {@code null}.
     */
    public FastSet<K> keySet() {
        return new FastSet<K>(service.keySet());
    }

    /**
     * Returns a collection view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. The collection
     * supports removing values (hence entries) but not adding new values. 
     */
    public FastCollection<V> values() {
        return new FastCollection<V>() {
            private static final long serialVersionUID = 0x600L; // Version.
            private final CollectionService<V> serviceValues = service.values();

            @Override
            protected CollectionService<V> service() {
                return serviceValues;
            }
        };
    }

    /**
     * Returns a set view of the mappings contained in 
     * this map. The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa. The set 
     * support adding/removing entries. As far as the set is concerned,
     * two entries are considered equals if they have the same keys regardless
     * of their values. 
     */
    public FastSet<Entry<K, V>> entrySet() {
        return new FastSet<Entry<K, V>>(service.entrySet());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Closures operations.
    //

    /** 
     * Executes the specified read-only action on this map.
     *    
     * @param action the read-only action.
     * @throws UnsupportedOperationException if the action tries to update 
     *         this map.
     * @throws ClassCastException if the action type is not compatible with 
     *         this map (e.g. action on sorted map and this is a hash map). 
     * @see #update(Consumer)
     */
    @Realtime(limit = LINEAR)
    @SuppressWarnings("unchecked")
    public void perform(Consumer<? extends Map<K, V>> action) {
        service().perform((Consumer<MapService<K, V>>) action, service());
    }

    /** 
     * Executes the specified update action on this map. 
     *    
     * @param action the update action.
     * @throws ClassCastException if the action type is not compatible with 
     *         this map (e.g. action on sorted map and this is a hash map). 
     * @see #perform(Consumer)
     */
    @Realtime(limit = LINEAR)
    @SuppressWarnings("unchecked")
    public void update(Consumer<? extends Map<K, V>> action) {
        service().update((Consumer<MapService<K, V>>) action, service());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Map Interface.
    //

    /** Returns the number of entries/keys/values in this map. */
    @Override
    @Realtime(limit = CONSTANT)
    public int size() {
        return service.size();
    }

    /** Indicates if this map is empty */
    @Override
    @Realtime(limit = CONSTANT)
    public boolean isEmpty() {
        return service.isEmpty();
    }

    /** Indicates if this map contains the specified key. */
    @Override
    @Realtime(limit = CONSTANT)
    public boolean containsKey(Object key) {
        return service.containsKey(key);
    }

    /** Indicates if this map contains the specified value. */
    @Override
    @Realtime(limit = LINEAR)
    public boolean containsValue(Object value) {
        return service.containsValue(value);
    }

    /** Returns the value for the specified key. */
    @Override
    @Realtime(limit = CONSTANT)
    public V get(Object key) {
        return service.get(key);
    }

    /** Associates the specified value with the specified key. */
    @Override
    @Realtime(limit = CONSTANT)
    public V put(K key, V value) {
        return service.put(key, value);
    }

    /** Adds the specified map entries to this map. */
    @Override
    @Realtime(limit = LINEAR)
    public void putAll(Map<? extends K, ? extends V> map) {
        service.putAll(map);
    }

    /** Removes the entry for the specified key. */
    @Override
    @Realtime(limit = CONSTANT)
    public V remove(Object key) {
        return service.remove(key);
    }

    /** Removes all this map's entries. */
    @Override
    @Realtime(limit = CONSTANT)
    public void clear() {
        service.clear();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Map Interface.
    //

    /** Associates the specified value with the specified key only if the 
     * specified key has no current mapping. */
    @Override
    @Realtime(limit = CONSTANT)
    public V putIfAbsent(K key, V value) {
        return service.putIfAbsent(key, value);
    }

    /** Removes the entry for a key only if currently mapped to a given value. */
    @Override
    @Realtime(limit = CONSTANT)
    public boolean remove(Object key, Object value) {
        return service.remove(key, value);
    }

    /** Replaces the entry for a key only if currently mapped to a given value. */
    @Override
    @Realtime(limit = CONSTANT)
    public boolean replace(K key, V oldValue, V newValue) {
        return service.replace(key, oldValue, newValue);
    }

    /** Replaces the entry for a key only if currently mapped to some value. */
    @Override
    @Realtime(limit = CONSTANT)
    public V replace(K key, V value) {
        return service.replace(key, value);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    /**
     * Returns this map with the specified map's entries added.
     */
    public FastMap<K, V> putAll(FastMap<? extends K, ? extends V> that) {
        putAll((Map<? extends K, ? extends V>) that);
        return this;
    }

    /** 
     * Returns an immutable reference over this map. The immutable 
     * value is an {@link #unmodifiable() unmodifiable} view of this map
     * for which the caller guarantees that no change will ever be made 
     * (e.g. there is no reference left to the original map).
     */
    public <T extends Map<K, V>> Immutable<T> toImmutable() {
        return new Immutable<T>() {
            @SuppressWarnings("unchecked")
            final T value = (T) unmodifiable();

            @Override
            public T value() {
                return value;
            }
        };
    }

    /** Returns the string representation of this map entries. */
    @Override
    @Realtime(limit = LINEAR)
    public String toString() {
        Iterator<?> i = service.iterator();
        TextBuilder dest = new TextBuilder();
        dest.append('[');
        while (i.hasNext()) {
            dest.append(i.next());
            if (i.hasNext()) {
                dest.append(',').append(' ');
            }
        }
        return dest.append(']').toString();
    }

    /**
      * Returns this map service implementation.
      */
    protected MapService<K, V> service() {
        return service;
    }

}
