/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.unierlangen.med.imi.allensparql;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matesn
 */
public class dbHelper {

    private String driver = "";
    private String url = "";
    private String user = "";
    private String password = "";
    private String i2b2schema = "";
    private String server = "";
    private String port = "";
    private String SID = "";
    Connection conn = null;
    private String lastSQL = "";
    Statement stmt = null;
    ResultSet rs = null;
    ResultSetMetaData rsMetaData = null;
    int numberOfColumns;

    void loadConfigFile(String configurationFile) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(configurationFile);
            Properties prop = new Properties();
            prop.load(in);
            in.close();
            setDriver(prop.getProperty("db.driver"));
            setUrl(prop.getProperty("db.urlprefix") + prop.getProperty("db.server") + ":" + prop.getProperty("db.port")
                    + ":" + prop.getProperty("db.SID"));
            setUser(prop.getProperty("db.username"));
            setI2b2schema(prop.getProperty("db.i2b2schema") + ".");
            setPassword(prop.getProperty("db.password"));
            setServer(prop.getProperty("db.server"));
            setPort(prop.getProperty("db.port"));
            setSID(prop.getProperty("db.SID"));

        } catch (IOException ex) {
            Logger.getLogger(dbHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(dbHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void initConnention() {
        System.out.println("Creating connection with: " + getUrl() + ", " + getUser() + ", " + getPassword());
        try {
            // Treiber laden
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
            } catch (ClassNotFoundException e) {
                System.out.println("Fehler: oracle.jdbc.driver.OracleDriver nicht gefunden!");
                System.out.println(e);
                System.exit(1);
            }
            DriverManager.setLoginTimeout(1);
            conn = DriverManager.getConnection(getUrl(), getUser(), getPassword());
        } catch (SQLException ex) {
            Logger.getLogger(dbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void executeSQLDirect(String sqlCommand) {
        lastSQL = sqlCommand;
        System.out.println("Executing SQL statement: " + sqlCommand);

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlCommand);
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(dbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ResultSet executeSQL(String sqlCommand) {
        lastSQL = sqlCommand;
        System.out.println("SQL statement: " + sqlCommand);

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlCommand);
            rsMetaData = rs.getMetaData();
            numberOfColumns = rsMetaData.getColumnCount();

        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(dbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rs;
    }

    Boolean nextEntry() {

        try {
            if (rs.next()) {
                return true;
            } else {
                rs.close();
                stmt.close();
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(dbHelper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public String getColumn(String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException ex) {
            Logger.getLogger(dbHelper.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String getLastSQL() {
        return lastSQL;
    }

    public void closeConnection() {

        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(dbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the driver
     */
    public String getDriver() {
        return driver;
    }

    /**
     * @param driver the driver to set
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the i2b2schema
     */
    public String getI2b2schema() {
        return i2b2schema;
    }

    /**
     * @param i2b2schema the i2b2schema to set
     */
    public void setI2b2schema(String i2b2schema) {
        this.i2b2schema = i2b2schema;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the SID
     */
    public String getSID() {
        return SID;
    }

    /**
     * @param SID the SID to set
     */
    public void setSID(String SID) {
        this.SID = SID;
    }
}
