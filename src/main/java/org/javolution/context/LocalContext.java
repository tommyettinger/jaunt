/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.context;

import org.javolution.context.SecurityContext.Permission;
import org.javolution.lang.Configurable;
import org.javolution.osgi.internal.OSGiServices;

/**
 * A context holding locally scoped {@link Parameter parameters} values.
 * 
 * For example, when performing modulo arithmetics the actual modulo being used is usually the same for most operations
 * and does not need to be specified for each operation.
 * 
 * ```java
 * import javolution.context.LocalContext.Parameter;
 * public class ModuloInteger extends Number {
 *     public static final Parameter<Integer> MODULO = new Parameter<Integer>() {
 *          protected Integer getDefault() { return -1; }
 *     }; 
 *     public ModuloInteger times(ModuloInteger that) { ... }    
 * }     
 * LocalContext ctx = LocalContext.enter(); 
 * try {
 *     ctx.supersede(ModuloInteger.MODULO, 13); // Sets local modulo value.
 *     x = a.times(b).plus(c.times(d)); // Operations modulo 13
 *     ...
 * } finally {
 *     ctx.exit(); // Reverts to previous modulo setting. 
 * }}</p>
 * ```
 *     
 * As for any context, local context settings are inherited during {@link ConcurrentContext} executions. 
 *
 * @author  <jean-marie@dautelle.com>
 * @version 7.0, March 31, 2017
 */
public abstract class LocalContext extends AbstractContext {

    /**
     * A {@link Configurable configurable} parameter whose value can be locally superseded within the scope of 
     * {@link LocalContext}.
     */
    public static abstract class Parameter<T> extends Configurable<T> {

        /**
         * Holds the general permission to supersede any parameter value (action "supersede").
         */
        public static final Permission<Parameter<?>> SUPERSEDE_PERMISSION = new Permission<Parameter<?>>(
                Parameter.class, "supersede");

        /**
         * Holds this instance supersede permission.
         */
        private final Permission<Parameter<T>> supersedePermission;

        /**
         * Creates a new parameter (configurable).
         */
        public Parameter() {
            this.supersedePermission = new Permission<Parameter<T>>(
                    Parameter.class, "supersede", this);
        }

        /**
         * @return the permission to locally supersede the current value 
         * of this instance.
         */
        public Permission<Parameter<T>> getSupersedePermission() {
            return supersedePermission;
        }

        /**
         * Returns the current parameter value (the default value if not 
         * reconfigured nor {@link LocalContext#supersede superseded}).
         */
        public T get() {
            LocalContext ctx = current(LocalContext.class);
            return (ctx != null) ? ctx.getValue(this, super.get()) : super.get();
        }
    }

    /**
     * Default constructor.
     */
    protected LocalContext() {}

    /**
     * Enters a new local context instance.
     * @return the inner local context entered.
     */
    public static LocalContext enter() {
        LocalContext ctx = current(LocalContext.class);
        if (ctx == null) { // Root.
            ctx = OSGiServices.getLocalContext();
        }
        return (LocalContext) ctx.enterInner();
    }

    /**
     * Supersedes the value of the specified parameter. 
     * 
     * @param  <T> the type of the parameter
     * @param  param the local parameter whose local value is overridden.
     * @param  localValue the new local value.
     * @throws SecurityException if the permission to override the specified 
     *         parameter is not granted.
     * @throws NullPointerException if the specified local value is {@code null}.
     */
    public abstract <T> void supersede(Parameter<T> param, T localValue);

    /**
     * Returns the local value of the specified parameter or the specified 
     * default value if not {@link LocalContext#supersede superseded}. 
     * 
     * @param  <T> the type of the parameter
     * @param param the local parameter whose local value is returned.
     * @param defaultValue the parameter value if not superseded.
     * @return value of the parameter
     */
    protected abstract <T> T getValue(Parameter<T> param, T defaultValue);
    
}