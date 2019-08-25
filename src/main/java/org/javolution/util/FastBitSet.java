/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import static org.javolution.annotations.Realtime.Limit.LINEAR;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.javolution.annotations.Nullable;
import org.javolution.annotations.Realtime;
import org.javolution.lang.Index;
import org.javolution.lang.MathLib;
import org.javolution.util.function.Order;
import org.javolution.util.function.Predicate;

/**
 * A high-performance bit-set integrated with the collection framework as a set of {@link Index indices} 
 * and obeying the collection semantic for methods such as {@link #size} (cardinality) or {@link #equals}
 * (same set of indices).</p>
 *   
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 7.0, September 13, 2015
 */
public class FastBitSet extends AbstractSet<Index> {

    private static final long serialVersionUID = 0x700L; // Version.
    private static final long[] ALL_CLEARED = new long[0];
    
    /** Holds the bits (64 bits per long). */
    private long[] bits;
    
    /** 
     * Creates a new bit-set (all bits cleared).
     */
    public FastBitSet() {
        bits = ALL_CLEARED;
    }

      ////////////////////////////////////////////////////////////////////////////
     // Set operations.
     //

    @Override
     public final boolean add(Index index, boolean allowDuplicate) {
         return add(index); // allowDuplicate flag ignored.
     }

     /**
     * Performs the logical AND operation on this bit set and the
     * given bit set. This means it builds the intersection
     * of the two sets. The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    @Realtime(limit = LINEAR)
    public final void and(FastBitSet that) {
        long[] thatBits = that.toLongArray();
        int n = MathLib.min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; i++) {
            this.bits[i] &= thatBits[i];
        }
        for (int i = n; i < bits.length; i++) {
            this.bits[i] = 0L;
        }
    }

 	/**
     * Performs the logical AND operation on this bit set and the
     * complement of the given bit set.  This means it
     * selects every element in the first set, that isn't in the
     * second set. The result is stored into this bit set.
     *
     * @param that the second bit set
     */
    @Realtime(limit = LINEAR)
    public final void andNot(FastBitSet that) {
        long[] thatBits = that.toLongArray();
        int n = MathLib.min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; i++) {
            this.bits[i] &= ~thatBits[i];
        }
    }

     /**
     * Returns the number of bits set to {@code true} (or the size of this 
     * set).
     *
     * @return the number of bits being set.
     */
    public final int cardinality() {
        int sum = 0;
        for (int i = 0; i < bits.length; i++) {
            sum += MathLib.bitCount(bits[i]);
        }
        return sum;
    }

     @Override
     public final void clear() {
         bits = ALL_CLEARED;
     }

     /**
     * Removes the specified integer value from this set. That is
     * the corresponding bit is cleared.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code index < 0}
     */
    public final void clear(int bitIndex) {
        int longIndex = bitIndex >> 6;
        if (longIndex >= bits.length)
            return;
        bits[longIndex] &= ~(1L << bitIndex);
    }

 	/**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param  fromIndex index of the first bit to be cleared.
     * @param  toIndex index after the last bit to be cleared.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @Realtime(limit = LINEAR)
    public final void clear(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 6;
        if (i >= bits.length)
            return; // Ensures that i < _length
        int j = toIndex >>> 6;
        if (i == j) {
            bits[i] &= ((1L << fromIndex) - 1) | (-1L << toIndex);
            return;
        }
        bits[i] &= (1L << fromIndex) - 1;
        if (j < bits.length) {
            bits[j] &= -1L << toIndex;
        }
        for (int k = i + 1; (k < j) && (k < bits.length); k++) {
            bits[k] = 0;
        }
    }

	@Override
 	public final FastBitSet clone() {
 	    FastBitSet copy = new FastBitSet();
 	    copy.bits = this.bits.clone();
 	    return copy;
 	}
	
     ////////////////////////////////////////////////////////////////////////////
     // BitSet Operations.
     //

    @Override
    public final FastIterator<Index> descendingIterator(@Nullable Index from) {
        int start = (from == null) ? this.length() - 1 : from.intValue();
        return new IteratorImpl(this, start, true);
    }

    /**
     * Sets the bit at the index to the opposite value.
     *
     * @param bitIndex the index of the bit.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public final void flip(int bitIndex) {
        int i = bitIndex >> 6;
        ensureCapacity(i + 1);
        bits[i] ^= 1L << bitIndex;
    }

    /**
     * Sets a range of bits to the opposite value.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @Realtime(limit = LINEAR)
    public final void flip(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 6;
        int j = toIndex >>> 6;
        ensureCapacity(j + 1);
        if (i == j) {
            bits[i] ^= (-1L << fromIndex) & ((1L << toIndex) - 1);
            return;
        }
        bits[i] ^= -1L << fromIndex;
        bits[j] ^= (1L << toIndex) - 1;
        for (int k = i + 1; k < j; k++) {
            bits[k] ^= -1;
        }
    }

    /**
     * Returns {@code true } if the specified integer is in 
     * this bit set; {@code false } otherwise.
     *
     * @param bitIndex a non-negative integer.
     * @return the value of the bit at the specified index.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public final boolean get(int bitIndex) {
        int i = bitIndex >> 6;
        return (i >= bits.length) ? false : (bits[i] & (1L << bitIndex)) != 0;
    }

    /**
     * Returns a new bit set composed of a range of bits from this one.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex the high index (exclusive).
     * @return a context allocated bit set instance.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @Realtime(limit = LINEAR)
    public final FastBitSet get(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex)
            throw new IndexOutOfBoundsException();
        FastBitSet bitSet = new FastBitSet();
        int length = MathLib.min(bits.length, (toIndex >>> 6) + 1);
        bitSet.bits = new long[length];
        System.arraycopy(bits, 0, bitSet.bits, 0, length);
        bitSet.clear(0, fromIndex);
        bitSet.clear(toIndex, length << 6);
        return bitSet;
    }

    /** 
     * Sets the specified bit, returns <code>true</code>
     * if previously set. */
    public final boolean getAndSet(int bitIndex, boolean value) {
        int i = bitIndex >> 6;
        ensureCapacity(i + 1);
        boolean previous = (bits[i] & (1L << bitIndex)) != 0;
        if (value) { 
            bits[i] |= 1L << bitIndex;
        } else {
            bits[i] &= ~(1L << bitIndex);
        }
        return previous;
    }
   
    @Override
    public Index getAny(Index index) {
        return get(index.intValue()) ? index : null;
    }

    /**
     * Returns {@code true} if this bit set shares at least one
     * common bit with the specified bit set.
     *
     * @param that the bit set to check for intersection
     * @return {@code true} if the sets intersect; {@code false} otherwise.
     */
    @Realtime(limit = LINEAR)
    public final boolean intersects(FastBitSet that) {
        long[] thatBits = that.toLongArray();
        int i = MathLib.min(this.bits.length, thatBits.length);
        while (--i >= 0) {
            if ((bits[i] & thatBits[i]) != 0) return true; 
        }
        return false;
    }

    @Override
	public final boolean isEmpty() {
		return size() == 0;
	}

    @Override
    public final FastIterator<Index> iterator(@Nullable Index from) {
        int start = (from == null) ? 0: from.intValue();
        return new IteratorImpl(this, start, false);
    }
    
    /**
     * Returns the logical number of bits actually used by this bit
     * set.  It returns the index of the highest set bit plus one.
     * 
     * <p> Note: This method does not return the number of set bits
     *           which is returned by {@link #size} </p>
     *
     * @return the index of the highest set bit plus one.
     */
    public final int length() {
        trim();
        if (bits.length == 0) return 0;
        return (bits.length << 6) - MathLib.numberOfLeadingZeros(bits[bits.length -1]);
    }

    /**
     * Returns the index of the next {@code false} bit, from the specified bit
     * (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public final int nextClearBit(int fromIndex) {
        int offset = fromIndex >> 6;
        long mask = 1L << fromIndex;
        while (offset < bits.length) {
            long h = bits[offset];
            do {
                if ((h & mask) == 0) { return fromIndex; }
                mask <<= 1;
                fromIndex++;
            } while (mask != 0);
            mask = 1;
            offset++;
        }
        return fromIndex;
    }

    /**
     * Returns the index of the next {@code true} bit, from the specified bit
     * (inclusive). If there is none, {@code -1} is returned. 
     * The following code will iterates through the bit set:[code]
     *    for (int i=nextSetBit(0); i >= 0; i = nextSetBit(i+1)) {
     *         ...
     *    }[/code]
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0} 
     */
    public final int nextSetBit(int fromIndex) {
        int offset = fromIndex >> 6;
        long mask = 1L << fromIndex;
        while (offset < bits.length) {
            long h = bits[offset];
            do {
                if ((h & mask) != 0)
                    return fromIndex;
                mask <<= 1;
                fromIndex++;
            } while (mask != 0);
            mask = 1;
            offset++;
        }
        return -1;
    }

    /**
     * Performs the logical OR operation on this bit set and the one specified.
     * In other words, builds the union of the two sets.  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    @Realtime(limit = LINEAR)
    public final void or(FastBitSet that) {
        long[] thatBits = (that instanceof FastBitSet) ? ((FastBitSet) that).bits
                : that.toLongArray();
        ensureCapacity(thatBits.length);
        for (int i = thatBits.length; --i >= 0;) {
            bits[i] |= thatBits[i];
        }
    }

    @Override
     public final Order<? super Index> order() {
         return Index.ORDER;
     }

    /**
     * Returns the index of the previous {@code false} bit, 
     * from the specified bit (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < -1} 
     */
    public final int previousClearBit(int fromIndex) {
        int offset = fromIndex >> 6;
        long mask = 1L << fromIndex;
        while (offset >= 0) {
            long h = bits[offset];
            do {
                if ((h & mask) == 0)
                    return fromIndex;
                mask >>= 1;
                fromIndex--;
            } while (mask != 0);
            mask = 1L << 63;
            offset--;
        }
        return -1;
    }

    /**
     * Returns the index of the previous {@code true} bit, from the 
     * specified bit (inclusive). If there is none, {@code -1} is returned. 
     * The following code will iterates through the bit set:[code]
     *     for (int i = length(); (i = previousSetBit(i-1)) >= 0; ) {
     *        ...
     *     }[/code]
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < -1} 
     */
    public final int previousSetBit(int fromIndex) {
        int offset = fromIndex >> 6;
        long mask = 1L << fromIndex;
        while (offset >= 0) {
            long h = bits[offset];
            do {
                if ((h & mask) != 0)
                    return fromIndex;
                mask >>= 1;
                fromIndex--;
            } while (mask != 0);
            mask = 1L << 63;
            offset--;
        }
        return -1;
    }

    @Override
    public Index removeAny(Index index) {
        return getAndSet(index.intValue(), false) ? index : null;
    }

    @Override
    public boolean removeIf(Predicate<? super Index> filter) {
        boolean modified = false;
        FastIterator<Index> itr = iterator();
        while (itr.hasNext(filter)) {
            clear(itr.next().intValue());
            modified = true;
        }
        return modified;
    }

    /**
     * Adds the specified integer to this set (corresponding bit is set to 
     * {@code true}.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public final void set(int bitIndex) {
        int i = bitIndex >> 6;
        ensureCapacity(i + 1);
        bits[i] |= 1L << bitIndex;
    }

	/**
     * Sets the bit at the given index to the specified value.
     *
     * @param bitIndex the position to set.
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public final void set(int bitIndex, boolean value) {
        if (value) {
            set(bitIndex);
        } else {
            clear(bitIndex);
        }
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param  fromIndex index of the first bit to be set.
     * @param  toIndex index after the last bit to be set.
     * @throws IndexOutOfBoundsException if 
     *          {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    @Realtime(limit = LINEAR)
    public final void set(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
        	throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 6;
        int j = toIndex >>> 6;
        ensureCapacity(j + 1);
        if (i == j) {
            bits[i] |= (-1L << fromIndex) & ((1L << toIndex) - 1);
            return;
        }
        bits[i] |= -1L << fromIndex;
        bits[j] |= (1L << toIndex) - 1;
        for (int k = i + 1; k < j; k++) {
            bits[k] = -1;
        }
    }

    /**
     * Sets the bits between from (inclusive) and to (exclusive) to the
     * specified value.
     *
     * @param fromIndex the start range (inclusive).
     * @param toIndex the end range (exclusive).
     * @param value the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    @Realtime(limit = LINEAR)
    public final void set(int fromIndex, int toIndex, boolean value) {
        if (value) {
            set(fromIndex, toIndex);
        } else {
            clear(fromIndex, toIndex);
        }
    }

    @Override
 	public final int size() {
        return cardinality();
 	}

    /** Returns the minimal length <code>long[]</code> representation of this bitset.
     * 
     * @return Array of longs representing this bitset 
     */
    public final long[] toLongArray() {
        trim();
        return bits;
    }

    /**
     * Performs the logical XOR operation on this bit set and the one specified.
     * In other words, builds the symmetric remainder of the two sets 
     * (the elements that are in one set, but not in the other).  
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    @Realtime(limit = LINEAR)
    public final void xor(FastBitSet that) {
        long[] thatBits = (that instanceof FastBitSet) ? ((FastBitSet) that).bits
                : that.toLongArray();
        ensureCapacity(thatBits.length);
        for (int i = thatBits.length; --i >= 0;) {
            bits[i] ^= thatBits[i];
        }
    }

    // Checks capacity.
    private void ensureCapacity(int capacity) {
        if (bits.length < capacity) {
            bits = Arrays.copyOf(bits, MathLib.max(bits.length * 2, capacity));
        }
    }

    // Removes trailing zeros.
    private void trim() {
        int n = bits.length;
        while ((--n >= 0) && (bits[n] == 0L)) {}
        if (++n != bits.length) { // Trim.
            bits = Arrays.copyOf(bits, n);
        }        
    }

    /** BitSet iterator implementation. */
    private static final class IteratorImpl implements FastIterator<Index> {

        private final FastBitSet that;
        private int nextIndex;
        private int currentIndex = -1;
        private boolean reversed;

        public IteratorImpl(FastBitSet that, int from, boolean reversed) {
            this.that = that;
            this.nextIndex = reversed ? that.previousSetBit(from) : that.nextSetBit(from);
            this.reversed = reversed;
        }

        public boolean hasNext() {
            return (nextIndex >= 0);
        }

        @Override
        public boolean hasNext(Predicate<? super Index> matching) {
            while (hasNext()) {
                if (matching.test(Index.of(nextIndex))) return true;
                next();
            }
            return false;
        }

        public Index next() {
            if (nextIndex < 0)
                throw new NoSuchElementException();
            currentIndex = nextIndex;
            nextIndex = reversed ? that.previousSetBit(nextIndex - 1) : that.nextSetBit(nextIndex + 1);
            return Index.of(currentIndex);
        }

        public void remove() {
            if (currentIndex < 0)
                throw new IllegalStateException();
            that.clear(currentIndex);
            currentIndex = -1;
        }

    }
}
