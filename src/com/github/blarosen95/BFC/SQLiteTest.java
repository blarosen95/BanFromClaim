package com.github.blarosen95.BFC;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

class SQLiteTest {

    private static Connection con;
    private static boolean hasData = false;
    private static File dataFolder = Main.getInstance().getDataFolder();
    private static String banDBFile = dataFolder.getAbsolutePath() + File.separator + "BFC.db";
    private static NameToUUID nameToUUID = new NameToUUID();


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

    @Deprecated
    boolean addBanOffline(Player owner, String bannedPlayer) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        String bannedUUID = nameToUUID.getUUID(bannedPlayer);

        if (bannedUUID == null || bannedUUID.equals("invalid name")) {
            owner.sendMessage(String.format("Could not find the offline player '%s', did you type it correctly?", bannedPlayer));
            return false;
        }

        // TODO: 10/12/2018 This is the downfall of the API usage...
        if (bannedUUID.equals("Rate Limited")) {
            owner.sendMessage(String.format("We can't add a ban for the offline player '%s' right now due to rate limits. You might want to wait for them to come back online.", bannedPlayer));
            return false;
        }

        PreparedStatement psQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner_uuid=? AND banned_uuid=?");
        psQuery.setString(1, owner.getUniqueId().toString());
        psQuery.setString(2, bannedUUID);
        ResultSet rsQueried = psQuery.executeQuery();
        if (rsQueried.next()) {
            return false;
        }

        PreparedStatement prep = con.prepareStatement("INSERT INTO claim_bans (owner, owner_uuid, banned_player, banned_uuid) VALUES(?,?,?,?)");
        prep.setString(1, owner.getName());
        prep.setString(2, owner.getUniqueId().toString());
        prep.setString(3, bannedPlayer);
        prep.setString(4, bannedUUID);
        prep.execute();
        return true;
    }

    @Deprecated
    boolean removeBan(Player owner, String bannedPlayer) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        String bannedUUID = nameToUUID.getUUID(bannedPlayer);
        if (bannedUUID == null || bannedUUID.equals("invalid name")) {
            owner.sendMessage(String.format("Could not find the player '%s', did you type it correctly?", bannedPlayer));
            return false;
        }

        PreparedStatement psQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner_uuid=? AND banned_uuid=?");
        psQuery.setString(1, owner.getUniqueId().toString());
        psQuery.setString(2, bannedUUID);
        ResultSet rsQueried = psQuery.executeQuery();
        if (!rsQueried.next()) {
            return false;
        }

        PreparedStatement prep = con.prepareStatement("DELETE FROM claim_bans WHERE owner_uuid=? AND banned_uuid=?");
        prep.setString(1, owner.getUniqueId().toString());
        prep.setString(2, bannedUUID);
        prep.execute();
        return true;
    }

    boolean removeBan2(Player owner, OfflinePlayer banPlayer) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement psQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner_uuid=? AND banned_uuid=?");
        psQuery.setString(1, owner.getUniqueId().toString());
        psQuery.setString(2, banPlayer.getUniqueId().toString());
        ResultSet rsQueried = psQuery.executeQuery();
        if (!rsQueried.next()) {
            return false;
        }

        PreparedStatement prep = con.prepareStatement("DELETE FROM claim_bans WHERE owner_uuid=? AND banned_uuid=?");
        prep.setString(1, owner.getUniqueId().toString());
        prep.setString(2, banPlayer.getUniqueId().toString());
        prep.execute();
        return true;
    }

    @Deprecated
    ResultSet findBan(String owner, Player bannedPlayer) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        String ownerUUID = nameToUUID.getUUID(owner);
        if (!ownerHasBans(owner) || ownerUUID == null || ownerUUID.equals("invalid name")) {
            return null; // TODO: 10/11/2018 check for null pointer errors from this before next release! Any null values returned from this method will indicate an event we won't process.
        }

        PreparedStatement psQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner_uuid=? AND banned_uuid=?");
        psQuery.setString(1, ownerUUID);
        psQuery.setString(2, bannedPlayer.getUniqueId().toString());
        return psQuery.executeQuery();
    }

    ResultSet findBan2(String owner, Player bannedPlayer) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        if (!ownerHasBans2(owner)) {
            return null; // TODO: 10/12/2018 Confirm no issues from here.
        }

        PreparedStatement psQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner=? AND banned_uuid=?");
        psQuery.setString(1, owner);
        psQuery.setString(2, bannedPlayer.getUniqueId().toString());
        return psQuery.executeQuery();
    }

    @Deprecated
    private boolean ownerHasBans(String owner) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        String ownerUUID = nameToUUID.getUUID(owner);
        if (ownerUUID == null || ownerUUID.equals("invalid name")) {
            // This means that the name on the sign is outdated and no longer the shop owner's real username. Nothing I can really do about it, players need to keep their signs up to date.
            return false;
        }
        PreparedStatement ownerQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner_uuid=?");
        ownerQuery.setString(1, ownerUUID);
        ResultSet rs = ownerQuery.executeQuery();
        return rs.next();
    }

    private boolean ownerHasBans2(String owner) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }
        if (owner == null || owner.equals("")) {
            //Indicates that the sign didn't have a ChestShop owner name on it... todo: check the ChestShopHandler to confirm this is handled already...
            return false;
        }
        PreparedStatement ownerQuery = con.prepareStatement("SELECT * FROM claim_bans WHERE owner=?");
        ownerQuery.setString(1, owner);
        ResultSet rs = ownerQuery.executeQuery();
        return rs.next();
    }

    boolean hasBanned(Player who) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement bans = con.prepareStatement("SELECT DISTINCT owner FROM claim_bans WHERE owner_uuid=?");
        bans.setString(1, who.getUniqueId().toString());
        ResultSet rs = bans.executeQuery();
        return rs.next();
    }

    boolean hasBeenBanned(Player who) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement banned = con.prepareStatement("SELECT * FROM claim_bans WHERE banned_uuid=?");
        banned.setString(1, who.getUniqueId().toString());
        ResultSet rs = banned.executeQuery();
        return rs.next();
    }

    boolean updateBans(Player who) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement needUpdateCheck = con.prepareStatement("SELECT owner FROM claim_bans WHERE owner_uuid=?");
        needUpdateCheck.setString(1, who.getUniqueId().toString());
        ResultSet ownersSet = needUpdateCheck.executeQuery();

        boolean needsUpdates = false;
        while (ownersSet.next()) {
            if (!ownersSet.getString(1).equals(who.getName())) {
                needsUpdates = true;
            }
        }
        if (!needsUpdates) {
            return false;
        }

        PreparedStatement updateStatement = con.prepareStatement("UPDATE claim_bans SET owner=? WHERE owner_uuid=?");
        updateStatement.setString(1, who.getName()); //new username
        updateStatement.setString(2, who.getUniqueId().toString()); //uuid
        updateStatement.execute();
        return true;
    }

    boolean updateBanned(Player who) throws SQLException, ClassNotFoundException {
        if (con == null) {
            getConnection();
        }

        PreparedStatement needUpdateCheck = con.prepareStatement("SELECT banned_player FROM claim_bans WHERE banned_uuid=?");
        needUpdateCheck.setString(1, who.getUniqueId().toString());
        ResultSet bannedSet = needUpdateCheck.executeQuery();

        boolean needsUpdates = false;
        while (bannedSet.next()) {
            if (!bannedSet.getString(1).equals(who.getName())) {
                needsUpdates = true;
            }
        }

        if (!needsUpdates) {
            return false;
        }

        PreparedStatement updateStatement = con.prepareStatement("UPDATE claim_bans SET banned_player=? WHERE banned_uuid=?");
        updateStatement.setString(1, who.getName());
        updateStatement.setString(2, who.getUniqueId().toString());
        updateStatement.execute();
        return true;
    }
}
