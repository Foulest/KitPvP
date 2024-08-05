# KitPvP

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![CodeQL Badge](https://github.com/Foulest/KitPvP/actions/workflows/codeql.yml/badge.svg)](https://github.com/Foulest/KitPvP/actions/workflows/codeql.yml)
[![JitPack Badge](https://jitpack.io/v/Foulest/KitPvP.svg)](https://jitpack.io/#Foulest/KitPvP)
[![Downloads](https://img.shields.io/github/downloads/Foulest/KitPvP/total.svg)](https://github.com/Foulest/KitPvP/releases)

**KitPvP** is a fully-featured core plugin for the KitPvP gamemode.

## Features

### Kits and Customization

- **22 Unique Kits**: Choose from a diverse range of kits, each with distinct abilities and play styles.
- **Interactive GUIs**: Seamless interfaces for kit selection, purchasing, and enchantments.
- **Flexible Kit Management**: Customize kit prices, abilities, cooldowns, and toggle kits on/off as needed.

### Gameplay and Experience

- **Killstreak System**: Keep track of killstreaks and unlock rewards for consistent performance.
- **Combat Log Prevention**: Combat tagging and logging maintain fair play.
- **Bounty System**: Enhance the thrill with player-placed bounties.

### Advanced Features

- **Kit Enchanting**: Upgrade your kits with enchantments that expire upon death.
- **Diverse Healing Options**: Choose between soup-based and potion-based healing methods.
- **Integrated Economy**: In-game economy system supporting transactions and balances.

### Integration and Management

- **Database Integration**: Utilizes both SQLite and MariaDB for robust data management.
- **PlaceholderAPI Compatibility**: Enhance functionality using PlaceholderAPI.
- **WorldGuard Regions**: Natively integrates with WorldGuard to handle region management.

## Dependencies

- **[Spigot 1.8.9](https://papermc.io/downloads/all)**
- **[WorldEdit 6.1.9](https://dev.bukkit.org/projects/worldedit/files/2597538)**
- **[WorldGuard 6.1](https://dev.bukkit.org/projects/worldguard/files/881691)**
- **[PlaceholderAPI](https://spigotmc.org/resources/placeholderapi.6245)** *(optional)*

## Compiling

1. Clone the repository.
2. Open a command prompt/terminal to the repository directory.
3. Run `gradlew shadowJar` on Windows, or `./gradlew shadowJar` on macOS or Linux.
4. The built `KitPvP-X.X.X.jar` file will be in the `build/libs` folder.

## Download and Run

1. Download the latest version from the [releases page](https://github.com/Foulest/KitPvP/releases) or compile it
   yourself.
2. Make sure your server is running Spigot 1.8.8 or a public fork of it.
3. Place the `KitPvP-X.X.X.jar` file in your server's `plugins` folder.
4. Start or restart your server.

## Getting Help

For support or queries, please open an issue in the [Issues section](https://github.com/Foulest/KitPvP/issues).
