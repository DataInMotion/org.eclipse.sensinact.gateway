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
package org.eclipse.sensinact.gateway.app.manager.application;

import org.eclipse.sensinact.gateway.app.api.exception.ApplicationRuntimeException;
import org.eclipse.sensinact.gateway.app.api.exception.LifeCycleException;
import org.eclipse.sensinact.gateway.app.api.exception.ResourceNotFoundException;
import org.eclipse.sensinact.gateway.app.manager.component.Component;
import org.eclipse.sensinact.gateway.app.manager.component.ResourceDataProvider;
import org.eclipse.sensinact.gateway.app.manager.json.AppCondition;
import org.eclipse.sensinact.gateway.app.manager.json.AppContainer;
import org.eclipse.sensinact.gateway.app.manager.json.AppSnaMessage;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.app.manager.watchdog.AppExceptionWatchDog;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessage;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class wraps the components and handles the lifecycle of the components
 *
 * @author Rémi Druilhe
 */
public class Application extends AbstractSensiNactApplication {
    private static Logger LOG = LoggerFactory.getLogger(Application.class);
    private static Logger LOG4J = LoggerFactory.getLogger(Application.class.getCanonicalName());
    private final List<ServiceRegistration> serviceRegistrations;
    private final Map<ResourceDataProvider, Collection<ResourceSubscription>> resourceSubscriptions;
    private final Map<String, Component> components;
    private final LinkedBlockingQueue<SnaMessage> waitingEvents;
    private final ActionHookQueue actionHookQueue;
    private final AppExceptionWatchDog watchDog;

    /**
     * Constructor
     *
     * @param mediator              the mediator
     * @param identifier            the name of the application
     * @param serviceRegistrations  the OSGi service registrations for this application (i.e., the output of
     *                              each components)
     * @param resourceSubscriptions the subscriptions to the sNa resources
     * @param components            the components making up the application
     * @param watchDog              the exception watchdog that is associated with the threads
     */
    public Application(AppServiceMediator mediator, AppContainer container, String name, List<ServiceRegistration> serviceRegistrations, Map<ResourceDataProvider, Collection<ResourceSubscription>> resourceSubscriptions, Map<String, Component> components, AppExceptionWatchDog watchDog) {
        this(mediator, container, name, null, serviceRegistrations, resourceSubscriptions, components, watchDog);
    }

    /**
     * Constructor
     *
     * @param mediator              the mediator
     * @param identifier            the name of the application
     * @param serviceRegistrations  the OSGi service registrations for this application (i.e., the output of
     *                              each components)
     * @param resourceSubscriptions the subscriptions to the sNa resources
     * @param components            the components making up the application
     * @param watchDog              the exception watchdog that is associated with the threads
     */
    public Application(AppServiceMediator mediator, AppContainer container, String name, String identifier, List<ServiceRegistration> serviceRegistrations, Map<ResourceDataProvider, Collection<ResourceSubscription>> resourceSubscriptions, Map<String, Component> components, AppExceptionWatchDog watchDog) {
        super(mediator, name, identifier);
        this.serviceRegistrations = serviceRegistrations;
        this.resourceSubscriptions = resourceSubscriptions;
        //TODO: switch to an unmodifiable collection ?
        this.components = components;
        this.waitingEvents = new LinkedBlockingQueue<SnaMessage>();
        this.actionHookQueue = new ActionHookQueue(mediator);
        this.watchDog = watchDog;
    }

    /**
     * Start the application
     *
     * @return a success/error message
     */
    protected SnaErrorMessage doStart() {
        try {
            this.actionHookQueue.instantiate();

        } catch (LifeCycleException e) {
            return new AppSnaMessage(super.mediator, "/AppManager/" + getName(), SnaErrorMessage.Error.SYSTEM_ERROR, "Unable to start the application " + getName() + " > " + e.getMessage());
        }
        for (Map.Entry<String, Component> map : components.entrySet()) {
            try {
                map.getValue().instantiate(super.getSession());

            } catch (LifeCycleException e) {
                return new AppSnaMessage(this.mediator, "/AppManager/" + getName(), SnaErrorMessage.Error.SYSTEM_ERROR, "Unable to start the application " + getName() + " > " + e.getMessage());

            } catch (ApplicationRuntimeException e) {
                return new AppSnaMessage(this.mediator, "/AppManager/" + getName(), SnaErrorMessage.Error.SYSTEM_ERROR, "Unable to start the application " + getName() + " > " + e.getMessage());
            }
        }
        try {
            for (Map.Entry<ResourceDataProvider, Collection<ResourceSubscription>> map : resourceSubscriptions.entrySet()) {
                Collection<ResourceSubscription> resourceSubscriptions = map.getValue();
                for (ResourceSubscription resourceSubscription : resourceSubscriptions) {
                    String[] uriElements = UriUtils.getUriElements(resourceSubscription.getResourceUri());
                    if (uriElements.length != 3) {
                        continue;
                    }
                    Resource resource = super.getSession().resource(uriElements[0], uriElements[1], uriElements[2]);
                    if (resource == null) {
                        throw new ResourceNotFoundException("The resource " + resourceSubscription.getResourceUri() + " does not exist. Unable to subscribe to the resource.");
                    }
                    Set<Constraint> constraints = new HashSet<Constraint>();
                    if (resourceSubscription.getConditions() != null) {
                        for (AppCondition condition : resourceSubscription.getConditions()) {
                            constraints.add(condition.getConstraint());
                        }
                    }
                    String subscriptionId = resource.subscribe(DataResource.VALUE, this, constraints).getSubscriptionId();
                    resourceSubscription.setSubscriptionId(subscriptionId);
                }
            }
        } catch (ResourceNotFoundException e) {
            return new AppSnaMessage(this.mediator, "/AppManager/" + getName(), SnaErrorMessage.Error.SYSTEM_ERROR, "Unable to start the application " + getName() + " > " + e.getMessage());
        }
        return new AppSnaMessage(this.mediator, "/AppManager/" + getName(), SnaErrorMessage.Error.NO_ERROR, "Application " + getName() + " started");
    }

    /**
     * Stop the application
     *
     * @return a success/error message
     */
    protected SnaErrorMessage doStop() {

        for (Map.Entry<ResourceDataProvider, Collection<ResourceSubscription>> map : resourceSubscriptions.entrySet()) {
            Collection<ResourceSubscription> resourceSubscriptions = map.getValue();
            for (ResourceSubscription resourceSubscription : resourceSubscriptions) {
                Resource resource;
                try {
                    String[] uriElements = UriUtils.getUriElements(resourceSubscription.getResourceUri());

                    if (uriElements.length != 3) {
                        continue;
                    }
                    resource = super.getSession().resource(uriElements[0], uriElements[1], uriElements[2]);
                } catch (NullPointerException e) {
                    continue;
                }
                resource.unsubscribe(DataResource.VALUE, resourceSubscription.getSubscriptionId());
            }
        }
        for (Map.Entry<String, Component> map : components.entrySet()) {
            try {
                map.getValue().uninstantiate();

            } catch (LifeCycleException e) {
                return new AppSnaMessage(this.mediator, "/AppManager/" + getName(), SnaErrorMessage.Error.SYSTEM_ERROR, "Unable to stop the application " + getName() + " > " + e.getMessage());
            }
        }
        try {
            this.actionHookQueue.uninstantiate();

        } catch (LifeCycleException e) {
            return new AppSnaMessage(this.mediator, "/AppManager/" + getName(), SnaErrorMessage.Error.SYSTEM_ERROR, "Unable to start the application " + getName() + " > " + e.getMessage());
        }
        return new AppSnaMessage(this.mediator, "/AppManager/" + getName(), SnaErrorMessage.Error.NO_ERROR, "Application " + getName() + " stopped");
    }

    /**
     * Uninstall properly the application
     *
     * @return a success/error message
     */
    public SnaErrorMessage uninstall() {
        for (ServiceRegistration serviceRegistration : serviceRegistrations) {
            serviceRegistration.unregister();
        }
        return new AppSnaMessage(this.mediator, "/AppManager/" + getName(), SnaErrorMessage.Error.NO_ERROR, "Application " + getName() + " uninstalled");
    }

    /**
     * @see Recipient#callback(String, SnaMessage[])
     */
    public void callback(String callbackId, SnaMessage[] messages) throws Exception {
        waitingEvents.put(messages[0]);
        triggerNextEvent();
    }

    /**
     * This function create a {@link Thread} to wrap the processing of the next event
     */
    private void triggerNextEvent() throws Exception {
        Thread thread = new Thread() {
            public void run() {
                SnaMessage message = waitingEvents.poll();
                JSONObject messageJson = new JSONObject(message.getJSON());
                LOG4J.debug("Processing message {}", message.getJSON());
                String[] uri = message.getPath().split("/");
                String resourceUri = "/" + uri[1] + "/" + uri[2] + "/" + uri[3];
                Object value = messageJson.getJSONObject("notification").get("value");
                ResourceDataProvider dataProvider = null;
                for (Map.Entry<ResourceDataProvider, Collection<ResourceSubscription>> subscription : resourceSubscriptions.entrySet()) {
                    if (resourceUri.equals(subscription.getKey().getUri())) {
                        dataProvider = subscription.getKey();
                        break;
                    }
                }
                if (dataProvider == null) {
                    try {
                        throw new ResourceNotFoundException("Resource " + resourceUri + " not found while triggering a new event");

                    } catch (ResourceNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                List<String> sources = new ArrayList<String>();
                sources.add(resourceUri);

                // Notify the first component with the new event
                dataProvider.updateAndNotify(new UUID(System.currentTimeMillis(), System.currentTimeMillis()), value, sources);

                // Fire the PluginHook actions after the component graph browsing
                actionHookQueue.fireHooks();
                if (!waitingEvents.isEmpty()) {
                    try {
                        triggerNextEvent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setUncaughtExceptionHandler(watchDog);
        thread.start();
    }

    public Map<ResourceDataProvider, Collection<ResourceSubscription>> getResourceSubscriptions() {
        return resourceSubscriptions;
    }

    public Map<String, Component> getComponents() {
        return Collections.unmodifiableMap(components);
    }
}
