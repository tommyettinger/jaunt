/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that any attempt to change the state of a class instance, a static field, a method return value 
 * or a parameter is forbidden (equivalent to C++ {@code const} keyword). 
 * For elements tagged {@code ReadOnly}, the compiler may flag errors if an attempt to call a method modifying 
 * the state of the object is made. 
 * 
 * ```java
 * ​@ReadOnly
 * class ConstTable<E> extends FastTable<E> implements Immutable { ... }
 * 
 * ​@ReadOnly Document [][] docs1; // Array of arrays of read-only documents
 * Document @ReadOnly [][] docs2; // Read-only array of arrays of documents (JDK 1.8+)
 * Document[] @ReadOnly [] docs3; // Array of read-only arrays of documents (JDK 1.8+)
 * }
 * ```
 * 
 * Note: Read-only classes for which the entire reachable graph is read-only should implement the 
 *       {@link org.javolution.lang.Immutable Immutable} interface.
 *   
 * @author  <jean-marie@dautelle.com>
 * @version 7.0 September 13, 2015
 */
@Documented
@Inherited
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
	       ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly  {

    /**
     * Indicates if this element is constant (default {@code true}).
     * 
     * @return true if constant
     */
    boolean value() default true;

    /**
     * Provides additional information (default {@code ""}).
     * 
     * @return comment
     */
    String comment() default "";

}
