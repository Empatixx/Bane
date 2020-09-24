package cz.Empatix.Database;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class Database {
    Connection connection;
    String dbName;
    String table;

    public Database() {
        dbName = "data";
        table = "general";
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateGeneralTable);
            s.close();

            s.executeUpdate(SQLiteCreateUpgrades);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        init();
        insert("money", 0, "general");
        insertUpgrade("pistol", 0, "upgrades");
        insertUpgrade("luger", 0, "upgrades");
        insertUpgrade("shotgun", 0, "upgrades");
        insertUpgrade("uzi", 0, "upgrades");
        insertUpgrade("m4", 0, "upgrades");
        insertUpgrade("grenadelauncher", 0, "upgrades");
        insertUpgrade("revolver", 0, "upgrades");
        insertUpgrade("thompson", 0, "upgrades");
    }

    public Connection getSQLConnection() {
        File dataFolder = new File(dbName + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();

            } catch (IOException e) {
                System.out.println("File write error: " + dbName + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            System.out.println("SQLite exception on initialize " + ex);
        } catch (ClassNotFoundException ex) {
            System.out.println("You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void init() {
        connection = getSQLConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table);
            ResultSet rs = ps.executeQuery();
            close(ps, rs);

        } catch (SQLException ex) {
            System.out.println("Unable to retreive connection " + ex);
        }
    }

    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }


    /*public String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS general (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "`money` int(11) NOT NULL" +
            ");";

     */
    public String SQLiteCreateGeneralTable = "CREATE TABLE IF NOT EXISTS general (" +
            "`type` varchar(11) PRIMARY KEY NOT NULL," +
            "`value` int(11) NOT NULL" +
            ");";
    public String SQLiteCreateUpgrades = "CREATE TABLE IF NOT EXISTS upgrades (" +
            "`name` varchar(11) PRIMARY KEY NOT NULL," +
            "`numUpgrades` int(11) NOT NULL" +
            ");";


    public Integer getValue(String type, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table);

            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("type").equals(type)) {
                    return rs.getInt("value"); // Return the players ammount of kills. If you wanted to get total (just a random number for an example for you guys) You would change this to total!
                }
            }
        } catch (SQLException ex) {
            System.out.println("SQLITE: cannot perform connection execution");
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLITE: cannot perform connection close");
            }
        }
        return 0;
    }
    public Integer getValueUpgrade(String type, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table);

            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("name").equals(type)) {
                    return rs.getInt("numUpgrades"); // Return the players ammount of kills. If you wanted to get total (just a random number for an example for you guys) You would change this to total!
                }
            }
        } catch (SQLException ex) {
            System.out.println("SQLITE: cannot perform connection execution");
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLITE: cannot perform connection close");
            }
        }
        return 0;
    }
    public Integer setValue(String type, int value) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE general " +
                    "SET value = ? " +
                    "WHERE type = ?;");

            ps.setInt(1, value);

            ps.setString(2, type);

            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("SQLITE: cannot perform connection execution");
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLITE: cannot perform connection close");
            }
        }
        return 0;
    }
    public Integer setValueUpgrade(String name, int value) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE upgrades " +
                    "SET numUpgrades = ? " +
                    "WHERE name = ?;");

            ps.setInt(1, value);

            ps.setString(2, name);

            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("SQLITE: cannot perform connection execution");
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLITE: cannot perform connection close");
            }
        }
        return 0;
    }
    public void insert(String type, int value, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        boolean isAlreadyInserted = true;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT 1 FROM " + table + " WHERE type = '" + type + "';");

            rs = ps.executeQuery();

            isAlreadyInserted = rs.next();

        } catch (SQLException ex) {
            System.out.println("SQLITE: cannot perform connection execution");
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLITE: cannot perform connection close");
            }
        }
        conn = null;
        ps = null;
        if (isAlreadyInserted) return;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO " + table + "(type,value) VALUES(?,?);");


            ps.setString(1, type);

            ps.setInt(2, value);

            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("SQLITE: cannot perform connection execution");
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLITE: cannot perform connection close");
            }
        }
    }
    public void insertUpgrade(String name, int value, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        boolean isAlreadyInserted = true;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT 1 FROM "+table+" WHERE name = '"+name+"';");

            rs = ps.executeQuery();

            isAlreadyInserted = rs.next();

        } catch (SQLException ex) {
            System.out.println("SQLITE: cannot perform connection execution");
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLITE: cannot perform connection close");
            }
        }
        conn = null;
        ps = null;
        if(isAlreadyInserted) return;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO "+table+"(name,numUpgrades) VALUES(?,?);");


            ps.setString(1,name);

            ps.setInt(2,value);

            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("SQLITE: cannot perform connection execution");
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLITE: cannot perform connection close");
            }
        }
    }






}
