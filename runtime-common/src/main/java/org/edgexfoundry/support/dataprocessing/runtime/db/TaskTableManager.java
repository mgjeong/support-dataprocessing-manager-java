/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.edgexfoundry.support.dataprocessing.runtime.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

//    [Reference Query]
//    INSERT INTO TABLE_NAME [(column1, column2, column3,...columnN)] VALUES (value1, value2, value3,...valueN);
//    SELECT column1, column2, columnN FROM table_name WHERE [condition];;
//    UPDATE table_name SET column1 = value1, column2 = value2...., columnN = valueN WHERE [condition];
//    DELETE FROM table_name WHERE [condition];

public final class TaskTableManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTableManager.class);

    private static TaskTableManager instance = null;
    private static SqliteConnector dbCon = null;
    private static final String TASKTABLENAME = "tasks";

    private TaskTableManager() {
        dbCon = SqliteConnector.getInstance();
    }

    public void close() {
        dbCon.close();
        dbCon = null;
    }

    public static synchronized TaskTableManager getInstance() throws Exception {

        // Get the TaskTableManager instance.
        if (instance == null) {
            // If doesn't exists any instance of TaskTableManager
            // then make a new instance.
            instance = new TaskTableManager();
            instance.createTaskTable();
        }
        return instance;
    }

    //create "Task" table
    private void createTaskTable() throws Exception {

        // Build Table Description to create table.
        SqliteConnector.TableDesc td = new SqliteConnector.TableDesc(TASKTABLENAME);
        td.addColum(Entry.type.name(), new SqliteConnector.ColumnDesc("TEXT", "NOT NULL"));
        td.addColum(Entry.name.name(), new SqliteConnector.ColumnDesc("TEXT", "NOT NULL"));
        td.addColum(Entry.param.name(), new SqliteConnector.ColumnDesc("TEXT", "NOT NULL"));
        td.addColum(Entry.path.name(), new SqliteConnector.ColumnDesc("TEXT", "NOT NULL"));
        td.addColum(Entry.classname.name(), new SqliteConnector.ColumnDesc("TEXT", "NOT NULL"));
        td.addColum(Entry.removable.name(), new SqliteConnector.ColumnDesc("INTEGER", "NOT NULL"));

        td.setPrimaryKeys(String.format("PRIMARY KEY (%s, %s)", Entry.type.name(), Entry.name.name()));

        // Create Table.
        dbCon.createTable(td);
    }

    public void insertTask(String taskType, String taskName, String taskParam,
                           String jarPath, String className, int removable)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(taskType, taskName, taskParam, jarPath, className)
                || (!(removable == 0 || removable == 1))) {
            throw new InvalidParameterException();
        }

        // Build Query for Insert row.
        String query = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s)"
                        + " VALUES ( '%s', '%s', '%s', '%s', '%s', %d);",
                TASKTABLENAME,
                Entry.type.name(),
                Entry.name.name(),
                Entry.path.name(),
                Entry.param.name(),
                Entry.classname.name(),
                Entry.removable.name(),
                taskType,
                taskName,
                jarPath,
                taskParam,
                className,
                removable);

        LOGGER.info(query);

        dbCon.executeUpdate(query);
    }

    public List<Map<String, String>> getAllTasks() throws SQLException {
        // Build Query for Insert row.
        return getTasks("*");
    }

    public List<Map<String, String>> getAllTaskProperty() throws SQLException {
        return getTasks(Entry.type.name(), Entry.name.name(), Entry.param.name());
    }


    public List<Map<String, String>> getTasks(String... cols)
            throws SQLException, InvalidParameterException {
        // Build Query for Insert row.

        if (SqliteConnector.Util.validateParams(cols)) {
            throw new InvalidParameterException();
        }

        String query = "SELECT ";

        for (int i = 0; i < cols.length; ++i) {
            query += String.format("%s", cols[i]);
            if ((i + 1) != cols.length) {
                query += ", ";
            }
        }
        query += String.format(" FROM %s;", TASKTABLENAME);

        LOGGER.info(query);

        return dbCon.executeSelect(query);
    }

    public List<Map<String, String>> getTaskByType(String taskType)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(taskType)) {
            throw new InvalidParameterException();
        }


        // Build Query for Insert row.
        String query = String.format("SELECT * FROM %s WHERE %s='%s';",
                TASKTABLENAME,
                Entry.type.name(), taskType);

        LOGGER.info(query);

        return dbCon.executeSelect(query);
    }

    public List<Map<String, String>> getTaskByName(String taskName)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(taskName)) {
            throw new InvalidParameterException();
        }
        // Build Query for get a row.
        String query = String.format("SELECT * FROM %s WHERE %s='%s';",
                TASKTABLENAME,
                Entry.name.name(), taskName);

        LOGGER.info(query);

        return dbCon.executeSelect(query);
    }

    public List<Map<String, String>> getTaskByTypeAndName(String taskType, String taskName)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(taskType, taskName)) {
            throw new InvalidParameterException();
        }

        // Build Query for get a row.
        String query = String.format("SELECT * FROM %s WHERE %s='%s' and %s='%s';",
                TASKTABLENAME,
                Entry.type.name(), taskType,
                Entry.name.name(), taskName);

        LOGGER.info(query);

        return dbCon.executeSelect(query);
    }

    public List<Map<String, String>> getTaskByPath(String path)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(path)) {
            throw new InvalidParameterException();
        }

        // Build Query for get a row.
        String query = String.format("SELECT * FROM %s WHERE %s='%s';",
                TASKTABLENAME,
                Entry.path.name(), path);

        LOGGER.info(query);

        return dbCon.executeSelect(query);
    }

    public int getJarReferenceCount(String path)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(path)) {
            throw new InvalidParameterException();
        }

        List<Map<String, String>> list = getTaskByPath(path);

        if (null == list) {
            return 0;
        }

        return list.size();
    }

    public int getJarReferenceCount(String taskType, String taskName)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(taskType, taskName)) {
            throw new InvalidParameterException();
        }

        int ret = 0;

        List<Map<String, String>> list = getTaskByTypeAndName(taskType, taskName);

        if (null != list) {
            ret = list.size();
        }

        return ret;
    }

    public String getJarPathByTypeAndName(String taskType, String taskName)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(taskType, taskName)) {
            throw new InvalidParameterException();
        }

        String ret = null;

        List<Map<String, String>> resMap = getTaskByTypeAndName(taskType, taskName);
        if (0 < resMap.size()) {
            ret = resMap.get(0).get("path");
        }

        return ret;
    }

    public void deleteTaskByPath(String jarPath)
            throws SQLException, InvalidParameterException {
        if (SqliteConnector.Util.validateParams(jarPath)) {
            throw new InvalidParameterException();
        }
        String query = String.format("DELETE FROM %s WHERE %s='%s';", TASKTABLENAME,
                Entry.path.name(), jarPath);

        LOGGER.info(query);

        dbCon.executeUpdate(query);
    }

//    @Deprecated
//    public void deleteTaskById(String tid) throws SQLException {
//        String query = String.format("DELETE FROM %s WHERE %s='%s';", TASKTABLENAME, Entry.tid.name(), tid);
//
//        LOGGER.info(query);
//
//        dbCon.executeUpdate(query);
//    }

    public void deleteTaskByTypeAndName(String taskType, String taskName)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(taskType, taskName)) {
            throw new InvalidParameterException();
        }

        String query = String.format("DELETE FROM %s WHERE type='%s' AND name='%s';",
                TASKTABLENAME,
                taskType,
                taskName);

        LOGGER.info(query);

        dbCon.executeUpdate(query);
    }


    public void deleteTasksByTypeAndName(String taskType, String taskName)
            throws SQLException, InvalidParameterException {

        if (SqliteConnector.Util.validateParams(taskType, taskName)) {
            throw new InvalidParameterException();
        }

        String query = String.format("DELETE FROM %s WHERE PATH IN "
                        + "(SELECT PATH FROM %s WHERE type='%s' AND name='%s');",
                TASKTABLENAME, TASKTABLENAME,
                taskType,
                taskName);

        LOGGER.info(query);

        dbCon.executeUpdate(query);
    }

    public enum Entry {
        tid,
        type,
        name,
        param,
        path,
        classname,
        removable
    }
}
