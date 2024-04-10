package net.ollycodes.modlogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerRecord {
    public String uuid;
    public List<PlayerConnection> connections = new ArrayList<>();

    public PlayerRecord(String uuid) {
        this.uuid = uuid;
    }

    public static class PlayerConnection {
        public Date timestamp;
        public List<String> mods;
        public String username;
    }
}

