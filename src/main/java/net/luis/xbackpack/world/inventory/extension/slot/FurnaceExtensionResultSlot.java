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

import net.luis.xbackpack.world.inventory.extension.FurnaceExtensionMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class FurnaceExtensionResultSlot extends ExtensionSlot {
	
	private final Player player;
	private int removeCount;
	
	public FurnaceExtensionResultSlot(@NotNull FurnaceExtensionMenu extensionMenu, @NotNull Player player, @NotNull IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(extensionMenu, itemHandler, index, xPosition, yPosition);
		this.player = player;
	}
	
	@Override
	public @NotNull FurnaceExtensionMenu getMenu() {
		return (FurnaceExtensionMenu) super.getMenu();
	}
	
	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return false;
	}
	
	@Override
	public @NotNull ItemStack remove(int amount) {
		if (this.hasItem()) {
			this.removeCount += Math.min(amount, this.getItem().getCount());
		}
		return super.remove(amount);
	}
	
	@Override
	public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
		this.checkTakeAchievements(stack);
		super.onTake(player, stack);
	}
	
	@Override
	protected void onQuickCraft(@NotNull ItemStack stack, int amount) {
		this.removeCount += amount;
		this.checkTakeAchievements(stack);
	}
	
	@Override
	protected void checkTakeAchievements(@NotNull ItemStack stack) {
		stack.onCraftedBy(this.player, this.removeCount);
		EventHooks.firePlayerSmeltedEvent(this.player, stack, this.removeCount);
		this.removeCount = 0;
	}
}
