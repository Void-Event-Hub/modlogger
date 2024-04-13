# ModLogger
A server side Forge mod to log mods installed on clients connecting to the server.

## Download
Downloads are available on the [releases page](https://github.com/olly007opm/ModLogger/releases).

## Config
The config file is located at `config/modlogger/config.json`. The following options are available:
- `bannedMods`: An array of mod IDs that are not allowed on the server.
- `defaultMods`: An array of mod IDs that are expected on the client.
- `matchExactModName`: Whether to match the exact mod name when checking for banned mods. If this is set to false, any mod ID that contains the banned mod ID will be detected.
- `reloadCommandPermissionLevel`: The permission level required to run the `/modlogger reload` command. Set to -1 to disable the command.
- `webhook`: The configuration for Discord webhooks.
  - `discordWebhook`: The URL of the Discord webhook to send messages to. If this is not set, Discord messages will not be sent.
  - `onBanned`: Whether to send a Discord message when a banned mod is detected.
  - `onAdded`: Whether to send a Discord message when a mod is detected that is not in the default mods list.
  - `onDefault`: Whether to send a Discord message when the mods list matches the default mods.
- `kick`: The configuration for kicking players.
  - `onBanned`: Whether to kick players when a banned mod is detected.
  - `onAdded`: Whether to kick players when a mod is detected that is not in the default mods list.
  - `showBannedMods`: Whether to show the banned mods in the kick message.
  - `showAddedMods`: Whether to show the added mods in the kick message.
  - `bannedMessage`: The message to show when a player is kicked for using banned mods.
  - `addedMessage`: The message to show when a player is kicked for using additional mods.
  - `bannedMessageWithMods`: The message to show when a player is kicked for using banned mods, with the list of banned mods.
  - `addedMessageWithMods`: The message to show when a player is kicked for using additional mods, with the list of additional mods.
  - `playerWhitelist`: An array of player usernames that are exempt from being kicked.

## Join Data
The mods list of every join attempt is logged under `config/modlogger/players/<uuid>.json`, where `<uuid>` is the UUID of the player.

## Credits
The client mod list detection mixin is based on ModBlacklist by pedruhb
https://github.com/pedruhb/ModBlacklist/tree/main
Licensed under the Creative Commons Zero v1.0 Universal
