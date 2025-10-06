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

package net.luis.xbackpack.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.luis.xbackpack.world.inventory.slot.MoveableSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public abstract class AbstractScrollableContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_back");
	private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front");

	// Reflection fields for accessing private AbstractContainerScreen fields
	private static Field draggingItemField;
	private static Field isSplittingStackField;
	private static Field clickedSlotField;
	private static Field quickCraftingTypeField;
	private static Field quickCraftingRemainderField;

	static {
		try {
			draggingItemField = AbstractContainerScreen.class.getDeclaredField("draggingItem");
			draggingItemField.setAccessible(true);
			isSplittingStackField = AbstractContainerScreen.class.getDeclaredField("isSplittingStack");
			isSplittingStackField.setAccessible(true);
			clickedSlotField = AbstractContainerScreen.class.getDeclaredField("clickedSlot");
			clickedSlotField.setAccessible(true);
			quickCraftingTypeField = AbstractContainerScreen.class.getDeclaredField("quickCraftingType");
			quickCraftingTypeField.setAccessible(true);
			quickCraftingRemainderField = AbstractContainerScreen.class.getDeclaredField("quickCraftingRemainder");
			quickCraftingRemainderField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Failed to access AbstractContainerScreen private fields", e);
		}
	}

	private boolean scrolling = false;
	protected int scrollOffset = 0;
	
	protected AbstractScrollableContainerScreen(@NotNull T menu, @NotNull Inventory inventory, @NotNull Component titleComponent) {
		super(menu, inventory, titleComponent);
	}

	// Helper methods to access private fields via reflection
	protected ItemStack getDraggingItem() {
		try {
			return (ItemStack) draggingItemField.get(this);
		} catch (IllegalAccessException e) {
			return ItemStack.EMPTY;
		}
	}

	protected boolean isSplittingStack() {
		try {
			return isSplittingStackField.getBoolean(this);
		} catch (IllegalAccessException e) {
			return false;
		}
	}

	protected Slot getClickedSlot() {
		try {
			return (Slot) clickedSlotField.get(this);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	protected int getQuickCraftingType() {
		try {
			return quickCraftingTypeField.getInt(this);
		} catch (IllegalAccessException e) {
			return 0;
		}
	}

	protected int getQuickCraftingRemainder() {
		try {
			return quickCraftingRemainderField.getInt(this);
		} catch (IllegalAccessException e) {
			return 0;
		}
	}
	
	@Override
	protected abstract void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY);
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		//region Avoid super call
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		for (Renderable widget : this.renderables) {
			widget.render(graphics, mouseX, mouseY, partialTicks);
		}
		//endregion
		graphics.pose().pushMatrix();
		graphics.pose().translate(this.leftPos, this.topPos);

		this.hoveredSlot = this.getHoveredSlot(mouseX, mouseY);
		this.renderSlotHighlightBack(graphics);
		this.renderSlots(graphics);
		this.renderSlotHighlightFront(graphics);

		this.renderLabels(graphics, mouseX, mouseY);
		ItemStack draggingItem = this.getDraggingItem();
		ItemStack mouseStack = draggingItem.isEmpty() ? this.menu.getCarried() : draggingItem;
		if (!mouseStack.isEmpty()) {
			int renderOffset = draggingItem.isEmpty() ? 8 : 16;
			String count = null;
			if (!draggingItem.isEmpty() && this.isSplittingStack()) {
				mouseStack = mouseStack.copyWithCount(Mth.ceil(mouseStack.getCount() / 2.0F));
			} else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
				mouseStack = mouseStack.copyWithCount(this.getQuickCraftingRemainder());
				if (mouseStack.isEmpty()) {
					count = ChatFormatting.YELLOW + "0";
				}
			}
			int x = mouseX - this.leftPos - 8;
			int y = mouseY - this.topPos - renderOffset;
			graphics.renderItem(mouseStack, x, y);
			graphics.renderItemDecorations(this.font, mouseStack, x, y, count);
		}

		graphics.pose().popMatrix();
		this.renderTooltip(graphics, mouseX, mouseY);
	}
	
	protected @NotNull SlotRenderType getSlotRenderType(@NotNull Slot slot) {
		return SlotRenderType.DEFAULT;
	}
	
	@Override
	protected void renderSlots(@NotNull GuiGraphics graphics) {
		for (Slot slot : this.menu.slots) {
			if (slot.isActive() && this.getSlotRenderType(slot) != SlotRenderType.SKIP) {
				this.renderSlot(graphics, slot);
			}
		}
	}
	
	protected void renderSlotHighlightBack(@NotNull GuiGraphics graphics) {
		this.renderSlotHighlight(graphics, SLOT_HIGHLIGHT_BACK_SPRITE);
	}

	protected void renderSlotHighlightFront(@NotNull GuiGraphics graphics) {
		this.renderSlotHighlight(graphics, SLOT_HIGHLIGHT_FRONT_SPRITE);
	}
	
	private void renderSlotHighlight(@NotNull GuiGraphics graphics, @NotNull ResourceLocation sprite) {
		Slot slot = this.hoveredSlot;
		if (slot != null && slot.isHighlightable() && this.getSlotRenderType(slot) != SlotRenderType.SKIP) {
			int y = slot instanceof MoveableSlot moveableSlot ? moveableSlot.getY(this.scrollOffset) : slot.y;
			graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, slot.x - 4, y - 4, 24, 24);
		}
	}
	
	@Override
	protected void renderSlot(@NotNull GuiGraphics graphics, @NotNull Slot slot) {
		int y = slot instanceof MoveableSlot moveableSlot ? moveableSlot.getY(this.scrollOffset) : slot.y;
		ItemStack slotStack = slot.getItem();
		ItemStack carriedStack = this.menu.getCarried();
		ItemStack draggingItem = this.getDraggingItem();
		Slot clickedSlot = this.getClickedSlot();
		boolean isSplittingStack = this.isSplittingStack();
		boolean quickReplace = false;
		boolean isClickedSlot = slot == clickedSlot && !draggingItem.isEmpty() && !isSplittingStack;
		String stackCount = null;
		if (slot == clickedSlot && !draggingItem.isEmpty() && isSplittingStack && !slotStack.isEmpty()) {
			slotStack = slotStack.copyWithCount(slotStack.getCount() / 2);
		} else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carriedStack.isEmpty()) {
			if (this.quickCraftSlots.size() == 1) {
				return;
			}
			if (AbstractContainerMenu.canItemQuickReplace(slot, carriedStack, true) && this.menu.canDragTo(slot)) {
				quickReplace = true;
				int count = Math.min(carriedStack.getMaxStackSize(), slot.getMaxStackSize(carriedStack));
				int craftPlaceCount = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.getQuickCraftingType(), carriedStack) + (slot.getItem().isEmpty() ? 0 : slot.getItem().getCount());
				if (craftPlaceCount > count) {
					craftPlaceCount = count;
					stackCount = ChatFormatting.YELLOW.toString() + count;
				}
				slotStack = carriedStack.copyWithCount(craftPlaceCount);
			} else {
				this.quickCraftSlots.remove(slot);
			}
		}
		graphics.pose().pushMatrix();
		graphics.pose().translate(0.0f, 0.0f);
		if (slotStack.isEmpty() && slot.isActive()) {
			ResourceLocation icon = slot.getNoItemIcon();
			if (icon != null) {
				graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, icon, slot.x, y, 16, 16);
				isClickedSlot = true;
			}
		}
		if (!isClickedSlot) {
			if (quickReplace) {
				graphics.fill(slot.x, y, slot.x + 16, y + 16, -2130706433);
			}
			int modelOffset = slot.x + slot.y * this.imageWidth;
			if (slot.isFake()) {
				graphics.renderFakeItem(slotStack, slot.x, y, modelOffset);
			} else {
				graphics.renderItem(slotStack, slot.x, y, modelOffset);
			}
			graphics.renderItemDecorations(this.font, slotStack, slot.x, y, stackCount);
		}
		graphics.pose().popMatrix();
	}
	
	protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem() && this.getSlotRenderType(this.hoveredSlot) == SlotRenderType.DEFAULT) {
			ItemStack itemStack = this.hoveredSlot.getItem();
			List<Component> tooltipLines = itemStack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, this.minecraft.player, this.minecraft.options.advancedItemTooltips ? net.minecraft.world.item.TooltipFlag.ADVANCED : net.minecraft.world.item.TooltipFlag.NORMAL);
			List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> tooltipComponents = tooltipLines.stream()
				.map(component -> net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(component.getVisualOrderText()))
				.toList();
			graphics.renderTooltip(this.font, tooltipComponents, mouseX, mouseY, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null, itemStack);
		}
	}

	public @Nullable Slot getHoveredSlot(double mouseX, double mouseY) {
		for (int i = 0; i < this.menu.slots.size(); ++i) {
			Slot slot = this.menu.slots.get(i);
			if (this.isHovering(slot, mouseX, mouseY) && this.getSlotRenderType(slot) == SlotRenderType.DEFAULT) {
				return slot;
			}
		}
		return null;
	}
	
	protected abstract int getScrollbarWidth();
	
	protected abstract int getScrollbarHeight();
	
	protected abstract boolean isInScrollbar(double mouseX, double mouseY);
	
	protected abstract int clampMouseMove(double mouseY);
	
	protected abstract int clampMouseScroll(double delta);
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && this.isInScrollbar(mouseX, mouseY)) {
			this.scrolling = true;
			this.scrollOffset = this.clampMouseMove(mouseY);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.scrolling = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == 0 && this.scrolling) {
			this.scrollOffset = this.clampMouseMove(mouseY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		this.scrollOffset = this.clampMouseScroll(deltaY);
		return true;
	}

	public boolean isHovering(@NotNull Slot slot, double mouseX, double mouseY) {
		return this.isHovering(slot.x, slot instanceof MoveableSlot moveableSlot ? moveableSlot.getY(this.scrollOffset) : slot.y, 16, 16, mouseX, mouseY);
	}
}
