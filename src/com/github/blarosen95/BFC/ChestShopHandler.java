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
import java.util.UUID;


import com.Acrobot.Breeze.Utils.BlockUtil;

class ChestShopHandler implements Listener {

    //Having our Event's Priority set to highest SHOULD let our plugin handle the event before ChestShop is allowed to handle it
    //Apparently the lower the priority, the sooner it is run (go figure), luckily there is a priority lower than that of ChestShop's
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractWithSign(PlayerInteractEvent event) throws SQLException, ClassNotFoundException {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Action action = event.getAction();
        Player player = event.getPlayer();

        Sign sign;
        SQLiteTest sqLiteTest = new SQLiteTest();

        //If the block clicked is a sign
        if (BlockUtil.isSign(block)) {
            sign = (Sign) block.getState();

            //If it's a valid chest shop sign
            if (ChestShopSign.isValid(sign)) {
                //Let's find any Claim info on the sign here
                GriefPreventionHandler griefPreventionHandler = new GriefPreventionHandler();
                String ownerUUID = griefPreventionHandler.findBySign(sign);
                String signOwnerName = sign.getLine(0);

                //Let's make sure ownerUUID isn't null (this is a restriction on ChestShop events based on whether the claim owner has banned the player)
                if (ownerUUID != null) {
                    //Get the name from ownerUUID (even if offline)
                    String owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName();
                    ResultSet resultSet = sqLiteTest.findBan(owner, ownerUUID, player.getName(), player.getUniqueId().toString());

                    //If the query found that the owner of the claim where the sign is located has the player banned
                    if (resultSet.next()) {
                        event.setCancelled(true);
                        player.sendMessage(String.format("%s has banned you from their claims, you'll have to shop elsewhere.", owner));
                    }
                     else //noinspection Duplicates
                        if (signOwnerName != null) {
                        ResultSet resultSet2 = sqLiteTest.findOwnerUUID(signOwnerName);
                        if (resultSet2 != null) {
                            //The line below could return a SQL Exception if the signOwnerName has not banned someone from our DB
                                //Easiest solution is to use ChestShop's account database to grab UUID from names, since it populates when player's first join
                            String signOwnerUUID = resultSet2.getString(1);
                            ResultSet banCheckSet = sqLiteTest.findBan(signOwnerName, signOwnerUUID, player.getName(), player.getUniqueId().toString());
                            if (banCheckSet.next()) {
                                event.setCancelled(true);
                                player.sendMessage(String.format("%s has banned you from their shops, you'll have to shop elsewhere.", signOwnerName));
                            }
                        }
                    }
                }
                //If the nested if above didn't run, ownerUUID is null and we need to check whether the seller/buyer on the sign has banned the player
                else //noinspection Duplicates
                    if (signOwnerName != null) {
                    //In order to find UUID, we have to do a preliminary query
                    //This query will check our db for signOwnerName and return all associated Owner_UUID records
                        //Without duplicates for Owner_UUID
                            //If more than one row is found, it's because there are more than one UUID's associated with the given name (names are unfortunately no longer unique)
                    ResultSet resultSet = sqLiteTest.findOwnerUUID(signOwnerName);
                    if (resultSet != null) {
                        String signOwnerUUID = resultSet.getString(1);
                        ResultSet banCheckSet = sqLiteTest.findBan(signOwnerName, signOwnerUUID, player.getName(), player.getUniqueId().toString());
                        if (banCheckSet.next()) {
                            event.setCancelled(true);
                            player.sendMessage(String.format("%s has banned you from their shops, you'll have to shop elsewhere.", signOwnerName));
                        }
                    }
                }
            }
        }
    }
}
