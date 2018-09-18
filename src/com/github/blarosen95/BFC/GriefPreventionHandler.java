package com.github.blarosen95.BFC;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.*;
import org.bukkit.entity.Player;

class GriefPreventionHandler {
    private final DataStore dataStore;

    GriefPreventionHandler() {
        this.dataStore = GriefPrevention.instance.dataStore;
    }

    boolean ignoringClaims(Player player) {
        return dataStore.getPlayerData(player.getUniqueId()).ignoreClaims;
    }

    Boolean shouldWarpToSpawn(Location targetLoc, OfflinePlayer player) {
        Claim claim = this.dataStore.getClaimAt(targetLoc, true, null);
        //If the target isn't in any claim
        if (claim == null) {
            return false;
        }
        //If the target is in a claim owned by the player banning them, this is true.
        return claim.ownerID.equals(player.getUniqueId());
    }
}