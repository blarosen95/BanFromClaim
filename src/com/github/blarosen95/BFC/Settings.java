package com.github.blarosen95.BFC;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

class Settings {
    private final File settingsFile = new File(Main.getInstance().getDataFolder(), "Settings.yml");
    String playersOnly;
    String noPermission;
    String banFromClaimUsage;
    String settingsReloaded;
    String cantBanSelf;
    String playerOffline;
    String notInClaim;
    String notManager;
    String exempt;
    String cantBanTrusted;
    String noSafeLocation;
    String banSuccessful;

    Settings() {
        this.reloadSettings();
    }

    void reloadSettings() {
        //if the settingsFile does NOT exist
        if (!this.settingsFile.exists()) {
            Main.getInstance().saveResource("Settings.yml", true);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(this.settingsFile);
        this.playersOnly = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.PlayersOnly"));
        this.noPermission = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.NoPermission"));
        this.banFromClaimUsage = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.BanFromClaimUsage"));
        this.settingsReloaded = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.SettingsReloaded"));
        this.cantBanSelf = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.CantBanSelf"));
        this.playerOffline = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.PlayerOffline"));
        this.notInClaim = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.NotInClaim"));
        this.notManager = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.NotManager"));
        this.exempt = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.Exempt"));
        this.cantBanTrusted = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.CantBanTrusted"));
        this.noSafeLocation = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.NoSafeLocation"));
        this.banSuccessful = ChatColor.translateAlternateColorCodes('&', config.getString("Messages.BanSuccessful"));
    }
}