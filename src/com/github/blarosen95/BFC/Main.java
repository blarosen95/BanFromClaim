package com.github.blarosen95.BFC;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;
    private static Settings settings;
    static GriefPreventionHandler griefPrevention;

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
        registerEvent(new PlayerConnect());
    }

    public void onEnable() {

        Plugin griefPreventionPlugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if (griefPreventionPlugin != null && griefPreventionPlugin.isEnabled()) {
            instance = this;
            settings = new Settings();
            griefPrevention = new GriefPreventionHandler();
            File cSDataFolder = Bukkit.getServer().getPluginManager().getPlugin("ChestShop").getDataFolder();
            String cSUserDBFile = cSDataFolder.getAbsolutePath() + File.separator + "users.db";

            String bFCUsersDBFile = this.getDataFolder().getAbsolutePath() + File.separator + "usersCopied.db";
            if (!Files.exists(Paths.get(bFCUsersDBFile))) {
                try {
                    Files.copy(Paths.get(cSUserDBFile), Paths.get(bFCUsersDBFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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

    private OfflinePlayer getOfflinePlayer(Player exempt, String name) {
        boolean isExecutor = false;

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.getName().toLowerCase().equals(name.toLowerCase().trim())) {
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
                //If no arguments were used in the command
                if (args.length <= 0) {
                    cs.sendMessage(settings.banFromClaimUsage.replace("{COMMAND}", alias));
                    return true;
                }
                //If arguments were used in the command
                else {
                    //Replace with OfflinePlayer
                    OfflinePlayer target = this.getOfflinePlayer(player, args[0]);
                    if (target != null && target.isOnline()) {
                        Player targetPlayer = target.getPlayer();
                        if (targetPlayer != null && player.canSee(targetPlayer)) {
                            if (target == player) {
                                cs.sendMessage(settings.cantBanSelf);
                                return true;
                            } else {

                                /*String noBanReason = griefPrevention.noBanReason(player, targetPlayer);
                                if (noBanReason != null) {
                                    player.sendMessage(noBanReason);
                                    return true;
                                } else { */

                                    //If they should be warped to spawn
                                    if (griefPrevention.shouldWarpToSpawn(targetPlayer.getLocation(), player)) {
                                        //TO-DO: Replace this with a mywarps plugin call for /warp spawn!!!
                                        Location spawn = new Location(player.getWorld(), (double) 0, (double) 64, (double) 0);
                                        targetPlayer.teleport(spawn);
                                        player.sendMessage(settings.banSuccessfulWithWarp.replace("{PLAYER}", target.getName()));
                                        return true;
                                    } else {
                                        // Store the ban in the database
                                        SQLiteTest sqLiteTest = new SQLiteTest();
                                        try {
                                            sqLiteTest.addBan(cs.getName(), ((Player) cs).getUniqueId().toString(), target.getName(), target.getUniqueId().toString());
                                        } catch (SQLException | ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                        player.sendMessage(settings.banSuccessful.replace("{PLAYER}", target.getName()));
                                        return true;
                                    }
                                //} commented out for the noBanReason and its following "else {"
                            }
                        }
                    } else if (target != null && !target.isOnline()) {
                        if (target == player) {
                            cs.sendMessage(settings.cantBanSelf);
                            return true;
                        } else {
                            SQLiteTest sqLiteTest = new SQLiteTest();
                            try {
                                sqLiteTest.addBan(cs.getName(), ((Player) cs).getUniqueId().toString(), target.getName(), target.getUniqueId().toString());
                            } catch (SQLException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            player.sendMessage(settings.banSuccessful.replace("{PLAYER}", target.getName()));
                            return true;
                        }
                    } /*else {
                        cs.sendMessage(settings.playerOffline.replace("{ARGUMENT}", args[0]));
                        return true;
                    } */
                }
            } else if (cmd.getName().equalsIgnoreCase("unbanfromclaim")) {
                if (args.length <= 0) {
                    player.sendMessage(settings.unbanFromClaimUsage.replace("{COMMAND}", alias));
                    return true;
                }
                UUID targUUID = null;
                CSSQLite cssqLite = new CSSQLite();
                try {
                    ResultSet rs = cssqLite.findUUID(args[0]);
                    if (rs.next()) {
                        targUUID = UUID.fromString(rs.getString(1));
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (targUUID == null) {
                    cs.sendMessage(settings.noSuchPlayer.replace("{PLAYER}", args[0]));
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(targUUID);


                SQLiteTest sqLiteTest = new SQLiteTest();
                try {
                    assert target != null;
                    sqLiteTest.removeBan(cs.getName(), ((Player) cs).getUniqueId().toString(), target.getName(), target.getUniqueId().toString());
                    cs.sendMessage(String.format("%s is no longer banned from your claims.", target.getName()));
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (cmd.getName().equalsIgnoreCase("claimbans")) {
                SQLiteTest sqLiteTest = new SQLiteTest();


                try {
                    ArrayList claimBansList = sqLiteTest.listTheirBans(player.getUniqueId().toString());
                    ResultSet bansList = (ResultSet) claimBansList.get(0);
                    ResultSet bansCount = (ResultSet) claimBansList.get(1);
                    StringBuilder builderOfBans = new StringBuilder();
                    String listOfBans;
                    String numBans = "0";

                    if (bansCount.next()) {
                        numBans = bansCount.getString(1);
                    }
                    if (numBans.equals("0")) {
                        //cs.sendMessage("You do not currently have any players banned from your claims.");
                        cs.sendMessage(settings.noClaimBans);
                        return true;
                    }

                    while (bansList.next()) {
                        builderOfBans.append(bansList.getString(1));
                        builderOfBans.append(", ");
                    }
                    listOfBans = builderOfBans.toString().replaceFirst(", $", "");

                    if (numBans.equals("1")) {
                        //cs.sendMessage(String.format("You currently have 1 player banned from your claims:\n%s", listOfBans));
                        cs.sendMessage(settings.oneClaimBan.replace("{LIST}", listOfBans));
                        return true;
                    }
                    if (!numBans.equals("0") && !numBans.equals("1")) {
                        //cs.sendMessage(String.format("You currently have %s players banned from your claims:\n%s", numBans, listOfBans));
                        cs.sendMessage(settings.multipleClaimBans.replace("{COUNT}", numBans).replace("{LIST}", listOfBans));
                        return true;
                    }

                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return true;
            }
        }
        return true;
    }
}