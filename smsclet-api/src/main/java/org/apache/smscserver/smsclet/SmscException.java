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

package org.apache.smscserver.smsclet;

/**
 * Smsclet exception class.
 * 
 * @author hceylan
 */
public class SmscException extends Exception {

    private static final long serialVersionUID = -1328383839915898987L;

    /**
     * Default constructor.
     */
    public SmscException() {
        super();
    }

    /**
     * Constructs a <code>SmscException</code> object with a message.
     * 
     * @param msg
     *            a description of the exception
     */
    public SmscException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <code>BaseException</code> object with a <code>Throwable</code> cause.
     * 
     * @param msg
     *            A description of the exception
     * 
     * @param th
     *            The original cause
     */
    public SmscException(String msg, Throwable th) {
        super(msg);
    }

    /**
     * Constructs a <code>SmscException</code> object with a <code>Throwable</code> cause.
     * 
     * @param th
     *            the original cause
     */
    public SmscException(Throwable th) {
        super(th.getMessage());
    }

    /**
     * Get the root cause.
     * 
     * @return The root cause
     * @deprecated Use {@link Exception#getCause()} instead
     */
    @Deprecated
    public Throwable getRootCause() {
        return this.getCause();
    }
}
