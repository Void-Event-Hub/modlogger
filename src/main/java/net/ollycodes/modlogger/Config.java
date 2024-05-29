package net.ollycodes.modlogger;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public List<String> bannedMods = new ArrayList<>();
    public List<String> defaultMods = new ArrayList<>();
    public List<String> ignoredMods = new ArrayList<>();
    public List<String> requiredMods = new ArrayList<>();
    public boolean matchExactModName = false;
    public int commandPermissionLevel = 4;
    public int bypassKickPermissionLevel = 4;
    public List<String> playerWhitelist = new ArrayList<>();

    public WebhookConfig webhook = new WebhookConfig();
    public KickConfig kick = new KickConfig();

    public static class WebhookConfig {
        public String discordWebhook = "";
        public boolean onBanned = true;
        public boolean onAdded = true;
        public boolean onDefault = true;
        public boolean onRequired = true;
    }

    public static class KickConfig {
        public boolean onBanned = false;
        public boolean onAdded = false;
        public boolean onRequired = false;
        public boolean showBannedMods = true;
        public boolean showAddedMods = true;
        public boolean showRequiredMods = true;
        public String bannedMessage = "You have been kicked for using banned mods. Please remove them and rejoin.";
        public String addedMessage = "You have been kicked for using additional mods. Please remove them and rejoin.";
        public String requiredMessage = "You have been kicked for removing required mods. Please re-add them and rejoin.";
        public String bannedMessageWithMods = "You have been kicked for using the following banned mods:\n%s\nPlease remove them and rejoin.";
        public String addedMessageWithMods = "You have been kicked for using the following additional mods:\n%s\nPlease remove them and rejoin.";
        public String requiredMessageWithMods = "You have been kicked for removing the following required mods:\n%s\nPlease re-add them and rejoin.";
    }
}
