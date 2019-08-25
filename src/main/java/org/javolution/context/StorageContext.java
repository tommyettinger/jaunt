/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.context;

import java.io.File;
import java.io.Serializable;

import org.javolution.lang.Configurable;
import org.javolution.osgi.internal.OSGiServices;

/**
 * A context for persistent storage integrated with OSGi and {@link SecurityContext}.
 *     
 * How the data is going to be stored (database, files) is implementation dependent. But how the resources are 
 * structured (hierarchical, semantics) is defined by the client implementing the {@link Resource} class.
 *  
 * ```java
 * class SemanticResource<T> extends SemanticEntity implements Resource<T> { ... }
 * ...
 * StorageContext ctx = StorageContext.enter(); // Enters the current storage service.
 * try {
 *     // Stores resource.
 *     SemanticResource<Image> logoId = new SemanticResource<Image>(
 *          "http://purl.org/goodrelations/v1#Logo|MyCompany");
 *     ctx.write(logoId, logoImg);  // May raise SecurityException. 
 *     ...
 *     // Retrieves resource.
 *     Image logoImg = ctx.read(logoId); // May raise SecurityException. 
 *  } finally {
 *     ctx.exit(); 
 *  }
 * ```
 *  
 * Permission to read/write resource values may or not be granted at all or only for particular resources. 
 * Sensitive data should always be encrypted (e.g. using a `SecuredStorageContext` sub-class).
 * There is no limit in the size of the data being stored (except the actual storage available). 
 * It is nonetheless recommended to split large data set in smaller resources to allow for partial/concurrent retrieval. 
 * 
 * @author  <jean-marie@dautelle.com>
 * @version 7.0, March 31, 2017
 */
public abstract class StorageContext extends AbstractContext {

    /**
     * Holds the default storage location for file based-implementations
     * (default {@code new File("storage")}).
     */
    public static final Configurable<File> FILE_STORAGE_LOCATION = new Configurable<File>() {
        @Override
        protected File getDefault() {
            return new File("storage");
        }

        protected File parse(String pathname) {
            return new File(pathname);
        }
    };

    /**
     * A resource identifier. 
     * 
     * @param <T> The resource type (the type of the value stored).
     */
    public interface Resource<T> {

        /**
         * @return an unique identifier for this resource.
         */
        public String uniqueID();

    }

    /**
     * Default constructor.
     */
    protected StorageContext() {
    }

    /**
     * Enters and returns a storage context instance.
     * @return Reference to the entered StorageContext
     */
    public static StorageContext enter() {
        return (StorageContext) currentStorageContext().enterInner();
    }

    /**
     * Reads the persistent value of the specified resource value. 
     * 
     * @param <V> type of persistent value
     * @param resource the entity whose persistent value is returned.
     * @return the resource value or <code>null</code> if none.
     * @throws SecurityException if the permission to read the resource 
     *         is not granted ({@code new 
     *         Permission<Resource<V>>(Resource.class, "read", resource)}).
     */
    public abstract <V extends Serializable> V read(Resource<V> resource) throws SecurityException;

    /**
     * Writes the persistent value of the specified resource.
     * 
     * @param <V> type of persistent value
     * @param resource the entity whose persistent value is stored.
     * @param value the persistent value.
     * @throws SecurityException if the permission to write the resource 
     *         is not granted ({@code new 
     *         Permission<Resource<V>>(Resource.class, "write", resource)}).
     */
    public abstract <V extends Serializable> void write(Resource<V> resource, V value) throws SecurityException;

    /**
     * Returns the current storage context. 
     */
    private static StorageContext currentStorageContext() {
        StorageContext ctx = current(StorageContext.class);
        if (ctx != null)
            return ctx;
        return OSGiServices.getStorageContext();
    }
}
