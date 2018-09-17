package com.github.blarosen95.BFC;

import com.Acrobot.ChestShop.Signs.ChestShopSign;
import org.bukkit.Bukkit;
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
import java.util.Set;
import java.util.UUID;


import com.Acrobot.Breeze.Utils.BlockUtil;

class ChestShopHandler implements Listener {
    private static Settings settings = new Settings();

    //Having our Event's Priority set to LOWEST lets our plugin handle the event before ChestShop is allowed to handle it
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractWithSign(PlayerInteractEvent event) throws SQLException, ClassNotFoundException {
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
        CSSQLite cssqLite = new CSSQLite();

        //If the block clicked is a sign
        if (BlockUtil.isSign(block)) {
            sign = (Sign) block.getState();

            //If it's a valid chest shop sign
            if (ChestShopSign.isValid(sign)) {
                //Let's find any Claim info on the sign here
                GriefPreventionHandler griefPreventionHandler = new GriefPreventionHandler();
                String ownerUUID = griefPreventionHandler.findBySign(sign);
                String signOwnerName = sign.getLine(0);

                //TODO: let's just remove the check for claim owners having bans on the shopper
                //Let's make sure ownerUUID isn't null (this is a restriction on ChestShop events based on whether the claim owner has banned the player)
 /*               if (ownerUUID != null) {
                    //Get the name from ownerUUID (even if offline)
                    String owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName();
                    ResultSet resultSet = sqLiteTest.findBan(owner, ownerUUID, player.getName(), player.getUniqueId().toString());

                    //If the query found that the owner of the claim where the sign is located has the player banned
                    if (resultSet.next()) {
                        //Cancel the event so that ChestShop never receives it
                        event.setCancelled(true);
                        player.sendMessage(settings.cantShopHere.replace("{PLAYER}", signOwnerName));
                        return;
                    }
                } */
                //If the nested if above didn't run, the claim owner has no ban on the player, and we need to check whether the seller/buyer on the sign has banned the player
                if (signOwnerName != null) {
                    ResultSet resultSet = cssqLite.findUUID(signOwnerName);
                    if (resultSet != null) {
                        String signOwnerUUID = resultSet.getString(1);
                        ResultSet banCheckSet = sqLiteTest.findBan(signOwnerName, signOwnerUUID, player.getName(), player.getUniqueId().toString());
                        if (banCheckSet.next()) {
                            event.setCancelled(true);
                            player.sendMessage(settings.cantShopHere.replace("{PLAYER}", signOwnerName));
                        }
                    }
                }
            }
        }
    }
}
