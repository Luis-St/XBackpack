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

package net.luis.xbackpack.client.gui.screens.extension;

import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.client.gui.screens.AbstractExtensionContainerScreen;
import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

public class FurnaceExtensionScreen extends AbstractExtensionScreen {
	
	private static final ResourceLocation BURN_PROGRESS_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/furnace/burn_progress");
	private static final ResourceLocation LIT_PROGRESS_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/furnace/lit_progress");
	
	private int cookingProgress;
	private int fuelProgress;
	
	public FurnaceExtensionScreen(@NotNull AbstractExtensionContainerScreen<?> screen, @NotNull List<BackpackExtension> extensions) {
		super(screen, BackpackExtensions.FURNACE.get(), extensions);
	}
	
	@Override
	protected void renderAdditional(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, boolean open) {
		if (open) {
			graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - this.fuelProgress, this.leftPos + this.imageWidth + 5, this.topPos + 104 - this.fuelProgress, 14, this.fuelProgress);
			graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_SPRITE, 24, 17, 0, 0, this.leftPos + this.imageWidth + 24, this.topPos + 88, this.cookingProgress, 17);
		}
	}
	
	public void update(int cookingProgress, int fuelProgress) {
		this.cookingProgress = cookingProgress;
		this.fuelProgress = fuelProgress;
	}
}
