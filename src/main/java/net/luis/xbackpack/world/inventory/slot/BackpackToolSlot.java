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

package net.luis.xbackpack.world.inventory.slot;

import net.luis.xbackpack.BackpackConstants;
import net.luis.xbackpack.XBackpack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackToolSlot extends SlotItemHandler {

	private static final ResourceLocation EMPTY_TOOL_SLOT = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "container/slot/empty_tool_slot");
	
	public BackpackToolSlot(@NotNull IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}
	
	@Override
	public int getMaxStackSize() {
		return 1;
	}
	
	@Override
	public int getMaxStackSize(@NotNull ItemStack stack) {
		return 1;
	}
	
	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return BackpackConstants.VALID_TOOL_SLOT_ITEMS.contains(stack.getItem());
	}
	
	@Override
	public @NotNull ResourceLocation getNoItemIcon() {
		return EMPTY_TOOL_SLOT;
	}
}
