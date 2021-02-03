package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.*;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
import net.foulest.kitpvp.utils.kits.KitSelector;
import net.foulest.kitpvp.utils.kits.KitShop;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventListener implements Listener {

    // TODO: Cancel footstep sounds in lobby with packets @retrooper
    // TODO: Add vanish item to Staff Mode hotbar @Foulest

    private static final EventListener instance = new EventListener();
    private final Random random = new Random();
    private final Spawn spawn = Spawn.getInstance();
    private final KitPvP kitPvP = KitPvP.getInstance();
    private final MySQL mySQL = MySQL.getInstance();
    private final KitManager kitManager = KitManager.getInstance();
    private final CombatLog combatLog = CombatLog.getInstance();
    private final StaffMode staffMode = StaffMode.getInstance();

    public static EventListener getInstance() {
        return instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        player.setHealth(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setArmorContents(null);

        try {
            playerData.load();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (playerData.isInStaffMode()) {
            staffMode.toggleStaffMode(player, false, true);
        }

        // TODO: Broken, doesn't hide already vanished staff @retrooper
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasMetadata("vanished") && !player.hasPermission("kitpvp.staff")) {
                player.hidePlayer(p);
            }
        }

        for (Kit kit : kitManager.getKits()) {
            if (kit.getCost() == 0 && !playerData.ownsKit(kit)) {
                playerData.addOwnedKit(kit);
            }
        }

        spawn.teleport(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (player.hasMetadata("noFall")) {
            player.removeMetadata("noFall", kitPvP);
            playerData.setPendingNoFallRemoval(false);
        }

        if (combatLog.isInCombat(player)) {
            DeathListener.handleDeath(player, true);
        }

        playerData.clearCooldowns();
        playerData.resetKillStreak();
        playerData.saveAll();
        playerData.unload();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        event.getDrops().clear();
        event.setDroppedExp(0);

        DeathListener.handleDeath(player, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE && player.hasPermission("kitpvp.modify"))) {
            if (event.getItemDrop().getItemStack().getType() == Material.BOWL) {
                event.getItemDrop().remove();
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLadderBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (event.getBlock().getType() == Material.LADDER && !(player != null
                && player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (Regions.getInstance().isInSafezone(player)) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArrowShoot(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) arrow.getShooter();
                Player receiver = (Player) event.getEntity();

                if (receiver == damager) {
                    event.setCancelled(true);
                    return;
                }

                combatLog.markForCombat(damager, receiver);

                MiscUtils.messagePlayer(damager, "&c" + receiver.getName() + " &eis on &c"
                        + String.format("%.01f", Math.max(receiver.getHealth() - event.getFinalDamage(), 0.0)) + " &ehealth.");

                new BukkitRunnable() {
                    public void run() {
                        ((CraftPlayer) receiver).getHandle().getDataWatcher().watch(9, (byte) 0);
                    }
                }.runTaskLater(kitPvP, 100L);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    @EventHandler
    public void onPlayerCraft(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();

            // Prevents players from hitting their own Wolves.
            if (event.getEntity() instanceof Wolf) {
                Wolf wolf = (Wolf) event.getEntity();

                if (wolf.getOwner() == damager) {
                    event.setCancelled(true);
                    return;
                }
            }

            // Prevents players from hitting their own Iron Golems.
            if (event.getEntity() instanceof IronGolem && event.getEntity().hasMetadata(damager.getName())) {
                event.setCancelled(true);
                return;
            }

            // Combat tags players for Player on Player damage.
            if (event.getEntity() instanceof Player) {
                Player receiver = (Player) event.getEntity();

                combatLog.markForCombat(damager, receiver);
            }
        }

        // Combat tags players for Wolf damage.
        if (event.getDamager() instanceof Wolf) {
            Wolf wolf = (Wolf) event.getDamager();

            if (wolf.getOwner() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) wolf.getOwner();
                Player receiver = (Player) event.getEntity();

                combatLog.markForCombat(damager, receiver);
            }
        }

        // Combat tags players for Iron Golem damage.
        if (event.getDamager() instanceof IronGolem && event.getEntity() instanceof Player) {
            IronGolem golem = (IronGolem) event.getDamager();
            Player receiver = (Player) event.getEntity();

            for (Player damager : Bukkit.getOnlinePlayers()) {
                if (golem.hasMetadata(damager.getName())) {
                    combatLog.markForCombat(damager, receiver);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData playerData = PlayerData.getInstance(player);

            if (playerData.isTeleportingToSpawn()) {
                playerData.getTeleportingToSpawnTask().cancel();
                MiscUtils.messagePlayer(player, MiscUtils.colorize("&cTeleportation cancelled, you took damage."));
                playerData.setTeleportingToSpawn(null);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerData playerData = PlayerData.getInstance(player);

        // Prevents players in staff mode from moving inventory items.
        if (playerData.isInStaffMode()) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        // Fixes the weird hotbar swap bug.
        if (event.getAction() == InventoryAction.HOTBAR_SWAP
                && (!playerData.hasKit() || !(event.getClickedInventory().getName() == null
                || event.getClickedInventory().getName().equals("container.inventory")))
                && Regions.getInstance().isInSafezone(player)) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        // Null/meta check.
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()
                || event.getCurrentItem().getItemMeta() == null) {
            return;
        }

        // Prevents players in kits from moving their armor.
        if (playerData.hasKit() && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        if (event.getView().getTitle().contains("Kit Shop")) {
            event.setCancelled(true);
            player.updateInventory();

            if (event.getCurrentItem().getItemMeta() == null) {
                return;
            }

            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (itemName == null || itemName.trim().isEmpty()) {
                return;
            }

            if (kitManager.valueOf(itemName) != null) {
                Kit kit = kitManager.valueOf(itemName);

                if ((PlayerData.getInstance(player).getCoins() - kit.getCost()) < 0) {
                    MiscUtils.messagePlayer(player, "&cYou do not have enough coins to purchase " + kit.getName() + ".");
                    return;
                }

                PlayerData.getInstance(player).addOwnedKit(kit);
                PlayerData.getInstance(player).removeCoins(kit.getCost());
                mySQL.update("INSERT INTO PlayerKits (uuid, kitName) VALUES ( '" + player.getUniqueId().toString() + "', " + kit.getName() + " )");
                MiscUtils.messagePlayer(player, "&aYou purchased the " + kit.getName() + " kit for " + kit.getCost() + " coins.");
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
                player.closeInventory();
            }

        } else if (event.getView().getTitle().contains("Kit Selector")) {
            event.setCancelled(true);
            player.updateInventory();

            String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            if (itemName == null || itemName.trim().isEmpty()) {
                return;
            }

            switch (itemName) {
                case "-->":
                    new KitSelector(player, KitSelector.getPage(player) + 1);
                    break;

                case "<--":
                    new KitSelector(player, KitSelector.getPage(player) - 1);
                    break;

                case "Kit Shop":
                    new KitShop(player);
                    break;

                default:
                    Kit kit = kitManager.valueOf(itemName);

                    if (kit != null) {
                        kit.apply(player);
                        MiscUtils.messagePlayer(player, "&aYou equipped the " + kitManager.valueOf(itemName).getName() + " kit.");
                        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
                        player.updateInventory();
                        player.closeInventory();
                    }
                    break;
            }

        } else if (!playerData.hasKit()) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();

        event.setFoodLevel(20);
        player.setSaturation(20);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason spawnEvent = event.getSpawnReason();

        if (spawnEvent != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                && spawnEvent != CreatureSpawnEvent.SpawnReason.SPAWNER
                && spawnEvent != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        PlayerData playerData = PlayerData.getInstance(player);

        if (event.getAction().toString().contains("RIGHT") && block != null
                && block.getState() instanceof InventoryHolder) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction().toString().contains("RIGHT") && item != null) {
            switch (item.getType()) {
                case WEB:
                case BLAZE_ROD:
                case IRON_BLOCK:
                case SLIME_BALL:
                case DISPENSER:
                    break;

                case FISHING_ROD:
                    if (Regions.getInstance().isInSafezone(player)) {
                        event.setCancelled(true);
                    }
                    break;

                case POTION:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Use Potions")) {
                        event.setCancelled(true);
                        player.updateInventory();

                        playerData.setUsingSoup(false);
                        playerData.saveStats();
                        MiscUtils.messagePlayer(player, "&aYou are now using Potions.");

                        ItemStack healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUse Soup &7(Right Click)").build();
                        player.getInventory().setItem(6, healingItem);
                        break;
                    }

                    if (Regions.getInstance().isInSafezone(player)) {
                        event.setCancelled(true);
                    }
                    break;

                case MUSHROOM_SOUP:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Use Soup")) {
                        event.setCancelled(true);
                        player.updateInventory();

                        playerData.setUsingSoup(true);
                        playerData.saveStats();
                        MiscUtils.messagePlayer(player, "&aYou are now using Soup.");

                        ItemStack healingItem = new ItemBuilder(Material.POTION).durability(16421).name("&aUse Potions &7(Right Click)").build();
                        player.getInventory().setItem(6, healingItem);
                        break;
                    }

                    if (Regions.getInstance().isInSafezone(player)) {
                        event.setCancelled(true);
                        break;
                    }

                    if (player.getHealth() < player.getMaxHealth()) {
                        event.setCancelled(true);
                        player.setHealth(Math.min(player.getHealth() + 7, player.getMaxHealth()));
                        player.setItemInHand(new ItemBuilder(Material.BOWL).name("&fBowl").build());
                    }
                    break;

                case WATCH:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Previous Kit")
                            && playerData.hasPreviousKit() && !playerData.hasKit()) {
                        event.setCancelled(true);
                        playerData.getPreviousKit().apply(player);
                        MiscUtils.messagePlayer(player, "&aYou equipped the " + playerData.getPreviousKit().getName() + " kit.");
                        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
                        player.updateInventory();
                        player.closeInventory();
                    }
                    break;

                case SKULL_ITEM:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Your Stats")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        MiscUtils.messagePlayer(player, "");
                        MiscUtils.messagePlayer(player, " &aYour Stats");
                        MiscUtils.messagePlayer(player, " &fKills: &e" + playerData.getKills());
                        MiscUtils.messagePlayer(player, " &fDeaths: &e" + playerData.getDeaths());
                        MiscUtils.messagePlayer(player, " &fK/D Ratio: &e" + playerData.getKDRText());
                        MiscUtils.messagePlayer(player, "");
                        MiscUtils.messagePlayer(player, " &fStreak: &e" + playerData.getKillstreak());
                        MiscUtils.messagePlayer(player, " &fHighest Streak: &e" + playerData.getTopKillstreak());
                        MiscUtils.messagePlayer(player, "");
                        MiscUtils.messagePlayer(player, " &fLevel: &e" + playerData.getLevel() + " &7(" + playerData.getExpPercent() + "%)");
                        MiscUtils.messagePlayer(player, " &fCoins: &6" + playerData.getCoins());
                        MiscUtils.messagePlayer(player, " &fBounty: &cWIP");
                        MiscUtils.messagePlayer(player, "");
                        MiscUtils.messagePlayer(player, " &fEvents Won: &cWIP");
                        MiscUtils.messagePlayer(player, " &fMost Used Kit: &cWIP");
                        MiscUtils.messagePlayer(player, "");
                    }
                    break;

                case NETHER_STAR:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Kit Selector")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        new KitSelector(player);
                    }
                    break;

                case EYE_OF_ENDER:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Staff Mode")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        staffMode.toggleStaffMode(player, true, false);
                    }
                    break;

                case COMPASS:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Random Teleport")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        List<Player> potentialPlayers = new ArrayList<>();

                        if (Bukkit.getOnlinePlayers().size() > 1) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (!PlayerData.getInstance(p).isInStaffMode()) {
                                    potentialPlayers.add(p);
                                }
                            }
                        }

                        if (!potentialPlayers.isEmpty()) {
                            Player randomPlayer;

                            if (potentialPlayers.size() == 1) {
                                randomPlayer = potentialPlayers.get(0);
                            } else {
                                randomPlayer = potentialPlayers.get(random.nextInt(potentialPlayers.size() - 1) + 1);
                            }

                            player.teleport(randomPlayer);
                            MiscUtils.messagePlayer(player, "&eTeleporting to &a" + randomPlayer.getName() + "&e...");
                        } else {
                            MiscUtils.messagePlayer(player, "&cNot enough players online to use this feature.");
                        }
                    }
                    break;

                case BED:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Staff Mode")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        staffMode.toggleStaffMode(player, false, false);
                    }
                    break;

                case ENDER_CHEST:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Kit Shop")) {
                        event.setCancelled(true);
                        player.updateInventory();
                        new KitShop(player);
                    }
                    break;

                default:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName() != null
                            && item.getItemMeta().getDisplayName().toLowerCase().contains("right click")) {
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                    break;
            }
        }

        if (playerData.isInStaffMode()) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (!playerData.isLoaded()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        double deltaY = event.getTo().getY() - event.getFrom().getY();
        double deltaXZ = Math.hypot(event.getTo().getX() - event.getFrom().getX(), event.getTo().getZ() - event.getFrom().getZ());
        boolean playerMoved = (deltaXZ > 0.05 || Math.abs(deltaY) > 0.05);

        // Locks the player in place if they aren't loaded.
        if (!playerData.isLoaded()) {
            event.setTo(event.getFrom());
            return;
        }

        // Ignores rotation updates.
        if (!playerMoved) {
            return;
        }

        // Cancels pending teleportation when moving.
        if (playerData.isTeleportingToSpawn()) {
            MiscUtils.messagePlayer(player, MiscUtils.colorize("&cTeleportation cancelled, you moved."));
            playerData.getTeleportingToSpawnTask().cancel();
            playerData.setTeleportingToSpawn(null);
        }

        // Kills the player if they leave the map/fall into the void.
        if (player.getLocation().getY() < 0 && player.getGameMode() != GameMode.CREATIVE) {
            DeathListener.handleDeath(player, false);
            return;
        }

        // Equips the player's previously used kit when they leave spawn without a kit equipped.
        if (!playerData.hasKit() && player.getGameMode() != GameMode.CREATIVE && !player.isDead()
                && !Regions.getInstance().isInSafezone(player)) {
            playerData.getPreviousKit().apply(player);
            MiscUtils.messagePlayer(player, "&cYour spawn protection has been removed.");
            return;
        }

        // Denies entry into spawn while combat tagged.
        // Also heals the player whilst in a safe zone.
        if (Regions.getInstance().isInSafezone(player)) {
            if (combatLog.isInCombat(player)) {
                event.setTo(event.getFrom());
                MiscUtils.messagePlayer(player, "&cYou can't enter spawn while combat tagged.");
            } else {
                player.setHealth(20);
                player.setFireTicks(0);

                if (!player.hasMetadata("noFall")) {
                    player.setMetadata("noFall", new FixedMetadataValue(kitPvP, true));
                }
            }
        }

        // Removes the player's noFall metadata.
        if (player.hasMetadata("noFall") && !playerData.isPendingNoFallRemoval()
                && !Regions.getInstance().isInSafezone(player)) {
            playerData.setPendingNoFallRemoval(true);

            new BukkitRunnable() {
                public void run() {
                    playerData.setPendingNoFallRemoval(false);

                    if (player.hasMetadata("noFall") && !Regions.getInstance().isInSafezone(player)) {
                        player.removeMetadata("noFall", kitPvP);
                    }
                }
            }.runTaskLater(kitPvP, 30L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL
                && event.getEntity().hasMetadata("noFall")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            Player player = (Player) event.getEntity();

            DeathListener.handleDeath(player, false);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
        }
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        if (event.toThunderState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();

        if (!(event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL
                && player.getGameMode() == GameMode.CREATIVE
                && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        event.setAmount(0);
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();

        if (!(player.getGameMode() == GameMode.CREATIVE && player.hasPermission("kitpvp.modify"))) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onCropTrample(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.PHYSICAL && event.hasBlock()) {
            Block block = player.getWorld().getBlockAt(event.getClickedBlock().getLocation());

            if (block.getType() == Material.SOIL) {
                event.setCancelled(true);
            }
        }
    }
}
