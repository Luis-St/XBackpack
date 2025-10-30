/*
 * XBackpack
 * Copyright (C) 2025 Luis Staudt
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

package net.luis.xbackpack.world.inventory.extension;

import com.google.common.collect.Maps;
import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.AbstractExtensionContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 *
 * @author Luis-St
 *
 */

public class ExtensionMenuRegistry {
	
	private static final Map<BackpackExtension, ExtensionMenuFactory> EXTENSION_FACTORIES = Maps.newHashMap();
	
	public static @NotNull AbstractExtensionMenu getExtensionMenu(@NotNull BackpackExtension extension, @NotNull AbstractExtensionContainerMenu menu, @NotNull Player player, @NotNull ExtensionMenuFactory fallbackFactory) {
		return EXTENSION_FACTORIES.getOrDefault(extension, fallbackFactory).create(menu, player);
	}
	
	/**
	 * Use this method to register a {@link ExtensionMenuFactory} for the {@link BackpackExtension}.
	 * Call this method in {@link FMLCommonSetupEvent}
	 */
	public static void registerOverride(@NotNull BackpackExtension extension, @NotNull String modId, @NotNull ExtensionMenuFactory factory) {
		if (EXTENSION_FACTORIES.containsKey(extension)) {
			XBackpack.LOGGER.error("Fail to register Extension Menu override for Mod {} of type {}, since there is already a Extension Menu present", modId, BackpackExtensions.REGISTRY.getKey(extension));
		} else {
			EXTENSION_FACTORIES.put(extension, factory);
		}
	}
}
