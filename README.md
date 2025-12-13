# FishTracker

A Minecraft Fabric mod that automatically tracks your caught fish on CosmosMc and sends the data to a remote API for statistics and analysis.

## Features

- **Automatic Fish Tracking**: Parses chat messages to detect fish catches, including rarity levels.
- **New Entry Detection**: Tracks when you catch a fish for the first time.
- **Crab Detection**: Special handling for crab catches.
- **Encrypted Data Transmission**: Uses Fernet encryption to securely send data to the API.
- **Configurable GUI**: In-game configuration menu accessible via keybind (default: K).
- **Debug Mode**: Optional debug logging for troubleshooting.

## Installation

1. Download the latest release JAR file from the [Releases](https://github.com/PetarMc1/fish-tracker-mod/releases) page.
2. Place the JAR file in your `.minecraft/mods` folder.
3. Install required dependencies:
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [Cloth Config](https://modrinth.com/mod/cloth-config)
   - [PetarLib](https://github.com/PetarMc1/petarlib)

## Configuration

After installing the mod, launch Minecraft and join the server. Press the configured key (default: K) to open the configuration GUI.

### Required Settings:
- **Username**: Your  username
- **Password**: Your account password for API authentication
- **API Key**: Your API key for the tracking service
- **Endpoint URL**: The API endpoint URL (default: https://api.tracker.petarmc.com)

### Optional Settings:
- **Debug Mode**: Enable for detailed logging (useful for troubleshooting)

The configuration is saved to `fishtracker.properties` in your Minecraft directory.

## Usage

1. Configure the mod with your credentials and API details.
2. Join CosmosMc server.
3. Start fishing! The mod will automatically detect and log your catches.
4. Data is encrypted and sent to the configured API endpoint.

## Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/fish-tracker-mod.git
   cd fish-tracker-mod
   ```

2. Build with Gradle:
   ```bash
   ./gradlew build
   ```

3. The built JAR will be in `build/libs/`.

## Dependencies
- Fabric API
- PetarLib 
- Cloth Config API

## License

This mod is licensed under the [MIT License](LICENSE)

