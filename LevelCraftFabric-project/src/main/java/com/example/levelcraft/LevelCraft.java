package com.example.levelcraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class LevelCraft implements ModInitializer {
    private static final Map<UUID, PlayerProgress> DATA = new ConcurrentHashMap<>();
    private static final Map<Block, Integer> ORE_XP = Map.ofEntries(
        Map.entry(Blocks.COAL_ORE, 3), Map.entry(Blocks.DEEPSLATE_COAL_ORE, 4),
        Map.entry(Blocks.COPPER_ORE, 4), Map.entry(Blocks.DEEPSLATE_COPPER_ORE, 5),
        Map.entry(Blocks.IRON_ORE, 6), Map.entry(Blocks.DEEPSLATE_IRON_ORE, 7),
        Map.entry(Blocks.GOLD_ORE, 8), Map.entry(Blocks.DEEPSLATE_GOLD_ORE, 9),
        Map.entry(Blocks.REDSTONE_ORE, 8), Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, 9),
        Map.entry(Blocks.LAPIS_ORE, 10), Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, 11),
        Map.entry(Blocks.DIAMOND_ORE, 15), Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, 18),
        Map.entry(Blocks.EMERALD_ORE, 16), Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, 18),
        Map.entry(Blocks.NETHER_QUARTZ_ORE, 5), Map.entry(Blocks.NETHER_GOLD_ORE, 4),
        Map.entry(Blocks.ANCIENT_DEBRIS, 25)
    );

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Playtime XP: every 5 minutes (6000 ticks), only for connected players.
            if (server.getTickCount() % 6000 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    addXp(server, player, 10, "playtime");
                }
            }
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return;
            Integer xp = ORE_XP.get(state.getBlock());
            if (xp != null) addXp(sp.getServer(), sp, xp, "mining");
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("level")
                .then(Commands.literal("check").executes(ctx -> show(ctx.getSource(), ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("top").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("Leaderboard support is planned for a future version."), false);
                    return 1;
                }))
            );
        });
    }

    private static int show(CommandSourceStack source, ServerPlayer player) {
        PlayerProgress p = get(player);
        source.sendSuccess(() -> Component.literal("Level " + p.level + " | " + p.xp + "/" + needed(p.level) + " XP"), false);
        return 1;
    }

    private static PlayerProgress get(ServerPlayer player) {
        return DATA.computeIfAbsent(player.getUUID(), id -> new PlayerProgress());
    }

    private static int needed(int level) { return 100 + (level - 1) * 50; }

    private static void addXp(MinecraftServer server, ServerPlayer player, int amount, String reason) {
        PlayerProgress p = get(player);
        p.xp += amount;
        while (p.xp >= needed(p.level)) {
            p.xp -= needed(p.level);
            p.level++;
            int reached = p.level;
            player.sendSystemMessage(Component.literal("§a§lLEVEL UP! §r§aYou reached Level " + reached + "!"));
            // Example reward: 4 emeralds every level. Customize reward logic here.
            runReward(server, player, "give " + player.getName().getString() + " minecraft:emerald 4");
        }
        if (reason.equals("mining")) {
            player.sendSystemMessage(Component.literal("§7+" + amount + " XP"));
        }
    }

    private static void runReward(MinecraftServer server, ServerPlayer player, String command) {
        String cmd = command.replace("{player}", player.getName().getString());
        server.getCommands().performPrefixedCommand(server.getCommands().getDispatcher().getRoot().getChild(""), cmd);
    }

    private static class PlayerProgress {
        int level = 1;
        int xp = 0;
    }
}
