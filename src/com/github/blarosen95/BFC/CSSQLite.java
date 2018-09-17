package com.github.blarosen95.BFC;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.Date;

@SuppressWarnings("SqlResolve")
class CSSQLite {
    private static Connection con;

    private static File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("BanFromClaim").getDataFolder();
    private static String userDBCopyFile = dataFolder.getAbsolutePath() + File.separator + "usersCopied.db";

    private void getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection(String.format("jdbc:sqlite:%s", userDBCopyFile));
    }

    ResultSet findUUID(String name) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement preparedStatement = con.prepareStatement("SELECT uuid FROM accounts WHERE name=? COLLATE NOCASE");
        preparedStatement.setString(1, name);
        //con.close();
        return preparedStatement.executeQuery();
    }

    Boolean findLatest(String playerName, String playerUUID) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM accounts WHERE name=? AND uuid=?");
        preparedStatement.setString(1, playerName);
        preparedStatement.setString(2, playerUUID);
        return preparedStatement.executeQuery().next();
    }

    void addAccount(String playerName, String shortName, String playerUUID, Date lastSeen) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO accounts (name, shortName, uuid, lastSeen) VALUES(?,?,?,?)");
        preparedStatement.setString(1, playerName);
        preparedStatement.setString(2, shortName);
        preparedStatement.setString(3, playerUUID);
        preparedStatement.setLong(4, lastSeen.getTime());
        preparedStatement.execute();
    }

    void updateAccount(String playerName, String shortName, String playerUUID, Date lastSeen) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement preparedStatement = con.prepareStatement("UPDATE accounts SET lastSeen=? WHERE uuid=?");
        preparedStatement.setLong(1,lastSeen.getTime());
        preparedStatement.setString(2, playerUUID);
        preparedStatement.execute();
    }

}
