package net.ollycodes.modlogger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {
    Path configPath = Paths.get("config/modlogger/config.json").toAbsolutePath();
    Path playersDirectory = Paths.get("config/modlogger/players").toAbsolutePath();
    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
            .setPrettyPrinting().create();
    Config config = new Config();

    public FileHandler() {
        try {
            Files.createDirectories(playersDirectory);
            createConfig();
            loadConfig();
            saveConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ModLogger.logger.info("FileHandler initialized");
    }

    public void createConfig() throws IOException {
        if (Files.exists(configPath)) return;
        Files.createFile(configPath);
        FileUtils.writeStringToFile(configPath.toFile(), gson.toJson(new Config()), "UTF-8");
        ModLogger.logger.debug("Created config file");
    }

    public void loadConfig() throws IOException {
        config = gson.fromJson(FileUtils.readFileToString(configPath.toFile(), "UTF-8"), Config.class);
        ModLogger.logger.debug("Loaded data from config file");
        ModLogger.logger.debug("Banned mods: {}", config.bannedMods);
        ModLogger.logger.debug("Required mods: {}", config.requiredMods);
        ModLogger.logger.debug("Default mods: {}", config.defaultMods);
    }

    public void saveConfig() throws IOException {
        FileUtils.writeStringToFile(configPath.toFile(), gson.toJson(config), "UTF-8");
        ModLogger.logger.debug("Saved data to config file");
    }

    public PlayerRecord getPlayerRecord(String uuid) throws IOException {
        Path playerRecordPath = playersDirectory.resolve(uuid + ".json");
        if (Files.exists(playerRecordPath)) {
            ModLogger.logger.debug("Fetched existing player record for {}", uuid);
            return gson.fromJson(FileUtils.readFileToString(playerRecordPath.toFile(), "UTF-8"), PlayerRecord.class);
        } else {
            ModLogger.logger.debug("Created new player record for {}", uuid);
            return new PlayerRecord(uuid);
        }
    }

    public void savePlayerRecord(PlayerRecord playerRecord) throws IOException {
        Path playerRecordPath = playersDirectory.resolve(playerRecord.uuid + ".json");
        FileUtils.writeStringToFile(playerRecordPath.toFile(), gson.toJson(playerRecord), "UTF-8");
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
            return FileUtils.readFileToString(Paths.get(messageFile.toURI()).toFile(), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
