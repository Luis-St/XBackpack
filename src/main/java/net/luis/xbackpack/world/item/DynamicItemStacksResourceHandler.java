/*
 * XBackpack
 * Copyright (C) 2026 Luis Staudt
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
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A dynamic item stacks resource handler that supports backward compatibility
 * with the old ItemStackHandler format and dynamic size validation.
 * <p>
 * Implements {@link IItemHandlerModifiable} for backward compatibility with
 * existing slot classes during the migration to the new Resource Handler API.
 *
 * @author Luis-St
 */
@SuppressWarnings("deprecation") // IItemHandlerModifiable is deprecated, implementing for backward compatibility
public class DynamicItemStacksResourceHandler extends ItemStacksResourceHandler implements IItemHandlerModifiable {

	private final int initialSize;

	public DynamicItemStacksResourceHandler(int size) {
		super(size);
		this.initialSize = size;
	}

	/**
	 * Gets the number of slots in this handler.
	 * @return the number of slots
	 */
	@Override
	public int getSlots() {
		return this.size();
	}

	/**
	 * Gets the ItemStack in the specified slot.
	 * @param slot the slot index
	 * @return the ItemStack in the slot
	 */
	@Override
	public @NotNull ItemStack getStackInSlot(int slot) {
		this.validateSlotIndex(slot);
		return this.stacks.get(slot);
	}

	/**
	 * Sets the ItemStack in the specified slot.
	 * @param slot the slot index
	 * @param stack the ItemStack to set
	 */
	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		this.validateSlotIndex(slot);
		this.stacks.set(slot, stack);
		this.onContentsChanged(slot);
	}

	/**
	 * Called when the contents of this handler change.
	 * Override in subclasses to add custom behavior.
	 * @param slot the slot that changed
	 */
	protected void onContentsChanged(int slot) {
		// Override in subclasses if needed
	}

	/**
	 * Called after deserialization completes.
	 * Override in subclasses to add custom behavior.
	 */
	protected void onLoad() {
		// Override in subclasses if needed
	}

	@Override
	public void serialize(@NotNull ValueOutput output) {
		super.serialize(output);
		output.putInt("initial_size", this.initialSize);
	}

	@Override
	public void deserialize(@NotNull ValueInput input) {
		// Check for old "Size" key to determine format
		// Old ItemStackHandler used "Size" key, new format uses "initial_size"
		int oldSize = input.getIntOr("Size", -1);
		int size = input.getIntOr("initial_size", this.initialSize);

		if (oldSize > 0) {
			// OLD FORMAT: Read from "Items" key using ItemStackWithSlot (sparse format)
			XBackpack.LOGGER.info("Migrating from old ItemStackHandler format (Size={})", oldSize);
			this.ensureStacksSize();
			input.listOrEmpty("Items", ItemStackWithSlot.CODEC).forEach(slot -> {
				if (slot.isValidInContainer(this.stacks.size())) {
					this.stacks.set(slot.slot(), slot.stack());
				}
			});
		} else {
			// NEW FORMAT: Use parent deserialize which reads "stacks" key
			if (size > this.initialSize) {
				XBackpack.LOGGER.error("DynamicItemStacksResourceHandler does not support shrinking of the inventory size");
				throw new RuntimeException("Tried to deserialize to a handler with more slots than it was created with");
			}
			super.deserialize(input);
			this.ensureStacksSize();
		}

		this.onLoad();
	}

	/**
	 * Ensures the stacks list is the correct size (initialSize).
	 */
	private void ensureStacksSize() {
		if (this.initialSize > this.stacks.size()) {
			NonNullList<ItemStack> newStacks = NonNullList.withSize(this.initialSize, ItemStack.EMPTY);
			for (int i = 0; i < this.stacks.size(); i++) {
				newStacks.set(i, this.stacks.get(i));
			}
			this.stacks = newStacks;
		} else if (this.initialSize < this.stacks.size()) {
			XBackpack.LOGGER.error("DynamicItemStacksResourceHandler does not support shrinking of the inventory size");
			throw new RuntimeException("Tried to decrease handler by " + (this.stacks.size() - this.initialSize) +
				" slots while it was created with " + this.initialSize + " slots");
		}
	}

	/**
	 * Validates that the slot index is within valid range.
	 * @param slot the slot index to validate
	 */
	protected void validateSlotIndex(int slot) {
		ensureStacksSize();
		if (slot < 0 || slot >= this.stacks.size()) {
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.stacks.size() + ")");
		}
	}

	/**
	 * Checks if a given item is valid for a slot.
	 * @param slot the slot index
	 * @param stack the ItemStack to check
	 * @return true if the item is valid for the slot
	 */
	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return this.isValid(slot, ItemResource.of(stack));
	}

	/**
	 * Gets the maximum stack size for a slot.
	 * @param slot the slot index
	 * @return the maximum stack size
	 */
	@Override
	public int getSlotLimit(int slot) {
		return 64; // Default Minecraft stack size
	}

	/**
	 * Inserts an ItemStack into a slot.
	 * @param slot the slot index
	 * @param stack the ItemStack to insert
	 * @param simulate if true, the insertion is simulated
	 * @return the remaining ItemStack that was not inserted
	 */
	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		this.validateSlotIndex(slot);
		ItemStack existing = this.stacks.get(slot);
		int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

		if (!existing.isEmpty()) {
			if (!ItemStack.isSameItemSameComponents(stack, existing)) {
				return stack;
			}
			limit -= existing.getCount();
		}

		if (limit <= 0) {
			return stack;
		}

		boolean reachedLimit = stack.getCount() > limit;

		if (!simulate) {
			if (existing.isEmpty()) {
				this.stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
			} else {
				existing.grow(reachedLimit ? limit : stack.getCount());
			}
			this.onContentsChanged(slot);
		}

		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	/**
	 * Extracts an ItemStack from a slot.
	 * @param slot the slot index
	 * @param amount the amount to extract
	 * @param simulate if true, the extraction is simulated
	 * @return the extracted ItemStack
	 */
	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return ItemStack.EMPTY;
		}

		this.validateSlotIndex(slot);
		ItemStack existing = this.stacks.get(slot);

		if (existing.isEmpty()) {
			return ItemStack.EMPTY;
		}

		int toExtract = Math.min(amount, existing.getCount());

		if (!simulate) {
			if (toExtract == existing.getCount()) {
				this.stacks.set(slot, ItemStack.EMPTY);
			} else {
				existing.shrink(toExtract);
			}
			this.onContentsChanged(slot);
		}

		return existing.copyWithCount(toExtract);
	}
}
