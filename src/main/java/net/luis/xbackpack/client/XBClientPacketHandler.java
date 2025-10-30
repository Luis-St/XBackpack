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

package net.luis.xbackpack.client;

import net.luis.xbackpack.client.gui.screens.BackpackScreen;
import net.luis.xbackpack.client.gui.screens.extension.*;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.modifier.ModifiableMenu.UpdateType;
import net.luis.xbackpack.world.inventory.modifier.filter.ItemFilter;
import net.luis.xbackpack.world.inventory.modifier.sorter.ItemSorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class XBClientPacketHandler {
	
	public static void updateBackpack(@NotNull CompoundTag tag) {
		LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
		BackpackProvider.get(player).deserialize(player.registryAccess(), tag);
	}
	
	private static @Nullable AbstractExtensionScreen getExtensionScreen(@NotNull BackpackExtension extension) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen instanceof BackpackScreen screen) {
			return screen.getExtensionScreen(extension);
		}
		return null;
	}
	
	public static void updateFurnaceExtension(int cookingProgress, int fuelProgress) {
		if (getExtensionScreen(BackpackExtensions.FURNACE.get()) instanceof FurnaceExtensionScreen furnaceExtension) {
			furnaceExtension.update(cookingProgress, fuelProgress);
		}
	}
	
	public static void updateAnvilExtension(int cost) {
		if (getExtensionScreen(BackpackExtensions.ANVIL.get()) instanceof AnvilExtensionScreen anvilExtension) {
			anvilExtension.update(cost);
		}
	}
	
	public static void updateEnchantmentTableExtension(ResourceLocation @NotNull [] enchantments, int @NotNull [] enchantmentLevels, int @NotNull [] enchantingCosts, int enchantmentSeed) {
		if (getExtensionScreen(BackpackExtensions.ENCHANTMENT_TABLE.get()) instanceof EnchantmentTableExtensionScreen enchantmentTableExtension) {
			enchantmentTableExtension.update(enchantments, enchantmentLevels, enchantingCosts, enchantmentSeed);
		}
	}
	
	public static void updateStonecutterExtension(boolean resetSelected) {
		if (getExtensionScreen(BackpackExtensions.STONECUTTER.get()) instanceof StonecutterExtensionScreen stonecutterScreen) {
			stonecutterScreen.updateRecipes(resetSelected);
		}
	}
	
	public static void updateBrewingStandExtension(int fuel, int brewTime) {
		if (getExtensionScreen(BackpackExtensions.BREWING_STAND.get()) instanceof BrewingStandExtensionScreen brewingStandScreen) {
			brewingStandScreen.update(fuel, brewTime);
		}
	}
	
	public static void updateBackpackItemModifiers(@NotNull ItemFilter filter, @NotNull ItemSorter sorter) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen instanceof BackpackScreen screen) {
			screen.getMenu().updateFilter(filter, UpdateType.SET_NO_UPDATE, null);
			screen.getMenu().updateSorter(sorter, UpdateType.SET_NO_UPDATE, null);
		}
	}
	
}
