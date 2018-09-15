package com.github.blarosen95.BFC;

import com.github.blarosen95.BFC.dbUtil.dbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

class CheckDb {
    private Connection connection;

    public CheckDb() {
        try {
            this.connection = dbConnection.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (this.connection == null) {
            System.exit(1);
        }
    }

    public boolean isDatabaseConnected() {
        return this.connection != null;
    }

    public boolean isBannedFromShop(UUID ownerUUID, UUID bannedUUID) throws Exception {
        //"select owner_uuid from claim_bans where banned_uuid = " +bannedUUID
        PreparedStatement pr = null;
        ResultSet rs = null;
        String sql = "select * from claim_bans where banned_uuid = ? AND owner_uuid = ?";

        try {
            pr = this.connection.prepareStatement(sql);
            pr.setString(1, bannedUUID.toString());
            pr.setString(2, ownerUUID.toString());
            //If issues occur, make sure that converts to a proper string!

            rs = pr.executeQuery();

            boolean bool1;

            //if rs has next (has a result matching the query we ran), it is true. otherwise, it's false.
            return rs.next();
        } catch (SQLException ex) {
            return false;
        }
//        finally {
//            pr.close();
//            rs.close();
//        }



    }
}
