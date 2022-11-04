package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.kits.*;
import net.foulest.kitpvp.region.Regions;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.kits.Kit;
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

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Handles all kit abilities
 */
public class KitListener implements Listener {

    public static final Map<UUID, Collection<PotionEffect>> drainedEffects = new HashMap<>();
    public static final Map<UUID, Location> imprisonedPlayers = new HashMap<>();
    private static final Random RANDOM = new Random();

    @EventHandler
    public static void onBurrowerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Location> roomLocations = getRoomLocations(player.getLocation());

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Burrower
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.BRICK
                && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            for (Location loc : roomLocations) {
                if (loc.getBlock().getType() != Material.AIR) {
                    MessageUtil.messagePlayer(player, "&cThere's not enough space above you to burrow.");
                    playerData.setCooldown(new Burrower(), 5, true);
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
                @Override
                public void run() {
                    for (BlockState block : pendingRollback) {
                        rollback(block);
                    }
                }
            }.runTaskLater(KitPvP.instance, 140L);

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);
            playerData.setNoFall(true);
            playerData.setPendingNoFallRemoval(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onCactusHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player receiver = (Player) event.getEntity();
            PlayerData damagerData = PlayerData.getInstance(damager);
            PlayerData receiverData = PlayerData.getInstance(receiver);

            if (damagerData == null) {
                event.setCancelled(true);
                damager.kickPlayer("Disconnected");
                return;
            }

            if (receiverData == null) {
                event.setCancelled(true);
                receiver.kickPlayer("Disconnected");
                return;
            }

            if (damagerData.getKit() instanceof Cactus
                    && receiverData.getKit() != null
                    && !Regions.isInSafezone(damager.getLocation())
                    && !Regions.isInSafezone(receiver.getLocation())
                    && damager.getItemInHand().getType() == Material.CACTUS) {

                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0, false, false));
            }
        }
    }

    @EventHandler
    public static void onDragonAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Dragon
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.FIREBALL
                && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            player.playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, (Integer) 1);
            player.playEffect(player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, (Integer) 1);
            player.playSound(player.getEyeLocation(), Sound.GHAST_FIREBALL, 1.0f, 0.0f);

            for (Entity entity : player.getNearbyEntities(5, 3, 5)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    Player receiver = (Player) entity;
                    PlayerData receiverData = PlayerData.getInstance(receiver);

                    if (receiverData == null) {
                        event.setCancelled(true);
                        player.kickPlayer("Disconnected");
                        return;
                    }

                    if (player.hasLineOfSight(entity) && receiverData.getKit() != null
                            && !Regions.isInSafezone(receiver.getLocation())) {
                        receiver.damage(8, player);
                        receiver.setFireTicks(150);
                    }
                }
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);
        }
    }

    @EventHandler
    public static void onFishermanAbility(PlayerFishEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (event.getCaught() instanceof Player) {
            Player receiver = (Player) event.getCaught();
            PlayerData receiverData = PlayerData.getInstance(receiver);

            if (receiverData == null) {
                event.setCancelled(true);
                player.kickPlayer("Disconnected");
                return;
            }

            CombatLog.markForCombat(player, receiver);

            if (playerData.getKit() instanceof Fisherman
                    && event.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY)
                    && !playerData.hasCooldown(true)
                    && !Regions.isInSafezone(player.getLocation())) {

                if (receiverData.getKit() != null && !Regions.isInSafezone(receiver.getLocation())) {
                    MessageUtil.messagePlayer(player, "&aYour ability has been used.");
                    playerData.setCooldown(playerData.getKit(), 30, true);
                    imprisonedPlayers.remove(receiver.getUniqueId());
                    event.getCaught().teleport(player.getLocation());
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public static void onGhostMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Ghost
                && !player.isSneaking()
                && !Regions.isInSafezone(player.getLocation())) {

            for (Entity entity : player.getNearbyEntities(6, 3, 6)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    ((Player) entity).playSound(player.getLocation(), Sound.CHICKEN_WALK, 0.01f, 0.5f);
                }
            }
        }
    }

    @EventHandler
    public static void onTamerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        // TODO: make dogs better

        if (playerData.getKit() instanceof Tamer
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.BONE
                && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);
            ArrayList<Wolf> list = new ArrayList<>();

            for (int i = 0; i < 3; ++i) {
                Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
                wolf.setOwner(player);
                wolf.isAngry();
                wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
                list.add(wolf);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Wolf wolf : list) {
                        wolf.remove();
                    }
                }
            }.runTaskLater(KitPvP.instance, 200L);
        }
    }

    @EventHandler
    public static void onHulkAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Hulk
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.PISTON_STICKY_BASE
                && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no targets found nearby.");
                playerData.setCooldown(playerData.getKit(), 5, true);
                return;
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);

            player.getWorld().createExplosion(player.getLocation(), 0.0f, false);

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData == null) {
                    event.setCancelled(true);
                    player.kickPlayer("Disconnected");
                    return;
                }

                if (playerInListData.getKit() != null && !Regions.isInSafezone(playerInList.getLocation())) {
                    playerInList.damage(10, player);

                    playerInList.getWorld().createExplosion(playerInList.getLocation(), 0.0f, false);

                    Vector direction = playerInList.getEyeLocation().getDirection();
                    direction.multiply(-4);
                    direction.setY(1.0);
                    playerInList.setVelocity(direction);
                    return;
                }
            }
        }
    }

    @EventHandler
    public static void onImprisonerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Imprisoner
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.DISPENSER
                && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);
            player.launchProjectile(Snowball.class).setMetadata("prison", new FixedMetadataValue(KitPvP.instance, true));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onImprisonerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();

            if (snowball.getShooter() instanceof Player) {
                Player damager = (Player) snowball.getShooter();
                PlayerData damagerData = PlayerData.getInstance(damager);

                if (damagerData == null) {
                    event.setCancelled(true);
                    damager.kickPlayer("Disconnected");
                    return;
                }

                if (event.getEntity() instanceof Player) {
                    Player receiver = (Player) event.getEntity();
                    PlayerData receiverData = PlayerData.getInstance(damager);

                    if (receiverData == null) {
                        event.setCancelled(true);
                        receiver.kickPlayer("Disconnected");
                        return;
                    }

                    if (damagerData.getKit() instanceof Imprisoner
                            && snowball.hasMetadata("prison")
                            && !imprisonedPlayers.containsKey(receiver.getUniqueId())
                            && receiverData.getKit() != null
                            && !Regions.isInSafezone(receiver.getLocation())) {

                        List<Block> cageBlocks = getCageBlocks(receiver.getLocation().add(0.0, 9.0, 0.0));
                        for (Block cageBlock : cageBlocks) {
                            if (cageBlock.getType() != Material.AIR) {
                                MessageUtil.messagePlayer(damager, "&cThere's not enough space above the target.");
                                damagerData.setCooldown(damagerData.getKit(), 5, true);
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
                            @Override
                            public void run() {
                                imprisonedPlayers.remove(receiver.getUniqueId());

                                for (BlockState block : pendingRollback) {
                                    rollback(block);
                                }
                            }
                        }.runTaskLater(KitPvP.instance, 80L);
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onKangarooAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Entity entityPlayer = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Kangaroo
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.FIREWORK
                && !playerData.hasCooldown(true)
                && entityPlayer.isOnGround()
                && !Regions.isInSafezone(player.getLocation())) {

            Vector direction = player.getEyeLocation().getDirection();

            if (player.isSneaking()) {
                direction.setY(0.3);
                direction.multiply(2.5);
            } else {
                direction.setY(1.2);
            }

            player.setVelocity(direction);
            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 20, true);
        }
    }

    @EventHandler
    public static void onMageAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Mage
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.GLOWSTONE_DUST
                && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            if (drainedEffects.containsKey(player.getUniqueId())) {
                MessageUtil.messagePlayer(player, "&cAbility failed; your effects are still drained.");
                return;
            }

            int effect = RANDOM.nextInt(21);
            int amplifier = RANDOM.nextInt(3);
            int duration = Math.max(5, (RANDOM.nextInt(30) + 1)) * 20;

            if (PotionEffectType.getById(effect) == null
                    || PotionEffectType.getById(effect).getName() == null
                    || PotionEffectType.getById(effect).getName().equals("HUNGER")
                    || PotionEffectType.getById(effect).getName().equals("SATURATION")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier, false, false));
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.getById(effect), duration, amplifier, false, false));
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);
        }
    }

    // TODO: make monk swap sword as well
    // TODO: send message to target
    @EventHandler(ignoreCancelled = true)
    public static void onMonkAbility(PlayerInteractEntityEvent event) {
        Player damager = event.getPlayer();
        PlayerData damagerData = PlayerData.getInstance(damager);

        if (damagerData == null) {
            event.setCancelled(true);
            damager.kickPlayer("Disconnected");
            return;
        }

        if (event.getRightClicked() instanceof Player) {
            Player receiver = (Player) event.getRightClicked();
            PlayerData receiverData = PlayerData.getInstance(receiver);

            if (receiverData == null) {
                event.setCancelled(true);
                receiver.kickPlayer("Disconnected");
                return;
            }

            if (damagerData.getKit() instanceof Monk
                    && damager.getItemInHand().getType() == Material.BLAZE_ROD
                    && !damagerData.hasCooldown(true)
                    && receiverData.getKit() != null
                    && !Regions.isInSafezone(damager.getLocation())
                    && !Regions.isInSafezone(receiver.getLocation())) {

                int random = RANDOM.nextInt(9);
                int heldItemSlot = receiver.getInventory().getHeldItemSlot();
                ItemStack itemInHand = receiver.getItemInHand();

                receiver.getInventory().setItem(heldItemSlot, receiver.getInventory().getItem(random));
                receiver.getInventory().setItem(random, itemInHand);
                receiver.updateInventory();

                MessageUtil.messagePlayer(damager, "&aYour ability has been used.");
                damagerData.setCooldown(damagerData.getKit(), 30, true);
            }
        }
    }

    @EventHandler
    public static void onSpidermanAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Spiderman
                && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.WEB
                && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 15, true);
            player.launchProjectile(Snowball.class).setMetadata("spiderman", new FixedMetadataValue(KitPvP.instance, true));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onSpidermanHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();

            if (snowball.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) snowball.getShooter();
                Player receiver = (Player) event.getEntity();
                PlayerData damagerData = PlayerData.getInstance(damager);
                PlayerData receiverData = PlayerData.getInstance(receiver);

                if (damagerData == null) {
                    event.setCancelled(true);
                    damager.kickPlayer("Disconnected");
                    return;
                }

                if (receiverData == null) {
                    event.setCancelled(true);
                    receiver.kickPlayer("Disconnected");
                    return;
                }

                if (damagerData.getKit() instanceof Spiderman) {
                    if (receiverData.getKit() == null || Regions.isInSafezone(receiver.getLocation())) {
                        damager.playSound(damager.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(damager, "&cYou can't use your ability on players in spawn.");
                        damagerData.setCooldown(damagerData.getKit(), 15, true);
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
                            @Override
                            public void run() {
                                for (BlockState blockState : blockStates) {
                                    rollback(blockState);
                                }
                            }
                        }.runTaskLater(KitPvP.instance, 120L);

                        MessageUtil.messagePlayer(damager, "&aYour ability has been used.");
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onSummonerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Summoner && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.IRON_BLOCK && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            for (Entity entity : player.getNearbyEntities(15, 5, 15)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getKit(), 5, true);
                return;
            }

            IronGolem ironGolem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);

            ironGolem.setMetadata(player.getName(), new FixedMetadataValue(KitPvP.instance, true));
            ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 99999, 1, false, false));
            ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999, 50, false, false));

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData == null) {
                    event.setCancelled(true);
                    playerInList.kickPlayer("Disconnected");
                    return;
                }

                if (playerInListData.getKit() != null && !Regions.isInSafezone(playerInList.getLocation())) {
                    ironGolem.setTarget(playerInList);
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    ironGolem.remove();
                }
            }.runTaskLater(KitPvP.instance, 200L);

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);
        }
    }

    @EventHandler
    public static void onThorAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Thor && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.IRON_AXE && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getKit(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData == null) {
                    event.setCancelled(true);
                    playerInList.kickPlayer("Disconnected");
                    return;
                }

                if (playerInListData.getKit() != null && !Regions.isInSafezone(playerInList.getLocation())) {
                    player.getWorld().strikeLightningEffect(playerInList.getLocation());
                    playerInList.damage(10, player);
                    playerInList.setFireTicks(100);
                }
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);
        }
    }

    @EventHandler
    public static void onTimelordAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Timelord && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.WATCH && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getKit(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData == null) {
                    event.setCancelled(true);
                    playerInList.kickPlayer("Disconnected");
                    return;
                }

                if (playerInListData.getKit() != null && !Regions.isInSafezone(playerInList.getLocation())) {
                    playerInList.getWorld().playEffect(playerInList.getLocation(), Effect.STEP_SOUND, 152);
                    playerInList.getWorld().playEffect(playerInList.getLocation().add(0.0, 1.0, 0.0), Effect.STEP_SOUND, 152);
                    playerInList.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 10, false, false));
                    playerInList.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 254, false, false));
                    playerInList.playSound(playerInList.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
                }
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);
            player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1, 1);
        }
    }

    @EventHandler
    public static void onVampireAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();
        List<PotionEffect> playerEffects = new ArrayList<>();

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Vampire && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.REDSTONE && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    Player nearby = (Player) entity;
                    PlayerData nearbyData = PlayerData.getInstance(nearby);

                    if (nearbyData == null) {
                        event.setCancelled(true);
                        nearby.kickPlayer("Disconnected");
                        return;
                    }

                    if (nearbyData.getKit() != null && !Regions.isInSafezone(nearby.getLocation())) {
                        playerList.add((Player) entity);
                    }
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getKit(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                playerEffects.addAll(playerInList.getActivePotionEffects());
            }

            if (playerEffects.isEmpty()) {
                MessageUtil.messagePlayer(player, "&cAbility failed; no effects found to drain.");
                playerData.setCooldown(playerData.getKit(), 5, true);
                return;
            }

            for (Player listPlayer : playerList) {
                drainedEffects.put(listPlayer.getUniqueId(), listPlayer.getActivePotionEffects());
                MessageUtil.messagePlayer(listPlayer, "&cYour effects have been drained by a Vampire!");

                for (PotionEffect potionEffect : drainedEffects.get(listPlayer.getUniqueId())) {
                    if (potionEffect.getType() != PotionEffectType.INCREASE_DAMAGE) {
                        listPlayer.playSound(listPlayer.getLocation(), Sound.CAT_HISS, 1, 1);
                        listPlayer.removePotionEffect(potionEffect.getType());
                        player.addPotionEffect(potionEffect);
                        MessageUtil.messagePlayer(player, ("&aYou drained the " + potionEffect.getType().getName() + " effect from " + listPlayer.getName() + "."));
                    }
                }

                PlayerData listPlayerData = PlayerData.getInstance(listPlayer);

                if (listPlayerData == null) {
                    event.setCancelled(true);
                    listPlayer.kickPlayer("Disconnected");
                    return;
                }

                Kit currentKit = listPlayerData.getKit();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && !player.getActivePotionEffects().isEmpty()) {
                            for (PotionEffect effect : player.getActivePotionEffects()) {
                                player.removePotionEffect(effect.getType());
                            }
                            MessageUtil.messagePlayer(player, "&cYour drained effects were removed.");
                        }

                        if (listPlayer.isOnline() && !drainedEffects.get(listPlayer.getUniqueId()).isEmpty()
                                && listPlayerData.getKit() != null && currentKit != null
                                && currentKit == listPlayerData.getKit()) {
                            if (drainedEffects.get(listPlayer.getUniqueId()) != null) {
                                for (PotionEffect effect : drainedEffects.get(listPlayer.getUniqueId())) {
                                    listPlayer.addPotionEffect(effect);
                                }

                                MessageUtil.messagePlayer(listPlayer, "&aYour drained effects were restored.");
                            }
                        }

                        drainedEffects.remove(listPlayer.getUniqueId());
                    }
                }.runTaskLater(KitPvP.instance, 200L);
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), 30, true);
            player.playSound(player.getLocation(), Sound.CAT_HISS, 1, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void onVampireHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player receiver = (Player) event.getEntity();
            PlayerData damagerData = PlayerData.getInstance(damager);
            PlayerData receiverData = PlayerData.getInstance(receiver);

            if (damagerData == null) {
                event.setCancelled(true);
                damager.kickPlayer("Disconnected");
                return;
            }

            if (receiverData == null) {
                event.setCancelled(true);
                receiver.kickPlayer("Disconnected");
                return;
            }

            if (damagerData.getKit() instanceof Vampire && receiverData.getKit() != null
                    && !Regions.isInSafezone(damager.getLocation()) && !Regions.isInSafezone(receiver.getLocation())) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, false, false));
            }
        }
    }

    @EventHandler
    public static void onZenAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (playerData == null) {
            event.setCancelled(true);
            player.kickPlayer("Disconnected");
            return;
        }

        if (playerData.getKit() instanceof Zen && event.getAction().toString().contains("RIGHT")
                && player.getItemInHand().getType() == Material.SLIME_BALL && !playerData.hasCooldown(true)
                && !Regions.isInSafezone(player.getLocation())) {

            Player closest = null;
            double closestDistance = 0;

            for (Entity entity : player.getNearbyEntities(25, 25, 25)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getKit(), 5, true);
                return;
            }

            for (Player listPlayer : playerList) {
                PlayerData listPlayerData = PlayerData.getInstance(listPlayer);

                if (listPlayerData == null) {
                    event.setCancelled(true);
                    listPlayer.kickPlayer("Disconnected");
                    return;
                }

                if (listPlayerData.getKit() != null && !Regions.isInSafezone(listPlayer.getLocation())) {
                    double distance = listPlayer.getLocation().distanceSquared(player.getLocation());

                    if (closest == null || distance < closestDistance) {
                        closest = listPlayer;
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
                MessageUtil.messagePlayer(player, "&aYou teleported to " + closest.getDisplayName() + ".");
                playerData.setCooldown(playerData.getKit(), 30, true);
            }
        }
    }

    public static List<Block> getCageBlocks(Location location) {
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

    public static List<Location> getPlatform(Location location) {
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

    public static List<Location> getRoomLocations(Location location) {
        location.add(0.0, 9.0, 0.0);

        ArrayList<Location> list = new ArrayList<>(getPlatform(location));

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

        list.addAll(getPlatform(location.add(0.0, 1.0, 0.0)));
        return list;
    }

    public static List<Location> getSurroundingLocations(Location location) {
        ArrayList<Location> list = new ArrayList<>();

        list.add(location.clone().add(-1.0, 0.0, 0.0));
        list.add(location.clone().add(0.0, 0.0, 1.0));
        list.add(location.clone().add(0.0, 0.0, -1.0));
        list.add(location.clone().add(1.0, 0.0, 0.0));
        return list;
    }

    public static void rollback(BlockState blockState) {
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
