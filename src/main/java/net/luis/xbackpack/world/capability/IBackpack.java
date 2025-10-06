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

package net.luis.xbackpack.world.capability;

import net.luis.xbackpack.world.backpack.config.BackpackConfig;
import net.luis.xbackpack.world.inventory.handler.*;
import net.luis.xbackpack.world.inventory.progress.ProgressHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public interface IBackpack {
	
	@NotNull Player getPlayer();
	
	@NotNull BackpackConfig getConfig();
	
	@NotNull ItemStackHandler getBackpackHandler();
	
	@NotNull ItemStackHandler getToolHandler();
	
	@NotNull ItemStackHandler getCraftingHandler();
	
	@NotNull SmeltingHandler getSmeltingHandler();
	
	@NotNull ProgressHandler getSmeltHandler();
	
	@NotNull CraftingHandler getAnvilHandler();
	
	@NotNull EnchantingHandler getEnchantingHandler();
	
	@NotNull CraftingHandler getStonecutterHandler();
	
	@NotNull CraftingFuelHandler getBrewingHandler();
	
	@NotNull ProgressHandler getBrewHandler();
	
	@NotNull CraftingHandler getGrindstoneHandler();
	
	@NotNull CraftingHandler getSmithingHandler();
	
	void tick();
	
	boolean broadcastChanges();
	
	@NotNull CompoundTag serialize(HolderLookup.@NotNull Provider provider);
	
	void deserialize(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag);
}
