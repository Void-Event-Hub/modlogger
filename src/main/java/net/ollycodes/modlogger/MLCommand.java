package net.ollycodes.modlogger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MLCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(
            Commands.literal("ml")
                .then(Commands.literal("reload").executes(MLCommand::reload))
                .then(Commands.literal("whitelist")
                    .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("add");
                            builder.suggest("remove");
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("username", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                context.getSource().getServer().getPlayerList().getPlayers().forEach(
                                    player -> builder.suggest(player.getGameProfile().getName())
                                );
                                return builder.buildFuture();
                            })
                            .executes(MLCommand::whitelist)
                        )
                    )
                )
        );
    }

    private static boolean canExecuteCommand(CommandSourceStack source) {
        if (source.getEntity() instanceof Player) {
            if (ModLogger.fileHandler.config.commandPermissionLevel == -1) return false;
            return source.hasPermission(ModLogger.fileHandler.config.commandPermissionLevel);
        }
        return true;
    }

    private static int whitelist(CommandContext<CommandSourceStack> context) {
        if (!canExecuteCommand(context.getSource())) return 0;

        String action = context.getArgument("action", String.class);
        String username = context.getArgument("username", String.class);

        if (!List.of("add", "remove").contains(action)) {
            context.getSource().sendFailure(
                Component.literal("[ModLogger] Invalid action: /ml whitelist <add|remove> <username>")
            );
            return 0;
        }

        try {
            ModLogger.fileHandler.loadConfig();
            if (Objects.equals(action, "add")) {
                if (ModLogger.fileHandler.config.playerWhitelist.contains(username)) {
                    context.getSource().sendFailure(Component.literal("[ModLogger] Player is already whitelisted"));
                    return 0;
                }
                ModLogger.fileHandler.config.playerWhitelist.add(username);
            } else {
                if (!ModLogger.fileHandler.config.playerWhitelist.contains(username)) {
                    context.getSource().sendFailure(Component.literal("[ModLogger] Player is not whitelisted"));
                    return 0;
                }
                ModLogger.fileHandler.config.playerWhitelist.remove(username);
            }
            ModLogger.fileHandler.saveConfig();
        } catch (IOException e) {
            ModLogger.logger.error("Failed to reload config: {}", e.getMessage());
            return 0;
        }

        context.getSource().sendSystemMessage(
            Component.literal(
                "[ModLogger] %1 has been %2 the whitelist"
                    .replace("%1", username)
                    .replace("%2", Objects.equals(action, "add") ? "added to" : "removed from")
            )
        );
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        if (!canExecuteCommand(context.getSource())) return 0;

        try {
            ModLogger.fileHandler.loadConfig();
            context.getSource().sendSystemMessage(
                Component.literal("[ModLogger] Reloaded mod logger config.")
            );
        } catch (IOException e) {
            ModLogger.logger.error("Failed to reload config: {}", e.getMessage());
            context.getSource().sendFailure(Component.literal("[ModLogger] Failed to reload mod logger config."));
        }

        return 1;
    }
}
