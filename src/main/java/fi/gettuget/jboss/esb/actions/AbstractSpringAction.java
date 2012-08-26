/*             
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                    Version 2, December 2004

 Copyright (C) 2012 Lauri Naukkarinen <gettuget@gmail.com>

 Everyone is permitted to copy and distribute verbatim or modified
 copies of this license document, and changing it is allowed as long
 as the name is changed.

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

  0. You just DO WHAT THE FUCK YOU WANT TO.
  
*/

package fi.gettuget.jboss.esb.actions;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.configure.ConfigProperty;
import org.jboss.soa.esb.configure.ConfigProperty.Use;
import org.jboss.soa.esb.lifecycle.annotation.Destroy;
import org.jboss.soa.esb.lifecycle.annotation.Initialize;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;


public abstract class AbstractSpringAction {

	private static final Logger logger = Logger.getLogger(AbstractSpringAction.class);

	private BeanFactoryReference beanFactoryReference;
	
	@ConfigProperty(name = "springContextXml", use = Use.REQUIRED)
	private String contextSelector;

	@ConfigProperty(name = "beanFactoryKey", use = Use.REQUIRED)
	private String factoryKey;
	
	/**
	 * Returns the bean factory instance.
	 * 
	 */
	public final BeanFactory getBeanFactory() {
		if (beanFactoryReference == null) {
			initialize();
		}
		return beanFactoryReference.getFactory();
	}
	
	/**
	 * Initializes the Spring context.
	 * 
	 */
	@Initialize
	public final void initialize() throws ActionLifecycleException {
				
		preInitialize();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Configuring spring with specified context " + contextSelector + " and factory key " + factoryKey);
		}

		BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(contextSelector);
		beanFactoryReference = locator.useBeanFactory(factoryKey);
		BeanFactory beanFactory = beanFactoryReference.getFactory();
		
		// The bean factory must be an instance of application context for retrieving an autowire capable bean factory.
		if (beanFactory instanceof ApplicationContext) {
			((ApplicationContext) beanFactory).getAutowireCapableBeanFactory().autowireBean(this);
		} else {
			throw new IllegalStateException("The bean factory is not an instance of ApplicationContext " + beanFactory);
		}
	}

	/**
	 * Executed before initialize()
	 * 
	 * Allows validation of config properties before bean autowiring and bean post-properties processing.
	 * 
	 */
	protected void preInitialize() throws ActionLifecycleException {
		// empty
	}

	/**
	 * Releases all reserved resources.
	 * 
	 */
	@Destroy
	public final void destroy() throws ActionLifecycleException {
		beanFactoryReference.release();
		postDestroy();
	}

	/**
	 * Executed after destroy()
	 * 
	 */
	protected void postDestroy() throws ActionLifecycleException {
		// empty
	}
}