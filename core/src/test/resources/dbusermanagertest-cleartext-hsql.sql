-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
-- 
--  http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.

CREATE TABLE SMSC_USER (      
   userid VARCHAR(64) NOT NULL PRIMARY KEY,       
   userpassword VARCHAR(64),      
   enableflag BOOLEAN DEFAULT TRUE,    
   idletime INT DEFAULT 0,             
   maxloginnumber INT DEFAULT 0,
   maxloginperip INT DEFAULT 0
);

-- password="pw1"
INSERT INTO SMSC_USER (userid, userpassword) VALUES ('user1', 'pw1');

-- password="pw2"
INSERT INTO SMSC_USER VALUES ('user2', 'pw2', false, 2, 3, 4);

-- password=""
INSERT INTO SMSC_USER (userid, userpassword) VALUES ('user3', '');

-- password="admin"
INSERT INTO SMSC_USER (userid, userpassword) VALUES ('admin', 'admin');

