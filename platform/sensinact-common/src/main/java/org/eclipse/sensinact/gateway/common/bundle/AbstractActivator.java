/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */

package org.eclipse.sensinact.gateway.common.bundle;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.util.PropertyUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Abstract implementation of the {@link BundleActivator} interface
 */
public abstract class AbstractActivator<M extends Mediator> implements BundleActivator 
{
	/**
	 * Completes the starting process 
	 */
	public abstract void doStart() throws Exception;

	/**
	 * Completes the stopping process 
	 */
	public abstract void doStop() throws Exception;
	
	/**
	 * Creates and returns the specific {@link Mediator} 
	 * extended implementation instance
	 * 
	 * @param context
	 * 		the current {@link BundleContext}
	 * @return
	 * 		the specific {@link Mediator} extended 
	 * 		implementation instance
	 */
	public abstract M doInstantiate(BundleContext context) ;
	
	/**
	 * {@link Mediator} extended implementation instance
	 */
	protected M mediator;
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.osgi.framework.BundleActivator#
	 * start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception 
	{		
		this.mediator = this.initMediator(context);		
		injectPropertyFields();
		
		//complete starting process
		this.doStart();
	}

	private void injectPropertyFields() throws Exception {
		this.mediator.debug("Starting introspection in bundle %s",
				mediator.getContext().getBundle().getSymbolicName());
		for(Field field:this.getClass().getDeclaredFields()){
			this.mediator.debug("Evaluating field %s", field.getName());
			for(Annotation propertyAnnotation:field.getAnnotations()){
				try {
					if(! (propertyAnnotation instanceof Property)) continue;
					Property propAn=(Property)propertyAnnotation;
					String propertyName=null;
					if(!propAn.name().equals("")){
						propertyName=propAn.name();
					}else {
						propertyName=field.getName();
					}
					Object propertyValue=this.mediator.getProperty(propertyName);
					field.setAccessible(true);
					if(propertyValue!=null){
						this.mediator.info("Setting property %s from bundleActivator %s on field %s to value %s",
							propAn.name(), this.mediator.getContext().getBundle().getSymbolicName(),
							field.getName(), propertyValue);
						field.set(this, propertyValue);
					}else if(propAn.defaultValue()!=null&&!propAn.defaultValue().trim().equals("")){
						String value = propAn.defaultValue();
						field.set(this, propAn.defaultValue());
						this.mediator.info("Setting property %s from bundleActivator %s on field %s to default value which is %s", 
								propAn.name(), this.mediator.getContext().getBundle().getSymbolicName(), 
								field.getName(),value);
					}else {
						this.mediator.error("Property %s from bundleActivator %s is mandatory, bundle might not be configured correctly", 
								propAn.name(),this.mediator.getContext().getBundle().getSymbolicName());
					}
				} catch (IllegalAccessException e) {
					this.mediator.error(String.format("The field '%s' required property injection in bundle symbolic name '%s', although it was not possible to assign the value to the field, make sure the access signature is 'public' ",
						field.getName(),this.mediator.getContext().getBundle().getSymbolicName()).toString());
					throw new Exception(e);
				}
			}
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.osgi.framework.BundleActivator#
	 * stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception 
	{
		doStop();
		if(this.mediator != null)
		{
			this.mediator.deactivate();			
		}
		this.mediator = null;
	}
	
    /**
	 * Initializes and returns the {@link Mediator} of the
	 * current {@link Bundle}
	 * 
	 * @param context
	 * 		The current {@link BundleContext} 
	 * @return
	 * 		 the {@link Mediator}
	 * @ 
	 * @throws IOException 
	 */
	protected M initMediator(BundleContext context)			
	{
		M mediator = doInstantiate(context);
		return mediator;
	}

}
