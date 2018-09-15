package com.github.blarosen95.BFC;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;
    private static Settings settings;
    private static GriefPreventionHandler griefPrevention;

    public Main() {

    }

    private String greenText(String msg) {
        return "\u001b[32;1m" + msg + "\u001b[0;m";
    }

    private String redText() {
        return "\u001b[31;1m" + "BanFromClaim can't start unless GriefPrevention is installed!" + "\u001b[0;m";
    }

    private void registerEvent(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    //Register events
    private void registerEvents() {
        registerEvent(new ChestShopHandler());
    }

    public void onEnable() {
        Plugin griefPreventionPlugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if (griefPreventionPlugin != null && griefPreventionPlugin.isEnabled()) {
            instance = this;
            settings = new Settings();
            griefPrevention = new GriefPreventionHandler();

            registerEvents();

            Bukkit.getPluginManager().registerEvents(this, this);
            this.getLogger().info(this.greenText(String.format("BanFromClaim v%s is enabled and working!", this.getDescription().getVersion())));
            //I will not be releasing this for other servers to use, so any updates can be had through me.
            //this.getUpdate();
        } else {
            this.getLogger().info(this.redText());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public void onDisable() {
        griefPrevention = null;
        settings = null;
        instance = null;
    }

    static Settings getSettings() {
        return settings;
    }

    static Main getInstance() {
        return instance;
    }

    private Player getOnlinePlayer(Player exempt, String name) {
        boolean isExecutor = false;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().contains(name.toLowerCase().trim())) {
                if (player != exempt) {
                    return player;
                }

                isExecutor = true;
            }
        }

        return isExecutor ? exempt : null;
    }

    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {

        if (cmd.getName().equalsIgnoreCase("BFCReload")) {
            if (!cs.hasPermission("banfromclaim.reload")) {
                cs.sendMessage(settings.noPermission.replace("{COMMAND}", alias));
                return true;
            } else {
                settings.reloadSettings();
                cs.sendMessage(settings.settingsReloaded.replace("{COMMAND}", alias));
                return true;
            }
        } else if (!(cs instanceof Player)) {
            cs.sendMessage(settings.playersOnly.replace("{COMMAND}", alias));
            return true;
        } else {
            Player player = (Player) cs;
            if (!player.hasPermission("banfromclaim.ban")) {
                player.sendMessage(settings.noPermission.replace("{COMMAND}", alias));
                return true;
            } else if (cmd.getName().equalsIgnoreCase("banfromclaim")) {
                if (args.length <= 0) {
                    cs.sendMessage(settings.banFromClaimUsage.replace("{COMMAND}", alias));
                    return true;
                } else {
                    Player target = this.getOnlinePlayer(player, args[0]);
                    if (target != null && player.canSee(target)) {
                        if (target == player) {
                            cs.sendMessage(settings.cantBanSelf);
                            return true;
                        } else {
                            String noBanReason = griefPrevention.noBanReason(player, target);
                            if (noBanReason != null) {
                                player.sendMessage(noBanReason);
                                return true;
                            } else {

                                Location loc = griefPrevention.safeLocation(target, target.getLocation());
                                if (loc == null) {
                                    player.sendMessage(settings.noSafeLocation.replace("{PLAYER}", target.getName()));
                                    //Still add them to the database here anyways
                                    return true;
                                } else {
                                    // Store the ban in the database
                                    SQLiteTest sqLiteTest = new SQLiteTest();
                                    try {
                                        sqLiteTest.addBan(cs.getName(), ((Player) cs).getUniqueId().toString(), target.getName(), target.getUniqueId().toString());
                                    } catch (SQLException | ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    Location spawn = new Location(player.getWorld(), (double) 0, (double) 64, (double) 0);
                                    target.teleport(spawn);
                                    player.sendMessage(settings.banSuccessful.replace("{PLAYER}", target.getName()));

                                    return true;
                                }
                            }
                        }
                    } else {
                        cs.sendMessage(settings.playerOffline.replace("{ARGUMENT}", args[0]));
                        return true;
                    }
                }
            } else if (cmd.getName().equalsIgnoreCase("unbanfromclaim")) {

                Player target = this.getOnlinePlayer(player, args[0]);
                SQLiteTest sqLiteTest = new SQLiteTest();
                try {
                    assert target != null;
                    sqLiteTest.removeBan(cs.getName(), ((Player) cs).getUniqueId().toString(), target.getName(), target.getUniqueId().toString());
                    cs.sendMessage(String.format("%s is no longer banned from your claims.", target.getName()));
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return true;
            }
        }
    }
}