/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.function;


/**
 * An object which can be divided in distinct parts and on which the same 
 * action may be performed on its part rather than its whole. 
 *  
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public interface Splittable<T> {

    /** 
     * Executes a read-only action on the specified part of this object.
     *       
     * @param action the read-only action.
     * @param part this object or a part of it.
     * @throws UnsupportedOperationException if the action tries to update the 
     *         specified part.
     */
    void perform(Consumer<T> action, T part);

    /** 
     * Executes an update action on the specified part of this object. 
     * Any change to the part is reflected in the whole (this object). 
     *       
     * @param action the action authorized to update this object part.
     * @param part this object or a part of it.
     */
    void update(Consumer<T> action, T part);

}