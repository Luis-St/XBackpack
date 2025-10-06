/*
 * XBackpack
 * Copyright (C) 2024 Luis Staudt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package net.luis.xbackpack.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import net.luis.xbackpack.server.commands.arguments.BackpackExtensionArgument;
import net.luis.xbackpack.server.commands.arguments.BackpackExtensionStateArgument;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.capability.IBackpack;
import net.luis.xbackpack.world.extension.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackCommand {
	
	public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(Commands.literal("backpack").requires((source) -> {
			return source.hasPermission(2);
		}).then(Commands.literal("extension").then(Commands.argument("player", EntityArgument.player()).then(Commands.literal("*").then(Commands.argument("state", BackpackExtensionStateArgument.state()).executes((command) -> {
			return setExtensionState(command.getSource(), EntityArgument.getPlayer(command, "player"), Lists.newArrayList(BackpackExtensions.REGISTRY.getValues()), BackpackExtensionStateArgument.get(command, "state"));
		}))).then(Commands.argument("extension", BackpackExtensionArgument.extension()).executes((command) -> {
			return getExtensionState(command.getSource(), EntityArgument.getPlayer(command, "player"), Lists.newArrayList(BackpackExtensionArgument.get(command, "extension")));
		}).then(Commands.argument("state", BackpackExtensionStateArgument.state()).executes((command) -> {
			return setExtensionState(command.getSource(), EntityArgument.getPlayer(command, "player"), Lists.newArrayList(BackpackExtensionArgument.get(command, "extension")), BackpackExtensionStateArgument.get(command, "state"));
		})))))/* Add here other settings */);
	}
	
	private static int getExtensionState(CommandSourceStack source, ServerPlayer player, @NotNull List<BackpackExtension> extensions) {
		if (extensions.size() > 1) {
			source.sendFailure(Component.translatable("xbackpack.commands.backpack.get.failure"));
		} else {
			source.sendSuccess(() -> Component.translatable("xbackpack.commands.backpack.get.success", getName(extensions.get(0)), player.getName().getString(), BackpackProvider.get(player).getConfig().getExtensionConfig().getState(extensions.get(0)).getName()), false);
		}
		return 1;
	}
	
	private static int setExtensionState(CommandSourceStack source, ServerPlayer player, @NotNull List<BackpackExtension> extensions, BackpackExtensionState state) {
		MutableInt i = new MutableInt(0);
		IBackpack backpack = BackpackProvider.get(player);
		for (BackpackExtension extension : extensions) {
			if (extension == BackpackExtensions.NO.get()) {
				continue;
			}
			if (extension.isDisabled()) {
				source.sendFailure(Component.translatable("xbackpack.commands.backpack.set.failure.disabled", getName(extension)));
				continue;
			}
			backpack.getConfig().getExtensionConfig().setState(player, extension, state);
			i.increment();
		}
		backpack.broadcastChanges();
		if (i.getValue() == 1) {
			source.sendSuccess(() -> Component.translatable("xbackpack.commands.backpack.set.success.single", getName(extensions.get(0)), player.getName().getString(), state.getName()), false);
		} else if (i.getValue() > 1) {
			source.sendSuccess(() -> Component.translatable("xbackpack.commands.backpack.set.success.multiple", i.getValue(), player.getName().getString(), state.getName()), false);
		}
		return 1;
	}
	
	private static @NotNull String getName(BackpackExtension extension) {
		String[] nameParts = Objects.requireNonNull(BackpackExtensions.REGISTRY.getKey(extension)).getPath().split("_");
		StringBuilder name = new StringBuilder();
		for (String namePart : nameParts) {
			String startChar = namePart.substring(0, 1).toUpperCase();
			name.append(startChar).append(namePart.substring(1)).append(" ");
		}
		return name.toString().trim();
	}
}
