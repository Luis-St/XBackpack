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

package net.luis.xbackpack.world.item;

import net.luis.xbackpack.XBackpack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

public class DynamicItemStackHandler extends ItemStackHandler {
	
	private final int initialSize;
	
	public DynamicItemStackHandler(int size) {
		super(size);
		this.initialSize = size;
	}
	
	@Override
	public void serialize(@NotNull ValueOutput output) {
		output.putInt("initial_size", this.initialSize);
		output.putInt("stack_count", this.stacks.size());
		List<ItemStack> stackList = new ArrayList<>(this.stacks);
		output.store("stacks", ItemStack.CODEC.listOf(), stackList);
	}

	@Override
	public void deserialize(@NotNull ValueInput input) {
		int size = input.getIntOr("initial_size", this.initialSize);
		int stackCount = input.getIntOr("stack_count", 0);
		boolean reduced = false;
		if (this.initialSize >= size) {
			this.setSize(this.initialSize);
		} else {
			this.setSize(size);
			reduced = true;
		}
		if (reduced) {
			XBackpack.LOGGER.error("DynamicItemStackHandler does currently not support shrinking of the inventory size");
			throw new RuntimeException("Tried to deserialize to an ItemStackHandler with more slots than it was created with");
		} else {
			List<ItemStack> stackList = input.read("stacks", ItemStack.CODEC.listOf()).orElse(List.of());
			for (int i = 0; i < stackList.size() && i < this.stacks.size(); i++) {
				this.stacks.set(i, stackList.get(i));
			}
		}
		this.onLoad();
	}
	
	@Override
	protected void validateSlotIndex(int slot) {
		if (this.initialSize > this.stacks.size()) {
			NonNullList<ItemStack> stacks = NonNullList.withSize(this.initialSize, ItemStack.EMPTY);
			for (int i = 0; i < this.stacks.size(); i++) {
				stacks.set(i, this.stacks.get(i));
			}
			this.stacks = stacks;
		} else if (this.initialSize < this.stacks.size()) {
			XBackpack.LOGGER.error("DynamicItemStackHandler does currently not support shrinking of the inventory size");
			throw new RuntimeException("Tried to decrease an ItemStackHandler by " + (this.stacks.size() - this.initialSize) + " while it was created with " + this.stacks.size() + " slots");
		}
		if (slot < 0 || slot >= this.stacks.size()) {
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.stacks.size() + ")");
		}
	}
}
