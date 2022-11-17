/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.rest.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Base of all REST access results
 */
public abstract class ResultRootDTO {

    /**
     * Internal status code (HTTP semantics)
     */
    public int statusCode;

    /**
     * Type of result content
     */
    public String type;

    /**
     * Result access URI
     */
    public String uri;

    /**
     * Error message
     */
    @JsonInclude(NON_NULL)
    public String error;
}