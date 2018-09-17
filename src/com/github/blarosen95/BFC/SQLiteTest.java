package com.github.blarosen95.BFC;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

class SQLiteTest {

    private static Connection con;
    private static boolean hasData = false;
    private static File dataFolder = Main.getInstance().getDataFolder();
    private static String banDBFile = dataFolder.getAbsolutePath() + File.separator + "BFC.db";


    ArrayList listTheirBans(String uuid) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        ArrayList<Object> listOut = new ArrayList<>();

        PreparedStatement preparedStatement = con.prepareStatement("SELECT banned_player FROM claim_bans WHERE owner_uuid=?");
        preparedStatement.setString(1, uuid);
        listOut.add(preparedStatement.executeQuery());
        PreparedStatement preparedStatement1 = con.prepareStatement("SELECT COUNT(banned_player) FROM claim_bans WHERE owner_uuid=?");
        preparedStatement1.setString(1, uuid);
        listOut.add(preparedStatement1.executeQuery());
        return listOut;
    }

    private void getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection(String.format("jdbc:sqlite:%s", banDBFile));
        initialise();
    }

    private void initialise() throws SQLException {
        if (!hasData) {
            hasData = true;

            Statement state = con.createStatement();
            ResultSet res = state.executeQuery("SELECT NAME FROM sqlite_master WHERE type='table' AND name='claim_bans'");

            if (!res.next()) {
                System.out.println("Building the claim_bans table with a pre-populated piece of data (if Notch and _Jeb ever were to join, Notch will need to /unbanfromclaim _Jeb.");
                Statement state2 = con.createStatement();

                state2.execute("CREATE TABLE claim_bans("
                + "owner varchar(16)," + "owner_uuid varchar(36),"
                        + "banned_player varchar(16)," + "banned_uuid varchar(36));");

                PreparedStatement prep = con.prepareStatement("INSERT INTO claim_bans (owner, owner_uuid, banned_player, banned_uuid) values(?,?,?,?);");
                prep.setString(1, "Notch");
                prep.setString(2, "069a79f4-44e9-4726-a5be-fca90e38aaf5");
                prep.setString(3, "_jeb");
                prep.setString(4, "45f50155-c09f-4fdc-b5ce-e30af2ebd1f0");
                 prep.execute();
            }
        }
    }

    void addBan(String owner, String ownerUUID, String bannedPlayer, String bannedUUID) throws SQLException, ClassNotFoundException {

        if (con == null) {
            getConnection();
        }

        boolean notBannedYet = false;
        PreparedStatement psQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner=? AND owner_uuid=? AND banned_player=? AND banned_uuid=?");
        psQuery.setString(1, owner);
        psQuery.setString(2, ownerUUID);
        psQuery.setString(3, bannedPlayer);
        psQuery.setString(4, bannedUUID);
        ResultSet rsQueried = psQuery.executeQuery();
        if (rsQueried.next()) {
            notBannedYet = true;
        }

        if (!notBannedYet) {
            PreparedStatement prep = con.prepareStatement("INSERT INTO claim_bans (owner, owner_uuid, banned_player, banned_uuid) values(?,?,?,?)");
            prep.setString(1, owner);
            prep.setString(2, ownerUUID);
            prep.setString(3, bannedPlayer);
            prep.setString(4, bannedUUID);
            prep.execute();
        }
    }

    void removeBan(String owner, String ownerUUID, String bannedPlayer, String bannedUUID) throws SQLException, ClassNotFoundException {

        if (con == null) {
            getConnection();
        }

        boolean banExists = false;
        PreparedStatement psQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner=? AND owner_uuid=? AND banned_player=? AND banned_uuid=?");
        psQuery.setString(1, owner);
        psQuery.setString(2, ownerUUID);
        psQuery.setString(3, bannedPlayer);
        psQuery.setString(4, bannedUUID);
        ResultSet rsQueried = psQuery.executeQuery();
        if (rsQueried.next()) {
            banExists = true;
        }

        if (banExists) {
            PreparedStatement prep = con.prepareStatement("DELETE FROM claim_bans WHERE owner=? AND owner_uuid=? AND banned_player=? AND banned_uuid=?");
            prep.setString(1, owner);
            prep.setString(2,ownerUUID);
            prep.setString(3, bannedPlayer);
            prep.setString(4, bannedUUID);
            prep.execute();
        }

    }

    ResultSet findBan(String owner, String ownerUUID, String bannedPlayer, String bannedUUID) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement psQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner=? AND owner_uuid=? AND banned_player=? AND banned_uuid=?");
        psQuery.setString(1, owner);
        psQuery.setString(2, ownerUUID);
        psQuery.setString(3, bannedPlayer);
        psQuery.setString(4, bannedUUID);
        return psQuery.executeQuery();
    }

    @Deprecated
    ResultSet findOwnerUUID(String owner) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement psQuery = con.prepareStatement("SELECT COUNT(DISTINCT owner_uuid) FROM claim_bans WHERE owner=?");
        PreparedStatement psQuery1 = con.prepareStatement("SELECT owner_uuid FROM claim_bans WHERE owner=?");
        psQuery.setString(1, owner);
        psQuery1.setString(1, owner);
        //If the returned result of the query (executed within the if statement) indicates that exactly one owner_uuid exists for the given owner
        if (Integer.valueOf(psQuery.executeQuery().getString(1)) == 1) {
            return psQuery1.executeQuery();
        }
        return null;
    }

}
