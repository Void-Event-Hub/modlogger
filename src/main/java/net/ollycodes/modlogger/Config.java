package net.ollycodes.modlogger;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public List<String> bannedMods = new ArrayList<>();
    public List<String> defaultMods = new ArrayList<>();
    public boolean matchExactModName = false;

    public WebhookConfig webhook = new WebhookConfig();
    public KickConfig kick = new KickConfig();

    public static class WebhookConfig {
        public String discordWebhook = "";
        public boolean onBanned = true;
        public boolean onAdded = true;
        public boolean onDefault = true;
    }

    public static class KickConfig {
        public boolean onBanned = false;
        public boolean onAdded = false;
        public boolean showBannedMods = false;
        public boolean showAddedMods = false;
        public String bannedMessage = "You have been kicked for using banned mods. Please remove them and rejoin.";
        public String addedMessage = "You have been kicked for using additional mods. Please remove them and rejoin.";
        public String bannedMessageWithMods = "You have been kicked for using the following banned mods: %s Please remove them and rejoin.";
        public String addedMessageWithMods = "You have been kicked for using the following additional mods: %s Please remove them and rejoin.";
    }
}
