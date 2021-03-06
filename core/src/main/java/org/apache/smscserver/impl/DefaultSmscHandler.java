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

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.write.WriteToClosedSessionException;
import org.apache.smscserver.ServerSmscStatistics;
import org.apache.smscserver.SmscHandler;
import org.apache.smscserver.SmscServerContext;
import org.apache.smscserver.command.Command;
import org.apache.smscserver.command.CommandFactory;
import org.apache.smscserver.listener.Listener;
import org.apache.smscserver.packet.impl.SmscStatusReplyImpl;
import org.apache.smscserver.smsclet.SmscIoSession;
import org.apache.smscserver.smsclet.SmscPacket;
import org.apache.smscserver.smsclet.SmscReply;
import org.apache.smscserver.smsclet.SmscRequest;
import org.apache.smscserver.smscletcontainer.SmscletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * @author hceylan
 * 
 */
public class DefaultSmscHandler implements SmscHandler {

    private final Logger LOG = LoggerFactory.getLogger(DefaultSmscHandler.class);

    private SmscServerContext serverContext;

    private Listener listener;

    /**
     * {@inheritDoc}
     * 
     */
    public void exceptionCaught(final DefaultSmscIoSession session, final Throwable cause) throws Exception {
        if (cause instanceof WriteToClosedSessionException) {
            WriteToClosedSessionException writeToClosedSessionException = (WriteToClosedSessionException) cause;
            this.LOG.warn("Client closed connection before all replies could be sent, last reply was {}",
                    writeToClosedSessionException.getRequest());
            session.close(false).awaitUninterruptibly(10000);
        } else {
            this.LOG.error("Exception caught, closing session", cause);
            session.close(false).awaitUninterruptibly(10000);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void init(final SmscServerContext context, final Listener listener) {
        this.serverContext = context;
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public SmscReply messageReceived(final DefaultSmscIoSession session, final SmscRequest request) throws Exception {
        session.updateLastAccessTime();

        if (!session.lock()) {
            this.LOG.warn("Cannot acquire session lock");
            return new SmscStatusReplyImpl(request, SmscReply.ErrorCode.ESME_RUNKNOWNERR);
        }

        try {
            SmscReply reply = this.serverContext.getSmscletContainer().onRequest(session.getSmscletSession(), request);
            if (reply != null) {
                return reply;
            }

            int commandID = request.getCommandId();
            CommandFactory commandFactory = this.serverContext.getCommandFactory();
            Command command = commandFactory.getCommand(commandID);

            if (command != null) {
                return command.execute(session, this.serverContext, request);
            }

            return null;
        } finally {
            session.unlock();
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void messageSent(final SmscIoSession session, final SmscPacket reply) throws Exception {
        // do nothing
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sessionClosed(final DefaultSmscIoSession session) throws Exception {
        this.LOG.debug("Closing session");

        try {
            this.serverContext.getSmscletContainer().onDisconnect(session.getSmscletSession());
        } catch (Exception e) {
            // swallow the exception, we're closing down the session anyways
            this.LOG.warn("Smsclet threw an exception on disconnect", e);
        }

        ServerSmscStatistics stats = ((ServerSmscStatistics) this.serverContext.getSmscStatistics());

        if (stats != null) {
            stats.setUnbind(session);
            stats.setCloseConnection(session);
            this.LOG.debug("Statistics bind and connection count decreased due to session close");
        } else {
            this.LOG.warn("Statistics not available in session, can not decrease bind and connection count");
        }
        this.LOG.debug("Session closed");
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sessionCreated(final DefaultSmscIoSession session) throws Exception {
        session.setListener(this.listener);

        ServerSmscStatistics stats = ((ServerSmscStatistics) this.serverContext.getSmscStatistics());

        if (stats != null) {
            stats.setOpenConnection(session);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sessionIdle(final DefaultSmscIoSession session, final IdleStatus status) throws Exception {
        this.LOG.info("Session idle, closing");

        session.close(false).awaitUninterruptibly(10000);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void sessionOpened(final DefaultSmscIoSession session) throws Exception {
        SmscletContainer smsclets = this.serverContext.getSmscletContainer();

        try {
            if (!smsclets.onConnect(session.getSmscletSession())) {
                this.LOG.debug("Smsclet returned DISCONNECT, session will be closed");
                session.close(false).awaitUninterruptibly(10000);

            } else {
                session.updateLastAccessTime();
            }
        } catch (Exception e) {
            this.LOG.debug("Smsclet threw exception", e);
        }
    }

}
