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

package org.apache.smscserver.test.spring;

import java.io.File;

import junit.framework.Assert;

import org.apache.smscserver.impl.DefaultSmscServer;
import org.apache.smscserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.smscserver.usermanager.Md5PasswordEncryptor;
import org.apache.smscserver.usermanager.SaltedPasswordEncryptor;
import org.apache.smscserver.usermanager.impl.PropertiesUserManager;

/**
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 * 
 */
public class FileUserManagerConfigTest extends SpringConfigTestTemplate {

    private static final String USER_FILE_PATH = "src/test/resources/conf/users.properties";

    private PropertiesUserManager createPropertiesUserManager(String config) {
        DefaultSmscServer server = (DefaultSmscServer) this.createServer(config);

        return (PropertiesUserManager) server.getUserManager();
    }

    public void testFalsePasswordEncryptor() throws Throwable {
        PropertiesUserManager um = this.createPropertiesUserManager("<file-user-manager file=\""
                + FileUserManagerConfigTest.USER_FILE_PATH + "\" encrypt-passwords=\"false\" />");

        Assert.assertTrue(um.getPasswordEncryptor() instanceof ClearTextPasswordEncryptor);
    }

    public void testFile() throws Throwable {
        PropertiesUserManager um = this.createPropertiesUserManager("<file-user-manager file=\""
                + FileUserManagerConfigTest.USER_FILE_PATH + "\" />");
        Assert.assertEquals(new File("src/test/resources/conf/users.properties"), um.getFile());
    }

    public void testMd5PasswordEncryptor() throws Throwable {
        PropertiesUserManager um = this.createPropertiesUserManager("<file-user-manager file=\""
                + FileUserManagerConfigTest.USER_FILE_PATH + "\" encrypt-passwords=\"md5\" />");

        Assert.assertTrue(um.getPasswordEncryptor() instanceof Md5PasswordEncryptor);
    }

    public void testNonePasswordEncryptor() throws Throwable {
        PropertiesUserManager um = this.createPropertiesUserManager("<file-user-manager file=\""
                + FileUserManagerConfigTest.USER_FILE_PATH + "\" encrypt-passwords=\"clear\" />");

        Assert.assertTrue(um.getPasswordEncryptor() instanceof ClearTextPasswordEncryptor);
    }

    public void testSaltedPasswordEncryptor() throws Throwable {
        PropertiesUserManager um = this.createPropertiesUserManager("<file-user-manager file=\""
                + FileUserManagerConfigTest.USER_FILE_PATH + "\" encrypt-passwords=\"salted\" />");

        Assert.assertTrue(um.getPasswordEncryptor() instanceof SaltedPasswordEncryptor);
    }

    public void testTruePasswordEncryptor() throws Throwable {
        PropertiesUserManager um = this.createPropertiesUserManager("<file-user-manager file=\""
                + FileUserManagerConfigTest.USER_FILE_PATH + "\" encrypt-passwords=\"true\" />");

        Assert.assertTrue(um.getPasswordEncryptor() instanceof Md5PasswordEncryptor);
    }

}
