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

package net.luis.xbackpack.client.gui.components;

import net.luis.xbackpack.XBackpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class ActionButton extends AbstractButton {
	
	private static final ResourceLocation MODIFIER_BUTTON_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "backpack/modifier_button");
	private static final ResourceLocation MODIFIER_BUTTON_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "backpack/modifier_button_highlighted");
	
	private final Consumer<ClickType> action;
	
	public ActionButton(int x, int y, int width, int height, @NotNull Consumer<ClickType> action) {
		super(x, y, width, height, Component.empty());
		this.action = action;
	}
	
	@Override
	public void onPress(net.minecraft.client.input.InputWithModifiers input) {}

	@Override
	public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		ResourceLocation sprite = this.isHovered() ? MODIFIER_BUTTON_HIGHLIGHTED_SPRITE : MODIFIER_BUTTON_SPRITE;
		graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean flag) {
		if (this.active && this.visible && this.isMouseOver(event.x(), event.y())) {
			if (event.button() == 0) {
				this.playDownSound(Minecraft.getInstance().getSoundManager());
				this.action.accept(ClickType.LEFT);
				return true;
			} else if (event.button() == 1) {
				this.playDownSound(Minecraft.getInstance().getSoundManager());
				this.action.accept(ClickType.RIGHT);
			}
		}
		return false;
	}
	
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationOutput) {}
	
	@Override
	protected void defaultButtonNarrationText(@NotNull NarrationElementOutput narrationOutput) {}
	
	public enum ClickType {
		
		RIGHT(), LEFT()
	}
}
