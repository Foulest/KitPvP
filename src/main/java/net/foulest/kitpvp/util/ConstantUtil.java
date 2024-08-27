package net.foulest.kitpvp.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class for constants.
 *
 * @author Foulest
 */
@UtilityClass
public class ConstantUtil {

    /**
     * Constant for when a player has no permission.
     */
    public final String NO_PERMISSION = "&cNo permission.";

    /**
     * Constant for when a player is not found.
     */
    public final String PLAYER_NOT_FOUND = "&cPlayer not found.";

    /**
     * Constant for when a command is disabled.
     */
    public final String COMMAND_DISABLED = "&cThat command is disabled.";

    /**
     * Constant for when a player does not have enough coins.
     */
    public final String NOT_ENOUGH_COINS = "&cYou don't have enough coins.";

    /**
     * Constant for when a player needs to be in spawn to use a command.
     */
    public final String NOT_IN_SPAWN = "&cYou must be in spawn to use this command.";

    /**
     * Constant for when a player needs to be in the game to use a command.
     */
    public final String IN_GAME_ONLY = "&cOnly players can execute this command.";

    /**
     * Constant for when a tab completer is unable to be registered.
     */
    public final String UNABLE_TO_REGISTER_TAB_COMPLETER = "Unable to register tab completer: ";

    /**
     * Constant for when a player cannot use a command while combat tagged.
     */
    public final String COMBAT_TAGGED = "&cYou may not use this command while combat tagged.";

    /**
     * Constant for when a player does not have a kit selected.
     */
    public final String NO_KIT_SELECTED = "&cYou do not have a kit selected.";

    /**
     * Constant for when a player teleports to spawn.
     */
    public final String TELEPORTED_TO_SPAWN = "&aTeleported to spawn.";

    /**
     * Constant for when a player needs to be on the ground.
     */
    public final String NOT_ON_GROUND = "&cYou need to be on the ground.";

    /**
     * Constant for when a player cannot use their ability in spawn.
     */
    public final String ABILITY_IN_SPAWN = "&cYou can't use your ability in spawn.";
}
