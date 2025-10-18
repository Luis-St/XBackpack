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

package net.luis.xbackpack.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.client.gui.screens.BackpackScreen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

@JeiPlugin
public class XBJeiPlugin implements IModPlugin {

	private final ResourceLocation pluginId;

	public XBJeiPlugin() {
		this.pluginId = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "jei");
	}

	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return this.pluginId;
	}

	@Override
	public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(BackpackScreen.class, new BackpackContainerHandler());
	}
}
