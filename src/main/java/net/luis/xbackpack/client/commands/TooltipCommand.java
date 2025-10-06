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

package net.luis.xbackpack.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class TooltipCommand {
	
	public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("tooltip").executes((command) -> {
			return getTooltipVisibility(command.getSource());
		}).then(Commands.literal("enable").executes((command) -> {
			return setTooltipVisibility(command.getSource(), true);
		})).then(Commands.literal("disable").executes((command) -> {
			return setTooltipVisibility(command.getSource(), false);
		})));
	}
	
	private static int getTooltipVisibility(@NotNull CommandSourceStack source) {
		if (source.getEntity() instanceof Player player) {
			boolean showModifierInfo = Objects.requireNonNull(BackpackProvider.get(player).getConfig().getClientConfig()).shouldShowModifierInfo();
			if (showModifierInfo) {
				source.sendSuccess(() -> Component.translatable("xbackpack.commands.tooltip.get.success.true"), false);
			} else {
				source.sendSuccess(() -> Component.translatable("xbackpack.commands.tooltip.get.success.false"), false);
			}
		} else {
			source.sendFailure(Component.translatable("xbackpack.commands.tooltip.failure"));
		}
		return 1;
	}
	
	private static int setTooltipVisibility(@NotNull CommandSourceStack source, boolean value) {
		if (source.getEntity() instanceof Player player) {
			Objects.requireNonNull(BackpackProvider.get(player).getConfig().getClientConfig()).setShowModifierInfo(value);
			if (value) {
				source.sendSuccess(() -> Component.translatable("xbackpack.commands.tooltip.set.success.true"), false);
			} else {
				source.sendSuccess(() -> Component.translatable("xbackpack.commands.tooltip.set.success.false"), false);
			}
		} else {
			source.sendFailure(Component.translatable("xbackpack.commands.tooltip.failure"));
		}
		return 1;
	}
}
