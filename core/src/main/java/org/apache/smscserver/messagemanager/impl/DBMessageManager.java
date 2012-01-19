/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.smscserver.messagemanager.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.smscserver.SmscServerConfigurationException;
import org.apache.smscserver.messagemanager.DBMessageManagerFactory;
import org.apache.smscserver.smsclet.MessageManager;
import org.apache.smscserver.smsclet.ShortMessage;
import org.apache.smscserver.smsclet.ShortMessageStatus;
import org.apache.smscserver.smsclet.SmscCannotReplaceException;
import org.apache.smscserver.smsclet.SmscException;
import org.apache.smscserver.util.DBUtils;
import org.apache.smscserver.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * Database implementation of {@link MessageManager}. It has been tested in MySQL and Oracle 8i database. The schema
 * file is </code>res/smsc-message-db.sql</code>
 * 
 * All the user attributes are replaced during run-time. So we can use your database schema. Then you need to modify the
 * SQLs in the configuration file.
 * 
 * @author hceylan
 */
public class DBMessageManager implements MessageManager {

    private static final Logger LOG = LoggerFactory.getLogger(DBMessageManager.class);

    private static final String ATTR_ID = "id";
    private static final String ATTR_DATA_CODING = "datacoding";
    private static final String ATTR_DEFAULT_MESSAGE_ID = "defaultmessage";
    private static final String ATTR_DESTINATION_ADDRESS = "destaddr";
    private static final String ATTR_DESTINATION_ADDRESS_NPI = DBMessageManager.ATTR_DESTINATION_ADDRESS + "npi";
    private static final String ATTR_DESTINATION_ADDRESS_TON = DBMessageManager.ATTR_DESTINATION_ADDRESS + "ton";
    private static final String ATTR_SOURCE_ADDRESS = "sourceaddr";
    private static final String ATTR_SOURCE_ADDRESS_NPI = DBMessageManager.ATTR_SOURCE_ADDRESS + "npi";
    private static final String ATTR_SOURCE_ADDRESS_TON = DBMessageManager.ATTR_SOURCE_ADDRESS + "ton";
    private static final String ATTR_ESM_CLASS = "esmclass";
    private static final String ATTR_MESSAGE_LENGTH = "messageLength";
    private static final String ATTR_NEXT_TRY_DELIVERY_TIME = "nexttrydelivertime";
    private static final String ATTR_PRIORITY_FLAG = "priorityflag";
    private static final String ATTR_PROTOCOL_ID = "protocolid";
    private static final String ATTR_RECEIVED = "received";
    private static final String ATTR_REPLACED_BY = "replacedby";
    private static final String ATTR_REPLACED = "replaced";
    private static final String ATTR_SCHEDULE_DATE = "scheduledate";
    private static final String ATTR_SERVICE_TYPE = "servicetype";
    private static final String ATTR_SHORT_MESSAGE = "shortmessage";
    private static final String ATTR_STATUS = "status";
    private static final String ATTR_VALIDITY_PERIOD = "validityperiod";

    private final DataSource datasource;

    private final String sqlCreateTable;
    private final String sqlInsertMessage;
    private final String sqlSelectMessage;
    private final String sqlUpdateMessage;
    private final String sqlSelectLatestReplacableMessage;

    /**
     * Internal constructor, do not use directly. Use {@link DBMessageManagerFactory} instead.
     */
    public DBMessageManager(DataSource datasource, String sqlCreateTable, String sqlInsertMessage,
            String sqlSelectMessage, String sqlUpdateMessage, String sqlSelectLatestReplacableMessage) {
        super();

        this.datasource = datasource;
        this.sqlCreateTable = sqlCreateTable;
        this.sqlInsertMessage = sqlInsertMessage;
        this.sqlSelectMessage = sqlSelectMessage;
        this.sqlUpdateMessage = sqlUpdateMessage;
        this.sqlSelectLatestReplacableMessage = sqlSelectLatestReplacableMessage;

        Connection con = null;
        Statement stmt = null;

        try {
            // test the connection
            con = this.createConnection();
            DBMessageManager.LOG.info("Database connection for message manager successfully opened.");

            // create table if not exists
            stmt = con.createStatement();
            stmt.execute(this.sqlCreateTable);
        } catch (Exception e) {
            String msg = "Failed to open connection to user database";
            DBMessageManager.LOG.error(msg, e);
            throw new SmscServerConfigurationException(msg, e);
        } finally {
            DBUtils.closeQuitelyWithConnection(stmt);
        }
    }

    private Connection createConnection() throws SQLException {
        return DBUtils.createConnection(this.datasource);
    }

    private Map<String, Object> populateFrom(ShortMessageImpl shortMessage) throws SmscException {
        String id = shortMessage.getId();
        if (id != null) {
            this.selectShortMessage(id);
        }

        if (id == null) {
            id = UUID.randomUUID().toString();
            Date now = Calendar.getInstance().getTime();
            shortMessage.setReceived(now);
            shortMessage.setNextTryDeliverTime(shortMessage.getScheduleDeliveryTime() != null ? shortMessage
                    .getScheduleDeliveryTime() : now);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DBMessageManager.ATTR_DATA_CODING, shortMessage.getDataCoding());
        map.put(DBMessageManager.ATTR_DEFAULT_MESSAGE_ID, shortMessage.getDefaultMessageId());
        map.put(DBMessageManager.ATTR_DESTINATION_ADDRESS, DBUtils.escapeString(shortMessage.getDestinationAddress()));
        map.put(DBMessageManager.ATTR_DESTINATION_ADDRESS_NPI, shortMessage.getDestinationAddressNPI());
        map.put(DBMessageManager.ATTR_DESTINATION_ADDRESS_TON, shortMessage.getDestinationAddressTON());
        map.put(DBMessageManager.ATTR_ESM_CLASS, shortMessage.getEsmClass());
        map.put(DBMessageManager.ATTR_ID, DBUtils.escapeString(id));
        map.put(DBMessageManager.ATTR_MESSAGE_LENGTH, shortMessage.getMessageLength());
        map.put(DBMessageManager.ATTR_NEXT_TRY_DELIVERY_TIME, DBUtils.asString(shortMessage.getNextTryDeliverTime()));
        map.put(DBMessageManager.ATTR_PRIORITY_FLAG, shortMessage.getPriorityFlag());
        map.put(DBMessageManager.ATTR_PROTOCOL_ID, shortMessage.getProtocolId());
        map.put(DBMessageManager.ATTR_RECEIVED, DBUtils.asString(shortMessage.getReceived()));
        map.put(DBMessageManager.ATTR_REPLACED, DBUtils.escapeString(shortMessage.getReplaced()));
        map.put(DBMessageManager.ATTR_REPLACED_BY, DBUtils.escapeString(shortMessage.getReplacedBy()));
        map.put(DBMessageManager.ATTR_SCHEDULE_DATE, DBUtils.asString(shortMessage.getScheduleDeliveryTime()));
        map.put(DBMessageManager.ATTR_SERVICE_TYPE, shortMessage.getServiceType());
        map.put(DBMessageManager.ATTR_SHORT_MESSAGE, DBUtils.escapeString(shortMessage.getShortMessage()));
        map.put(DBMessageManager.ATTR_SOURCE_ADDRESS, DBUtils.escapeString(shortMessage.getSourceAddress()));
        map.put(DBMessageManager.ATTR_SOURCE_ADDRESS_NPI, shortMessage.getSourceAddressNPI());
        map.put(DBMessageManager.ATTR_SOURCE_ADDRESS_TON, shortMessage.getSourceAddressTON());
        map.put(DBMessageManager.ATTR_STATUS, shortMessage.getStatus().toString());
        map.put(DBMessageManager.ATTR_VALIDITY_PERIOD, DBUtils.asString(shortMessage.getValidityPeriod()));

        return map;
    }

    private ShortMessage propulateFrom(ResultSet rs) throws SQLException {
        ShortMessageImpl shortMessage = new ShortMessageImpl();

        shortMessage.setDatacoding(rs.getInt(DBMessageManager.ATTR_DATA_CODING));
        shortMessage.setDefaultMessageId(rs.getInt(DBMessageManager.ATTR_DEFAULT_MESSAGE_ID));
        shortMessage.setDestinationAddress(rs.getString(DBMessageManager.ATTR_DESTINATION_ADDRESS));
        shortMessage.setDestinationAddressNPI(rs.getInt(DBMessageManager.ATTR_DESTINATION_ADDRESS_NPI));
        shortMessage.setDestinationAddressTON(rs.getInt(DBMessageManager.ATTR_DESTINATION_ADDRESS_TON));
        shortMessage.setEsmClass(rs.getInt(DBMessageManager.ATTR_ESM_CLASS));
        shortMessage.setId(rs.getString(DBMessageManager.ATTR_ID));
        shortMessage.setMessageLength(rs.getInt(DBMessageManager.ATTR_MESSAGE_LENGTH));
        shortMessage.setNextTryDeliverTime(rs.getTime(DBMessageManager.ATTR_NEXT_TRY_DELIVERY_TIME));
        shortMessage.setPriorityFlag(rs.getInt(DBMessageManager.ATTR_PRIORITY_FLAG));
        shortMessage.setProtocolId(rs.getInt(DBMessageManager.ATTR_PROTOCOL_ID));
        shortMessage.setReceived(rs.getTime(DBMessageManager.ATTR_RECEIVED));
        shortMessage.setReplaced(rs.getString(DBMessageManager.ATTR_REPLACED));
        shortMessage.setReplacedBy(rs.getString(DBMessageManager.ATTR_REPLACED_BY));
        shortMessage.setScheduleDeliveryTime(rs.getDate(DBMessageManager.ATTR_SCHEDULE_DATE));
        shortMessage.setServiceType(rs.getString(DBMessageManager.ATTR_SERVICE_TYPE));
        shortMessage.setShortMessage(rs.getString(DBMessageManager.ATTR_SHORT_MESSAGE));
        shortMessage.setSourceAddress(rs.getString(DBMessageManager.ATTR_SOURCE_ADDRESS));
        shortMessage.setSourceAddressNPI(rs.getInt(DBMessageManager.ATTR_SOURCE_ADDRESS_NPI));
        shortMessage.setSourceAddressTON(rs.getInt(DBMessageManager.ATTR_SOURCE_ADDRESS_TON));
        shortMessage.setStatus(ShortMessageStatus.valueOf(rs.getString(DBMessageManager.ATTR_STATUS)));
        shortMessage.setValidityPeriod(rs.getTime(DBMessageManager.ATTR_VALIDITY_PERIOD));

        return shortMessage;
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void replace(ShortMessage _shortMessage, boolean replace) throws SmscException {
        ShortMessageImpl shortMessage = (ShortMessageImpl) _shortMessage;

        // Only applicable for new short messages
        if (shortMessage.getId() != null) {
            throw new IllegalArgumentException("Id of the shortMessage must be null");
        }

        // Can we actually replace an existing short messages
        ShortMessage oldMessage = this.selectLatestShortMessage(shortMessage.getSourceAddress(),
                shortMessage.getDestinationAddress(), shortMessage.getServiceType());
        if ((oldMessage == null) && replace) {
            throw new SmscCannotReplaceException();
        }

        ShortMessageStatus status = oldMessage != null ? oldMessage.getStatus() : null;
        if (replace && (status != ShortMessageStatus.PENDING)) {
            throw new SmscCannotReplaceException();
        }

        // is the message still pending
        if (status == ShortMessageStatus.PENDING) {
            DBMessageManager.LOG.debug("Replcaement possible");
            Connection connection = null;
            Statement stmt = null;
            String sql = null;
            try {
                connection = this.createConnection();

                // begin transaction
                connection.setAutoCommit(false);

                this.storeShortMessageImpl(shortMessage, stmt);
                DBUtils.closeQuitely(stmt);

                try {
                    ((ShortMessageImpl) oldMessage).setReplacedBy(shortMessage.getId());
                    this.storeShortMessageImpl(shortMessage, stmt);
                } catch (Exception e) {
                    connection.rollback();

                    throw e;
                }

                connection.commit();

            } catch (Exception e) {
                DBUtils.handleException(sql, e);
            }
        } else {
            DBMessageManager.LOG.debug("Falling back to store...");

            shortMessage.setReplaceIfPresent(false);
            this.storeShortMessage(shortMessage);
        }
    }

    private ShortMessage selectLatestShortMessage(String sourceAddress, String destinationAddress, String serviceType)
            throws SmscException {
        Statement stmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            // prepare statement
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DBMessageManager.ATTR_SOURCE_ADDRESS, DBUtils.escapeString(sourceAddress));
            map.put(DBMessageManager.ATTR_DESTINATION_ADDRESS, DBUtils.escapeString(destinationAddress));
            map.put(DBMessageManager.ATTR_SERVICE_TYPE, serviceType);
            sql = StringUtils.replaceString(this.sqlSelectLatestReplacableMessage, map);
            DBMessageManager.LOG.debug(sql);

            // execute query
            stmt = this.createConnection().createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return this.propulateFrom(rs);
            }

            return null;
        } catch (Exception e) {
            throw DBUtils.handleException(sql, e);
        } finally {
            DBUtils.closeQuitelyWithConnection(rs, stmt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public ShortMessage selectShortMessage(String id) throws SmscException {
        Statement stmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            // prepare statement
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DBMessageManager.ATTR_ID, DBUtils.escapeString(id));
            sql = StringUtils.replaceString(this.sqlSelectMessage, map);
            DBMessageManager.LOG.debug(sql);

            // execute query
            stmt = this.createConnection().createStatement();
            rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return this.propulateFrom(rs);
            }

            return null;
        } catch (Exception e) {
            throw DBUtils.handleException(sql, e);
        } finally {
            DBUtils.closeQuitelyWithConnection(rs, stmt);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void storeShortMessage(ShortMessage _shortMessage) throws SmscException {
        ShortMessageImpl shortMessage = (ShortMessageImpl) _shortMessage;

        if (shortMessage.isReplaceIfPresent()) {
            DBMessageManager.LOG.debug("Falling back to replace...");
            this.replace(_shortMessage, false);
        } else {

            Statement stmt = null;
            String sql = null;

            try {
                stmt = this.createConnection().createStatement();

                sql = this.storeShortMessageImpl(shortMessage, stmt);

                shortMessage.getId();
            } catch (Exception e) {
                throw DBUtils.handleException(sql, e);
            } finally {
                DBUtils.closeQuitelyWithConnection(stmt);
            }
        }
    }

    private String storeShortMessageImpl(ShortMessageImpl shortMessage, Statement stmt) throws SmscException,
            SQLException {
        String sql;

        sql = shortMessage.getId() != null ? this.sqlUpdateMessage : this.sqlInsertMessage;
        sql = StringUtils.replaceString(sql, this.populateFrom(shortMessage));
        DBMessageManager.LOG.debug(sql);

        // execute query
        stmt.executeUpdate(sql);

        return sql;
    }
}