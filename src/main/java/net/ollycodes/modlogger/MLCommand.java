package net.ollycodes.modlogger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;

public class MLCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("mlreload").executes(MLCommand::reload));
    }

    private static int reload(CommandContext<CommandSourceStack> command) {
        if (ModLogger.fileHandler.config.reloadCommandPermissionLevel == -1) return 0;
        if (!command.getSource().hasPermission(ModLogger.fileHandler.config.reloadCommandPermissionLevel)) return 0;
        TextComponent message;
        try {
            ModLogger.fileHandler.readConfig();
            message = new TextComponent("Reloaded mod logger config.");
        } catch (IOException e) {
            ModLogger.logger.error("Failed to reload config: {}", e.getMessage());
            message = new TextComponent("Failed to reload mod logger config.");
        }

        if (command.getSource().getEntity() instanceof Player player){
            player.sendMessage(message, Util.NIL_UUID);
        } else {
            command.getSource().sendSuccess(message, false);
        }
        return 1;
    }
}
