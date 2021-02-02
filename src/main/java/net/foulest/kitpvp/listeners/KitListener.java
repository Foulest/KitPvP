package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.utils.KitUser;
import net.foulest.kitpvp.utils.MiscUtils;
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
        KitUser kitUser = KitUser.getInstance(player);
        List<Location> roomLocations = getRoomLocations(player.getLocation());

        if (kitManager.hasRequiredKit(player, "Burrower") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.BRICK
                && !kitUser.hasCooldown(player, "Burrower")) {

            for (Location loc : roomLocations) {
                if (loc.getBlock().getType() != Material.AIR) {
                    MiscUtils.messagePlayer(player, "&cThere's not enough space above you to burrow.");
                    kitUser.setCooldown("Burrower", kitManager.valueOf("Burrower").getDisplayItem().getType(), 5, true);
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

            Bukkit.getScheduler().scheduleSyncDelayedTask(kitPvP, () -> {
                for (BlockState block : pendingRollback) {
                    rollback(block);
                }
            }, 140);

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Burrower", kitManager.valueOf("Burrower").getDisplayItem().getType(), 30, true);
            player.setMetadata("noFall", new FixedMetadataValue(kitPvP, true));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCactusHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player receiver = (Player) event.getEntity();
            KitUser damagerUser = KitUser.getInstance(damager);
            KitUser receiverUser = KitUser.getInstance(receiver);

            if (damagerUser.getKit().getName().equals("Cactus") && receiverUser.hasKit()) {
                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0, false, false));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDragonAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Dragon") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.FIREBALL
                && !kitUser.hasCooldown(player, "Dragon")) {

            player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            player.playEffect(player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            player.playSound(player.getEyeLocation(), Sound.GHAST_FIREBALL, 1.0f, 0.0f);

            for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
                if (entity instanceof Player) {
                    Player receiver = (Player) entity;
                    KitUser receiverUser = KitUser.getInstance(receiver);

                    if (player.hasLineOfSight(entity) && !receiverUser.isInSafezone()) {
                        receiver.damage(1, player);
                        receiver.setHealth(receiver.getHealth() - 4);
                        receiver.setFireTicks(150);
                    }
                }
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Dragon", kitUser.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onFishermanAbility(PlayerFishEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Fisherman") && !kitUser.hasCooldown(player, "Fisherman")
                && event.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY)) {

            if (event.getCaught() instanceof Player) {
                Player receiver = (Player) event.getCaught();
                KitUser receiverUser = KitUser.getInstance(receiver);

                if (!receiverUser.isInSafezone()) {
                    MiscUtils.messagePlayer(player, "&aYour ability has been used.");
                    kitUser.setCooldown("Fisherman", kitUser.getKit().getDisplayItem().getType(), 30, true);
                    imprisonedPlayers.remove(receiver.getUniqueId());
                    event.getCaught().teleport(player.getLocation());
                } else {
                    kitUser.setCooldown("Fisherman", kitUser.getKit().getDisplayItem().getType(), 5, true);
                }

            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onGhostMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Ghost") && !player.isSneaking()
                && event.getTo() != event.getFrom() && !kitUser.isInSafezone()) {
            for (Entity entity : player.getNearbyEntities(6, 3, 6)) {
                if (entity instanceof Player) {
                    ((Player) entity).playSound(player.getLocation(), Sound.CHICKEN_WALK, 0.01f, 0.5f);
                }
            }
        }
    }

    @EventHandler
    public void onTamerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Tamer") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.BONE
                && !kitUser.hasCooldown(player, "Tamer")) {

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Tamer", kitUser.getKit().getDisplayItem().getType(), 30, true);
            ArrayList<Wolf> list = new ArrayList<>();

            for (int i = 0; i < 3; ++i) {
                Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
                wolf.setOwner(player);
                wolf.isAngry();
                list.add(wolf);
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(kitPvP, () -> {
                for (Wolf wolf : list) {
                    wolf.remove();
                }
            }, 200);
        }
    }

    @EventHandler
    public void onHulkAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Hulk") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.PISTON_STICKY_BASE
                && !kitUser.hasCooldown(player, "Hulk")) {

            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                kitUser.setCooldown("Hulk", kitUser.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            player.getWorld().createExplosion(player.getLocation(), 0.0f, false);

            for (Player playerInList : playerList) {
                KitUser playerInListUser = KitUser.getInstance(playerInList);

                if (!playerInListUser.isInSafezone()) {
                    playerInList.getWorld().createExplosion(playerInList.getLocation(), 0.0f, false);
                    playerInList.damage(10, player);

                    Vector direction = playerInList.getEyeLocation().getDirection();
                    direction.multiply(-4);
                    direction.setY(1.0);
                    playerInList.setVelocity(direction);
                }
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Hulk", kitUser.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onImprisonerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Imprisoner") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.DISPENSER
                && !kitUser.hasCooldown(player, "Imprisoner")) {

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Imprisoner", kitUser.getKit().getDisplayItem().getType(), 30, true);
            player.launchProjectile(Snowball.class).setMetadata("prison", new FixedMetadataValue(kitPvP, true));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onImprisonerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();

            if (snowball.getShooter() instanceof Player) {
                Player damager = (Player) snowball.getShooter();
                KitUser damagerUser = KitUser.getInstance(damager);

                if (event.getEntity() instanceof Player) {
                    Player receiver = (Player) event.getEntity();
                    KitUser receiverUser = KitUser.getInstance(damager);

                    if (kitManager.hasRequiredKit(damager, "Imprisoner") && snowball.hasMetadata("prison")
                            && !receiverUser.isInSafezone() && !imprisonedPlayers.containsKey(receiver.getUniqueId())) {

                        List<Block> cageBlocks = getCageBlocks(receiver.getLocation().add(0.0, 9.0, 0.0));
                        for (Block cageBlock : cageBlocks) {
                            if (cageBlock.getType() != Material.AIR) {
                                MiscUtils.messagePlayer(damager, "&cThere's not enough space above the target.");
                                damagerUser.setCooldown("Imprisoner", damagerUser.getKit().getDisplayItem().getType(), 5, true);
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

                        Bukkit.getScheduler().scheduleSyncDelayedTask(kitPvP, () -> {
                            imprisonedPlayers.remove(receiver.getUniqueId());
                            for (BlockState block : pendingRollback) {
                                rollback(block);
                            }
                        }, 80L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onKangarooAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Entity entityPlayer = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Kangaroo") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.FIREWORK
                && !kitUser.hasCooldown(player, "Kangaroo") && entityPlayer.isOnGround()) {

            Vector direction = player.getEyeLocation().getDirection();

            if (player.isSneaking()) {
                direction.setY(0.3);
                direction.multiply(2.5);
            } else {
                direction.setY(1.2);
            }

            player.setVelocity(direction);
            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Kangaroo", kitUser.getKit().getDisplayItem().getType(), 20, true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onMageAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Mage") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.GLOWSTONE_DUST
                && !kitUser.hasCooldown(player, "Mage")) {

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
            kitUser.setCooldown("Mage", kitUser.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMonkAbility(PlayerInteractEntityEvent event) {
        Player damager = event.getPlayer();
        KitUser damagerUser = KitUser.getInstance(damager);

        if (event.getRightClicked() instanceof Player) {
            Player receiver = (Player) event.getRightClicked();
            KitUser receiverUser = KitUser.getInstance(receiver);

            if (kitManager.hasRequiredKit(damager, "Monk") && !damagerUser.hasCooldown(damager, "Monk")
                    && !damagerUser.isInSafezone() && !receiverUser.isInSafezone() && receiverUser.hasKit()
                    && damager.getItemInHand().getType() == Material.BLAZE_ROD) {

                int random = MiscUtils.random.nextInt(9);
                int heldItemSlot = receiver.getInventory().getHeldItemSlot();
                ItemStack itemInHand = receiver.getItemInHand();

                receiver.getInventory().setItem(heldItemSlot, receiver.getInventory().getItem(random));
                receiver.getInventory().setItem(random, itemInHand);
                receiver.updateInventory();

                MiscUtils.messagePlayer(damager, "&aYour ability has been used.");
                damagerUser.setCooldown("Monk", damagerUser.getKit().getDisplayItem().getType(), 30, true);
            }
        }
    }

    @EventHandler
    public void onSpidermanAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);

        if (kitManager.hasRequiredKit(player, "Spiderman") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.WEB
                && !kitUser.hasCooldown(player, "Spiderman")) {

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Spiderman", kitUser.getKit().getDisplayItem().getType(), 15, true);
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
                KitUser damagerUser = KitUser.getInstance(damager);
                KitUser receiverUser = KitUser.getInstance(receiver);

                if (kitManager.hasRequiredKit(damager, "Spiderman")) {
                    if (!receiverUser.hasKit() || receiverUser.isInSafezone()) {
                        damager.playSound(damager.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                        MiscUtils.messagePlayer(damager, "&cYou can't use your ability on players in spawn.");
                        damagerUser.setCooldown("Spiderman", damagerUser.getKit().getDisplayItem().getType(), 15, true);
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

                        Bukkit.getScheduler().scheduleSyncDelayedTask(kitPvP, () -> {
                            for (BlockState blockState : blockStates) {
                                rollback(blockState);
                            }
                        }, 120);

                        MiscUtils.messagePlayer(damager, "&aYour ability has been used.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSummonerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Summoner") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.IRON_BLOCK
                && !kitUser.hasCooldown(player, "Summoner")) {

            for (Entity entity : player.getNearbyEntities(15, 5, 15)) {
                if (entity instanceof Player) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                kitUser.setCooldown("Summoner", kitUser.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            IronGolem ironGolem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);

            ironGolem.setMetadata(player.getName(), new FixedMetadataValue(kitPvP, true));
            ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 99999, 1, false, false));
            ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999, 50, false, false));

            for (Player playerInList : playerList) {
                KitUser playerInListUser = KitUser.getInstance(playerInList);

                if (!playerInListUser.isInSafezone()) {
                    ironGolem.setTarget(playerInList);
                }
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(kitPvP, ironGolem::remove, 200);

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Summoner", kitUser.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onThorAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Thor") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.IRON_AXE
                && !kitUser.hasCooldown(player, "Thor")) {

            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity instanceof Player) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                kitUser.setCooldown("Thor", kitUser.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                KitUser playerInListUser = KitUser.getInstance(playerInList);

                if (!playerInListUser.isInSafezone()) {
                    player.getWorld().strikeLightningEffect(playerInList.getLocation());
                    playerInList.damage(12, player);
                    playerInList.setFireTicks(100);
                }
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Thor", kitUser.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onTimelordAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Timelord") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.WATCH
                && !kitUser.hasCooldown(player, "Timelord")) {

            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity instanceof Player) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                kitUser.setCooldown("Timelord", kitUser.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                KitUser playerInListUser = KitUser.getInstance(playerInList);

                if (!playerInListUser.isInSafezone()) {
                    playerInList.getWorld().playEffect(playerInList.getLocation(), Effect.STEP_SOUND, 152);
                    playerInList.getWorld().playEffect(playerInList.getLocation().add(0.0, 1.0, 0.0), Effect.STEP_SOUND, 152);
                    playerInList.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 10, false, false));
                    playerInList.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 254, false, false));
                    playerInList.playSound(playerInList.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
                }
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Timelord", kitUser.getKit().getDisplayItem().getType(), 30, true);
            player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1, 1);
        }
    }

    @EventHandler
    public void onVampireAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);
        List<Player> playerList = new ArrayList<>();
        List<PotionEffect> playerEffects = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Vampire") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.REDSTONE
                && !kitUser.hasCooldown(player, "Vampire")) {

            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player) {
                    KitUser entityUser = KitUser.getInstance((Player) entity);

                    if (!entityUser.isInSafezone()) {
                        playerList.add((Player) entity);
                    }
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                kitUser.setCooldown("Vampire", kitUser.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                playerEffects.addAll(playerInList.getActivePotionEffects());
            }

            if (playerEffects.isEmpty()) {
                MiscUtils.messagePlayer(player, "&cAbility failed; no effects found to drain.");
                kitUser.setCooldown("Vampire", kitUser.getKit().getDisplayItem().getType(), 5, true);
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

                KitUser kitUserInList = KitUser.getInstance(playerInList);
                Kit currentKit = kitUserInList.getKit();

                Bukkit.getScheduler().runTaskLater(kitPvP, () -> {
                    if (!player.getActivePotionEffects().isEmpty()) {
                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }
                        MiscUtils.messagePlayer(player, "&cYour drained effects were removed.");
                    }

                    if (!drainedEffects.get(playerInList.getUniqueId()).isEmpty() && kitUserInList.hasKit()
                            && currentKit == kitUserInList.getKit()) {
                        for (PotionEffect effect : drainedEffects.get(playerInList.getUniqueId())) {
                            playerInList.addPotionEffect(effect);
                        }
                        MiscUtils.messagePlayer(playerInList, "&aYour drained effects were restored.");
                    }

                    drainedEffects.remove(playerInList.getUniqueId());
                }, 200L);
            }

            MiscUtils.messagePlayer(player, "&aYour ability has been used.");
            kitUser.setCooldown("Vampire", kitUser.getKit().getDisplayItem().getType(), 30, true);
            player.playSound(player.getLocation(), Sound.CAT_HISS, 1, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player receiver = (Player) event.getEntity();
            KitUser damagerUser = KitUser.getInstance(damager);
            KitUser receiverUser = KitUser.getInstance(receiver);

            if (damagerUser.getKit().getName().equals("Vampire") && receiverUser.hasKit()) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, false, false));
            }
        }
    }

    @EventHandler
    public void onZenAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitUser kitUser = KitUser.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (kitManager.hasRequiredKit(player, "Zen") && !kitUser.isInSafezone()
                && event.getAction().toString().contains("RIGHT") && player.getItemInHand().getType() == Material.SLIME_BALL
                && !kitUser.hasCooldown(player, "Zen")) {

            Player closest = null;
            double closestDistance = 0;

            for (Entity entity : player.getNearbyEntities(25, 25, 25)) {
                if (entity instanceof Player) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                MiscUtils.messagePlayer(player, "&cAbility failed; no players found nearby.");
                kitUser.setCooldown("Zen", kitUser.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                KitUser playerInListUser = KitUser.getInstance(playerInList);

                if (!playerInListUser.isInSafezone()) {
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
                kitUser.setCooldown("Zen", kitUser.getKit().getDisplayItem().getType(), 30, true);
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
