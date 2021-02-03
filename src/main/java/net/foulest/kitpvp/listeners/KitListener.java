package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.MiscUtils;
import net.foulest.kitpvp.utils.PlayerData;
import net.foulest.kitpvp.utils.Regions;
import net.foulest.kitpvp.utils.kits.Kit;
import net.foulest.kitpvp.utils.kits.KitManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class KitListener implements Listener {

    private static final KitListener instance = new KitListener();
    public final Map<UUID, Collection<PotionEffect>> drainedEffects = new HashMap<>();
    public final Map<UUID, Location> imprisonedPlayers = new HashMap<>();
    private final KitPvP kitPvP = KitPvP.getInstance();
    private final KitManager kitManager = KitManager.getInstance();

    public static KitListener getInstance() {
        return instance;
    }

    @EventHandler
    public void onBurrowerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Location> roomLocations = getRoomLocations(player.getLocation());

        if (kitManager.hasRequiredKit(player, "Burrower") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.BRICK && !playerData.hasCooldown(player, "Burrower")
                && !Regions.getInstance().isInSafezone(player)) {

            for (Location loc : roomLocations) {
                if (loc.getBlock().getType() != Material.AIR) {
                    MiscUtils.messagePlayer(player, "&cThere's not enough space above you to burrow.");
                    playerData.setCooldown("Burrower", kitManager.valueOf("Burrower").getDisplayItem().getType(), 5, true);
                    return;
                }
            }

            ArrayList<BlockState> pendingRollback = new ArrayList<>();

            for (Location location : roomLocations) {
                pendingRollback.add(location.getBlock().getState());
                location.getBlock().setType(Material.BRICK);
            }

            roomLocations.get(0).getBlock().setType(Material.GLOWSTONE);
            player.teleport(player.getLocation().add(0.0, 10.0, 0.0));

            new BukkitRunnable() {
                public void run() {
                    for (BlockState block : pendingRollback) {
                        rollback(block);
                    }
                }
            }.runTaskLater(kitPvP, 140L);

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Burrower", kitManager.valueOf("Burrower").getDisplayItem().getType(), 30, true);
            player.setMetadata("noFall", new FixedMetadataValue(kitPvP, true));
        }
    }

    // TODO: Fix the rollback system, some blocks aren't being rolled back
    @EventHandler
    public void onEskimoAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Entity entityPlayer = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Location> eskimoLocations = getEskimoLocations(player.getLocation());

        if (kitManager.hasRequiredKit(player, "Eskimo") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.PACKED_ICE && !playerData.hasCooldown(player, "Eskimo")
                && !Regions.getInstance().isInSafezone(player)) {

            if (!entityPlayer.isOnGround()) {
                MiscUtils.messagePlayer(player, "&cYou need to be on the ground.");
                playerData.setCooldown("Eskimo", kitManager.valueOf("Eskimo").getDisplayItem().getType(), 5, true);
                return;
            }

            ArrayList<BlockState> pendingRollback = new ArrayList<>();

            for (Location location : eskimoLocations) {
                pendingRollback.add(location.getBlock().getState());
                location.getBlock().setType(Material.PACKED_ICE);
            }

            new BukkitRunnable() {
                public void run() {
                    for (BlockState block : pendingRollback) {
                        rollback(block);
                    }
                }
            }.runTaskLater(kitPvP, 140L);

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Eskimo", kitManager.valueOf("Eskimo").getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCactusHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player receiver = (Player) event.getEntity();
            PlayerData damagerData = PlayerData.getInstance(damager);
            PlayerData receiverData = PlayerData.getInstance(receiver);

            if (damagerData.getKit().getName().equals("Cactus") && receiverData.hasKit()
                    && !Regions.getInstance().isInSafezone(damager)
                    && !Regions.getInstance().isInSafezone(receiver)) {
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0, false, false));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDragonAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Dragon") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.FIREBALL && !playerData.hasCooldown(player, "Dragon")
                && !Regions.getInstance().isInSafezone(player)) {

            player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            player.playEffect(player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            player.playSound(player.getEyeLocation(), Sound.GHAST_FIREBALL, 1.0f, 0.0f);

            for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    Player receiver = (Player) entity;
                    PlayerData receiverData = PlayerData.getInstance(receiver);

                    if (player.hasLineOfSight(entity) && receiverData.hasKit()
                            && !Regions.getInstance().isInSafezone(receiver)) {
                        receiver.damage(8, player);
                        receiver.setFireTicks(150);
                    }
                }
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Dragon", playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onFishermanAbility(PlayerFishEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Fisherman") && event.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY)
                && !playerData.hasCooldown(player, "Fisherman") && !Regions.getInstance().isInSafezone(player)) {

            if (event.getCaught() instanceof Player) {
                Player receiver = (Player) event.getCaught();
                PlayerData receiverData = PlayerData.getInstance(receiver);

                if (receiverData.hasKit() && !Regions.getInstance().isInSafezone(receiver)) {
                    MiscUtils.messagePlayer(player, "&aYour ability has been used.");
                    playerData.setCooldown("Fisherman", playerData.getKit().getDisplayItem().getType(), 30, true);
                    imprisonedPlayers.remove(receiver.getUniqueId());
                    event.getCaught().teleport(player.getLocation());
                } else {
                    event.setCancelled(true);
                }

            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onGhostMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        double deltaY = event.getTo().getY() - event.getFrom().getY();
        double deltaXZ = Math.hypot(event.getTo().getX() - event.getFrom().getX(), event.getTo().getZ() - event.getFrom().getZ());
        boolean playerMoved = (deltaXZ > 0.05 || Math.abs(deltaY) > 0.05);

        if (!playerMoved) {
            return;
        }

        if (kitManager.hasRequiredKit(player, "Ghost") && !player.isSneaking()
                && !Regions.getInstance().isInSafezone(player)) {
            for (Entity entity : player.getNearbyEntities(6, 3, 6)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    ((Player) entity).playSound(player.getLocation(), Sound.CHICKEN_WALK, 0.01f, 0.5f);
                }
            }
        }
    }

    @EventHandler
    public void onTamerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Tamer") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.BONE && !playerData.hasCooldown(player, "Tamer")
                && !Regions.getInstance().isInSafezone(player)) {

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Tamer", playerData.getKit().getDisplayItem().getType(), 30, true);
            ArrayList<Wolf> list = new ArrayList<>();

            for (int i = 0; i < 3; ++i) {
                Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
                wolf.setOwner(player);
                wolf.isAngry();
                wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
                list.add(wolf);
            }

            new BukkitRunnable() {
                public void run() {
                    for (Wolf wolf : list) {
                        wolf.remove();
                    }
                }
            }.runTaskLater(kitPvP, 200L);
        }
    }

    @EventHandler
    public void onHulkAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Hulk") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.PISTON_STICKY_BASE
                && !playerData.hasCooldown(player, "Hulk") && !Regions.getInstance().isInSafezone(player)) {

            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown("Hulk", playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            player.getWorld().createExplosion(player.getLocation(), 0.0f, false);

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData.hasKit() && !Regions.getInstance().isInSafezone(playerInList)) {
                    playerInList.getWorld().createExplosion(playerInList.getLocation(), 0.0f, false);
                    playerInList.damage(10, player);

                    Vector direction = playerInList.getEyeLocation().getDirection();
                    direction.multiply(-4);
                    direction.setY(1.0);
                    playerInList.setVelocity(direction);
                }
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Hulk", playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onImprisonerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Imprisoner") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.DISPENSER && !playerData.hasCooldown(player, "Imprisoner")
                && !Regions.getInstance().isInSafezone(player)) {

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Imprisoner", playerData.getKit().getDisplayItem().getType(), 30, true);
            player.launchProjectile(Snowball.class).setMetadata("prison", new FixedMetadataValue(kitPvP, true));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onImprisonerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();

            if (snowball.getShooter() instanceof Player) {
                Player damager = (Player) snowball.getShooter();
                PlayerData damagerData = PlayerData.getInstance(damager);

                if (event.getEntity() instanceof Player) {
                    Player receiver = (Player) event.getEntity();
                    PlayerData receiverData = PlayerData.getInstance(damager);

                    if (kitManager.hasRequiredKit(damager, "Imprisoner") && snowball.hasMetadata("prison")
                            && !imprisonedPlayers.containsKey(receiver.getUniqueId()) && receiverData.hasKit()
                            && !Regions.getInstance().isInSafezone(receiver)) {

                        List<Block> cageBlocks = getCageBlocks(receiver.getLocation().add(0.0, 9.0, 0.0));
                        for (Block cageBlock : cageBlocks) {
                            if (cageBlock.getType() != Material.AIR) {
                                MiscUtils.messagePlayer(damager, "&cThere's not enough space above the target.");
                                damagerData.setCooldown("Imprisoner", damagerData.getKit().getDisplayItem().getType(), 5, true);
                                return;
                            }
                        }

                        ArrayList<BlockState> pendingRollback = new ArrayList<>();

                        for (Block block : cageBlocks) {
                            pendingRollback.add(block.getState());
                        }

                        cageBlocks.get(0).setType(Material.MOSSY_COBBLESTONE);
                        for (int i = 1; i < 9; ++i) {
                            cageBlocks.get(i).setType(Material.IRON_FENCE);
                        }
                        cageBlocks.get(9).setType(Material.MOSSY_COBBLESTONE);
                        cageBlocks.get(10).setType(Material.LAVA);

                        receiver.damage(4.0, damager);

                        Location prisonLoc = receiver.getLocation().add(0.0, 9.0, 0.0);
                        prisonLoc.setX(prisonLoc.getBlockX() + 0.5);
                        prisonLoc.setY(Math.floor(prisonLoc.getY()));
                        prisonLoc.setZ(prisonLoc.getBlockZ() + 0.5);
                        receiver.teleport(prisonLoc);

                        imprisonedPlayers.put(receiver.getUniqueId(), prisonLoc);

                        new BukkitRunnable() {
                            public void run() {
                                imprisonedPlayers.remove(receiver.getUniqueId());

                                for (BlockState block : pendingRollback) {
                                    rollback(block);
                                }
                            }
                        }.runTaskLater(kitPvP, 80L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onKangarooAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Entity entityPlayer = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Kangaroo") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.FIREWORK && !playerData.hasCooldown(player, "Kangaroo")
                && entityPlayer.isOnGround() && !Regions.getInstance().isInSafezone(player)) {

            Vector direction = player.getEyeLocation().getDirection();

            if (player.isSneaking()) {
                direction.setY(0.3);
                direction.multiply(2.5);
            } else {
                direction.setY(1.2);
            }

            player.setVelocity(direction);
            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Kangaroo", playerData.getKit().getDisplayItem().getType(), 20, true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onMageAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Mage") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.GLOWSTONE_DUST && !playerData.hasCooldown(player, "Mage")
                && !Regions.getInstance().isInSafezone(player)) {

            if (drainedEffects.containsKey(player.getUniqueId())) {
                MiscUtils.messagePlayer(player, "&cAbility failed; your effects are still drained.");
                return;
            }

            int effect = MiscUtils.random.nextInt(21);
            int amplifier = MiscUtils.random.nextInt(3);
            int duration = Math.max(5, (MiscUtils.random.nextInt(30) + 1)) * 20;

            if (PotionEffectType.getById(effect).getName().equals("HUNGER")
                    || PotionEffectType.getById(effect).getName().equals("SATURATION")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier, false, false));
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.getById(effect), duration, amplifier, false, false));
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Mage", playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMonkAbility(PlayerInteractEntityEvent event) {
        Player damager = event.getPlayer();
        PlayerData damagerData = PlayerData.getInstance(damager);

        if (event.getRightClicked() instanceof Player) {
            Player receiver = (Player) event.getRightClicked();
            PlayerData receiverData = PlayerData.getInstance(receiver);

            if (kitManager.hasRequiredKit(damager, "Monk")
                    && damager.getItemInHand().getType() == Material.BLAZE_ROD
                    && !damagerData.hasCooldown(damager, "Monk") && receiverData.hasKit()
                    && !Regions.getInstance().isInSafezone(damager) && !Regions.getInstance().isInSafezone(receiver)) {

                int random = MiscUtils.random.nextInt(9);
                int heldItemSlot = receiver.getInventory().getHeldItemSlot();
                ItemStack itemInHand = receiver.getItemInHand();

                receiver.getInventory().setItem(heldItemSlot, receiver.getInventory().getItem(random));
                receiver.getInventory().setItem(random, itemInHand);
                receiver.updateInventory();

                MiscUtils.messagePlayer(damager, "&aYour ability has been used.");
                damagerData.setCooldown("Monk", damagerData.getKit().getDisplayItem().getType(), 30, true);
            }
        }
    }

    @EventHandler
    public void onSpidermanAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Spiderman") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.WEB && !playerData.hasCooldown(player, "Spiderman")
                && !Regions.getInstance().isInSafezone(player)) {

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Spiderman", playerData.getKit().getDisplayItem().getType(), 15, true);
            player.launchProjectile(Snowball.class).setMetadata("spiderman", new FixedMetadataValue(kitPvP, true));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpidermanHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();

            if (snowball.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) snowball.getShooter();
                Player receiver = (Player) event.getEntity();
                PlayerData damagerData = PlayerData.getInstance(damager);
                PlayerData receiverData = PlayerData.getInstance(receiver);

                if (kitManager.hasRequiredKit(damager, "Spiderman")) {
                    if (!receiverData.hasKit() || !Regions.getInstance().isInSafezone(receiver)) {
                        damager.playSound(damager.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                        MiscUtils.messagePlayer(damager, "&cYou can't use your ability on players in spawn.");
                        damagerData.setCooldown("Spiderman", damagerData.getKit().getDisplayItem().getType(), 15, true);
                        return;
                    }

                    if (snowball.hasMetadata("spiderman")) {
                        ArrayList<BlockState> blockStates = new ArrayList<>();
                        Block block = receiver.getLocation().getBlock();

                        while (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.STATIONARY_LAVA) {
                            receiver.getLocation().add(0.0, 1.0, 0.0);
                            block = receiver.getLocation().getBlock();
                        }

                        for (Location loc : getSurroundingLocations(receiver.getLocation())) {
                            if (loc.getBlock().getType() == Material.WEB) {
                                continue;
                            }

                            for (Location loc2 : getSurroundingLocations(loc)) {
                                if (loc2.getBlock().getType() != Material.LADDER) {
                                    blockStates.add(loc.getBlock().getState());
                                }
                            }
                        }

                        if (block.getType() != Material.WEB) {
                            blockStates.add(block.getState());
                        }

                        for (BlockState blockState : blockStates) {
                            blockState.getBlock().setType(Material.WEB);
                        }

                        new BukkitRunnable() {
                            public void run() {
                                for (BlockState blockState : blockStates) {
                                    rollback(blockState);
                                }
                            }
                        }.runTaskLater(kitPvP, 120L);

                        MiscUtils.messagePlayer(damager, "&aYour ability has been used.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSummonerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Summoner") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.IRON_BLOCK && !playerData.hasCooldown(player, "Summoner")
                && !Regions.getInstance().isInSafezone(player)) {

            for (Entity entity : player.getNearbyEntities(15, 5, 15)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown("Summoner", playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            IronGolem ironGolem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);

            ironGolem.setMetadata(player.getName(), new FixedMetadataValue(kitPvP, true));
            ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 99999, 1, false, false));
            ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999, 50, false, false));

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData.hasKit() && !Regions.getInstance().isInSafezone(playerInList)) {
                    ironGolem.setTarget(playerInList);
                }
            }

            new BukkitRunnable() {
                public void run() {
                    ironGolem.remove();
                }
            }.runTaskLater(kitPvP, 200L);

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Summoner", playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onThorAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Thor") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.IRON_AXE && !playerData.hasCooldown(player, "Thor")
                && !Regions.getInstance().isInSafezone(player)) {

            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown("Thor", playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData.hasKit() && !Regions.getInstance().isInSafezone(playerInList)) {
                    player.getWorld().strikeLightningEffect(playerInList.getLocation());
                    playerInList.damage(12, player);
                    playerInList.setFireTicks(100);
                }
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Thor", playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onTimelordAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Timelord") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.WATCH && !playerData.hasCooldown(player, "Timelord")
                && !Regions.getInstance().isInSafezone(player)) {

            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown("Timelord", playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData.hasKit() && !Regions.getInstance().isInSafezone(playerInList)) {
                    playerInList.getWorld().playEffect(playerInList.getLocation(), Effect.STEP_SOUND, 152);
                    playerInList.getWorld().playEffect(playerInList.getLocation().add(0.0, 1.0, 0.0), Effect.STEP_SOUND, 152);
                    playerInList.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 10, false, false));
                    playerInList.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 254, false, false));
                    playerInList.playSound(playerInList.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
                }
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Timelord", playerData.getKit().getDisplayItem().getType(), 30, true);
            player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1, 1);
        }
    }

    @EventHandler
    public void onVampireAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();
        List<PotionEffect> playerEffects = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Vampire") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.REDSTONE && !playerData.hasCooldown(player, "Vampire")
                && !Regions.getInstance().isInSafezone(player)) {

            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    Player nearbyPlayer = (Player) entity;
                    PlayerData nearbyPlayerData = PlayerData.getInstance(nearbyPlayer);

                    if (nearbyPlayerData.hasKit() && !Regions.getInstance().isInSafezone(nearbyPlayer)) {
                        playerList.add((Player) entity);
                    }
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown("Vampire", playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                playerEffects.addAll(playerInList.getActivePotionEffects());
            }

            if (playerEffects.isEmpty()) {
                MiscUtils.messagePlayer(player, "&cAbility failed; no effects found to drain.");
                playerData.setCooldown("Vampire", playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                drainedEffects.put(playerInList.getUniqueId(), playerInList.getActivePotionEffects());
                MiscUtils.messagePlayer(playerInList, "&cYour effects have been drained by a Vampire!");

                for (PotionEffect potionEffect : drainedEffects.get(playerInList.getUniqueId())) {
                    if (potionEffect.getType() != PotionEffectType.INCREASE_DAMAGE) {
                        playerInList.playSound(playerInList.getLocation(), Sound.CAT_HISS, 1, 1);
                        playerInList.removePotionEffect(potionEffect.getType());
                        player.addPotionEffect(potionEffect);
                        MiscUtils.messagePlayer(player, ("&aYou drained the " + potionEffect.getType().getName() + " effect from " + playerInList.getName() + "."));
                    }
                }

                PlayerData playerDataInList = PlayerData.getInstance(playerInList);
                Kit currentKit = playerDataInList.getKit();

                new BukkitRunnable() {
                    public void run() {
                        if (player.isOnline() && !player.getActivePotionEffects().isEmpty()) {
                            for (PotionEffect effect : player.getActivePotionEffects()) {
                                player.removePotionEffect(effect.getType());
                            }
                            MiscUtils.messagePlayer(player, "&cYour drained effects were removed.");
                        }

                        if (playerInList.isOnline() && !drainedEffects.get(playerInList.getUniqueId()).isEmpty()
                                && playerDataInList.hasKit() && currentKit == playerDataInList.getKit()) {
                            for (PotionEffect effect : drainedEffects.get(playerInList.getUniqueId())) {
                                playerInList.addPotionEffect(effect);
                            }
                            MiscUtils.messagePlayer(playerInList, "&aYour drained effects were restored.");
                        }

                        drainedEffects.remove(playerInList.getUniqueId());
                    }
                }.runTaskLater(kitPvP, 200L);
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown("Vampire", playerData.getKit().getDisplayItem().getType(), 30, true);
            player.playSound(player.getLocation(), Sound.CAT_HISS, 1, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player receiver = (Player) event.getEntity();
            PlayerData damagerData = PlayerData.getInstance(damager);
            PlayerData receiverData = PlayerData.getInstance(receiver);

            if (damagerData.getKit().getName().equals("Vampire") && receiverData.hasKit()
                    && !Regions.getInstance().isInSafezone(damager) && !Regions.getInstance().isInSafezone(receiver)) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, false, false));
            }
        }
    }

    @EventHandler
    public void onZenAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Zen") && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.SLIME_BALL && !playerData.hasCooldown(player, "Zen")
                && !Regions.getInstance().isInSafezone(player)) {

            Player closest = null;
            double closestDistance = 0;

            for (Entity entity : player.getNearbyEntities(25, 25, 25)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown("Zen", playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData.hasKit() && !Regions.getInstance().isInSafezone(playerInList)) {
                    double distance = playerInList.getLocation().distanceSquared(player.getLocation());

                    if (closest == null || distance < closestDistance) {
                        closest = playerInList;
                        closestDistance = distance;
                    }
                }
            }

            if (closest != null) {
                imprisonedPlayers.remove(player.getUniqueId());
                player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 1);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                player.teleport(closest.getLocation());
                player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 1);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                closest.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));
                MiscUtils.messagePlayer(player, "&aYou teleported to " + closest.getDisplayName() + ".");
                playerData.setCooldown("Zen", playerData.getKit().getDisplayItem().getType(), 30, true);
            }
        }
    }

    public List<Block> getCageBlocks(Location location) {
        ArrayList<Block> list = new ArrayList<>();

        list.add(location.clone().add(0.0, -1.0, 0.0).getBlock());
        list.add(location.clone().add(-1.0, 0.0, 0.0).getBlock());
        list.add(location.clone().add(0.0, 0.0, 1.0).getBlock());
        list.add(location.clone().add(0.0, 0.0, -1.0).getBlock());
        list.add(location.clone().add(1.0, 0.0, 0.0).getBlock());
        list.add(location.clone().add(-1.0, 0.0, -1.0).getBlock());
        list.add(location.clone().add(-1.0, 0.0, 1.0).getBlock());
        list.add(location.clone().add(1.0, 0.0, -1.0).getBlock());
        list.add(location.clone().add(1.0, 0.0, 1.0).getBlock());
        list.add(location.clone().add(0.0, 2.0, 0.0).getBlock());
        list.add(location.getBlock());
        list.add(location.add(0.0, 1.0, 0.0).getBlock());
        return list;
    }

    public List<Location> getPlatform(Location location) {
        ArrayList<Location> list = new ArrayList<>();

        list.add(location.clone());
        list.add(location.clone().add(-1.0, 0.0, 0.0));
        list.add(location.clone().add(0.0, 0.0, -1.0));
        list.add(location.clone().add(1.0, 0.0, 0.0));
        list.add(location.clone().add(0.0, 0.0, 1.0));
        list.add(location.clone().add(-1.0, 0.0, -1.0));
        list.add(location.clone().add(1.0, 0.0, -1.0));
        list.add(location.clone().add(1.0, 0.0, 1.0));
        list.add(location.clone().add(-1.0, 0.0, 1.0));
        return list;
    }

    // TODO: Add all the locations correctly
    public List<Location> getEskimoLocations(Location location) {
        ArrayList<Location> list = new ArrayList<>();

        list.add(location.clone().add(-3.0, 0.0, 0.0));
        list.add(location.clone().add(-3.0, 0.0, 1.0));
        list.add(location.clone().add(-3.0, 0.0, -1.0));
        list.add(location.clone().add(3.0, 0.0, 0.0));
        list.add(location.clone().add(3.0, 0.0, 1.0));
        list.add(location.clone().add(3.0, 0.0, -1.0));
        list.add(location.clone().add(-3.0, 1.0, 0.0));
        list.add(location.clone().add(-3.0, 1.0, 1.0));
        list.add(location.clone().add(-3.0, 1.0, -1.0));
        list.add(location.clone().add(3.0, 1.0, 0.0));
        list.add(location.clone().add(3.0, 1.0, 1.0));
        list.add(location.clone().add(3.0, 1.0, -1.0));

        list.add(location.clone().add(-2.0, 0.0, 2.0));
        list.add(location.clone().add(-2.0, 0.0, -2.0));
        list.add(location.clone().add(2.0, 0.0, 2.0));
        list.add(location.clone().add(2.0, 0.0, -2.0));
        list.add(location.clone().add(-2.0, 1.0, 2.0));
        list.add(location.clone().add(-2.0, 1.0, -2.0));
        list.add(location.clone().add(2.0, 1.0, 2.0));
        list.add(location.clone().add(2.0, 1.0, -2.0));

        list.add(location.clone().add(0.0, 0.0, -3.0));
        list.add(location.clone().add(1.0, 0.0, -3.0));
        list.add(location.clone().add(-1.0, 0.0, -3.0));
        list.add(location.clone().add(0.0, 0.0, 3.0));
        list.add(location.clone().add(1.0, 0.0, 3.0));
        list.add(location.clone().add(-1.0, 0.0, 3.0));
        list.add(location.clone().add(0.0, 1.0, -3.0));
        list.add(location.clone().add(1.0, 1.0, -3.0));
        list.add(location.clone().add(-1.0, 1.0, -3.0));
        list.add(location.clone().add(0.0, 1.0, 3.0));
        list.add(location.clone().add(1.0, 1.0, 3.0));
        list.add(location.clone().add(-1.0, 1.0, 3.0));

        list.add(location.clone().add(-3.0, 1.0, 0.0));
        list.add(location.clone().add(0.0, 1.0, -3.0));
        list.add(location.clone().add(3.0, 1.0, 0.0));
        list.add(location.clone().add(0.0, 1.0, 3.0));

        list.add(location.clone().add(-2.0, 2.0, 0.0));
        list.add(location.clone().add(0.0, 2.0, -2.0));
        list.add(location.clone().add(2.0, 2.0, 0.0));
        list.add(location.clone().add(0.0, 2.0, 2.0));

        list.add(location.clone().add(-1.0, 3.0, 0.0));
        list.add(location.clone().add(-1.0, 3.0, 1.0));
        list.add(location.clone().add(-1.0, 3.0, -1.0));
        list.add(location.clone().add(0.0, 3.0, 0.0));
        list.add(location.clone().add(0.0, 3.0, 1.0));
        list.add(location.clone().add(0.0, 3.0, -1.0));
        list.add(location.clone().add(1.0, 3.0, 0.0));
        list.add(location.clone().add(1.0, 3.0, 1.0));
        list.add(location.clone().add(1.0, 3.0, -1.0));
        return list;
    }

    public List<Location> getRoomLocations(Location location) {
        location.add(0.0, 9.0, 0.0);

        ArrayList<Location> list = new ArrayList<>(this.getPlatform(location));

        for (int i = 0; i < 3; ++i) {
            location.add(0.0, 1.0, 0.0);
            list.add(location.clone().add(0.0, 0.0, -2.0));
            list.add(location.clone().add(0.0, 0.0, 2.0));
            list.add(location.clone().add(2.0, 0.0, 0.0));
            list.add(location.clone().add(-2.0, 0.0, 0.0));
            list.add(location.clone().add(-2.0, 0.0, 2.0));
            list.add(location.clone().add(-2.0, 0.0, -2.0));
            list.add(location.clone().add(2.0, 0.0, -2.0));
            list.add(location.clone().add(2.0, 0.0, 2.0));
            list.add(location.clone().add(1.0, 0.0, 2.0));
            list.add(location.clone().add(-1.0, 0.0, 2.0));
            list.add(location.clone().add(-2.0, 0.0, 1.0));
            list.add(location.clone().add(-2.0, 0.0, -1.0));
            list.add(location.clone().add(-1.0, 0.0, -2.0));
            list.add(location.clone().add(1.0, 0.0, -2.0));
            list.add(location.clone().add(2.0, 0.0, -1.0));
            list.add(location.clone().add(2.0, 0.0, 1.0));
        }

        list.addAll(this.getPlatform(location.add(0.0, 1.0, 0.0)));
        return list;
    }

    public List<Location> getSurroundingLocations(Location location) {
        ArrayList<Location> list = new ArrayList<>();

        list.add(location.clone().add(-1.0, 0.0, 0.0));
        list.add(location.clone().add(0.0, 0.0, 1.0));
        list.add(location.clone().add(0.0, 0.0, -1.0));
        list.add(location.clone().add(1.0, 0.0, 0.0));
        return list;
    }

    // TODO: Fix the rollback system, some blocks aren't being rolled back
    public void rollback(BlockState blockState) {
        if (blockState instanceof Sign) {
            Sign sign = (Sign) blockState;
            Location location = sign.getLocation();

            location.getWorld().getBlockAt(location).setType(blockState.getType());

            Sign sign2 = (Sign) location.getWorld().getBlockAt(location).getState();
            for (int i = 0; i < 4; ++i) {
                sign2.setLine(i, sign.getLines()[i]);
            }

            sign2.update(true);

        } else {
            blockState.update(true);
        }
    }
}
