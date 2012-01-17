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

package org.apache.smscserver.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.smscserver.ConnectionConfig;
import org.apache.smscserver.ConnectionConfigFactory;
import org.apache.smscserver.command.CommandFactory;
import org.apache.smscserver.command.CommandFactoryFactory;
import org.apache.smscserver.listener.Listener;
import org.apache.smscserver.listener.ListenerFactory;
import org.apache.smscserver.smsclet.Authority;
import org.apache.smscserver.smsclet.SmscStatistics;
import org.apache.smscserver.smsclet.Smsclet;
import org.apache.smscserver.smsclet.UserManager;
import org.apache.smscserver.smscletcontainer.SmscletContainer;
import org.apache.smscserver.smscletcontainer.impl.DefaultSmscletContainer;
import org.apache.smscserver.usermanager.PropertiesUserManagerFactory;
import org.apache.smscserver.usermanager.impl.BaseUser;
import org.apache.smscserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.smscserver.usermanager.impl.TransferRatePermission;
import org.apache.smscserver.usermanager.impl.WritePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * SMSC server configuration implementation. It holds all the components used.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class DefaultSmscServerContext implements SmscServerContext {

    private final Logger LOG = LoggerFactory.getLogger(DefaultSmscServerContext.class);

    private UserManager userManager = new PropertiesUserManagerFactory().createUserManager();

    private SmscletContainer smscletContainer = new DefaultSmscletContainer();

    private SmscStatistics statistics = new DefaultSmscStatistics();

    private CommandFactory commandFactory = new CommandFactoryFactory().createCommandFactory();

    private ConnectionConfig connectionConfig = new ConnectionConfigFactory().createConnectionConfig();

    private Map<String, Listener> listeners = new HashMap<String, Listener>();

    private static final List<Authority> ADMIN_AUTHORITIES = new ArrayList<Authority>();
    private static final List<Authority> ANON_AUTHORITIES = new ArrayList<Authority>();

    /**
     * The thread pool executor to be used by the server using this context
     */
    private ThreadPoolExecutor threadPoolExecutor = null;

    static {
        DefaultSmscServerContext.ADMIN_AUTHORITIES.add(new WritePermission());

        DefaultSmscServerContext.ANON_AUTHORITIES.add(new ConcurrentLoginPermission(20, 2));
        DefaultSmscServerContext.ANON_AUTHORITIES.add(new TransferRatePermission(4800, 4800));
    }

    public DefaultSmscServerContext() {
        // create the default listener
        this.listeners.put("default", new ListenerFactory().createListener());
    }

    public void addListener(String name, Listener listener) {
        this.listeners.put(name, listener);
    }

    /**
     * Create default users.
     */
    public void createDefaultUsers() throws Exception {
        UserManager userManager = this.getUserManager();

        // create admin user
        String adminName = userManager.getAdminName();
        if (!userManager.doesExist(adminName)) {
            this.LOG.info("Creating user : " + adminName);
            BaseUser adminUser = new BaseUser();
            adminUser.setName(adminName);
            adminUser.setPassword(adminName);
            adminUser.setEnabled(true);

            adminUser.setAuthorities(DefaultSmscServerContext.ADMIN_AUTHORITIES);

            adminUser.setHomeDirectory("./res/home");
            adminUser.setMaxIdleTime(0);
            userManager.save(adminUser);
        }

        // create anonymous user
        if (!userManager.doesExist("anonymous")) {
            this.LOG.info("Creating user : anonymous");
            BaseUser anonUser = new BaseUser();
            anonUser.setName("anonymous");
            anonUser.setPassword("");

            anonUser.setAuthorities(DefaultSmscServerContext.ANON_AUTHORITIES);

            anonUser.setEnabled(true);

            anonUser.setHomeDirectory("./res/home");
            anonUser.setMaxIdleTime(300);
            userManager.save(anonUser);
        }
    }

    /**
     * Close all the components.
     */
    public void dispose() {
        this.listeners.clear();
        this.smscletContainer.getSmsclets().clear();
        if (this.threadPoolExecutor != null) {
            this.LOG.debug("Shutting down the thread pool executor");
            this.threadPoolExecutor.shutdown();
            try {
                this.threadPoolExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            } finally {
                // TODO: how to handle?
            }
        }
    }

    /**
     * Get the command factory.
     */
    public CommandFactory getCommandFactory() {
        return this.commandFactory;
    }

    public ConnectionConfig getConnectionConfig() {
        return this.connectionConfig;
    }

    public Listener getListener(String name) {
        return this.listeners.get(name);
    }

    public Map<String, Listener> getListeners() {
        return this.listeners;
    }

    /**
     * Get Smsclet.
     */
    public Smsclet getSmsclet(String name) {
        return this.smscletContainer.getSmsclet(name);
    }

    /**
     * Get smsclet handler.
     */
    public SmscletContainer getSmscletContainer() {
        return this.smscletContainer;
    }

    /**
     * Get ssc statistics.
     */
    public SmscStatistics getSmscStatistics() {
        return this.statistics;
    }

    public synchronized ThreadPoolExecutor getThreadPoolExecutor() {
        if (this.threadPoolExecutor == null) {
            int maxThreads = this.connectionConfig.getMaxThreads();
            if (maxThreads < 1) {
                int maxLogins = this.connectionConfig.getMaxBinds();
                if (maxLogins > 0) {
                    maxThreads = maxLogins;
                } else {
                    maxThreads = 16;
                }
            }
            this.LOG.debug("Intializing shared thread pool executor with max threads of {}", maxThreads);
            this.threadPoolExecutor = new OrderedThreadPoolExecutor(maxThreads);
        }
        return this.threadPoolExecutor;
    }

    /**
     * Get user manager.
     */
    public UserManager getUserManager() {
        return this.userManager;
    }

    public Listener removeListener(String name) {
        return this.listeners.remove(name);
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public void setListener(String name, Listener listener) {
        this.listeners.put(name, listener);
    }

    public void setListeners(Map<String, Listener> listeners) {
        this.listeners = listeners;
    }

    public void setSmscletContainer(SmscletContainer smscletContainer) {
        this.smscletContainer = smscletContainer;
    }

    public void setSmscStatistics(SmscStatistics statistics) {
        this.statistics = statistics;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
}