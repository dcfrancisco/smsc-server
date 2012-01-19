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

package org.apache.smscserver.test.usermanager;

import org.apache.smscserver.usermanager.UsernamePasswordAuthentication;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * 
 * @author hceylan
 * 
 */
public class UsernamePasswordAuthenticationTest extends TestCase {

    public void testConstructor() {
        UsernamePasswordAuthentication auth = new UsernamePasswordAuthentication(null, "user", "pass");

        Assert.assertEquals("user", auth.getUsername());
        Assert.assertEquals("pass", auth.getPassword());
    }

    public void testConstructorNullPassword() {
        UsernamePasswordAuthentication auth = new UsernamePasswordAuthentication(null, "user", null);

        Assert.assertEquals("user", auth.getUsername());
        Assert.assertNull(auth.getPassword());
    }

    public void testConstructorNullUsername() {
        UsernamePasswordAuthentication auth = new UsernamePasswordAuthentication(null, null, "pass");

        Assert.assertNull(auth.getUsername());
        Assert.assertEquals("pass", auth.getPassword());
    }

}