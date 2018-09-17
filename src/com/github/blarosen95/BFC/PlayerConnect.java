package com.github.blarosen95.BFC;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.Date;

public class PlayerConnect implements Listener {

    private static CSSQLite cssqLite = new CSSQLite();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void onPlayerConnect(final PlayerJoinEvent event) {
        boolean latestAccount = false;
        try {
            latestAccount = cssqLite.findLatest(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!latestAccount) {
            try {
                cssqLite.addAccount(event.getPlayer().getName(), "TO-DO", event.getPlayer().getUniqueId().toString(), new Date());
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                cssqLite.updateAccount(event.getPlayer().getName(), "TO-DO", event.getPlayer().getUniqueId().toString(), new Date());
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
