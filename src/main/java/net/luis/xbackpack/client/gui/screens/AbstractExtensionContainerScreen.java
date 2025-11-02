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

package net.luis.xbackpack.client.gui.screens;

import com.google.common.collect.Lists;
import net.luis.xbackpack.client.gui.screens.extension.AbstractExtensionScreen;
import net.luis.xbackpack.network.XBNetworkHandler;
import net.luis.xbackpack.network.packet.extension.UpdateExtensionPacket;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.extension.BackpackExtensionState;
import net.luis.xbackpack.world.inventory.AbstractExtensionContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

import static net.luis.xbackpack.world.extension.BackpackExtensions.*;

/**
 *
 * @author Luis-St
 *
 */

public abstract class AbstractExtensionContainerScreen<T extends AbstractExtensionContainerMenu> extends AbstractScrollableContainerScreen<T> {

	private final List<BackpackExtension> extensions = initExtensions();
	private final List<AbstractExtensionScreen> extensionScreens = Lists.newArrayList();
	private BackpackExtension extension = NO.get();

	private static List<BackpackExtension> initExtensions() {
		List<BackpackExtension> list = new ArrayList<>();
		for (BackpackExtension extension : REGISTRY) {
			if (!extension.isDisabled()) {
				list.add(extension);
			}
		}
		return list;
	}
	
	protected AbstractExtensionContainerScreen(@NotNull T menu, @NotNull Inventory inventory, @NotNull Component titleComponent) {
		super(menu, inventory, titleComponent);
	}
	
	public @NotNull BackpackExtension getExtension() {
		return this.extension == null ? NO.get() : this.extension;
	}
	
	@Override
	protected void init() {
		super.init();
		this.extensionScreens.forEach((extensionScreen) -> extensionScreen.init(this.minecraft, this.font, this.imageWidth, this.imageHeight, this.leftPos, this.topPos));
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderScreen(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);
	}
	
	protected abstract void renderScreen(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);
	
	@Override
	protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		this.renderExtensions(graphics, partialTicks, mouseX, mouseY);
	}
	
	private void renderExtensions(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		for (BackpackExtension extension : this.extensions) {
			AbstractExtensionScreen extensionScreen = this.getExtensionScreen(extension);
			if (extensionScreen != null) {
				if (this.extension == extension && this.extension != NO.get()) {
					extensionScreen.renderOpened(graphics, partialTicks, mouseX, mouseY);
				} else if (this.isExtensionRenderable(extension)) {
					extensionScreen.render(graphics, partialTicks, mouseX, mouseY);
				}
			}
		}
	}
	
	@Override
	protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		super.renderTooltip(graphics, mouseX, mouseY);
		for (BackpackExtension extension : this.extensions) {
			AbstractExtensionScreen extensionScreen = this.getExtensionScreen(extension);
			if (extensionScreen != null && this.canUseExtension(extension)) {
				extensionScreen.renderTooltip(graphics, mouseX, mouseY, this.extension == extension && this.extension != NO.get(), this.isExtensionRenderable(extension), (itemStack) -> {
					List<Component> tooltipLines = itemStack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, this.minecraft.player, this.minecraft.options.advancedItemTooltips ? net.minecraft.world.item.TooltipFlag.ADVANCED : net.minecraft.world.item.TooltipFlag.NORMAL);
					List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> tooltipComponents = tooltipLines.stream()
						.map(component -> net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(component.getVisualOrderText()))
						.toList();
					graphics.renderTooltip(this.font, tooltipComponents, mouseX, mouseY, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null, itemStack);
				});
			}
		}
	}
	
	protected boolean canUseExtension(@NotNull BackpackExtension extension) {
		return BackpackProvider.get(Objects.requireNonNull(Objects.requireNonNull(this.minecraft).player)).getConfig().getExtensionConfig().getWithState(BackpackExtensionState.UNLOCKED).contains(extension);
	}
	
	protected boolean isExtensionRenderable(BackpackExtension extension) {
		if (!this.canUseExtension(extension)) {
			return false;
		} else if (this.extension == NO.get()) {
			return true;
		} else if (this.extensions.indexOf(this.extension) > this.extensions.indexOf(extension)) {
			return true;
		} else {
			return this.getExtensionOffset(extension) > this.getExtensionOffset(this.extension) + this.extension.getImageHeight();
		}
	}
	
	public int getExtensionOffset(@NotNull BackpackExtension extension) {
		int offset = 3;
		for (BackpackExtension backpackExtension : this.extensions) {
			if (backpackExtension == extension) {
				break;
			}
			offset += backpackExtension.getIconHeight() + 2;
		}
		return offset;
	}
	
	protected boolean isInExtension(@NotNull BackpackExtension extension, double mouseX, double mouseY) {
		if (this.extension == extension || this.isExtensionRenderable(extension)) {
			double topX = this.leftPos + this.imageWidth;
			double topY = this.topPos + this.getExtensionOffset(extension);
			return topX + extension.getIconWidth() >= mouseX && mouseX >= topX && topY + extension.getIconHeight() >= mouseY && mouseY >= topY;
		}
		return false;
	}
	
	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean flag) {
		// Check extension icon clicks first
		for (BackpackExtension extension : this.extensions) {
			if (this.isInExtension(extension, event.x(), event.y())) {
				this.updateExtension(extension);
				return true; // Consume the click event
			}
		}
		// Check if the extension screen handles the click
		AbstractExtensionScreen extensionScreen = this.getExtensionScreen(this.extension);
		if (extensionScreen != null && extensionScreen.mouseClicked(event, flag)) {
			return true;
		}
		// Finally, let the parent handle slot clicks
		return super.mouseClicked(event, flag);
	}

	@Override
	public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
		AbstractExtensionScreen extensionScreen = this.getExtensionScreen(this.extension);
		if (extensionScreen != null && extensionScreen.mouseReleased(event)) {
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
		AbstractExtensionScreen extensionScreen = this.getExtensionScreen(this.extension);
		if (extensionScreen != null && extensionScreen.mouseDragged(event, dragX, dragY)) {
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		AbstractExtensionScreen extensionScreen = this.getExtensionScreen(this.extension);
		if (extensionScreen != null && extensionScreen.mouseScrolled(mouseX, mouseY, deltaY)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
	}
	
	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos) {
		int buttonOffset = 21;
		if (this.extensions.stream().noneMatch(this::canUseExtension)) {
			return super.hasClickedOutside(mouseX, mouseY, leftPos, topPos);
		} else if (this.extension == NO.get()) {
			this.imageWidth += buttonOffset;
			boolean flag = super.hasClickedOutside(mouseX, mouseY, leftPos, topPos);
			this.imageWidth -= buttonOffset;
			return flag;
		} else if (leftPos > mouseX) {
			return true;
		} else if (topPos > mouseY) {
			return true;
		} else if (mouseY > topPos + this.imageHeight && leftPos + this.imageWidth > mouseX) {
			return true;
		} else if (mouseX > leftPos + this.imageWidth) {
			int extensionOffset = this.getExtensionOffset(this.extension);
			if (topPos + extensionOffset > mouseY) {
				return true;
			} else if (mouseX > leftPos + this.imageWidth + this.extension.getImageWidth()) {
				return true;
			} else {
				return mouseY > topPos + extensionOffset + this.extension.getImageHeight();
			}
		}
		return false;
	}
	
	protected void addExtensionScreen(@NotNull BiFunction<AbstractExtensionContainerScreen<T>, @NotNull List<BackpackExtension>, @NotNull AbstractExtensionScreen> screenFactory) {
		AbstractExtensionScreen extensionScreen = screenFactory.apply(this, this.extensions);
		if (!extensionScreen.getExtension().isDisabled()) {
			this.extensionScreens.add(extensionScreen);
		}
	}
	
	public AbstractExtensionScreen getExtensionScreen(@NotNull BackpackExtension extension) {
		return this.extensionScreens.stream().filter((extensionScreen) -> extensionScreen.getExtension() == extension).findAny().orElse(null);
	}
	
	private void updateExtension(@Nullable BackpackExtension extension) {
		Objects.requireNonNull(this.minecraft).getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		if (this.extension == extension || extension == null || extension.isDisabled()) {
			this.extension = NO.get();
		} else {
			this.extension = extension;
		}
		XBNetworkHandler.INSTANCE.sendToServer(new UpdateExtensionPacket(this.extension));
	}
}
