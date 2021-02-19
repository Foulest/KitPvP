package net.foulest.kitpvp.listeners;

import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.kits.*;
import net.foulest.kitpvp.utils.MessageUtil;
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

/**
 * @author Foulest
 * @created 02/18/2021
 * @project KitPvP
 */
public class KitListener implements Listener {

    protected static final Map<UUID, Collection<PotionEffect>> DRAINED_EFFECTS = new HashMap<>();
    protected static final Map<UUID, Location> IMPRISONED_PLAYERS = new HashMap<>();
    private static final String HUNGER_NAME = "HUNGER";
    private static final String SATURATION_NAME = "SATURATION";
    private static final String PRISON_METADATA = "prison";
    private static final String SPIDERMAN_METADATA = "spiderman";
    private static final String NO_FALL_METADATA = "noFall";
    private static final String RIGHT_CLICK = "RIGHT";
    private static final KitListener INSTANCE = new KitListener();
    private static final KitPvP KITPVP = KitPvP.getInstance();

    public static KitListener getInstance() {
        return INSTANCE;
    }

    @EventHandler
    public void onBurrowerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Location> roomLocations = getRoomLocations(player.getLocation());

        if (playerData.getKit() instanceof Burrower
                && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.BRICK
                && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            for (Location loc : roomLocations) {
                if (loc.getBlock().getType() != Material.AIR) {
                    MessageUtil.messagePlayer(player, "&cThere's not enough space above you to burrow.");
                    playerData.setCooldown(new Burrower(), new Burrower().getDisplayItem().getType(), 5, true);
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
            }.runTaskLater(KITPVP, 140L);

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
            player.setMetadata(NO_FALL_METADATA, new FixedMetadataValue(KITPVP, true));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCactusHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player receiver = (Player) event.getEntity();
            PlayerData damagerData = PlayerData.getInstance(damager);
            PlayerData receiverData = PlayerData.getInstance(receiver);

            if (damagerData.getKit() instanceof Cactus
                    && receiverData.hasKit()
                    && !Regions.getInstance().isInSafezone(damager)
                    && !Regions.getInstance().isInSafezone(receiver)
                    && damager.getItemInHand().getType() == Material.CACTUS) {

                receiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0, false, false));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onDragonAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData.getKit() instanceof Dragon
                && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.FIREBALL
                && !playerData.hasCooldown(true)
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

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onFishermanAbility(PlayerFishEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData.getKit() instanceof Fisherman
                && event.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY)
                && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            if (event.getCaught() instanceof Player) {
                Player receiver = (Player) event.getCaught();
                PlayerData receiverData = PlayerData.getInstance(receiver);

                if (receiverData.hasKit() && !Regions.getInstance().isInSafezone(receiver)) {
                    MessageUtil.messagePlayer(player, "&aYour ability has been used.");
                    playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
                    IMPRISONED_PLAYERS.remove(receiver.getUniqueId());
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
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData.getKit() instanceof Ghost
                && !player.isSneaking()
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

        if (playerData.getKit() instanceof Tamer
                && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.BONE
                && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
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
            }.runTaskLater(KITPVP, 200L);
        }
    }

    @EventHandler
    public void onHulkAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (playerData.getKit() instanceof Hulk
                && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.PISTON_STICKY_BASE
                && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no targets found nearby.");
                playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            player.getWorld().createExplosion(player.getLocation(), 0.0f, false);

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData.hasKit() && !Regions.getInstance().isInSafezone(playerInList)) {
                    playerInList.damage(10, player);

                    playerInList.getWorld().createExplosion(playerInList.getLocation(), 0.0f, false);

                    Vector direction = playerInList.getEyeLocation().getDirection();
                    direction.multiply(-4);
                    direction.setY(1.0);
                    playerInList.setVelocity(direction);
                    return;
                }
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onImprisonerAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData.getKit() instanceof Imprisoner
                && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.DISPENSER
                && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
            player.launchProjectile(Snowball.class).setMetadata(PRISON_METADATA, new FixedMetadataValue(KITPVP, true));
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

                    if (damagerData.getKit() instanceof Imprisoner
                            && snowball.hasMetadata(PRISON_METADATA)
                            && !IMPRISONED_PLAYERS.containsKey(receiver.getUniqueId())
                            && receiverData.hasKit()
                            && !Regions.getInstance().isInSafezone(receiver)) {

                        List<Block> cageBlocks = getCageBlocks(receiver.getLocation().add(0.0, 9.0, 0.0));
                        for (Block cageBlock : cageBlocks) {
                            if (cageBlock.getType() != Material.AIR) {
                                MessageUtil.messagePlayer(damager, "&cThere's not enough space above the target.");
                                damagerData.setCooldown(damagerData.getKit(), damagerData.getKit().getDisplayItem().getType(), 5, true);
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

                        IMPRISONED_PLAYERS.put(receiver.getUniqueId(), prisonLoc);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                IMPRISONED_PLAYERS.remove(receiver.getUniqueId());

                                for (BlockState block : pendingRollback) {
                                    rollback(block);
                                }
                            }
                        }.runTaskLater(KITPVP, 80L);
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

        if (playerData.getKit() instanceof Kangaroo
                && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.FIREWORK
                && !playerData.hasCooldown(true)
                && entityPlayer.isOnGround()
                && !Regions.getInstance().isInSafezone(player)) {

            Vector direction = player.getEyeLocation().getDirection();

            if (player.isSneaking()) {
                direction.setY(0.3);
                direction.multiply(2.5);
            } else {
                direction.setY(1.2);
            }

            player.setVelocity(direction);
            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 20, true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onMageAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData.getKit() instanceof Mage
                && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.GLOWSTONE_DUST
                && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            if (DRAINED_EFFECTS.containsKey(player.getUniqueId())) {
                MessageUtil.messagePlayer(player, "&cAbility failed; your effects are still drained.");
                return;
            }

            int effect = MessageUtil.RANDOM.nextInt(21);
            int amplifier = MessageUtil.RANDOM.nextInt(3);
            int duration = Math.max(5, (MessageUtil.RANDOM.nextInt(30) + 1)) * 20;

            if ((HUNGER_NAME).equals(PotionEffectType.getById(effect).getName())
                    || (SATURATION_NAME).equals(PotionEffectType.getById(effect).getName())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier, false, false));
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.getById(effect), duration, amplifier, false, false));
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMonkAbility(PlayerInteractEntityEvent event) {
        Player damager = event.getPlayer();
        PlayerData damagerData = PlayerData.getInstance(damager);

        if (event.getRightClicked() instanceof Player) {
            Player receiver = (Player) event.getRightClicked();
            PlayerData receiverData = PlayerData.getInstance(receiver);

            if (damagerData.getKit() instanceof Monk
                    && damager.getItemInHand().getType() == Material.BLAZE_ROD
                    && !damagerData.hasCooldown(true)
                    && receiverData.hasKit()
                    && !Regions.getInstance().isInSafezone(damager)
                    && !Regions.getInstance().isInSafezone(receiver)) {

                int random = MessageUtil.RANDOM.nextInt(9);
                int heldItemSlot = receiver.getInventory().getHeldItemSlot();
                ItemStack itemInHand = receiver.getItemInHand();

                receiver.getInventory().setItem(heldItemSlot, receiver.getInventory().getItem(random));
                receiver.getInventory().setItem(random, itemInHand);
                receiver.updateInventory();

                MessageUtil.messagePlayer(damager, "&aYour ability has been used.");
                damagerData.setCooldown(damagerData.getKit(), damagerData.getKit().getDisplayItem().getType(), 30, true);
            }
        }
    }

    @EventHandler
    public void onSpidermanAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData.getKit() instanceof Spiderman
                && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.WEB
                && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 15, true);
            player.launchProjectile(Snowball.class).setMetadata(SPIDERMAN_METADATA, new FixedMetadataValue(KITPVP, true));
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

                if (damagerData.getKit() instanceof Spiderman) {
                    if (!receiverData.hasKit() || Regions.getInstance().isInSafezone(receiver)) {
                        damager.playSound(damager.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                        MessageUtil.messagePlayer(damager, "&cYou can't use your ability on players in spawn.");
                        damagerData.setCooldown(damagerData.getKit(), damagerData.getKit().getDisplayItem().getType(), 15, true);
                        return;
                    }

                    if (snowball.hasMetadata(SPIDERMAN_METADATA)) {
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
                        }.runTaskLater(KITPVP, 120L);

                        MessageUtil.messagePlayer(damager, "&aYour ability has been used.");
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

        if (playerData.getKit() instanceof Summoner && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.IRON_BLOCK && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            for (Entity entity : player.getNearbyEntities(15, 5, 15)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            IronGolem ironGolem = (IronGolem) player.getWorld().spawnEntity(player.getLocation(), EntityType.IRON_GOLEM);

            ironGolem.setMetadata(player.getName(), new FixedMetadataValue(KITPVP, true));
            ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 99999, 1, false, false));
            ironGolem.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999, 50, false, false));

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData.hasKit() && !Regions.getInstance().isInSafezone(playerInList)) {
                    ironGolem.setTarget(playerInList);
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    ironGolem.remove();
                }
            }.runTaskLater(KITPVP, 200L);

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onThorAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (playerData.getKit() instanceof Thor && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.IRON_AXE && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                PlayerData playerInListData = PlayerData.getInstance(playerInList);

                if (playerInListData.hasKit() && !Regions.getInstance().isInSafezone(playerInList)) {
                    player.getWorld().strikeLightningEffect(playerInList.getLocation());
                    playerInList.damage(10, player);
                    playerInList.setFireTicks(100);
                }
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
        }
    }

    @EventHandler
    public void onTimelordAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();

        if (playerData.getKit() instanceof Timelord && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.WATCH && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity instanceof Player && Bukkit.getOnlinePlayers().contains(entity)) {
                    playerList.add((Player) entity);
                }
            }

            if (playerList.isEmpty()) {
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 5, true);
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

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
            player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1, 1);
        }
    }

    @EventHandler
    public void onVampireAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getInstance(player);
        List<Player> playerList = new ArrayList<>();
        List<PotionEffect> playerEffects = new ArrayList<>();

        if (playerData.getKit() instanceof Vampire && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.REDSTONE && !playerData.hasCooldown(true)
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
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F);
                MessageUtil.messagePlayer(player, "&cAbility failed; no players found nearby.");
                playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                playerEffects.addAll(playerInList.getActivePotionEffects());
            }

            if (playerEffects.isEmpty()) {
                MessageUtil.messagePlayer(player, "&cAbility failed; no effects found to drain.");
                playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 5, true);
                return;
            }

            for (Player playerInList : playerList) {
                DRAINED_EFFECTS.put(playerInList.getUniqueId(), playerInList.getActivePotionEffects());
                MessageUtil.messagePlayer(playerInList, "&cYour effects have been drained by a Vampire!");

                for (PotionEffect potionEffect : DRAINED_EFFECTS.get(playerInList.getUniqueId())) {
                    if (potionEffect.getType() != PotionEffectType.INCREASE_DAMAGE) {
                        playerInList.playSound(playerInList.getLocation(), Sound.CAT_HISS, 1, 1);
                        playerInList.removePotionEffect(potionEffect.getType());
                        player.addPotionEffect(potionEffect);
                        MessageUtil.messagePlayer(player, ("&aYou drained the " + potionEffect.getType().getName() + " effect from " + playerInList.getName() + "."));
                    }
                }

                PlayerData playerDataInList = PlayerData.getInstance(playerInList);
                Kit currentKit = playerDataInList.getKit();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && !player.getActivePotionEffects().isEmpty()) {
                            for (PotionEffect effect : player.getActivePotionEffects()) {
                                player.removePotionEffect(effect.getType());
                            }
                            MessageUtil.messagePlayer(player, "&cYour drained effects were removed.");
                        }

                        if (playerInList.isOnline() && !DRAINED_EFFECTS.get(playerInList.getUniqueId()).isEmpty()
                                && playerDataInList.hasKit() && currentKit == playerDataInList.getKit()) {
                            for (PotionEffect effect : DRAINED_EFFECTS.get(playerInList.getUniqueId())) {
                                playerInList.addPotionEffect(effect);
                            }
                            MessageUtil.messagePlayer(playerInList, "&aYour drained effects were restored.");
                        }

                        DRAINED_EFFECTS.remove(playerInList.getUniqueId());
                    }
                }.runTaskLater(KITPVP, 200L);
            }

            MessageUtil.messagePlayer(player, "&aYour ability has been used.");
            playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
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

            if (damagerData.getKit() instanceof Vampire && receiverData.hasKit()
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

        if (playerData.getKit() instanceof Zen && event.getAction().toString().contains(RIGHT_CLICK)
                && player.getItemInHand().getType() == Material.SLIME_BALL && !playerData.hasCooldown(true)
                && !Regions.getInstance().isInSafezone(player)) {

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
                playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 5, true);
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
                IMPRISONED_PLAYERS.remove(player.getUniqueId());
                player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 1);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                player.teleport(closest.getLocation());
                player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 1);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                closest.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));
                MessageUtil.messagePlayer(player, "&aYou teleported to " + closest.getDisplayName() + ".");
                playerData.setCooldown(playerData.getKit(), playerData.getKit().getDisplayItem().getType(), 30, true);
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
