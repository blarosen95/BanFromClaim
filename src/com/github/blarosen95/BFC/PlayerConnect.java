package com.github.blarosen95.BFC;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class PlayerConnect implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onPlayerConnect(final PlayerJoinEvent event) {
        SQLiteTest sqLiteTest = new SQLiteTest();

        Player who = event.getPlayer();
        String whoUUID = who.getUniqueId().toString();
        String whoName = who.getName();

        try {
            if (sqLiteTest.hasBanned(who)) {
                // If they've banned, check if all bans for their UUID match their current name.
                if (sqLiteTest.updateBans(who)) {
                    who.sendMessage("Your username in the ShopBans Database has been updated!");
                }
            }

            if (sqLiteTest.hasBeenBanned(who)) {
                // If they've been banned, check if bans for their UUID match their current name.
                if (sqLiteTest.hasBeenBanned(who)) {
                    if (sqLiteTest.updateBanned(who)) {
                        return;
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
