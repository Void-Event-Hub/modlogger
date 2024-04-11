package net.ollycodes.modlogger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Webhook {
    private final String bannedMessage = ModLogger.fileHandler.getMessage("banned");
    private final String addedMessage = ModLogger.fileHandler.getMessage("added");
    private final String defaultMessage = ModLogger.fileHandler.getMessage("default");

    public String formatModsList(List<String> mods) {
        return "- " + String.join("\\n- ", mods);
    }

    public String prepareMessage(String message, String uuid, String username, Date timestamp, List<String> defaultMods) {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        return message
            .replace("<USERNAME>", username)
            .replace("<TIMESTAMP>", formatter.format(timestamp))
            .replace("<UUID>", uuid)
            .replace("<DEFAULT_COUNT>", String.valueOf(defaultMods.size()))
            .replace("<DEFAULT_TOTAL>", String.valueOf(ModLogger.fileHandler.config.defaultMods.size()));
    }

    public void sendBannedMessage(
        String uuid, String username, Date timestamp,
        List<String> defaultMods, List<String> addedMods, List<String> bannedMods
    ) {
        String message = prepareMessage(bannedMessage, uuid, username, timestamp, defaultMods)
            .replace("<BANNED_COUNT>", String.valueOf(bannedMods.size()))
            .replace("<ADDED_COUNT>", String.valueOf(addedMods.size()))
            .replace("<BANNED_LIST>", formatModsList(bannedMods))
            .replace("<ADDED_LIST>", formatModsList(addedMods));
        sendWebhook(message);
        ModLogger.logger.debug("Sent banned message to webhook");
    }

    public void sendAddedMessage(
        String uuid, String username, Date timestamp,
        List<String> defaultMods, List<String> addedMods
    ) {
        String message = prepareMessage(addedMessage, uuid, username, timestamp, defaultMods)
            .replace("<ADDED_COUNT>", String.valueOf(addedMods.size()))
            .replace("<ADDED_LIST>", formatModsList(addedMods));
        sendWebhook(message);
        ModLogger.logger.debug("Sent added message to webhook");
    }

    public void sendDefaultMessage(
        String uuid, String username, Date timestamp,
        List<String> defaultMods
    ) {
        String message = prepareMessage(defaultMessage, uuid, username, timestamp, defaultMods);
        sendWebhook(message);
        ModLogger.logger.debug("Sent default message to webhook");
    }

    public void sendWebhook(String message) {
        if (ModLogger.fileHandler.config.discordWebhook.isEmpty()) {
            ModLogger.logger.warn("No webhook configured");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ModLogger.fileHandler.config.discordWebhook))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            ModLogger.logger.debug("Sent message to webhook: {}", message);
        } catch (IOException | InterruptedException e) {
            ModLogger.logger.error("Failed to send webhook", e);
        }
    }
}
