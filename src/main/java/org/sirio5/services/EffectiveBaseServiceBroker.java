package org.sirio5.services;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.services.*;

/**
 * A generic implementation of a <code>ServiceBroker</code> which
 * provides:
 *
 * <ul>
 * <li>Maintaining service name to class name mapping, allowing
 * pluggable service implementations.</li>
 * <li>Providing <code>Services</code> with a configuration based on
 * system wide configuration mechanism.</li>
 * <li>Integration of TurbineServiceProviders for looking up
 * non-local services</li>
 * </ul>
 *
 * @author <a href="mailto:burton@apache.org">Kevin Burton</a>
 * @author <a href="mailto:krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: EffectiveBaseServiceBroker.java 1706239 2015-10-01 13:18:35Z tv $
 */
public abstract class EffectiveBaseServiceBroker implements ServiceBroker
{
  /** Logging */
  private static final Log log = LogFactory.getLog(EffectiveBaseServiceBroker.class);

  /**
   * Mapping of Service names to class names, keep order.
   */
  protected final Map<String, Class<?>> mapping = new LinkedHashMap<String, Class<?>>();

  /**
   * A repository of Service instances.
   */
  protected final Hashtable<String, Service> services = new Hashtable<String, Service>();

  /**
   * Configuration for the services broker.
   * The configuration should be set by the application
   * in which the services framework is running.
   */
  protected Configuration configuration;

  /**
   * A prefix for <code>Service</code> properties in
   * TurbineResource.properties.
   */
  public static final String SERVICE_PREFIX = "services.";

  /**
   * A <code>Service</code> property determining its implementing
   * class name .
   */
  public static final String CLASSNAME_SUFFIX = ".classname";

  /**
   * These are objects that the parent application
   * can provide so that application specific
   * services have a mechanism to retrieve specialized
   * information. For example, in Turbine there are services
   * that require the RunData object: these services can
   * retrieve the RunData object that Turbine has placed
   * in the service manager. This alleviates us of
   * the requirement of having init(Object) all
   * together.
   */
  protected final Hashtable<String, Object> serviceObjects = new Hashtable<String, Object>();

  /**
   * Application root path as set by the
   * parent application.
   */
  protected String applicationRoot;

  /**
   * mapping from service names to instances of TurbineServiceProviders
   */
  protected final Hashtable<String, Service> serviceProviderInstanceMap = new Hashtable<String, Service>();

  /**
   * Default constructor, protected as to only be usable by subclasses.
   *
   * This constructor does nothing.
   */
  protected EffectiveBaseServiceBroker()
  {
  }

  /**
   * Set the configuration object for the services broker.
   * This is the configuration that contains information
   * about all services in the care of this service
   * manager.
   *
   * @param configuration Broker configuration.
   */
  public void setConfiguration(Configuration configuration)
  {
    this.configuration = configuration;
  }

  /**
   * Get the configuration for this service manager.
   *
   * @return Broker configuration.
   */
  public Configuration getConfiguration()
  {
    return configuration;
  }

  /**
   * Initialize this service manager.
   * @throws InitializationException if the initialization fails
   */
  public void init()
     throws InitializationException
  {
    // Check:
    //
    // 1. The configuration has been set.
    // 2. Make sure the application root has been set.

    // FIXME: Make some service framework exceptions to throw in
    // the event these requirements aren't satisfied.
    // Create the mapping between service names
    // and their classes.
    initMapping();

    // Start services that have their 'earlyInit'
    // property set to 'true'.
    initServices(false);
  }

  /**
   * Set an application specific service object
   * that can be used by application specific
   * services.
   *
   * @param name name of service object
   * @param value value of service object
   */
  public void setServiceObject(String name, Object value)
  {
    serviceObjects.put(name, value);
  }

  /**
   * Get an application specific service object.
   *
   * @param name the name of the service object
   * @return Object application specific service object
   */
  public Object getServiceObject(String name)
  {
    return serviceObjects.get(name);
  }

  /**
   * Check recursively if the given checkIfc interface is among the implemented
   * interfaces
   *
   * @param checkIfc interface to check for
   * @param interfaces interfaces to scan
   * @return true if the interface is implemented
   */
  protected boolean checkForInterface(Class<?> checkIfc, Class<?>[] interfaces)
  {
    for(Class<?> ifc : interfaces)
    {
      if(ifc == checkIfc)
      {
        return true;
      }

      Class<?>[] subInterfaces = ifc.getInterfaces();
      if(checkForInterface(checkIfc, subInterfaces))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Creates a mapping between Service names and class names.
   *
   * The mapping is built according to settings present in
   * TurbineResources.properties. The entries should have the
   * following form:
   *
   * <pre>
   * services.MyService.classname=com.mycompany.MyServiceImpl
   * services.MyOtherService.classname=com.mycompany.MyOtherServiceImpl
   * </pre>
   *
   * <br>
   *
   * Generic ServiceBroker provides no Services.
   * @throws InitializationException if a service class could not be found
   */
  protected void initMapping()
     throws InitializationException
  {
    // we need to temporarily store the earlyInit flags to avoid
    // ConcurrentModificationExceptions
    Map<String, String> earlyInitFlags = new LinkedHashMap<String, String>();

    /*
         * These keys returned in an order that corresponds
         * to the order the services are listed in
         * the TR.props.
     */
    for(Iterator<String> keys = configuration.getKeys(); keys.hasNext();)
    {
      String key = keys.next();
      String[] keyParts = StringUtils.split(key, ".");

      if((keyParts.length == 3)
         && (keyParts[0] + ".").equals(SERVICE_PREFIX)
         && ("." + keyParts[2]).equals(CLASSNAME_SUFFIX))
      {
        String serviceKey = keyParts[1];
        log.info("Added Mapping for Service: " + serviceKey);

        if(!mapping.containsKey(serviceKey))
        {
          String className = configuration.getString(key);
          try
          {
            Class<?> clazz = Class.forName(className);
            mapping.put(serviceKey, clazz);

            // detect TurbineServiceProviders
            if(checkForInterface(TurbineServiceProvider.class, clazz.getInterfaces()))
            {
              log.info("Found a TurbineServiceProvider: " + serviceKey + " - initializing it early");
              earlyInitFlags.put(SERVICE_PREFIX + serviceKey + ".earlyInit", "true");
            }
          }
          // those two errors must be passed to the VM
          catch(ThreadDeath t)
          {
            throw t;
          }
          catch(OutOfMemoryError t)
          {
            throw t;
          }
          catch(ClassNotFoundException e)
          {
            throw new InitializationException("Class " + className
               + " is unavailable. Check your jars and classes.", e);
          }
          catch(NoClassDefFoundError e)
          {
            throw new InitializationException("Class " + className
               + " is unavailable. Check your jars and classes.", e);
          }
        }
      }
    }

    for(Map.Entry<String, String> entry : earlyInitFlags.entrySet())
    {
      configuration.setProperty(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Determines whether a service is registered in the configured
   * <code>TurbineResources.properties</code>.
   *
   * @param serviceName The name of the service whose existence to check.
   * @return Registration predicate for the desired services.
   */
  @Override
  public boolean isRegistered(String serviceName)
  {
    return (services.get(serviceName) != null);
  }

  /**
   * Returns an Iterator over all known service names.
   *
   * @return An Iterator of service names.
   */
  public Iterator<String> getServiceNames()
  {
    return mapping.keySet().iterator();
  }

  /**
   * Returns an Iterator over all known service names beginning with
   * the provided prefix.
   *
   * @param prefix The prefix against which to test.
   * @return An Iterator of service names which match the prefix.
   */
  public Iterator<String> getServiceNames(String prefix)
  {
    Set<String> keys = new LinkedHashSet<String>(mapping.keySet());
    for(Iterator<String> key = keys.iterator(); key.hasNext();)
    {
      if(!key.next().startsWith(prefix))
      {
        key.remove();
      }
    }

    return keys.iterator();
  }

  /**
   * Performs early initialization of specified service.
   *
   * @param name The name of the service (generally the
   * <code>SERVICE_NAME</code> constant of the service's interface
   * definition).
   * @exception InitializationException Initialization of this
   * service was not successful.
   */
  @Override
  public synchronized void initService(String name)
     throws InitializationException
  {
    // Calling getServiceInstance(name) assures that the Service
    // implementation has its name and broker reference set before
    // initialization.
    Service instance = getServiceInstance(name);

    if(!instance.getInit())
    {
      // this call might result in an indirect recursion
      instance.init();
    }
  }

  /**
   * Performs early initialization of all services. Failed early
   * initialization of a Service may be non-fatal to the system,
   * thus any exceptions are logged and the initialization process
   * continues.
   */
  public void initServices()
  {
    try
    {
      initServices(false);
    }
    catch(InitializationException notThrown)
    {
      log.debug("Caught non fatal exception", notThrown);
    }
  }

  /**
   * Performs early initialization of all services. You can decide
   * to handle failed initializations if you wish, but then
   * after one service fails, the other will not have the chance
   * to initialize.
   *
   * @param report <code>true</code> if you want exceptions thrown.
   * @throws RuntimeException if the service could not be instantiated
   * @throws InitializationException if the service could not be initialized
   */
  public void initServices(boolean report)
     throws InitializationException
  {
    if(report)
    {
      // Throw exceptions
      for(Iterator<String> names = getServiceNames(); names.hasNext();)
      {
        doInitService(names.next());
      }
    }
    else
    {
      // Eat exceptions
      for(Iterator<String> names = getServiceNames(); names.hasNext();)
      {
        try
        {
          doInitService(names.next());
        }
        catch(InitializationException e)
        {
          log.error(e);
        }
      }
    }
    log.info("Finished initializing all services!");
  }

  /**
   * Internal utility method for use in {@link #initServices(boolean)}
   * to prevent duplication of code.
   */
  protected void doInitService(String name)
     throws InitializationException
  {
    // Only start up services that have their earlyInit flag set.
    if(getConfiguration(name).getBoolean("earlyInit", false))
    {
      log.info("Start Initializing service (early): " + name);
      initService(name);
      log.info("Finish Initializing service (early): " + name);
    }
  }

  /**
   * Shuts down a <code>Service</code>, releasing resources
   * allocated by an <code>Service</code>, and returns it to its
   * initial (uninitialized) state.
   *
   * @param name The name of the <code>Service</code> to be
   * uninitialized.
   */
  @Override
  public synchronized void shutdownService(String name)
  {
    try
    {
      Service service = getServiceInstance(name);
      if(service != null && service.getInit())
      {
        service.shutdown();

//        if(service.getInit() && service instanceof BaseService)
//        {
//          // BaseService::shutdown() does this by default,
//          // but could've been overriden poorly.
//          ((BaseService) service).setInit(false);
//        }
      }
    }
    catch(Exception e)
    {
      // Assuming harmless -- log the error and continue.
      log.error("Shutdown of a nonexistent Service '"
         + name + "' was requested", e);
    }
  }

  /**
   * Shuts down all Turbine services, releasing allocated resources and
   * returning them to their initial (uninitialized) state.
   */
  @Override
  public void shutdownServices()
  {
    log.info("Shutting down all services!");

    String serviceName = null;

    /*
         * Now we want to reverse the order of
         * this list. This functionality should be added to
         * the ExtendedProperties in the commons but
         * this will fix the problem for now.
     */
    ArrayList<String> reverseServicesList = new ArrayList<String>();

    for(Iterator<String> serviceNames = getServiceNames(); serviceNames.hasNext();)
    {
      serviceName = serviceNames.next();
      reverseServicesList.add(0, serviceName);
    }

    for(Iterator<String> serviceNames = reverseServicesList.iterator(); serviceNames.hasNext();)
    {
      serviceName = serviceNames.next();
      log.info("Shutting down service: " + serviceName);
      shutdownService(serviceName);
    }
  }

  /**
   * Returns an instance of requested Service.
   *
   * @param name The name of the Service requested.
   * @return An instance of requested Service.
   * @exception RuntimeException if the service is unknown or
   * can't be initialized.
   */
  @Override
  public Object getService(String name)
     throws RuntimeException
  {
    Service service;

    if(this.isLocalService(name))
    {
      try
      {
        service = getServiceInstance(name);
        if(!service.getInit())
        {
          synchronized(service.getClass())
          {
            if(!service.getInit())
            {
              log.info("Start Initializing service (late): " + name);
              service.init();
              log.info("Finish Initializing service (late): " + name);
            }
          }
        }
        if(!service.getInit())
        {
          // this exception will be caught & rethrown by this very method.
          // getInit() returning false indicates some initialization issue,
          // which in turn prevents the InitableBroker from passing a
          // reference to a working instance of the initable to the client.
          throw new InitializationException(
             "init() failed to initialize service " + name);
        }
        return service;
      }
      catch(InitializationException e)
      {
        throw new RuntimeException("Service " + name
           + " failed to initialize", e);
      }
    }
    else if(this.isNonLocalService(name))
    {
      return this.getNonLocalService(name);
    }
    else
    {
      throw new RuntimeException(
         "ServiceBroker: unknown service " + name
         + " requested");
    }
  }

  /**
   * Retrieves an instance of a Service without triggering late
   * initialization.
   *
   * Early initialization of a Service can require access to Service
   * properties. The Service must have its name and serviceBroker
   * set by then. Therefore, before calling
   * Initable.initClass(Object), the class must be instantiated with
   * InitableBroker.getInitableInstance(), and
   * Service.setServiceBroker() and Service.setName() must be
   * called. This calls for two - level accessing the Services
   * instances.
   *
   * @param name The name of the service requested.
   * @exception RuntimeException The service is unknown or
   * can't be initialized.
   */
  protected Service getServiceInstance(String name)
     throws RuntimeException
  {
    Service service = services.get(name);

    if(service == null)
    {
      if(!this.isLocalService(name))
      {
        throw new RuntimeException(
           "ServiceBroker: unknown service " + name
           + " requested");
      }

      try
      {
        Class<?> clazz = mapping.get(name);

        try
        {
          service = (Service) clazz.newInstance();

          // check if the newly created service is also a
          // service provider - if so then remember it
          if(service instanceof TurbineServiceProvider)
          {
            this.serviceProviderInstanceMap.put(name, service);
          }
        }
        // those two errors must be passed to the VM
        catch(ClassCastException e)
        {
          throw new RuntimeException("Class " + clazz
             + " doesn't implement the Service interface", e);
        }
        catch(ThreadDeath t)
        {
          throw t;
        }
        catch(OutOfMemoryError t)
        {
          throw t;
        }
        catch(Throwable t)
        {
          throw new RuntimeException("Failed to instantiate " + clazz, t);
        }
      }
      catch(RuntimeException e)
      {
        throw new RuntimeException(
           "Failed to instantiate service " + name, e);
      }
      service.setServiceBroker(this);
      service.setName(name);
      services.put(name, service);
    }

    return service;
  }

  /**
   * Returns the configuration for the specified service.
   *
   * @param name The name of the service.
   * @return Configuration of requested Service.
   */
  @Override
  public Configuration getConfiguration(String name)
  {
    return configuration.subset(SERVICE_PREFIX + name);
  }

  /**
   * Set the application root.
   *
   * @param applicationRoot application root
   */
  public void setApplicationRoot(String applicationRoot)
  {
    this.applicationRoot = applicationRoot;
  }

  /**
   * Get the application root as set by
   * the parent application.
   *
   * @return String application root
   */
  public String getApplicationRoot()
  {
    return applicationRoot;
  }

  /**
   * Determines if the requested service is managed by this
   * ServiceBroker.
   *
   * @param name The name of the Service requested.
   * @return true if the service is managed by the this ServiceBroker
   */
  protected boolean isLocalService(String name)
  {
    return this.mapping.containsKey(name);
  }

  /**
   * Determines if the requested service is managed by an initialized
   * TurbineServiceProvider. We use the service names to lookup
   * the TurbineServiceProvider to ensure that we get a fully
   * inititialized service.
   *
   * @param name The name of the Service requested.
   * @return true if the service is managed by a TurbineServiceProvider
   */
  protected boolean isNonLocalService(String name)
  {
    String serviceName = null;
    TurbineServiceProvider turbineServiceProvider = null;
    Enumeration<String> list = this.serviceProviderInstanceMap.keys();

    while(list.hasMoreElements())
    {
      serviceName = list.nextElement();
      turbineServiceProvider = (TurbineServiceProvider) this.getService(serviceName);

      if(turbineServiceProvider.exists(name))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Get a non-local service managed by a TurbineServiceProvider.
   *
   * @param name The name of the Service requested.
   * @return the requested service
   * @throws RuntimeException the service couldn't be instantiated
   */
  protected Object getNonLocalService(String name)
     throws RuntimeException
  {
    String serviceName = null;
    TurbineServiceProvider turbineServiceProvider = null;
    Enumeration<String> list = this.serviceProviderInstanceMap.keys();

    while(list.hasMoreElements())
    {
      serviceName = list.nextElement();
      turbineServiceProvider = (TurbineServiceProvider) this.getService(serviceName);

      if(turbineServiceProvider.exists(name))
      {
        return turbineServiceProvider.get(name);
      }
    }

    throw new RuntimeException(
       "ServiceBroker: unknown non-local service " + name
       + " requested");
  }
}
