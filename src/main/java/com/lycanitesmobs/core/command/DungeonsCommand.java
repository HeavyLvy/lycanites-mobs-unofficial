package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.data.config.ConfigDungeons;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.worldgen.dungeon.instance.DungeonInstance;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.List;

public class DungeonsCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("dungeons")
				.then(Commands.literal("reload").executes(DungeonsCommand::reload))
				.then(Commands.literal("enable").executes(DungeonsCommand::enable))
				.then(Commands.literal("disable").executes(DungeonsCommand::disable))
				.then(Commands.literal("locate").executes(DungeonsCommand::locate));
	}

	public static int reload(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		DungeonManager.getInstance().reload();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.dungeons.reload"), true);
		return 0;
	}

	public static int enable(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		ConfigDungeons.INSTANCE.dungeonsEnabled.set(true);
		ConfigDungeons.INSTANCE.dungeonsEnabled.save();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.dungeons.enable"), true);
		return 0;
	}

	public static int disable(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		ConfigDungeons.INSTANCE.dungeonsEnabled.set(false);
		ConfigDungeons.INSTANCE.dungeonsEnabled.save();
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.dungeons.disable"), true);
		return 0;
	}

	public static int locate(final CommandContext<CommandSourceStack> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		context.getSource().sendSuccess(() -> Component.translatable("lyc.command.dungeons.locate"), true);
		Level world = context.getSource().getLevel();
		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		List<DungeonInstance> nearbyDungeons = extendedWorld.getNearbyDungeonInstances(new ChunkPos(new BlockPos(LMHelperClass.convertToVec3i(context.getSource().getPosition()))), ConfigDungeons.INSTANCE.dungeonDistance.get() * 2);
		if(nearbyDungeons.isEmpty()) {
			context.getSource().sendSuccess(() -> Component.translatable("common.none"), true);
			return 0;
		}
		for(DungeonInstance dungeonInstance : nearbyDungeons) {
			context.getSource().sendSuccess(() -> Component.literal(dungeonInstance.toString()), true);
		}
		return 0;
	}
}
