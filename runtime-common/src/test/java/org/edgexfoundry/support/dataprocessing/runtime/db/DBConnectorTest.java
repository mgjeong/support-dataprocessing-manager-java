package org.edgexfoundry.support.dataprocessing.runtime.db;

/*
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static DBConnector.ColumnDesc;
import static DBConnector.TableDesc;
*/

public class DBConnectorTest {

    //FIXLATER:
    /*
    private static DBConnector dbCon = null;
    private static String testTableName = "job_test";
    private static String testTableNameWithComplexKey = "job_test_complex";

    @BeforeClass
    public static void createTest() throws Exception {
        dbCon = DBConnector.getInstance();
        Assert.assertNotNull(dbCon);

        // create test tables
        createTestTables();
    }

    @AfterClass
    public static void destroyTest() throws Exception {
        // destroy test tables
        dropTestTables();

        dbCon.close();
        dbCon = null;
    }

    private static void createTestTables() throws Exception {
        TableDesc td = new TableDesc(testTableName);
        td.addColum("jid", new ColumnDesc("TEXT", "PRIMARY KEY NOT NULL"));
        td.addColum("gid", new ColumnDesc("TEXT", "NOT NULL"));
        td.addColum("state", new ColumnDesc("TEXT", "NOT NULL"));
        td.addColum("input", new ColumnDesc("TEXT", "NOT NULL"));
        td.addColum("output", new ColumnDesc("TEXT", "NOT NULL"));
        td.addColum("taskinfo", new ColumnDesc("TEXT", "NOT NULL"));
        td.addColum("engineid", new ColumnDesc("TEXT", "NOT NULL"));
        dbCon.createTable(td);
    }

    private static void dropTestTables() throws SQLException {
        String query = String.format("DROP table %s;", testTableName);
        System.out.println(query);
        dbCon.executeUpdate(query);
    }

    @Test
    public void testMultipleConnectionAttempts() {
        dbCon.connect();
        dbCon.connect();
    }

    @Test
    public void testInvalidCreateTableQuery() {

        boolean isCalled = false;

        TableDesc td = new TableDesc(null);

        td.addColum("jid", new ColumnDesc("TEXT", "PRIMARY KEY NOT NULL"));

        try {
            dbCon.createTable(td);
        } catch (Exception e) {
            e.printStackTrace();
            isCalled = true;
        }

        Assert.assertTrue(isCalled);
    }

    @Test
    public void testComplexPrimaryKey() throws Exception {
        TableDesc td = new TableDesc(testTableNameWithComplexKey);
        td.addColum("a", new ColumnDesc("TEXT", "NOT NULL"));
        td.addColum("b", new ColumnDesc("TEXT", "NOT NULL"));
        td.setPrimaryKeys(String.format("PRIMARY KEY (%s, %s)",
                "a", "b"));
        dbCon.createTable(td);

        String query = String.format("DROP table %s;", testTableNameWithComplexKey);
        dbCon.executeUpdate(query);
    }

    @Test
    public void testValidJobInsert() throws SQLException {
        String query = String.format("INSERT OR REPLACE INTO %s (%s, %s, %s, %s, %s, %s, %s ) "
                                        + "VALUES ( '%s','%s','%s','%s','%s','%s','%s');",
                testTableName, "jid", "gid", "state", "input", "output", "taskinfo", "engineid",
                "jobid_abcdefg", "groupid_abcdefg", "running", "zmq:111:111:111",
                "zmq:222:222:222", "type, name, params", "flinkid_abcdefg");
        System.out.println(query);
        dbCon.executeUpdate(query);

        // Try select
        query = String.format("SELECT %s, %s, %s FROM %s;", "jid", "gid", "state", testTableName);
        System.out.println(query);
        List<Map<String, String>> jobList = dbCon.executeSelect(query);
        Assert.assertNotNull(jobList);
        for (Iterator<Map<String, String>> it = jobList.iterator(); it.hasNext(); ) {
            Map<String, String> job = it.next();
            Assert.assertNotNull(job);
            String jobId = job.get("jid");
            System.out.println(jobId);
            Assert.assertNotNull(jobId);
            String groupId = job.get("gid");
            System.out.println(groupId);
            Assert.assertNotNull(groupId);
            String state = job.get("state");
            System.out.println(state);
            Assert.assertNotNull(state);
        }
    }

    @Test
    public void testInvalidUpdateQuery() {

        boolean isCalled = false;
        try {
            dbCon.executeUpdate("query");
        } catch (SQLException e) {
            e.printStackTrace();
            isCalled = true;
        }

        Assert.assertTrue(isCalled);

    }

    @Test
    public void testInvalidSelectQuery() {

        boolean isCalled = false;
        try {
            dbCon.executeSelect("query");
        } catch (SQLException e) {
            e.printStackTrace();
            isCalled = true;
        }

        Assert.assertTrue(isCalled);

    }


    @Test
    public void testDBUtilWithValidParams() {
        Assert.assertFalse(DBConnector.Util.validateParams("", "", ""));
    }

    @Test
    public void testDBUtilWithInvalidParams() {
        Assert.assertTrue(DBConnector.Util.validateParams("", null, ""));
        Assert.assertTrue(DBConnector.Util.validateParams(null));
    }
    */
}
