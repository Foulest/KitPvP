package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.*;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventListener implements Listener {

    private static final EventListener instance = new EventListener();
    private final Random random = new Random();
    private final Spawn spawn = Spawn.getInstance();
    private final KitPvP kitPvP = KitPvP.getInstance();
    private final MySQL mySQL = MySQL.getInstance();
    private final KitManager kitManager = KitManager.getInstance();
    private final CombatLog combatLog = CombatLog.getInstance();
    private final BrandListener brandListener = BrandListener.getInstance();
    private final StaffMode staffMode = StaffMode.getInstance();

    public static EventListener getInstance() {
        return instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        brandListener.addChannel(player, "MC|BRAND");

        player.setHealth(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setArmorContents(null);

        try {
            kitUser.load();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (kitUser.isInStaffMode()) {
            staffMode.toggleStaffMode(player, false, true);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasMetadata("vanished") && !player.hasPermission("kitpvp.staff")) {
                player.hidePlayer(p);
            }
        }

        for (Kit kit : kitManager.getKits()) {
            if (kit.getCost() == 0 && !kitUser.ownsKit(kit)) {
                kitUser.addOwnedKit(kit);
            }
        }

        spawn.teleport(player);
        player.getInventory().setHeldItemSlot(0);
        player.setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (player.hasMetadata("noFall")) {
            player.removeMetadata("noFall", kitPvP);
            kitUser.setPendingNoFallRemoval(false);
        }

        if (combatLog.isInCombat(player)) {
            if (combatLog.getLastAttacker(player) != null) {
                KitUser killer = KitUser.getInstance(combatLog.getLastAttacker(player));

                killer.getPlayer().playSound(killer.getPlayer().getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 0.0f);
                killer.addKill();
                killer.addKillstreak();
                MiscUtils.messagePlayer(killer.getPlayer(), "&a&lKILL! &7You killed &f" + player.getName() + "&7.");
            }

            combatLog.remove(player);
            kitUser.addDeath();
        }

        kitUser.clearCooldowns();
        kitUser.resetKillStreak();
        kitUser.saveAll();
        kitUser.unload();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        event.getDrops().clear();
        event.setDroppedExp(0);

        DeathListener.handleDeath(player);
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
            KitUser kitUser = KitUser.getInstance(player);

            if (kitUser.isInSafezone()) {
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

                Bukkit.getScheduler().runTaskLater(kitPvP, () -> ((CraftPlayer) receiver).getHandle().getDataWatcher().watch(9, (byte) 0), 100L);
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
            KitUser kitUser = KitUser.getInstance(player);

            if (kitUser.isTeleportingToSpawn()) {
                kitUser.getTeleportingToSpawnTask().cancel();
                MiscUtils.messagePlayer(player, MiscUtils.colorize("&cTeleportation cancelled."));
                kitUser.setTeleportingToSpawn(null);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        KitUser kitUser = KitUser.getInstance(player);

        // Fixes the weird hotbar swap bug.
        if (event.getAction() == InventoryAction.HOTBAR_SWAP && kitUser.isInSafezone()
                && (!kitUser.hasKit() || !(event.getClickedInventory().getName() == null
                || event.getClickedInventory().getName().equals("container.inventory")))) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        // ???
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()
                || event.getCurrentItem().getItemMeta() == null) {
            return;
        }

        // Prevents users in staff mode from moving inventory items.
        if (kitUser.isInStaffMode()) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        // Prevents users in kits from moving their armor.
        if (kitUser.hasKit() && event.getSlotType() == InventoryType.SlotType.ARMOR) {
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

                if ((KitUser.getInstance(player).getCoins() - kit.getCost()) < 0) {
                    MiscUtils.messagePlayer(player, "&cYou do not have enough coins to purchase " + kit.getName() + ".");
                    return;
                }

                KitUser.getInstance(player).addOwnedKit(kit);
                KitUser.getInstance(player).removeCoins(kit.getCost());
                mySQL.update("INSERT INTO PlayerKits (uuid, kitId) VALUES ( '" + player.getUniqueId().toString() + "', " + kit.getId() + " )");
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
                    if (kitManager.valueOf(itemName) != null) {
                        kitManager.valueOf(itemName).apply(player);
                        MiscUtils.messagePlayer(player, "&aYou equipped the " + kitManager.valueOf(itemName).getName() + " kit.");
                        player.playSound(player.getLocation(), Sound.SLIME_WALK, 1, 1);
                        player.updateInventory();
                        player.closeInventory();
                    }
                    break;
            }

        } else if (!kitUser.hasKit()) {
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
        KitUser kitUser = KitUser.getInstance(player);

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
                    if (kitUser.isInSafezone()) {
                        event.setCancelled(true);
                    }
                    break;

                case MUSHROOM_SOUP:
                    if (player.getHealth() < player.getMaxHealth()) {
                        event.setCancelled(true);
                        player.updateInventory();
                        player.setHealth(Math.min(player.getHealth() + 7, player.getMaxHealth()));
                        player.setItemInHand(new ItemBuilder(Material.BOWL).name("&fBowl").build());
                    }
                    break;

                case WATCH:
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Previous Kit")
                            && kitUser.hasPreviousKit() && !kitUser.hasKit()) {
                        event.setCancelled(true);
                        player.updateInventory();
                        kitUser.getPreviousKit().apply(player);
                        MiscUtils.messagePlayer(player, "&aYou equipped the " + kitUser.getPreviousKit().getName() + " kit.");
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
                        MiscUtils.messagePlayer(player, " &fKills: &e" + kitUser.getKills());
                        MiscUtils.messagePlayer(player, " &fDeaths: &e" + kitUser.getDeaths());
                        MiscUtils.messagePlayer(player, " &fK/D Ratio: &e" + kitUser.getKDRText());
                        MiscUtils.messagePlayer(player, "");
                        MiscUtils.messagePlayer(player, " &fStreak: &e" + kitUser.getKillstreak());
                        MiscUtils.messagePlayer(player, " &fHighest Streak: &e" + kitUser.getTopKillstreak());
                        MiscUtils.messagePlayer(player, "");
                        MiscUtils.messagePlayer(player, " &fLevel: &e" + kitUser.getLevel() + " &7(" + kitUser.getExpPercent() + "%)");
                        MiscUtils.messagePlayer(player, " &fCoins: &6" + kitUser.getCoins());
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
                                if (!KitUser.getInstance(p).isInStaffMode()) {
                                    potentialPlayers.add(p);
                                }
                            }
                        }

                        if (potentialPlayers.size() > 1) {
                            Player randomPlayer = potentialPlayers.get(random.nextInt(potentialPlayers.size() - 1) + 1);
                            player.teleport(randomPlayer);
                            MiscUtils.messagePlayer(player, "&aTeleporting to " + randomPlayer.getName() + "...");
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

        if (kitUser.isInStaffMode()) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (!kitUser.isLoaded()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        // Locks the user in place if they aren't loaded.
        if (!kitUser.isLoaded()) {
            event.setTo(event.getFrom());
            return;
        }

        if ((event.getTo().getX() != event.getFrom().getX()
                || event.getTo().getY() != event.getFrom().getY()
                || event.getTo().getZ() != event.getFrom().getZ()) && kitUser.isTeleportingToSpawn()) {
            MiscUtils.messagePlayer(player, MiscUtils.colorize("&cTeleportation cancelled, you moved."));
            kitUser.getTeleportingToSpawnTask().cancel();
            kitUser.setTeleportingToSpawn(null);
        }

        // Kills the player if they leave the map/fall into the void.
        if (!kitUser.isInRegion() && player.getGameMode() != GameMode.CREATIVE) {
            DeathListener.handleDeath(player);
            return;
        }

        // Teleports the player back to spawn if they leave without a kit.
        if (!kitUser.isInSafezone(event.getTo()) && !kitUser.hasKit() && !player.isDead()
                && kitUser.isLoaded() && !kitUser.isInStaffMode() && player.getGameMode() != GameMode.CREATIVE) {
            spawn.teleport(player);
            player.getInventory().setHeldItemSlot(0);
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            MiscUtils.messagePlayer(player, "&cYou can't leave spawn without selecting a kit.");
            return;
        }

        // Denies entry into spawn while combat tagged.
        // Also heals the user whilst in a safe zone.
        if (kitUser.isInSafezone(event.getTo())) {
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

        // Removes the user's noFall metadata.
        if (!kitUser.isInSafezone(event.getTo()) && player.hasMetadata("noFall") && !kitUser.isPendingNoFallRemoval()) {
            kitUser.setPendingNoFallRemoval(true);

            Bukkit.getScheduler().runTaskLater(kitPvP, () -> {
                kitUser.setPendingNoFallRemoval(false);

                if (player.hasMetadata("noFall") && !kitUser.isInSafezone()) {
                    player.removeMetadata("noFall", kitPvP);
                }
            }, 30L);
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

            DeathListener.handleDeath(player);
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
