package com.github.blarosen95.BFC;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;

@SuppressWarnings("SqlResolve")
class CSSQLite {
    private static Connection con;
    private static File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("ChestShop").getDataFolder();
    private static String userDBFile = dataFolder.getAbsolutePath() + File.separator + "users.db";

    String getUserDBFile() {
        return userDBFile;
    }

    private void getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection(String.format("jdbc:sqlite:%s", userDBFile));
    }

    ResultSet findUUID(String name) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        PreparedStatement preparedStatement = con.prepareStatement("SELECT uuid FROM accounts WHERE name=?");
        preparedStatement.setString(1, name);
        return preparedStatement.executeQuery();
    }
}
