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

package net.luis.xbackpack.world.inventory.extension.slot;

import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.inventory.extension.AbstractExtensionMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 *
 * @author Luis-St
 *
 */

public class ExtensionSlot extends SlotItemHandler implements ExtensionMenuSlot {
	
	private final AbstractExtensionMenu extensionMenu;
	private final boolean sendChanges;
	
	public ExtensionSlot(@NotNull AbstractExtensionMenu extensionMenu, @NotNull IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		this(extensionMenu, itemHandler, index, xPosition, yPosition, true);
	}
	
	public ExtensionSlot(@NotNull AbstractExtensionMenu extensionMenu, @NotNull IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean sendChanges) {
		super(itemHandler, index, xPosition, yPosition);
		this.extensionMenu = extensionMenu;
		this.sendChanges = sendChanges;
	}
	
	@Override
	public @NotNull AbstractExtensionMenu getMenu() {
		return this.extensionMenu;
	}
	
	@Override
	public @NotNull BackpackExtension getExtension() {
		return this.extensionMenu.getExtension();
	}
	
	@Override
	public void set(@NotNull ItemStack stack) {
		super.set(stack);
		if (this.sendChanges) {
			this.extensionMenu.slotsChanged();
		}
	}
	
	@Override
	public void initialize(@NotNull ItemStack stack) {
		super.initialize(stack);
		if (this.sendChanges) {
			this.extensionMenu.slotsChanged();
		}
	}
	
	@Override
	public @NotNull Optional<ItemStack> tryRemove(int minAmount, int maxAmount, @NotNull Player player) {
		Optional<ItemStack> optional = super.tryRemove(minAmount, maxAmount, player);
		if (this.sendChanges) {
			this.extensionMenu.slotsChanged();
		}
		return optional;
	}
	
	@Override
	public @NotNull ItemStack safeInsert(@NotNull ItemStack stack, int count) {
		ItemStack remainingStack = super.safeInsert(stack, count);
		if (this.sendChanges) {
			this.extensionMenu.slotsChanged();
		}
		return remainingStack;
	}
}
