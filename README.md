# Excrele's Hostile Mob Limiter (EHML)

## Overview
Excrele's Hostile Mob Limiter (EHML) is a modular Spigot plugin for Minecraft 1.21.5 designed to control hostile mob spawns and manage mob cleanup. It provides configurable features to limit mob spawning globally and per mob type, reduce spawns near low-health players, clean up mobs on player death, and manage settings via an in-game GUI or configuration file.

### Features
- **Global Hostile Mob Limit**: Caps the total number of hostile mobs in loaded chunks.
- **Per-Mob-Type Limits**: Sets specific spawn limits for individual hostile mob types.
- **Low-Health Spawn Delay**: Slows down spawns near players with health below a threshold.
- **Player Death Mob Cleanup**: Removes 50% of hostile mobs within a radius if too many are present on player death.
- **In-Game GUI**: Toggle features dynamically using `/ehmlgui`.
- **Reload Command**: Reload configuration with `/ehml reload`.
- **Configurable Toggles**: Enable/disable each feature via `config.yml` or GUI.

## Installation
1. **Build the Plugin**:
   - Clone the repository or download the source files.
   - Use Maven or Gradle to include Spigot 1.21.5 as a dependency. Example `pom.xml` dependency:
     ```xml
     <dependency>
         <groupId>org.spigotmc</groupId>
         <artifactId>spigot-api</artifactId>
         <version>1.21.5-R0.1-SNAPSHOT</version>
         <scope>provided</scope>
     </dependency>
     ```
   - Compile the project into a JAR file, ensuring `config.yml` and `plugin.yml` are in the `src/main/resources` folder.
2. **Deploy to Server**:
   - Place the compiled JAR in the `plugins` folder of your Spigot 1.21.5 server.
   - Start the server to generate `config.yml` in `plugins/EHML`.
3. **Configure**:
   - Edit `config.yml` in `plugins/EHML` to customize settings (see Configuration section).
   - Use `/ehml reload` to apply changes manually or `/ehmlgui` to toggle features in-game.

## Usage
### Commands
- **/ehml reload**:
  - Reloads `config.yml` without restarting the server.
  - Permission: `ehml.reload` (default: operators).
- **/ehmlgui**:
  - Opens an in-game GUI to toggle features.
  - Permission: `ehml.gui` (default: operators).

### Permissions
- `ehml.reload`: Allows use of `/ehml reload` (default: op).
- `ehml.gui`: Allows use of `/ehmlgui` (default: op).

### GUI
- Accessed via `/ehmlgui`.
- Features are represented by items:
  - **Global Limit**: Diamond Sword
  - **Per-Mob Limits**: Bow
  - **Low-Health Delay**: Potion
  - **Death Cleanup**: Player Head
- Click an item to toggle the feature (Enabled/Disabled).
- Changes are saved to `config.yml` and applied immediately.

## Configuration
The `config.yml` file in `plugins/EHML` controls all settings:
```yaml
# Global hostile mob limit settings
global-limit-enabled: true     # Enable/disable global hostile mob limit
global-hostile-limit: 70       # Global limit for all hostile mobs in loaded chunks

# Per-mob-type limits settings
mob-limits-enabled: true       # Enable/disable per-mob-type limits
mob-limits:
  zombie: 20
  skeleton: 15
  creeper: 10
  spider: 10
  enderman: 5

# Low-health spawn delay settings
low-health-delay-enabled: true # Enable/disable low-health spawn delay
low-health-threshold: 5.0      # Player health threshold (out of 20.0)
spawn-delay-radius: 30.0       # Radius to check for low-health players (blocks)
spawn-delay-chance: 0.5        # Chance to cancel spawn (0.0 to 1.0)

# Player death mob cleanup settings
death-cleanup-enabled: true    # Enable/disable mob cleanup on player death
death-mob-cleanup-radius: 10.0 # Radius to check for hostile mobs (blocks)
death-mob-threshold: 5         # Number of hostile mobs to trigger cleanup
death-mob-kill-percentage: 0.5 # Percentage of hostile mobs to kill (0.0 to 1.0)
```

## File Structure
```
project-root/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/excrele/ehml/
│   │   │       ├── EHML.java               # Main plugin class, orchestrates modules and commands
│   │   │       ├── ConfigManager.java      # Manages configuration loading, saving, and validation
│   │   │       ├── GlobalLimitModule.java  # Enforces global hostile mob limit
│   │   │       ├── PerMobLimitModule.java  # Enforces per-mob-type spawn limits
│   │   │       ├── LowHealthDelayModule.java # Slows spawns near low-health players
│   │   │       ├── DeathCleanupModule.java # Cleans up mobs on player death
│   │   │       ├── ReloadCommand.java      # Handles /ehml reload command
│   │   │       ├── GUIManager.java         # Manages in-game GUI for feature toggling
│   │   │       └── GUIListener.java        # Handles GUI click events
│   │   └── resources/
│   │       ├── plugin.yml            # Plugin metadata, commands, and permissions
│   │       └── config.yml            # Default configuration
├── README.md                         # Project documentation
└── pom.xml / build.gradle            # Build configuration (Maven/Gradle)
```

## Development Notes
- **Dependencies**: Requires Spigot 1.21.5 API.
- **Modularity**: Each feature is implemented as a separate module for maintainability.
- **Fixes Applied**:
  - `GlobalLimitModule`: Fixed `getServer` error by passing `JavaPlugin` instance.
  - `PerMobLimitModule`: Fixed private `plugin` access by passing `JavaPlugin` instance.
  - `GUIManager` and `GUIListener`: Fixed `SKULL_ITEM` and `setItemTag` errors using `PLAYER_HEAD` and persistent data container.
- **Logging**: Detailed logs for configuration, spawn events, and mob cleanup are output to the console.
- **Known Limitations**:
  - Mob counts in `PerMobLimitModule` may not update on mob death or despawn. Consider adding `EntityDeathEvent` and `EntityRemoveFromWorldEvent` listeners for accuracy.
- **Future Improvements**:
  - Expand GUI to edit numerical settings (e.g., `global-hostile-limit`).
  - Add tab completion for commands.
  - Implement per-world configuration support.

## License
This project is licensed under the MIT License.

## Contact
For support or contributions, please contact the author via GitHub or the Spigot forum.