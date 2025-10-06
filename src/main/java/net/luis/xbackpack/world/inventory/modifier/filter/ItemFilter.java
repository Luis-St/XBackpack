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

package net.luis.xbackpack.world.inventory.modifier.filter;

import net.luis.xbackpack.world.inventory.modifier.ItemModifier;
import net.luis.xbackpack.world.inventory.modifier.ItemModifierType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public interface ItemFilter extends ItemModifier {
	
	@Override
	default @NotNull ItemModifierType getType() {
		return ItemModifierType.FILTER;
	}
	
	boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm, boolean negate);
}
