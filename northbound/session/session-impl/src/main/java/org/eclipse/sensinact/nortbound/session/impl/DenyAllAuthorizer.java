/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.nortbound.session.impl;

import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer.PreAuth.DENY;

import java.util.Collection;
import java.util.List;

import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.PermissionLevel;

class DenyAllAuthorizer implements Authorizer {

    @Override
    public PreAuth preAuthProvider(PermissionLevel level, String provider) {
        return DENY;
    }

    @Override
    public PreAuth preAuthService(PermissionLevel level, String provider, String service) {
        return DENY;
    }

    @Override
    public PreAuth preAuthResource(PermissionLevel level, String provider, String service, String resource) {
        return DENY;
    }

    @Override
    public boolean hasProviderPermission(PermissionLevel level, String modelPackageUri, String model,
            String provider) {
        return false;
    }

    @Override
    public boolean hasServicePermission(PermissionLevel level, String modelPackageUri, String model,
            String provider, String service) {
        return false;
    }

    @Override
    public boolean hasResourcePermission(PermissionLevel level, String modelPackageUri, String model,
            String provider, String service, String resource) {
        return false;
    }

    @Override
    public Collection<String> visibleServices(String modelPackageUri, String model, String provider,
            Collection<String> services) {
        return List.of();
    }

    @Override
    public Collection<String> visibleResources(String modelPackageUri, String model, String provider,
            String service, Collection<String> services) {
        return List.of();
    }
    
}