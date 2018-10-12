package com.github.blarosen95.BFC;

import com.Acrobot.ChestShop.Signs.ChestShopSign;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.Acrobot.Breeze.Utils.BlockUtil;

class ChestShopHandler implements Listener {
    private static Settings settings = new Settings();

    //Having our Event's Priority set to LOWEST lets our plugin handle the event before ChestShop is allowed to handle it
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractWithSign(PlayerInteractEvent event) throws SQLException, ClassNotFoundException {
        if (event.isCancelled() || event.getClickedBlock() == null || event.getPlayer() == null) return;
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Action action = event.getAction();
        Player player = event.getPlayer();

        if (Main.griefPrevention.ignoringClaims(player)) {
            return;
        }

        Sign sign;
        SQLiteTest sqLiteTest = new SQLiteTest();
        //If the block clicked is a sign
        if (BlockUtil.isSign(block)) {
            sign = (Sign) block.getState();

            //If it's a valid chest shop sign
            if (ChestShopSign.isValid(sign)) {
                String signOwnerName = sign.getLine(0);
                if (signOwnerName != null) {
                    ResultSet checkBansSet = sqLiteTest.findBan2(signOwnerName, player);
                    //If the ResultSet isn't null and it contains a row,
                    if (checkBansSet != null && checkBansSet.next()) {
                        //The row it contains will be this player's ban record for this shop owner.
                        event.setCancelled(true);
                        player.sendMessage(settings.cantShopHere.replace("{PLAYER}", signOwnerName));
                    } else {
                        return;
                    }
                }
            }
        }
    }
}