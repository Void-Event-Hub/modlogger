package net.ollycodes.modlogger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler {
    Path configPath = Path.of("config/modlogger/config.json").toAbsolutePath();
    Path playersDirectory = Path.of("config/modlogger/players").toAbsolutePath();
    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
            .setPrettyPrinting().create();
    Config config = new Config();

    public FileHandler() {
        try {
            Files.createDirectories(playersDirectory);
            createConfig();
            readConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("FileHandler initialized");
    }

    public void createConfig() throws IOException {
        if (Files.exists(configPath)) return;
        Files.createFile(configPath);
        Files.writeString(configPath, gson.toJson(new Config()));
    }

    public void readConfig() throws IOException {
        config = gson.fromJson(Files.readString(configPath), Config.class);
    }

    public PlayerRecord getPlayerRecord(String uuid) throws IOException {
        Path playerRecordPath = playersDirectory.resolve(uuid + ".json");
        if (Files.exists(playerRecordPath)) {
            return gson.fromJson(Files.readString(playerRecordPath), PlayerRecord.class);
        } else {
            return new PlayerRecord(uuid);
        }
    }

    public void savePlayerRecord(PlayerRecord playerRecord) throws IOException {
        Path playerRecordPath = playersDirectory.resolve(playerRecord.uuid + ".json");
        Files.writeString(playerRecordPath, gson.toJson(playerRecord));
    }

    public void logConnection(String uuid, PlayerRecord.PlayerConnection connection) throws IOException {
        PlayerRecord playerRecord = getPlayerRecord(uuid);
        playerRecord.connections.add(connection);
        savePlayerRecord(playerRecord);
    }

    public String getBannedMessage() {
        URL bannedMessageFile = getClass().getClassLoader().getResource("webhook/banned.json");
        if (bannedMessageFile == null) throw new RuntimeException("banned.json not found");
        try {
            return Files.readString(Path.of(bannedMessageFile.toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getAddedMessage() {
        URL addedMessageFile = getClass().getClassLoader().getResource("webhook/added.json");
        if (addedMessageFile == null) throw new RuntimeException("added.json not found");
        try {
            return Files.readString(Path.of(addedMessageFile.toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getDefaultMessage() {
        URL defaultMessageFile = getClass().getClassLoader().getResource("webhook/default.json");
        if (defaultMessageFile == null) throw new RuntimeException("default.json not found");
        try {
            return Files.readString(Path.of(defaultMessageFile.toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
