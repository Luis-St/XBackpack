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

package net.luis.xbackpack.world.inventory.handler;

import com.google.common.collect.Lists;
import net.luis.xbackpack.core.components.XBDataComponents;
import net.luis.xbackpack.world.inventory.slot.SlotWrapper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 *
 * @author Luis-St
 *
 */

public class ModifiableHandler implements IItemHandlerModifiable {
	
	private final ItemStackHandler mainHandler;
	private final List<SlotWrapper> slotWrappers;
	
	public ModifiableHandler(int size) {
		this(new ItemStackHandler(size));
	}
	
	public ModifiableHandler(@NotNull ItemStackHandler mainHandler) {
		this.mainHandler = mainHandler;
		this.slotWrappers = Lists.newArrayList();
		this.initSlotWrappers(mainHandler.getSlots(), SlotWrapper::ofUnwrapped);
	}
	
	private void validateSlotIndex(int slot) {
		if (slot >= this.mainHandler.getSlots() || 0 > slot) {
			throw new RuntimeException("Slot " + slot + " is not in valid range - [0," + this.mainHandler.getSlots() + ")");
		}
	}
	
	private boolean isCurrentlyModified() {
		for (SlotWrapper slotWrapper : this.slotWrappers) {
			if (slotWrapper.getMainSlot() != slotWrapper.getSlot()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int getSlots() {
		return this.mainHandler.getSlots();
	}
	
	@Override
	public @NotNull ItemStack getStackInSlot(int slot) {
		int wrappedSlot = this.getWrappedSlot(slot);
		if (wrappedSlot != -1) {
			return this.mainHandler.getStackInSlot(wrappedSlot);
		}
		return ItemStack.EMPTY;
	}
	
	public @NotNull ItemStack insertItem(@NotNull ItemStack stack, boolean simulate) {
		for (int i = 0; i < this.getSlots() && !stack.isEmpty(); i++) {
			stack = this.insertItem(i, stack, simulate);
		}
		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}
	
	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		int wrappedSlot = this.getWrappedSlot(slot);
		if (wrappedSlot != -1) {
			return this.mainHandler.insertItem(wrappedSlot, stack, simulate);
		}
		return stack;
	}
	
	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		int wrappedSlot = this.getWrappedSlot(slot);
		if (wrappedSlot != -1) {
			return this.mainHandler.extractItem(wrappedSlot, amount, simulate);
		}
		return ItemStack.EMPTY;
	}
	
	@Override
	public int getSlotLimit(int slot) {
		int wrappedSlot = this.getWrappedSlot(slot);
		if (wrappedSlot != -1) {
			return this.mainHandler.getSlotLimit(wrappedSlot);
		}
		return 0;
	}
	
	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		int wrappedSlot = this.getWrappedSlot(slot);
		if (wrappedSlot != -1) {
			if (this.mainHandler.getStackInSlot(wrappedSlot).isEmpty() && (wrappedSlot != slot || this.isCurrentlyModified())) {
				return false;
			}
			return this.mainHandler.isItemValid(wrappedSlot, stack);
		}
		return false;
	}
	
	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		int wrappedSlot = this.getWrappedSlot(slot);
		if (wrappedSlot != -1) {
			this.mainHandler.setStackInSlot(wrappedSlot, stack);
		}
	}
	
	private void initSlotWrappers(int size, @NotNull Function<Integer, SlotWrapper> function) {
		for (int i = 0; i < size; i++) {
			this.slotWrappers.add(function.apply(i));
		}
	}
	
	private @NotNull SlotWrapper getSlotWrapper(int mainSlot) {
		this.validateSlotIndex(mainSlot);
		SlotWrapper wrapper = this.slotWrappers.get(mainSlot);
		if (wrapper.getMainSlot() == mainSlot) {
			return wrapper;
		}
		return this.slotWrappers.stream().filter((slotWrapper) -> slotWrapper.getMainSlot() == mainSlot).findFirst().orElseThrow();
	}
	
	public int getWrappedSlot(int mainSlot) {
		return this.getSlotWrapper(mainSlot).getSlot();
	}
	
	public void setWrappedSlot(int mainSlot, int slot) {
		this.validateSlotIndex(mainSlot);
		this.validateSlotIndex(slot);
		this.getSlotWrapper(mainSlot).setSlot(slot);
	}
	
	public void resetWrappedSlots() {
		this.resetWrappedSlots(SlotWrapper::ofUnwrapped);
	}
	
	public void resetWrappedSlots(@NotNull Function<Integer, SlotWrapper> function) {
		int size = this.slotWrappers.size();
		this.slotWrappers.clear();
		this.initSlotWrappers(size, function);
	}
	
	public @NotNull List<ItemStack> createModifiableList() {
		List<ItemStack> stacks = Lists.newArrayList();
		for (int i = 0; i < this.mainHandler.getSlots(); i++) {
			ItemStack stack = this.mainHandler.getStackInSlot(i).copy();
			if (!stack.isEmpty()) {
				stack.set(XBDataComponents.MODIFICATION_SLOT_INDEX.get(), i);
				stacks.add(stack);
			}
		}
		return stacks;
	}
	
	public void applyModifications(@NotNull List<ItemStack> stacks) {
		this.resetWrappedSlots(SlotWrapper::ofDisabled);
		for (int i = 0; i < stacks.size(); i++) {
			Integer slot = stacks.get(i).get(XBDataComponents.MODIFICATION_SLOT_INDEX.get());
			if (slot != null) {
				this.setWrappedSlot(i, slot);
			}
		}
	}
}
