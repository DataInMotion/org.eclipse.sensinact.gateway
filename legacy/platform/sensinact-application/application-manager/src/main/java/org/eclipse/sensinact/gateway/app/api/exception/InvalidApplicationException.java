/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.api.exception;

/**
 * This exception is used when the application catch an exception
 *
 * @author Rémi Druilhe
 */
public class InvalidApplicationException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidApplicationException() {
        super();
    }

    public InvalidApplicationException(String message) {
        super(message);
    }
}
