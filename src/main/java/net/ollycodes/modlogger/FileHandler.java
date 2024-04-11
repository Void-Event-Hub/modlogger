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
        ModLogger.logger.info("FileHandler initialized");
    }

    public void createConfig() throws IOException {
        if (Files.exists(configPath)) return;
        Files.createFile(configPath);
        Files.writeString(configPath, gson.toJson(new Config()));
        ModLogger.logger.debug("Created config file");
    }

    public void readConfig() throws IOException {
        config = gson.fromJson(Files.readString(configPath), Config.class);
        ModLogger.logger.debug("Read data from config file");
    }

    public PlayerRecord getPlayerRecord(String uuid) throws IOException {
        Path playerRecordPath = playersDirectory.resolve(uuid + ".json");
        if (Files.exists(playerRecordPath)) {
            ModLogger.logger.debug("Fetched existing player record for {}", uuid);
            return gson.fromJson(Files.readString(playerRecordPath), PlayerRecord.class);
        } else {
            ModLogger.logger.debug("Created new player record for {}", uuid);
            return new PlayerRecord(uuid);
        }
    }

    public void savePlayerRecord(PlayerRecord playerRecord) throws IOException {
        Path playerRecordPath = playersDirectory.resolve(playerRecord.uuid + ".json");
        Files.writeString(playerRecordPath, gson.toJson(playerRecord));
        ModLogger.logger.debug("Saved player record for {}", playerRecord.uuid);
    }

    public void logConnection(String uuid, PlayerRecord.PlayerConnection connection) throws IOException {
        PlayerRecord playerRecord = getPlayerRecord(uuid);
        playerRecord.connections.add(connection);
        savePlayerRecord(playerRecord);
        ModLogger.logger.debug("Logged connection for {}", playerRecord.uuid);
    }

    public String getMessage(String type) {
        URL messageFile = getClass().getClassLoader().getResource("webhook/" + type + ".json");
        if (messageFile == null) throw new RuntimeException(type + ".json not found");
        try {
            return Files.readString(Path.of(messageFile.toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
