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
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class ExtensionResultSlot extends ResultSlot implements ExtensionMenuSlot {
	
	private final AbstractExtensionMenu extensionMenu;
	
	public ExtensionResultSlot(@NotNull AbstractExtensionMenu extensionMenu, @NotNull Player player, @NotNull CraftingContainer craftingContainer, @NotNull Container container, int index, int xPosition, int yPosition) {
		super(player, craftingContainer, container, index, xPosition, yPosition);
		this.extensionMenu = extensionMenu;
	}
	
	@Override
	public @NotNull AbstractExtensionMenu getMenu() {
		return this.extensionMenu;
	}
	
	@Override
	public @NotNull BackpackExtension getExtension() {
		return this.extensionMenu.getExtension();
	}
}
