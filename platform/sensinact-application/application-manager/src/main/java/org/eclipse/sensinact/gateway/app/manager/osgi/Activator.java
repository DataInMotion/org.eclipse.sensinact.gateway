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

package org.eclipse.sensinact.gateway.app.manager.osgi;

import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListenerAbstract;
import org.eclipse.sensinact.gateway.app.manager.application.persistence.APSApplication;
import org.eclipse.sensinact.gateway.app.manager.internal.AppManagerFactory;
import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * @see AbstractActivator
 *
 * @author Remi Druilhe
 */
public class Activator extends AbstractActivator<AppServiceMediator> {

    private static Logger LOG= LoggerFactory.getLogger(Activator.class);

    private AppManagerFactory appManagerFactory;

    private Thread persistenceThread;

    @Property(defaultValue = "application")
    private String directory;

    @Property(validationRegex = Property.INTEGER)
    private Long updateFileCheck;

    @Property
    private Boolean applicationPersist;

    @Property(defaultValue = "json",validationRegex = Property.ALPHANUMERIC)
    private String applicationFileExtension;

    /**
     * @see AbstractActivator#doStart()
     */
    public void doStart() throws Exception {

        APSApplication directoryMonitor=new APSApplication(new File(directory),updateFileCheck, applicationFileExtension);

        this.appManagerFactory = new AppManagerFactory(mediator,directoryMonitor);

        if(applicationPersist){
            LOG.info("Persistence mechanism is ON");
            directoryMonitor.registerServiceAvailabilityListener(appManagerFactory);
            persistenceThread=new Thread(directoryMonitor);
            persistenceThread.start();
        }else {
            LOG.info("Persistence mechanism is OFF");
        }

    }

    /**
     * @see AbstractActivator#doStop()
     */
    public void doStop() throws Exception {
        if(persistenceThread!=null) persistenceThread.interrupt();
        this.appManagerFactory.deleteAppManager();
    }

    /**
     * @see AbstractActivator#doInstantiate(BundleContext)
     */
    public AppServiceMediator doInstantiate(BundleContext context)
    {
        return new AppServiceMediator(context);
    }
}
