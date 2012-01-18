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

package org.apache.smscserver.usermanager.impl;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.smscserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.smscserver.usermanager.PasswordEncryptor;

/**
 * 
 * @author hceylan
 * 
 */
public class ClearTextPasswordEncryptorTest extends TestCase {

    protected PasswordEncryptor createPasswordEncryptor() {
        return new ClearTextPasswordEncryptor();
    }

    public void testMatches() {
        PasswordEncryptor encryptor = this.createPasswordEncryptor();

        Assert.assertTrue(encryptor.matches("foo", "foo"));

        Assert.assertFalse(encryptor.matches("foo", "bar"));
    }

    public void testMatchesNullPasswordToCheck() {
        PasswordEncryptor encryptor = this.createPasswordEncryptor();

        try {
            encryptor.matches(null, "bar");
            Assert.fail("Must throw NullPointerException");
        } catch (NullPointerException e) {
            // OK
        }
    }

    public void testMatchesNullStoredPassword() {
        PasswordEncryptor encryptor = this.createPasswordEncryptor();

        try {
            encryptor.matches("foo", null);
            Assert.fail("Must throw NullPointerException");
        } catch (NullPointerException e) {
            // OK
        }
    }

}
