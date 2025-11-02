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
import com.mojang.blaze3d.platform.InputConstants;
import net.luis.xbackpack.client.XBKeyMappings;
import net.luis.xbackpack.client.gui.components.ActionButton;
import net.luis.xbackpack.client.gui.components.ActionButton.ClickType;
import net.luis.xbackpack.client.gui.components.RenderData;
import net.luis.xbackpack.event.client.ClientEventHandler;
import net.luis.xbackpack.world.inventory.AbstractModifiableContainerMenu;
import net.luis.xbackpack.world.inventory.modifier.ItemModifierType;
import net.luis.xbackpack.world.inventory.modifier.filter.ItemFilters;
import net.luis.xbackpack.world.inventory.modifier.sorter.ItemSorters;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public abstract class AbstractModifiableContainerScreen<T extends AbstractModifiableContainerMenu> extends AbstractExtensionContainerScreen<T> {
	
	private EditBox searchBox;
	private boolean ignoreTextInput;
	private ActionButton filterButton;
	private ActionButton sorterButton;
	private ActionButton mergerButton;
	
	protected AbstractModifiableContainerScreen(@NotNull T menu, @NotNull Inventory inventory, @NotNull Component titleComponent) {
		super(menu, inventory, titleComponent);
	}
	
	@Override
	protected void init() {
		super.init();
		this.searchBox = this.getSearchData().addIfExists((xPosition, yPosition, width, height) -> {
			EditBox searchBox = new EditBox(this.font, xPosition, yPosition, width, height, Component.empty());
			searchBox.setMaxLength(this.getMaxSearchLength());
			searchBox.setBordered(false);
			searchBox.setVisible(true);
			searchBox.setCanLoseFocus(true);
			searchBox.setTextColor(16777215);
			return searchBox;
		}, this::addWidget);
		this.filterButton = this.getFilterData().addIfExists((xPosition, yPosition, width, height) -> {
			return new ActionButton(xPosition, yPosition, width, height, (type) -> {
				Objects.requireNonNull(Objects.requireNonNull(this.minecraft).gameMode).handleInventoryButtonClick(this.menu.containerId, Objects.requireNonNull(type) == ClickType.LEFT ? 0 : 1);
			});
		}, this::addRenderableWidget);
		this.sorterButton = this.getSorterData().addIfExists((xPosition, yPosition, width, height) -> {
			return new ActionButton(xPosition, yPosition, width, height, (type) -> {
				Objects.requireNonNull(Objects.requireNonNull(this.minecraft).gameMode).handleInventoryButtonClick(this.menu.containerId, Objects.requireNonNull(type) == ClickType.LEFT ? 2 : 3);
			});
		}, this::addRenderableWidget);
		this.mergerButton = this.getMergerData().addIfExists((xPosition, yPosition, width, height) -> {
			return new ActionButton(xPosition, yPosition, width, height, (type) -> {
				Objects.requireNonNull(Objects.requireNonNull(this.minecraft).gameMode).handleInventoryButtonClick(this.menu.containerId, 4);
			});
		}, this::addRenderableWidget);
	}
	
	protected abstract @NotNull RenderData getSearchData();
	
	protected abstract int getMaxSearchLength();
	
	protected abstract @NotNull RenderData getFilterData();
	
	protected abstract @NotNull RenderData getSorterData();
	
	protected abstract @NotNull RenderData getMergerData();
	
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		super.resize(minecraft, width, height);
		if (this.searchBox != null) {
			String searchBoxValue = this.searchBox.getValue();
			this.init(minecraft, width, height);
			this.searchBox.setValue(searchBoxValue);
			if (!this.searchBox.getValue().isEmpty()) {
				this.updateSearchTerm(searchBoxValue);
			}
		}
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.mergerButton.visible = this.menu.getFilter() == ItemFilters.NONE && this.menu.getSorter() == ItemSorters.NONE;
		super.render(graphics, mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void renderScreen(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (this.searchBox != null) {
			this.searchBox.render(graphics, mouseX, mouseY, partialTicks);
		}
	}
	
	@Override
	protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		super.renderTooltip(graphics, mouseX, mouseY);
		TooltipFlag tooltipFlag = this.getTooltipFlag();
		if (this.filterButton != null && this.filterButton.isMouseOver(mouseX, mouseY)) {
			List<Component> components = this.menu.getFilter().getTooltip(tooltipFlag);
			List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> tooltipComponents = components.stream()
				.map(component -> net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(component.getVisualOrderText()))
				.toList();
			graphics.renderTooltip(this.font, tooltipComponents, mouseX, mouseY, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null);
		}
		if (this.sorterButton != null && this.sorterButton.isMouseOver(mouseX, mouseY)) {
			List<Component> components = this.menu.getSorter().getTooltip(tooltipFlag);
			List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> tooltipComponents = components.stream()
				.map(component -> net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(component.getVisualOrderText()))
				.toList();
			graphics.renderTooltip(this.font, tooltipComponents, mouseX, mouseY, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null);
		}
		if (this.mergerButton != null && this.mergerButton.visible && this.mergerButton.isMouseOver(mouseX, mouseY)) {
			List<Component> components = this.getMergerTooltip(tooltipFlag);
			List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> tooltipComponents = components.stream()
				.map(component -> net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(component.getVisualOrderText()))
				.toList();
			graphics.renderTooltip(this.font, tooltipComponents, mouseX, mouseY, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null);
		}
	}
	
	protected @NotNull List<Component> getMergerTooltip(@NotNull TooltipFlag tooltipFlag) {
		if (tooltipFlag.isAdvanced()) {
			List<Component> tooltip = Lists.newArrayList();
			tooltip.add(Component.translatable("xbackpack.backpack_action.item_merger").withStyle(ChatFormatting.WHITE));
			tooltip.add(Component.translatable("xbackpack.backpack_action.item_merger.info").withStyle(ChatFormatting.GRAY));
			return tooltip;
		}
		return Lists.newArrayList(Component.translatable("xbackpack.backpack_action.item_merger").withStyle(ChatFormatting.WHITE));
	}
	
	protected abstract @NotNull TooltipFlag getTooltipFlag();
	
	@Override
	public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
		if (this.ignoreTextInput) {
			return false;
		} else if (this.searchBox != null) {
			String searchBoxValue = this.searchBox.getValue();
			if (this.searchBox.charTyped(event)) {
				if (!Objects.equals(searchBoxValue, this.searchBox.getValue())) {
					this.updateSearchTerm(this.searchBox.getValue());
				}
				return true;
			} else {
				return false;
			}
		}
		return super.charTyped(event);
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		this.ignoreTextInput = false;
		var key = com.mojang.blaze3d.platform.InputConstants.getKey(event);
		if (this.hoveredSlot != null && this.hoveredSlot.hasItem() && key.getNumericKeyValue().isPresent()) {
			this.ignoreTextInput = true;
			return true;
		} else if (this.searchBox != null && this.searchBox.isFocused()) {
			String searchBoxValue = this.searchBox.getValue();
			if (this.searchBox.keyPressed(event)) {
				if (!Objects.equals(searchBoxValue, this.searchBox.getValue())) {
					this.updateSearchTerm(this.searchBox.getValue());
				}
				return true;
			} else {
				return this.searchBox.isFocused() && this.searchBox.isVisible() && event.key() != 256 || super.keyPressed(event);
			}
		} else {
			if (XBKeyMappings.BACKPACK_OPEN.getKey().getValue() == event.key()) {
				ClientEventHandler.lastPacket = 10;
				this.onClose();
				return true;
			} else if (event.key() == 256) {
				if (this.filterButton != null && this.filterButton.isHovered()) {
					this.updateItemModifier(ItemModifierType.FILTER);
					return true;
				} else if (this.sorterButton != null && this.sorterButton.isHovered()) {
					this.updateItemModifier(ItemModifierType.SORTER);
					return true;
				}
			}
			return super.keyPressed(event);
		}
	}
	
	@Override
	public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
		this.ignoreTextInput = false;
		return super.keyReleased(event);
	}
	
	protected abstract void updateSearchTerm(@NotNull String searchBoxValue);
	
	protected abstract void updateItemModifier(@NotNull ItemModifierType modifierType);
}
