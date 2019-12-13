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
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 * Builder of a {@link ServiceImpl}
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public class ServiceBuilder {
	private Mediator mediator;

	protected final Class<? extends ServiceImpl> baseClass;
	private Class<? extends ServiceImpl> implementationClass;
	private String name;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the associated {@link Mediator}
	 */

	public ServiceBuilder(Mediator mediator) {
		this(mediator, ServiceImpl.class);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the associated {@link Mediator}
	 * @param baseClass
	 * 
	 */
	public ServiceBuilder(Mediator mediator, Class<? extends ServiceImpl> baseClass) {
		this.mediator = mediator;
		this.baseClass = (Class<? extends ServiceImpl>) baseClass;
	}

	/**
	 * Configures the name of the service to build
	 * 
	 * @param name
	 *            the name of the service to build
	 */
	public void configureName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the service to build
	 * 
	 * @return the name of the service to build
	 */
	public String getConfiguredName() {
		return this.name;
	}

	/**
	 * Configures the extended {@link ServiceImpl} type used to build the service
	 * 
	 * @param implementationClass
	 *            the extended {@link ServiceImpl} type of the service to build
	 */
	public void configureImplementationClass(Class<? extends ServiceImpl> implementationClass) {
		this.implementationClass = implementationClass;
	}

	/**
	 * Builds and returns a new {@link ServiceImpl} connected to the
	 * {@link ServiceProviderImpl} passed as parameter
	 * 
	 * @param provider
	 *            the service provider to which the {@link ServiceImpl} to create is
	 *            connected to
	 * @return a new {@link ServiceImpl}
	 * 
	 * @throws InvalidServiceException
	 */
	public final ServiceImpl build(ModelInstance<?> snaModelInstance, ServiceProviderImpl provider)
			throws InvalidServiceException {
		if (name == null) {
			throw new InvalidServiceException("Service's name is needed");
		}
		ServiceImpl serviceImpl = null;
		try {
			serviceImpl = ReflectUtils.getInstance(baseClass,
					implementationClass == null ? ServiceImpl.class : implementationClass,
					new Object[] { snaModelInstance, name, provider });
		} catch (Exception e) {
			this.mediator.error(e);
			throw new InvalidServiceException(e.getMessage(), e);
		}
		return serviceImpl;
	}
}
