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

/**
 * A set of {@link ProcessableData} processable by one {@link ResourceImpl} and
 * targeting one identified {@link ServiceImpl}
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface ServiceProcessableData<R extends ResourceProcessableData> extends ResourceProcessableContainer<R> {
	/**
	 * Returns the {@link Service}'s string identifier targeted by this
	 * PayloadFragment
	 * 
	 * @return the targeted {@link Service}'s identifier
	 */
	String getServiceId();
}
