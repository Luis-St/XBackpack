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

package net.luis.xbackpack.world.inventory.modifier;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

public interface ItemModifier {
	
	@NotNull String getName();
	
	@Deprecated // ToDo: Replace by Enum#ordinal
	int getId();
	
	@NotNull ItemModifierType getType();
	
	boolean isSelectable();
	
	default @NotNull List<Component> getTooltip(@NotNull TooltipFlag tooltipFlag) {
		if (tooltipFlag.isAdvanced()) {
			List<MutableComponent> info = this.getInfo();
			if (!info.isEmpty()) {
				List<Component> tooltip = Lists.newArrayList();
				tooltip.add(this.getDisplayName().withStyle(ChatFormatting.WHITE));
				tooltip.addAll(info.stream().map((component) -> component.withStyle(ChatFormatting.GRAY)).toList());
				return tooltip;
			}
		}
		return Lists.newArrayList(this.getDisplayName().withStyle(ChatFormatting.WHITE));
	}
	
	@NotNull MutableComponent getDisplayName();
	
	@NotNull List<MutableComponent> getInfo();
}
