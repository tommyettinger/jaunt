/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.function;

/**
 * A function which states or affirms the attribute or quality of something.
 * 
 * @param <T> The type of input object to test.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Predicate_(mathematical_logic)">
 *      Wikipedia: Predicate</a>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface Predicate<T> extends java.util.function.Predicate<T> {

	/**
	 * Predicate always returning {@code true}.
	 */
	public static final Predicate<Object> TRUE = new Predicate<Object>() {

		@Override
		public boolean test(Object param) {
			return true;
		}
	};

	/**
	 * Predicate always returning {@code false}.
	 */
	public static final Predicate<Object> FALSE = new Predicate<Object>() {

		@Override
		public boolean test(Object param) {
			return false;
		}
	};

    /**
     * Predicate returning {@code true} if the specified object is {@code null}.
     */
    public static final Predicate<Object> IS_NULL = new Predicate<Object>() {

        @Override
        public boolean test(Object param) {
            return param == null;
        }
    };

    /**
     * Predicate returning {@code true} if the specified object is not {@code null}.
     */
    public static final Predicate<Object> IS_NOT_NULL = new Predicate<Object>() {

        @Override
        public boolean test(Object param) {
            return param != null;
        }
    };
	/**
	 * Tests the specified value.
	 * 
	 * @param param the type of input object to test
	 * @return true if the test passes
	 */
	boolean test(T param);

}