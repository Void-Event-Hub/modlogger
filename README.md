# ModLogger
A server side Forge mod to log mods installed on clients connecting to the server.

## Download
Downloads are available on the [releases page](https://github.com/olly007opm/ModLogger/releases).

## Config
The config file is located at `config/modlogger/config.json`. The following options are available:
- `logFile`: The file to log the mod list to. This is a relative path from the server's root directory. Default is `mods.log`.
- `bannedMods`: An array of mod IDs that are not allowed on the server.
- `defaultMods`: An array of mod IDs that are expected on the client.
- `discordWebhook`: The URL of the Discord webhook to send messages to. If this is not set, Discord messages will not be sent.
- `webhookOnBanned`: Whether to send a Discord message when a banned mod is detected.
- `webhookOnAdded`: Whether to send a Discord message when a mod is detected that is not in the default mods list.
- `webhookOnDefault`: Whether to send a Discord message when the mods list matches the default mods.

## Join Data
The mods list of every join attempt is logged under `config/modlogger/players/<uuid>.json`, where `<uuid>` is the UUID of the player.

## Credits
The client mod list detection mixin is based on ModBlacklist by pedruhb
https://github.com/pedruhb/ModBlacklist/tree/main
Licensed under the Creative Commons Zero v1.0 Universal
