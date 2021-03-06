/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.smscserver;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.smscserver.command.CommandFactory;
import org.apache.smscserver.impl.DefaultSmscServer;
import org.apache.smscserver.impl.DefaultSmscServerContext;
import org.apache.smscserver.listener.Listener;
import org.apache.smscserver.smsclet.MessageManager;
import org.apache.smscserver.smsclet.Smsclet;
import org.apache.smscserver.smsclet.UserManager;
import org.apache.smscserver.smscletcontainer.impl.DefaultSmscletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the starting point of all the servers. Creates server instances based on the provided configuration.
 * 
 * @author hceylan
 */
public class SmscServerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SmscServerFactory.class);

    private final DefaultSmscServerContext serverContext;

    /**
     * Create a server with the default configuration.
     */
    public SmscServerFactory() {
        this.serverContext = new DefaultSmscServerContext();
    }

    /**
     * Add a {@link Listener} to this factory
     * 
     * @param name
     *            The name of the listener
     * @param listener
     *            The {@link Listener}
     */
    public void addListener(final String name, final Listener listener) {
        this.serverContext.addListener(name, listener);
    }

    /**
     * Create a {@link DefaultSmscServer} instance based on the provided configuration.
     * 
     * @return The {@link DefaultSmscServer} instance
     */
    public SmscServer createServer() {
        SmscServerFactory.LOG.info("SMSC Server is starting with the context {}",
                DefaultSmscServerContext.class.getCanonicalName());

        this.serverContext.info();

        return new DefaultSmscServer(this.serverContext);
    }

    /**
     * @return the serverContext
     */
    public SmscServerContext getServerContext() {
        return this.serverContext;
    }

    /**
     * Set the command factory to be used by servers created by this factory.
     * 
     * @param commandFactory
     *            The {@link CommandFactory}
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setCommandFactory(final CommandFactory commandFactory) {
        this.serverContext.setCommandFactory(commandFactory);
    }

    /**
     * Set the configuration to be used for the connections.
     * 
     * @param connectionConfig
     *            The {@link ConnectionConfig} the configuration to be used for the connections
     */
    public void setConnectionConfig(final ConnectionConfig connectionConfig) {
        this.serverContext.setConnectionConfig(connectionConfig);
    }

    /**
     * Set the configuration to be used for the connections.
     * 
     * @param deliveryManagerConfig
     *            The {@link ConnectionConfig} to be used by servers created by this factory
     */
    public void setDeliveryManagerConfig(final DeliveryManagerConfig deliveryManagerConfig) {
        this.serverContext.setDeliveryManagerConfig(deliveryManagerConfig);
    }

    /**
     * Set the listeners for servers created by this factory, replaces existing listeners
     * 
     * @param listeners
     *            The listeners to use for this server with the name as the key and the listener as the value
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setListeners(final Map<String, Listener> listeners) {
        this.serverContext.setListeners(listeners);
    }

    /**
     * Sets the message manager to be used by servers created by this factory
     * 
     * @param messageManager
     *            the {@link MessageManager}
     */
    public void setMessageManager(MessageManager messageManager) {
        this.serverContext.setMessageManager(messageManager);
    }

    /**
     * Sets the session lock timeout in milliseconds
     * 
     * @param sessionLockTimeout
     *            the sessionLockTimeout to set
     */
    public void setSessionLockTimeout(long sessionLockTimeout) {
        this.serverContext.setSessionLockTimeout(sessionLockTimeout);
    }

    /**
     * Set the {@link Smsclet}s to be active by servers created by this factory. Replaces existing {@link Smsclet}s
     * 
     * @param smsclets
     *            Smsclets as a map with the name as the key and the Smsclet as the value. The Smsclet container will
     *            iterate over the map in the order provided by the Map. If invocation order of Smsclets is of
     *            importance, make sure to provide a ordered Map, for example {@link LinkedHashMap}.
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setSmsclets(final Map<String, Smsclet> smsclets) {
        this.serverContext.setSmscletContainer(new DefaultSmscletContainer(smsclets));
    }

    /**
     * Set the user manager to be used by servers created by this factory
     * 
     * @param userManager
     *            The {@link UserManager}
     * @throws IllegalStateException
     *             If a custom server context has been set
     */
    public void setUserManager(final UserManager userManager) {
        this.serverContext.setUserManager(userManager);

        userManager.setContext(this.serverContext);
    }

}
