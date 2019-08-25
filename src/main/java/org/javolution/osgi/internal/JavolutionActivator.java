/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.osgi.internal;

import org.javolution.xml.stream.XMLInputFactory;
import org.javolution.xml.stream.XMLOutputFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Javolution OSGi bundle activator.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class JavolutionActivator implements BundleActivator {

    // Services provided by Javolution.
    private ServiceRegistration<XMLInputFactory> xmlInputFactoryRegistration;
    private ServiceRegistration<XMLOutputFactory> xmlOutputFactoryRegistration;

    // Javolution trackers.
    private ProgramTrackerImpl programTracker;
    
    @SuppressWarnings("unchecked")
    public void start(BundleContext bc) throws Exception {
        
        // Activate services trackers.
        OSGiServices.COMPUTE_CONTEXT_TRACKER.activate(bc);
        OSGiServices.CONCURRENT_CONTEXT_TRACKER.activate(bc);
        OSGiServices.CONFIGURABLE_LISTENER_TRACKER.activate(bc);
        OSGiServices.LOCAL_CONTEXT_TRACKER.activate(bc);
        OSGiServices.LOG_CONTEXT_TRACKER.activate(bc);
        OSGiServices.LOG_SERVICE_TRACKER.activate(bc);
        OSGiServices.SECURITY_CONTEXT_TRACKER.activate(bc);
        OSGiServices.STORAGE_CONTEXT_TRACKER.activate(bc);
        OSGiServices.TEXT_CONTEXT_TRACKER.activate(bc);
        OSGiServices.JAXB_ANNOTATION_FACTORY_TRACKER.activate(bc);
        OSGiServices.XML_CONTEXT_TRACKER.activate(bc);
        OSGiServices.XML_INPUT_FACTORY_TRACKER.activate(bc);
        OSGiServices.XML_OUTPUT_FACTORY_TRACKER.activate(bc);
        
        // OpenCL Program Tracker (load/unload OpenCL programs).
        programTracker = new ProgramTrackerImpl(bc);
        programTracker.open();

        // Publish XMLInputFactory/XMLOutputFactory services.
        xmlInputFactoryRegistration = (ServiceRegistration<XMLInputFactory>) bc
                .registerService(XMLInputFactory.class.getName(),
                        new XMLInputFactoryProvider(), null);
        xmlOutputFactoryRegistration = (ServiceRegistration<XMLOutputFactory>) bc
                .registerService(XMLOutputFactory.class.getName(),
                        new XMLOutputFactoryProvider(), null);
        
        // Ensures low latency for real-time classes.
        OSGiServices.initializeRealtimeClasses();
    }

    public void stop(BundleContext bc) throws Exception {
        OSGiServices.COMPUTE_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.CONCURRENT_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.CONFIGURABLE_LISTENER_TRACKER.deactivate(bc);
        OSGiServices.LOCAL_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.LOG_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.LOG_SERVICE_TRACKER.deactivate(bc);
        OSGiServices.SECURITY_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.STORAGE_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.TEXT_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.JAXB_ANNOTATION_FACTORY_TRACKER.deactivate(bc);
        OSGiServices.XML_CONTEXT_TRACKER.deactivate(bc);
        OSGiServices.XML_INPUT_FACTORY_TRACKER.deactivate(bc);
        OSGiServices.XML_OUTPUT_FACTORY_TRACKER.deactivate(bc);

        programTracker.close();
        
        xmlInputFactoryRegistration.unregister();
        xmlOutputFactoryRegistration.unregister();   
    }

}