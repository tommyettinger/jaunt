/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.lang;

import org.javolution.annotations.Realtime;
import org.javolution.util.function.Order;

import java.io.ObjectStreamException;

/**
 * <p> A non-negative number (64-bits unsigned) representing a position in an arrangement.
 * <pre>{@code
 * FastMap<Index, Double> sparseVector = FastMap.newMap(Index.ORDER);
 * FastMap<Binary<Index,Index>, Double> sparseMatrix = FastMap.newMap(Index.QUADTREE_ORDER);
 * }</pre></p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, February 25, 2014
 * @see org.javolution.util.function.Indexer
 */
@Realtime
public final class Index extends Number implements Comparable<Index>, Immutable {
    private static final long serialVersionUID = 0x700L; // Version.

    /**
     * Index order (for use in Javolution collections/map).
     */
    public static final Order<Index> ORDER = new Order<Index>() {

		private static final long serialVersionUID = Index.serialVersionUID;

		@Override
		public boolean areEqual(Index left, Index right) {
			return left.unsigned == right.unsigned;
		}

		@Override
		public int compare(Index left, Index right) {
			return left.compareTo(right);
		}

		@Override
		public long indexOf(Index index) {
			return index.unsigned;
		}
    
    };
    
    private static final Index[] INSTANCES = new Index[1024];

    /**
     * Holds the index unsigned {@code long} value.
     */
    private final long unsigned;

    static {
        for (int i = 0; i < INSTANCES.length; i++) {
            INSTANCES[i] = new Index(i);
        }
    }
    
    /**
     * Holds the index zero ({@code Index.of(0)}).
     */
    public static final Index ZERO = Index.of(0);
    
    /**
     * Holds the index one({@code Index.of(1)}).
     */
    public static final Index ONE = Index.of(1);

    /**
     * Holds the index maximum ({@code Index.of(0xFFFFFFFF)}).
     */
    public static final Index MAX = Index.of(-1L);


    /**
     * Returns the index for the specified 64-bits unsigned value (a preallocated instance if the specified value 
     * is small).
     * 
     * @param unsigned the unsigned 64-bits value.
     * @return the corresponding index.
     */
    public static Index of(long unsigned) {
        return (unsigned >= 0) & (unsigned < INSTANCES.length) ? INSTANCES[(int)unsigned] : new Index(unsigned);
    }

    /**
     * Returns the index for the specified 32-bits unsigned value (a preallocated instance if the specified value 
     * is small).
     * 
     * @param unsigned the unsigned 32-bits value.
     * @return the corresponding index.
     */
    public static Index of(int unsigned) {
        return (unsigned >= 0) & (unsigned < INSTANCES.length) ? INSTANCES[unsigned] :
        	new Index(MathLib.unsigned(unsigned));
    }

    /**
     * Creates an index having the specified 64-bits unsigned value.
     */
    private Index(long unsigned) {
        this.unsigned = unsigned;
    }

    @Override
    public int compareTo(Index that) {
        return compareTo(that.unsigned);
    }

    /**
     * Compares this index with the specified 64-bits unsigned value for order.
     */
    public int compareTo(long unsignedValue) {
        return MathLib.unsignedLessThan(this.unsigned, unsignedValue) ? -1 : this.unsigned == unsignedValue ? 0 : 1;
    }

    /**
     * Compares this index with the specified 32-bits unsigned value for order.
     */
    public int compareTo(int unsignedValue) {
        return MathLib.unsignedLessThan(this.unsigned, MathLib.unsigned(unsignedValue)) ? -1 
        		: this.unsigned == unsignedValue ? 0 : 1;
    }

    @Override
    public double doubleValue() {
        return (double) longValue();
    }

    /**
     * Indicates if this index has the same value as the one specified.
     */
    public boolean equals(Index that) {
        return (this.unsigned == that.unsigned);
    }

    /**
     * Indicates if this index has the specified 32-bits unsigned value.
     * @see #equals(Index)
     */
    public boolean equals(int unsigned) {
        return (this.unsigned == MathLib.unsigned(unsigned));
    }

    /**
     * Indicates if this index has the specified 64-bits unsigned value.
     * @see #equals(Index)
     */
    public boolean equals(long unsigned) {
        return (this.unsigned == unsigned);
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || (obj instanceof Index) ? equals((Index) obj)
                : false;
    }

    @Override
    public float floatValue() {
        return (float) longValue();
    }

    /**
     * Returns {@code intValue()}
     */
    @Override
    public int hashCode() {
        return intValue();
    }

    @Override
    public int intValue() {
        return (int) longValue();
    }

    @Override
    public long longValue() {
        return unsigned;
    }

    /**
     * Returns the index after this one.
     * 
     * @throws IndexOutOfBoundsException if {@code this.equals(Index.MAX)}
     */
    public Index next() {
        if (unsigned == -1L)
            throw new IndexOutOfBoundsException();
        return Index.of(unsigned + 1);
    }

    /**
     * Returns the index before this one.
     * 
     * @throws IndexOutOfBoundsException if {@code this.equals(Index.ZERO)}
     */
    public Index previous() {
        if (unsigned == 0)
            throw new IndexOutOfBoundsException();
        return Index.of(unsigned - 1);
    }

    @Override
    public String toString() {
        return Long.toString(longValue());
    }

    /**
     * Ensures index unicity during deserialization.
     */
    protected Object readResolve() throws ObjectStreamException {
        return Index.of(unsigned);
    }

}