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

package net.luis.xbackpack.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.luis.xbackpack.client.gui.screens.BackpackScreen;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackContainerHandler implements IGuiContainerHandler<BackpackScreen> {

	@Override
	public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull BackpackScreen screen) {
		LocalPlayer player = Objects.requireNonNull(screen.getMinecraft()).player;
		if (player == null) {
			return Lists.newArrayList();
		}
		if (!BackpackProvider.get(player).getConfig().getExtensionConfig().getWithState(BackpackExtensionState.UNLOCKED).isEmpty()) {
			// Extension icons area on the right side
			List<Rect2i> extraAreas = Lists.newArrayList(new Rect2i(screen.getGuiLeft() + screen.getXSize(), screen.getGuiTop(), 21, screen.getYSize()));
			BackpackExtension extension = screen.getExtension();
			// If an extension is open, add its area
			if (extension != BackpackExtensions.NO.get()) {
				extraAreas.add(new Rect2i(screen.getGuiLeft() + screen.getXSize(), screen.getGuiTop() + screen.getExtensionOffset(extension), extension.getImageWidth(), extension.getImageHeight()));
			}
			return extraAreas;
		}
		return Lists.newArrayList();
	}
}
