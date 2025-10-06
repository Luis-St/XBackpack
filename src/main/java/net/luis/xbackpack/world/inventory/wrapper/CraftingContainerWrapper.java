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

package net.luis.xbackpack.world.inventory.wrapper;

import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

public class CraftingContainerWrapper extends TransientCraftingContainer implements IItemHandlerModifiable {

	private final AbstractContainerMenu menu;
	private final IItemHandlerModifiable itemHandler;

	public CraftingContainerWrapper(@NotNull AbstractContainerMenu menu, @NotNull IItemHandlerModifiable itemHandler, int width, int height) {
		super(menu, width, height);
		this.menu = menu;
		this.itemHandler = itemHandler;
	}

	private AbstractContainerMenu getMenu() {
		return this.menu;
	}
	
	@Override
	public int getSlots() {
		return this.itemHandler.getSlots();
	}
	
	@Override
	public @NotNull ItemStack getStackInSlot(int slot) {
		return this.itemHandler.getStackInSlot(slot);
	}
	
	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		ItemStack itemStack = this.itemHandler.insertItem(slot, stack, simulate);
		this.getMenu().slotsChanged(this);
		return itemStack;
	}

	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack stack = this.itemHandler.extractItem(slot, amount, simulate);
		if (!stack.isEmpty()) {
			this.getMenu().slotsChanged(this);
		}
		return stack;
	}
	
	@Override
	public int getSlotLimit(int slot) {
		return this.itemHandler.getSlotLimit(slot);
	}
	
	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return this.itemHandler.isItemValid(slot, stack);
	}
	
	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		this.itemHandler.setStackInSlot(slot, stack);
		this.getMenu().slotsChanged(this);
	}
	
	@Override
	public int getContainerSize() {
		return this.getSlots();
	}
	
	@Override
	public boolean isEmpty() {
		for (int i = 0; i < this.getSlots(); i++) {
			if (!this.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public @NotNull List<ItemStack> getItems() {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < this.getSlots(); i++) {
			stacks.add(this.getStackInSlot(i));
		}
		return List.copyOf(stacks);
	}
	
	@Override
	public @NotNull ItemStack getItem(int slot) {
		return this.getStackInSlot(slot);
	}
	
	@Override
	public @NotNull ItemStack removeItemNoUpdate(int slot) {
		return this.extractItem(slot, slot, true);
	}
	
	@Override
	public @NotNull ItemStack removeItem(int slot, int amount) {
		ItemStack stack = this.extractItem(slot, amount, false);
		if (!stack.isEmpty()) {
			this.getMenu().slotsChanged(this);
		}
		return stack;
	}

	@Override
	public void setItem(int slot, @NotNull ItemStack stack) {
		this.insertItem(slot, stack, false);
		this.getMenu().slotsChanged(this);
	}
	
	@Override
	public void clearContent() {
		for (int i = 0; i < this.getSlots(); i++) {
			this.setStackInSlot(i, ItemStack.EMPTY);
		}
	}
	
	@Override
	public void fillStackedContents(@NotNull StackedItemContents contents) {
		for (int i = 0; i < this.getSlots(); i++) {
			contents.accountSimpleStack(this.getStackInSlot(i));
		}
	}
}
