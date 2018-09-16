package com.github.blarosen95.BFC;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

class GriefPreventionHandler {
    private final DataStore dataStore;
    private final Settings settings;

    GriefPreventionHandler() {
        this.dataStore = GriefPrevention.instance.dataStore;
        this.settings = Main.getSettings();
    }

    String noBanReason(Player player, Player target) {
        Claim claim = this.dataStore.getClaimAt(player.getLocation(), true, null);
        if (claim == null) {
            return this.settings.notInClaim;
        } else if (target.hasPermission("banfromclaim.ban.exempt")) {
            return this.settings.exempt.replace("{PLAYER}", target.getName());
        } else {
            if (claim.isAdminClaim()) {
                if (!player.hasPermission("GriefPrevention.adminclaims") && claim.allowGrantPermission(player) != null) {
                    return this.settings.notManager;
                }
            } else if (!claim.ownerID.equals(player.getUniqueId()) && claim.allowGrantPermission(player) != null) {
                return this.settings.notManager;
            }

            return !claim.isAdminClaim() && (claim.allowAccess(target) == null || claim.allowGrantPermission(target) == null) ? this.settings.cantBanTrusted.replace("{PLAYER}", target.getName()) : null;
        }
    }

    Boolean shouldWarpToSpawn(OfflinePlayer target, Location targetLoc, OfflinePlayer player) {
        Claim claim = this.dataStore.getClaimAt(targetLoc, true, null);
        //If the target isn't in any claim
        if (claim == null) {
            return false;
        }
        //If the target is in a claim owned by the player banning them, this is true.
        return claim.ownerID == player.getUniqueId();
    }

    @Deprecated
    Location safeLocation(OfflinePlayer target, Location claimLoc) {
        Claim claim = this.dataStore.getClaimAt(claimLoc, true, null);
        if (claim == null) {
            return null;
        } else {

            Location lesser = claim.getLesserBoundaryCorner();
            Location greater = claim.getGreaterBoundaryCorner();
            int xMin = Math.min(lesser.getBlockX(), greater.getBlockX());
            int xMax = Math.max(lesser.getBlockX(), greater.getBlockX());
            int zMin = Math.min(lesser.getBlockZ(), greater.getBlockZ());
            int zMax = Math.max(lesser.getBlockZ(), greater.getBlockZ());

            for (int x = xMin - 75; x <= xMax + 75; ++x) {
                for (int z = zMin - 75; z <= zMax + 75; ++z) {
                    if (!this.inArea(x, z, xMin, xMax, zMin, zMax)) {
                        int y = this.getHighestIntAt(x, z, claimLoc.getWorld());
                        if (y != 0) {
                            Location loc = new Location(claimLoc.getWorld(), (double) x, (double) y, (double) z);
                            if (this.notAClaim(target, loc) && this.safeBlock(loc.getBlock()) && this.safeBlock(loc.getBlock().getRelative(BlockFace.DOWN))) {
                                return loc.add(0.5D, 0.0D, 0.5D);
                            }
                        }
                    }
                }
            }

            return null;
        }
    }


    private int getHighestIntAt(int x, int z, World world) {
        for (int y = world.getMaxHeight() - 2; y >= 0; --y) {
            if (world.getBlockAt(x, y, z).getType() == Material.BEDROCK && y >= 15) {
                y -= 2;
            } else if (world.getBlockAt(x, y, z).getType().isSolid() && !world.getBlockAt(x, y + 1, z).getType().isSolid()) {
                return y + 1;
            }
        }

        return 0;
    }

    private boolean notAClaim(OfflinePlayer player, Location loc) {
        Claim claim = this.dataStore.getClaimAt(loc, true, null);
        return claim == null || claim.ownerID.equals(player.getUniqueId());
    }

    private boolean safeBlock(Block block) {
        if (block.getBiome().name().contains("OCEAN")) {
            return false;
        } else {
            Material blockType = block.getType();
            return blockType != Material.LAVA && blockType != Material.CACTUS && blockType != Material.MAGMA_BLOCK && blockType != Material.FIRE && blockType != Material.WATER;
        }

    }

    private boolean inArea(int x, int z, int minx, int maxX, int minZ, int maxZ) {
        return x >= minx && x <= maxX && z >= minZ && z <= maxZ;
    }

    //This will be called by ChestShopHandler to determine if the sign is in a claim and, if so, who's claim
    String findBySign(Sign sign) {
        Location signLocation = sign.getLocation();
        //If there's no claim here, claim is going to be null!
        Claim claim = this.dataStore.getClaimAt(signLocation, true, null);
        if (claim != null) {
            //Let's return the UUID of claim's owner (as a String)
            return claim.ownerID.toString();
        }
        //If the line below runs, it's because the claim is null
        return null;
    }

}