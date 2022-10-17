/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.notification.impl;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.PROVIDER_CREATED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.PROVIDER_DELETED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.RESOURCE_CREATED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.RESOURCE_DELETED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.SERVICE_CREATED;
import static org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status.SERVICE_DELETED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Instant;
import java.util.Map;

import org.eclipse.sensinact.prototype.notification.LifecycleNotification;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status;
import org.eclipse.sensinact.prototype.notification.ResourceActionNotification;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.eclipse.sensinact.prototype.notification.ResourceMetaDataNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;
import org.osgi.service.typedevent.TypedEventBus;

@ExtendWith(MockitoExtension.class)
class NotificationSenderTest {

    private static final String PROVIDER = "provider";
    private static final String PROVIDER_2 = "provider2";
    private static final String SERVICE = "service";
    private static final String SERVICE_2 = "service2";
    private static final String RESOURCE = "resource";
    private static final String RESOURCE_2 = "resource2";

    @Mock
    TypedEventBus bus;

    NotificationAccumulator accumulator;

    @BeforeEach
    void start() {
        accumulator = new NotificationAccumulatorImpl(bus);
    }

    @Nested
    class ProviderLifecycleTests {
        @Test
        void testAddProvider() {
            accumulator.addProvider(PROVIDER);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER),
                    argThat(isLifecycleNotificationWith(PROVIDER_CREATED, PROVIDER)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveProvider() {
            accumulator.removeProvider(PROVIDER);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER),
                    argThat(isLifecycleNotificationWith(PROVIDER_DELETED, PROVIDER)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddRemoveProvider() {
            accumulator.addProvider(PROVIDER);
            accumulator.removeProvider(PROVIDER);
            accumulator.completeAndSend();

            Mockito.verifyNoInteractions(bus);
        }

        @Test
        void testRemoveAddProvider() {
            accumulator.removeProvider(PROVIDER);
            accumulator.addProvider(PROVIDER);
            accumulator.completeAndSend();

            InOrder inOrder = Mockito.inOrder(bus);
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER),
                    argThat(isLifecycleNotificationWith(PROVIDER_DELETED, PROVIDER)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER),
                    argThat(isLifecycleNotificationWith(PROVIDER_CREATED, PROVIDER)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddAddProvider() {
            accumulator.addProvider(PROVIDER);
            accumulator.addProvider(PROVIDER);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER),
                    argThat(isLifecycleNotificationWith(PROVIDER_CREATED, PROVIDER)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveRemoveProvider() {
            accumulator.removeProvider(PROVIDER);
            accumulator.removeProvider(PROVIDER);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER),
                    argThat(isLifecycleNotificationWith(PROVIDER_DELETED, PROVIDER)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveAddRemoveProvider() {
            accumulator.removeProvider(PROVIDER);
            accumulator.addProvider(PROVIDER);
            accumulator.removeProvider(PROVIDER);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER),
                    argThat(isLifecycleNotificationWith(PROVIDER_DELETED, PROVIDER)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddProviders() {
            accumulator.addProvider(PROVIDER);
            accumulator.addProvider(PROVIDER_2);
            accumulator.completeAndSend();

            InOrder inOrder = Mockito.inOrder(bus);
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER),
                    argThat(isLifecycleNotificationWith(PROVIDER_CREATED, PROVIDER)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2),
                    argThat(isLifecycleNotificationWith(PROVIDER_CREATED, PROVIDER_2)));
            Mockito.verifyNoMoreInteractions(bus);
        }
    }

    @Nested
    class ServiceLifecycleTests {
        @Test
        void testAddService() {
            accumulator.addService(PROVIDER, SERVICE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER, SERVICE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveService() {
            accumulator.removeService(PROVIDER, SERVICE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_DELETED, PROVIDER, SERVICE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddRemoveService() {
            accumulator.addService(PROVIDER, SERVICE);
            accumulator.removeService(PROVIDER, SERVICE);
            accumulator.completeAndSend();

            Mockito.verifyNoInteractions(bus);
        }

        @Test
        void testRemoveAddService() {
            accumulator.removeService(PROVIDER, SERVICE);
            accumulator.addService(PROVIDER, SERVICE);
            accumulator.completeAndSend();

            InOrder inOrder = Mockito.inOrder(bus);
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_DELETED, PROVIDER, SERVICE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER, SERVICE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddAddService() {
            accumulator.addService(PROVIDER, SERVICE);
            accumulator.addService(PROVIDER, SERVICE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER, SERVICE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveRemoveService() {
            accumulator.removeService(PROVIDER, SERVICE);
            accumulator.removeService(PROVIDER, SERVICE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_DELETED, PROVIDER, SERVICE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveAddRemoveService() {
            accumulator.removeService(PROVIDER, SERVICE);
            accumulator.addService(PROVIDER, SERVICE);
            accumulator.removeService(PROVIDER, SERVICE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_DELETED, PROVIDER, SERVICE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddServices() {
            accumulator.addService(PROVIDER, SERVICE);
            accumulator.addService(PROVIDER, SERVICE_2);
            accumulator.addService(PROVIDER_2, SERVICE);
            accumulator.completeAndSend();

            InOrder inOrder = Mockito.inOrder(bus);
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER, SERVICE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE_2),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER, SERVICE_2)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2 + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER_2, SERVICE)));
            Mockito.verifyNoMoreInteractions(bus);
        }
    }

    @Nested
    class ResourceLifecycleTests {
        @Test
        void testAddResource() {
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE, RESOURCE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveResource() {
            accumulator.removeResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_DELETED, PROVIDER, SERVICE, RESOURCE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddRemoveResource() {
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.removeResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.completeAndSend();

            Mockito.verifyNoInteractions(bus);
        }

        @Test
        void testRemoveAddResource() {
            accumulator.removeResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.completeAndSend();

            InOrder inOrder = Mockito.inOrder(bus);
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_DELETED, PROVIDER, SERVICE, RESOURCE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE, RESOURCE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddAddResource() {
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE, RESOURCE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveRemoveResource() {
            accumulator.removeResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.removeResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_DELETED, PROVIDER, SERVICE, RESOURCE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveAddRemoveResource() {
            accumulator.removeResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.removeResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_DELETED, PROVIDER, SERVICE, RESOURCE)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddServices() {
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE_2);
            accumulator.addResource(PROVIDER, SERVICE_2, RESOURCE);
            accumulator.addResource(PROVIDER_2, SERVICE, RESOURCE);
            accumulator.completeAndSend();

            InOrder inOrder = Mockito.inOrder(bus);
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE, RESOURCE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE_2),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE, RESOURCE_2)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE_2 + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE_2, RESOURCE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2 + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER_2, SERVICE, RESOURCE)));
            Mockito.verifyNoMoreInteractions(bus);
        }
    }

    private static final String METADATA_KEY = "foo";
    private static final String METADATA_KEY_2 = "bar";
    private static final String METADATA_VALUE = "fizz";
    private static final Integer METADATA_VALUE_2 = 42;

    @Nested
    class MetadataTests {

        @Test
        void testAddNullMetadata() {
            Instant now = Instant.now();

            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, null, null, now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("METADATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isMetadataNotificationWith(PROVIDER, SERVICE, RESOURCE, emptyMap(), emptyMap(), now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddNewMetadata() {
            Instant now = Instant.now();

            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, null,
                    singletonMap(METADATA_KEY, METADATA_VALUE), now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("METADATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isMetadataNotificationWith(PROVIDER, SERVICE, RESOURCE, emptyMap(),
                            singletonMap(METADATA_KEY, METADATA_VALUE), now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testRemoveMetadata() {
            Instant now = Instant.now();

            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, singletonMap(METADATA_KEY, METADATA_VALUE),
                    null, now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("METADATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isMetadataNotificationWith(PROVIDER, SERVICE, RESOURCE,
                            singletonMap(METADATA_KEY, METADATA_VALUE), emptyMap(), now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddMetadataAcrossMultipleCalls() {
            Instant now = Instant.now();

            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, null,
                    singletonMap(METADATA_KEY, METADATA_VALUE), now);
            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, singletonMap(METADATA_KEY, METADATA_VALUE),
                    Map.of(METADATA_KEY, METADATA_VALUE, METADATA_KEY_2, METADATA_VALUE_2), now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("METADATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isMetadataNotificationWith(PROVIDER, SERVICE, RESOURCE, emptyMap(),
                            Map.of(METADATA_KEY, METADATA_VALUE, METADATA_KEY_2, METADATA_VALUE_2), now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddMetadataAcrossMultipleCallsWithTimeChange() {
            Instant now = Instant.now();

            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, null,
                    singletonMap(METADATA_KEY, METADATA_VALUE), now.minusSeconds(10));
            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, singletonMap(METADATA_KEY, METADATA_VALUE),
                    Map.of(METADATA_KEY, METADATA_VALUE, METADATA_KEY_2, METADATA_VALUE_2), now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("METADATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isMetadataNotificationWith(PROVIDER, SERVICE, RESOURCE, emptyMap(),
                            Map.of(METADATA_KEY, METADATA_VALUE, METADATA_KEY_2, METADATA_VALUE_2), now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddMetadataAcrossMultipleCallsReverseTime() {
            Instant now = Instant.now();

            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, null,
                    singletonMap(METADATA_KEY, METADATA_VALUE), now);

            Exception thrown = assertThrows(IllegalArgumentException.class, () -> {
                accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, singletonMap(METADATA_KEY, METADATA_VALUE),
                        Map.of(METADATA_KEY_2, METADATA_VALUE_2), now.minusSeconds(10));
            });

            assertTrue(thrown.getMessage().contains("out of temporal order"), "Wrong message: " + thrown.getMessage());
        }

        @Test
        void testAddRemoveMetadataAcrossMultipleCalls() {
            Instant now = Instant.now();

            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, null,
                    singletonMap(METADATA_KEY, METADATA_VALUE), now.minusSeconds(10));
            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, singletonMap(METADATA_KEY, METADATA_VALUE),
                    singletonMap(METADATA_KEY_2, METADATA_VALUE_2), now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("METADATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isMetadataNotificationWith(PROVIDER, SERVICE, RESOURCE, emptyMap(),
                            Map.of(METADATA_KEY_2, METADATA_VALUE_2), now)));
            Mockito.verifyNoMoreInteractions(bus);
        }
    }

    private static final Integer INTEGER_VALUE = 5;
    private static final Integer INTEGER_VALUE_2 = 14;

    @Nested
    class ResourceValueTests {

        @Test
        void testUpdateNull() {
            Instant now = Instant.now();

            accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE, null, null, now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("DATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isValueNotificationWith(PROVIDER, SERVICE, RESOURCE, null, null, now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testUpdateValue() {
            Instant now = Instant.now();

            accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE, null, INTEGER_VALUE, now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("DATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isValueNotificationWith(PROVIDER, SERVICE, RESOURCE, null, INTEGER_VALUE, now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testMultipleUpdate() {
            Instant now = Instant.now();

            accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE, null, INTEGER_VALUE, now);
            accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE, INTEGER_VALUE, INTEGER_VALUE_2, now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("DATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isValueNotificationWith(PROVIDER, SERVICE, RESOURCE, null, INTEGER_VALUE_2, now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddMetadataAcrossMultipleCallsWithTimeChange() {
            Instant now = Instant.now();

            accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE, null, INTEGER_VALUE, now.minusSeconds(10));
            accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE, INTEGER_VALUE, INTEGER_VALUE_2, now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("DATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isValueNotificationWith(PROVIDER, SERVICE, RESOURCE, null, INTEGER_VALUE_2, now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testAddMetadataAcrossMultipleCallsReverseTime() {
            Instant now = Instant.now();

            accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE, null, INTEGER_VALUE, now);
            Exception thrown = assertThrows(IllegalArgumentException.class, () -> {
                accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE, INTEGER_VALUE, INTEGER_VALUE_2,
                        now.minusSeconds(10));
            });

            assertTrue(thrown.getMessage().contains("out of temporal order"), "Wrong message: " + thrown.getMessage());
        }
    }

    @Nested
    class ResourceActionTests {

        @Test
        void testAction() {
            Instant now = Instant.now();

            accumulator.resourceAction(PROVIDER, SERVICE, RESOURCE, now);
            accumulator.completeAndSend();

            Mockito.verify(bus).deliver(eq("ACTION/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isActionNotificationWith(PROVIDER, SERVICE, RESOURCE, now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testMultiAction() {
            Instant now = Instant.now();

            accumulator.resourceAction(PROVIDER, SERVICE, RESOURCE, now);
            accumulator.resourceAction(PROVIDER, SERVICE, RESOURCE, now);
            accumulator.completeAndSend();

            Mockito.verify(bus, Mockito.times(2)).deliver(eq("ACTION/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isActionNotificationWith(PROVIDER, SERVICE, RESOURCE, now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testMultiActionDifferentTimes() {
            Instant now = Instant.now();

            accumulator.resourceAction(PROVIDER, SERVICE, RESOURCE, now.minusSeconds(10));
            accumulator.resourceAction(PROVIDER, SERVICE, RESOURCE, now);
            accumulator.completeAndSend();

            InOrder inOrder = Mockito.inOrder(bus);

            inOrder.verify(bus).deliver(eq("ACTION/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isActionNotificationWith(PROVIDER, SERVICE, RESOURCE, now.minusSeconds(10))));
            inOrder.verify(bus).deliver(eq("ACTION/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isActionNotificationWith(PROVIDER, SERVICE, RESOURCE, now)));
            Mockito.verifyNoMoreInteractions(bus);
        }

        @Test
        void testMultiActionDifferentTimesReverseOrder() {
            Instant now = Instant.now();

            accumulator.resourceAction(PROVIDER, SERVICE, RESOURCE, now);
            accumulator.resourceAction(PROVIDER, SERVICE, RESOURCE, now.minusSeconds(10));
            accumulator.completeAndSend();

            InOrder inOrder = Mockito.inOrder(bus);

            inOrder.verify(bus).deliver(eq("ACTION/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isActionNotificationWith(PROVIDER, SERVICE, RESOURCE, now.minusSeconds(10))));
            inOrder.verify(bus).deliver(eq("ACTION/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isActionNotificationWith(PROVIDER, SERVICE, RESOURCE, now)));
            Mockito.verifyNoMoreInteractions(bus);
        }
    }

    @Nested
    class NotificationOrderingTests {

        @Test
        void testNotificationOrdering() {
            Instant now = Instant.now();

            accumulator.resourceAction(PROVIDER, SERVICE, RESOURCE_2, now.minusSeconds(10));
            accumulator.resourceAction(PROVIDER, SERVICE, RESOURCE, now);
            accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE_2, null, INTEGER_VALUE, now);
            accumulator.resourceValueUpdate(PROVIDER, SERVICE, RESOURCE, null, INTEGER_VALUE_2, now);
            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE_2, null,
                    singletonMap(METADATA_KEY_2, METADATA_VALUE_2), now);
            accumulator.metadataValueUpdate(PROVIDER, SERVICE, RESOURCE, null,
                    singletonMap(METADATA_KEY, METADATA_VALUE), now);
            accumulator.addResource(PROVIDER_2, SERVICE_2, RESOURCE_2);
            accumulator.addResource(PROVIDER_2, SERVICE_2, RESOURCE);
            accumulator.addResource(PROVIDER_2, SERVICE, RESOURCE_2);
            accumulator.addResource(PROVIDER_2, SERVICE, RESOURCE);
            accumulator.addResource(PROVIDER, SERVICE_2, RESOURCE_2);
            accumulator.addResource(PROVIDER, SERVICE_2, RESOURCE);
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE_2);
            accumulator.addResource(PROVIDER, SERVICE, RESOURCE);
            accumulator.addService(PROVIDER_2, SERVICE_2);
            accumulator.addService(PROVIDER_2, SERVICE);
            accumulator.addService(PROVIDER, SERVICE_2);
            accumulator.addService(PROVIDER, SERVICE);
            accumulator.addProvider(PROVIDER_2);
            accumulator.addProvider(PROVIDER);
            accumulator.completeAndSend();

            InOrder inOrder = Mockito.inOrder(bus);

            // Depth first for provider
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER),
                    argThat(isLifecycleNotificationWith(PROVIDER_CREATED, PROVIDER)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER, SERVICE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE, RESOURCE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE_2),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE, RESOURCE_2)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE_2),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER, SERVICE_2)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE_2 + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE_2, RESOURCE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER + "/" + SERVICE_2 + "/" + RESOURCE_2),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER, SERVICE_2, RESOURCE_2)));

            // Depth first for provider 2
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2),
                    argThat(isLifecycleNotificationWith(PROVIDER_CREATED, PROVIDER_2)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2 + "/" + SERVICE),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER_2, SERVICE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2 + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER_2, SERVICE, RESOURCE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2 + "/" + SERVICE + "/" + RESOURCE_2),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER_2, SERVICE, RESOURCE_2)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2 + "/" + SERVICE_2),
                    argThat(isLifecycleNotificationWith(SERVICE_CREATED, PROVIDER_2, SERVICE_2)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2 + "/" + SERVICE_2 + "/" + RESOURCE),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER_2, SERVICE_2, RESOURCE)));
            inOrder.verify(bus).deliver(eq("LIFECYCLE/" + PROVIDER_2 + "/" + SERVICE_2 + "/" + RESOURCE_2),
                    argThat(isLifecycleNotificationWith(RESOURCE_CREATED, PROVIDER_2, SERVICE_2, RESOURCE_2)));

            // Metadata next
            inOrder.verify(bus).deliver(eq("METADATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isMetadataNotificationWith(PROVIDER, SERVICE, RESOURCE, emptyMap(),
                            singletonMap(METADATA_KEY, METADATA_VALUE), now)));
            inOrder.verify(bus).deliver(eq("METADATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE_2),
                    argThat(isMetadataNotificationWith(PROVIDER, SERVICE, RESOURCE_2, emptyMap(),
                            singletonMap(METADATA_KEY_2, METADATA_VALUE_2), now)));

            // Resource values next
            inOrder.verify(bus).deliver(eq("DATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isValueNotificationWith(PROVIDER, SERVICE, RESOURCE, null, INTEGER_VALUE_2, now)));
            inOrder.verify(bus).deliver(eq("DATA/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE_2),
                    argThat(isValueNotificationWith(PROVIDER, SERVICE, RESOURCE_2, null, INTEGER_VALUE, now)));

            // Finally the actions
            inOrder.verify(bus).deliver(eq("ACTION/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE),
                    argThat(isActionNotificationWith(PROVIDER, SERVICE, RESOURCE, now)));
            inOrder.verify(bus).deliver(eq("ACTION/" + PROVIDER + "/" + SERVICE + "/" + RESOURCE_2),
                    argThat(isActionNotificationWith(PROVIDER, SERVICE, RESOURCE_2, now.minusSeconds(10))));
            Mockito.verifyNoMoreInteractions(bus);
        }

    }

    ArgumentMatcher<LifecycleNotification> isLifecycleNotificationWith(Status status, String provider) {
        return isLifecycleNotificationWith(status, provider, null);
    }

    ArgumentMatcher<LifecycleNotification> isLifecycleNotificationWith(Status status, String provider, String service) {
        return isLifecycleNotificationWith(status, provider, service, null);
    }

    ArgumentMatcher<LifecycleNotification> isLifecycleNotificationWith(Status status, String provider, String service,
            String resource) {
        return i -> {
            try {
                assertEquals(status, i.status);
                assertEquals(provider, i.provider);
                assertEquals(service, i.service);
                assertEquals(resource, i.resource);
            } catch (AssertionFailedError e) {
                return false;
            }
            return true;
        };
    }

    ArgumentMatcher<ResourceMetaDataNotification> isMetadataNotificationWith(String provider, String service,
            String resource, Map<String, Object> oldValues, Map<String, Object> newValues, Instant timestamp) {
        return i -> {
            try {
                assertEquals(provider, i.provider);
                assertEquals(service, i.service);
                assertEquals(resource, i.resource);
                assertEquals(oldValues, i.oldValues);
                assertEquals(newValues, i.newValues);
                assertEquals(timestamp, i.timestamp);
            } catch (AssertionFailedError e) {
                return false;
            }
            return true;
        };
    }

    ArgumentMatcher<ResourceDataNotification> isValueNotificationWith(String provider, String service, String resource,
            Object oldValue, Object newValue, Instant timestamp) {
        return i -> {
            try {
                assertEquals(provider, i.provider);
                assertEquals(service, i.service);
                assertEquals(resource, i.resource);
                assertEquals(oldValue, i.oldValue);
                assertEquals(newValue, i.newValue);
                assertEquals(timestamp, i.timestamp);
            } catch (AssertionFailedError e) {
                return false;
            }
            return true;
        };
    }

    ArgumentMatcher<ResourceActionNotification> isActionNotificationWith(String provider, String service,
            String resource, Instant timestamp) {
        return i -> {
            try {
                assertEquals(provider, i.provider);
                assertEquals(service, i.service);
                assertEquals(resource, i.resource);
                assertEquals(timestamp, i.timestamp);
            } catch (AssertionFailedError e) {
                return false;
            }
            return true;
        };
    }
}
