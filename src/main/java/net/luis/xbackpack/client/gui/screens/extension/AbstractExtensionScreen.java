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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public abstract class AbstractExtensionScreen {
	
	private static final ResourceLocation EXTENSION_BUTTON_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "backpack/extension_button");
	
	protected final AbstractExtensionContainerScreen<?> screen;
	protected final BackpackExtension extension;
	protected final List<BackpackExtension> extensions;
	protected Minecraft minecraft;
	protected Font font;
	protected int imageWidth;
	protected int imageHeight;
	protected int leftPos;
	protected int topPos;
	
	protected AbstractExtensionScreen(@NotNull AbstractExtensionContainerScreen<?> screen, @NotNull BackpackExtension extension, @NotNull List<BackpackExtension> extensions) {
		this.screen = screen;
		this.extension = extension;
		this.extensions = extensions;
	}
	
	public final void init(@Nullable Minecraft minecraft, @NotNull Font font, int imageWidth, int imageHeight, int leftPos, int topPos) {
		this.minecraft = minecraft;
		this.font = font;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.leftPos = leftPos;
		this.topPos = topPos;
		if (this.minecraft != null) {
			this.init();
		}
	}
	
	protected void init() {}
	
	protected int getExtensionOffset(@NotNull BackpackExtension extension) {
		int offset = 3;
		for (BackpackExtension backpackExtension : this.extensions) {
			if (backpackExtension == extension) {
				break;
			}
			offset += extension.getIconHeight() + 2;
		}
		return offset;
	}
	
	protected boolean isExtensionRenderable(@NotNull BackpackExtension extension) {
		if (this.extension == BackpackExtensions.NO.get()) {
			return true;
		} else if (this.extensions.indexOf(this.extension) > this.extensions.indexOf(extension)) {
			return true;
		} else {
			return this.getExtensionOffset(extension) > this.getExtensionOffset(this.extension) + this.extension.getImageHeight();
		}
	}
	
	protected boolean isInExtension(BackpackExtension extension, double mouseX, double mouseY) {
		if (this.extension == extension || this.isExtensionRenderable(extension)) {
			double topX = this.leftPos + this.imageWidth;
			double topY = this.topPos + this.getExtensionOffset(extension);
			return topX + extension.getIconWidth() >= mouseX && mouseX >= topX && topY + extension.getIconHeight() >= mouseY && mouseY >= topY;
		}
		return false;
	}
	
	protected @NotNull ResourceLocation getTexture() {
		ResourceLocation location = Objects.requireNonNull(BackpackExtensions.REGISTRY.getKey(this.extension));
		return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "textures/gui/container/" + location.getPath() + "_extension.png");
	}

	public void render(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		int offset = this.getExtensionOffset(this.extension);
		int iconWidth = this.extension.getIconWidth();
		int iconHeight = this.extension.getIconHeight();
		graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, EXTENSION_BUTTON_SPRITE, this.leftPos + this.imageWidth, this.topPos + offset, iconWidth, iconHeight);
		graphics.renderItem(this.extension.getIcon(), this.leftPos + this.imageWidth + 1, this.topPos + 3 + offset);
	}
	
	public void renderOpened(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		int offset = this.getExtensionOffset(this.extension);
		int x = this.leftPos + this.imageWidth - 3;
		int y = this.topPos + offset;
		graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, this.getTexture(), x, y, 0.0F, 0.0F, 150, 150, 256, 256);
		graphics.renderItem(this.extension.getIcon(), this.leftPos + this.imageWidth + 1, this.topPos + 4 + offset);
		graphics.renderItemDecorations(this.font, this.extension.getIcon(), this.leftPos + this.imageWidth + 1, this.topPos + 4 + offset);
		graphics.drawString(this.font, this.extension.getTitle(), this.leftPos + this.imageWidth + 19, this.topPos + 9 + offset, 4210752, false);
		if (this.minecraft != null) {
			this.renderAdditional(graphics, partialTicks, mouseX, mouseY, true);
		}
	}

	protected void renderAdditional(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, boolean open) {}

	public void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY, boolean open, boolean renderable, @NotNull Consumer<ItemStack> tooltipRenderer) {
		if (this.isInExtension(this.extension, mouseX, mouseY) && !open && renderable) {
			List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> tooltipComponents =
				java.util.List.of(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(this.extension.getTooltip().getVisualOrderText()));
			graphics.renderTooltip(this.font, tooltipComponents, mouseX, mouseY, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null);
		}
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}
	
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}
	
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return false;
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return false;
	}
	
	public @NotNull BackpackExtension getExtension() {
		return this.extension;
	}
}
