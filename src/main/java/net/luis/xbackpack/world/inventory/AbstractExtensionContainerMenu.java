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

package net.luis.xbackpack.world.inventory;

import com.google.common.collect.Lists;
import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.extension.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Luis-St
 *
 */

public abstract class AbstractExtensionContainerMenu extends AbstractContainerMenu {
	
	private final List<AbstractExtensionMenu> extensionMenus = Lists.newArrayList();
	private BackpackExtension extension = BackpackExtensions.NO.get();
	
	protected AbstractExtensionContainerMenu(@NotNull MenuType<?> menuType, int id, @NotNull Inventory inventory) {
		super(menuType, id);
	}
	
	@NotNull
	public BackpackExtension getExtension() {
		return this.extension;
	}
	
	public void setExtension(@NotNull BackpackExtension extension) {
		if (this.extension != extension) {
			this.getExtensionMenu().ifPresent(AbstractExtensionMenu::close);
		}
		this.extension = extension;
		this.getExtensionMenu().ifPresent(AbstractExtensionMenu::open);
	}
	
	public boolean moveItemStackTo(@NotNull ItemStack stack, int startIndex, int endIndex) {
		if (!stack.isStackable()) {
			for (int i = startIndex; i < endIndex; i++) {
				Slot slot = this.getSlot(i);
				if (!slot.mayPlace(stack)) {
					continue;
				}
				if (!slot.hasItem()) {
					slot.setByPlayer(stack.copy());
					stack.setCount(0);
					slot.setChanged();
					return true;
				}
			}
			return false;
		}
		int firstEmptySlot = -1;
		for (int i = startIndex; i < endIndex; i++) {
			Slot slot = this.getSlot(i);
			if (!slot.mayPlace(stack)) {
				continue;
			}
			if (!slot.hasItem()) {
				if (firstEmptySlot == -1) {
					firstEmptySlot = i;
				}
				continue;
			}
			ItemStack slotStack = slot.getItem();
			if (slotStack.getCount() >= slotStack.getMaxStackSize() || !ItemStack.isSameItemSameComponents(stack, slotStack)) {
				continue;
			}
			int count = slotStack.getCount() + stack.getCount();
			int maxSize = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize());
			if (maxSize >= count) {
				slotStack.setCount(count);
				stack.setCount(0);
				slot.setChanged();
				return true;
			} else {
				slotStack.setCount(maxSize);
				stack.setCount(count - maxSize);
				slot.setChanged();
			}
		}
		if (!stack.isEmpty() && firstEmptySlot != -1) {
			Slot slot = this.getSlot(firstEmptySlot);
			slot.setByPlayer(stack.copy());
			stack.setCount(0);
			slot.setChanged();
			return true;
		}
		return stack.isEmpty();
	}
	
	protected boolean moveExtension(@NotNull ItemStack slotStack, int index) {
		AbstractExtensionMenu extensionMenu = this.getExtensionMenu(this.extension);
		return extensionMenu != null && extensionMenu.quickMoveStack(slotStack, index);
	}
	
	public void tick() {
		this.extensionMenus.stream().filter(AbstractExtensionMenu::requiresTickUpdate).forEach(AbstractExtensionMenu::slotsChanged);
	}
	
	@Override
	public void slotsChanged(@NotNull Container container) {
		super.slotsChanged(container);
		this.extensionMenus.forEach((extensionMenu) -> {
			extensionMenu.slotsChanged(container);
		});
	}
	
	@Override
	public boolean clickMenuButton(@NotNull Player player, int button) {
		AbstractExtensionMenu extensionMenu = this.getExtensionMenu(this.extension);
		if (extensionMenu != null) {
			return extensionMenu.clickMenuButton(player, button);
		}
		return super.clickMenuButton(player, button);
	}
	
	@NotNull
	public List<AbstractExtensionMenu> getExtensionMenus() {
		return this.extensionMenus;
	}
	
	protected void addExtensionMenu(@NotNull BackpackExtension extension, @NotNull Player player, @NotNull ExtensionMenuFactory menuFactory) {
		if (!extension.isDisabled()) {
			AbstractExtensionMenu extensionMenu = ExtensionMenuRegistry.getExtensionMenu(extension, this, player, menuFactory);
			extensionMenu.addSlots(this::addSlot);
			this.extensionMenus.add(extensionMenu);
		}
	}
	
	public @Nullable AbstractExtensionMenu getExtensionMenu(@NotNull BackpackExtension extension) {
		return this.extensionMenus.stream().filter((extensionMenu) -> extensionMenu.getExtension() == extension).findAny().orElse(null);
	}
	
	public @NotNull Optional<AbstractExtensionMenu> getExtensionMenu() {
		return Optional.ofNullable(this.getExtensionMenu(this.getExtension()));
	}
}
