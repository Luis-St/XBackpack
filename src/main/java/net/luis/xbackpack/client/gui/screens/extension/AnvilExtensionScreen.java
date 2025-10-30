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
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.extension.AnvilExtensionMenu;
import net.luis.xbackpack.world.inventory.handler.CraftingHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class AnvilExtensionScreen extends AbstractExtensionScreen {
	
	private static final ResourceLocation ERROR_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/anvil/error");
	
	private CraftingHandler handler;
	private int cost;
	
	public AnvilExtensionScreen(@NotNull AbstractExtensionContainerScreen<?> screen, @NotNull List<BackpackExtension> extensions) {
		super(screen, BackpackExtensions.ANVIL.get(), extensions);
	}
	
	@Override
	protected void init() {
		this.handler = BackpackProvider.get(Objects.requireNonNull(this.minecraft.player)).getAnvilHandler();
	}
	
	@Override
	protected void renderAdditional(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, boolean open) {
		if (open) {
			if (this.shouldRenderCanceled()) {
				graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, this.leftPos + this.imageWidth + 59, this.topPos + 71, 22, 21);
			}
			this.renderLabels(graphics);
		}
	}
	
	private boolean shouldRenderCanceled() {
		if (!this.handler.getInputHandler().getStackInSlot(0).isEmpty() || !this.handler.getInputHandler().getStackInSlot(1).isEmpty()) {
			return this.handler.getResultHandler().getStackInSlot(0).isEmpty();
		}
		return false;
	}
	
	private void renderLabels(@NotNull GuiGraphics graphics) {
		if (this.screen.getMenu().getExtensionMenu(this.extension) instanceof AnvilExtensionMenu menu) {
			if (this.cost > 0) {
				int color = 8453920;
				Component component = null;
				if (!this.handler.getResultHandler().getStackInSlot(0).isEmpty() && this.minecraft != null) {
					component = Component.translatable("xbackpack.backpack_extension.anvil.cost", this.cost);
					if (!menu.mayPickup(Objects.requireNonNull(this.minecraft.player))) {
						color = 16736352;
					}
				}
				if (component != null) {
					int drawColor = 0xFF000000 | color; // ensure cost text renders fully opaque
					int x = this.leftPos + this.imageWidth + 64;
					int y = this.topPos + 9 + this.getExtensionOffset(this.extension);
					if (10 > this.cost) {
						graphics.fill(x - 1, y - 2, x + this.font.width(component) + 3, y + 10, 1325400064);
						graphics.drawString(this.font, component, x + 1, y, drawColor);
					} else if (100 > this.cost) {
						graphics.fill(x - 7, y - 2, x + this.font.width(component) - 3, y + 10, 1325400064);
						graphics.drawString(this.font, component, x - 5, y, drawColor);
					} else {
						graphics.fill(x - 13, y - 2, x + this.font.width(component) - 9, y + 10, 1325400064);
						graphics.drawString(this.font, component, x - 11, y, drawColor);
					}
				}
			}
		}
	}
	
	public void update(int cost) {
		this.cost = cost;
	}
}
