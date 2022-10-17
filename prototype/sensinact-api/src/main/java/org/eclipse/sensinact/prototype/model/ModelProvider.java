/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.model;

/**
 * Implemented by device providers that want to programmatically register their
 * models
 */
public interface ModelProvider {

    void init(ModelManager manager);

    void destroy();

}
