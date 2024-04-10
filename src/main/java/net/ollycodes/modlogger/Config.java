package net.ollycodes.modlogger;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public List<String> bannedMods = new ArrayList<>();
    public List<String> defaultMods = new ArrayList<>();
    public String discordWebhook = "";
    public boolean webhookOnBanned = true;
    public boolean webhookOnAdded = true;
    public boolean webhookOnDefault = true;
}
