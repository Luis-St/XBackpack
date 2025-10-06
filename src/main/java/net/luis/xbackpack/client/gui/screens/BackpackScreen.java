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

import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.client.gui.components.RenderData;
import net.luis.xbackpack.client.gui.screens.extension.*;
import net.luis.xbackpack.network.XBNetworkHandler;
import net.luis.xbackpack.network.packet.modifier.ResetItemModifierPacket;
import net.luis.xbackpack.network.packet.modifier.UpdateSearchTermPacket;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.inventory.BackpackMenu;
import net.luis.xbackpack.world.inventory.extension.slot.ExtensionMenuSlot;
import net.luis.xbackpack.world.inventory.modifier.ItemModifierType;
import net.luis.xbackpack.world.inventory.modifier.filter.ItemFilters;
import net.luis.xbackpack.world.inventory.modifier.sorter.ItemSorters;
import net.luis.xbackpack.world.inventory.slot.BackpackSlot;
import net.luis.xbackpack.world.inventory.slot.MoveableSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackScreen extends AbstractModifiableContainerScreen<BackpackMenu> {
	
	private static final ResourceLocation BACKPACK = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "textures/gui/container/backpack.png");
	private static final ResourceLocation FILTER_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "backpack/filter");
	private static final ResourceLocation SORTER_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "backpack/sorter");
	private static final ResourceLocation MERGER_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "backpack/merger");
	private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "backpack/scroller");
	
	private @Nullable RenderData searchData;
	private @Nullable RenderData filterData;
	private @Nullable RenderData sorterData;
	private @Nullable RenderData mergerData;
	
	public BackpackScreen(BackpackMenu menu, Inventory inventory, Component titleComponent) {
		super(menu, inventory, titleComponent);
		this.imageWidth = 220;
		this.imageHeight = 220;
		this.inventoryLabelX += 22;
		this.inventoryLabelY = 127;
		this.addExtensionScreen(CraftingExtensionScreen::new);
		this.addExtensionScreen(FurnaceExtensionScreen::new);
		this.addExtensionScreen(AnvilExtensionScreen::new);
		this.addExtensionScreen(EnchantmentTableExtensionScreen::new);
		this.addExtensionScreen(StonecutterExtensionScreen::new);
		this.addExtensionScreen(BrewingStandExtensionScreen::new);
		this.addExtensionScreen(GrindstoneExtensionScreen::new);
		this.addExtensionScreen(SmithingTableExtensionScreen::new);
	}
	
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		this.searchData = null;
		this.filterData = null;
		this.sorterData = null;
		this.mergerData = null;
		super.resize(minecraft, width, height);
	}
	
	@Override
	protected @NotNull SlotRenderType getSlotRenderType(@NotNull Slot slot) {
		if (slot instanceof ExtensionMenuSlot extensionSlot) {
			return this.getExtension() == extensionSlot.getExtension() ? SlotRenderType.DEFAULT : SlotRenderType.SKIP;
		} else if (slot instanceof BackpackSlot) {
			int y = slot instanceof MoveableSlot moveableSlot ? moveableSlot.getY(this.scrollOffset) : slot.y;
			if (174 >= slot.x && slot.x >= 30 && 108 >= y && y >= 18) {
				return SlotRenderType.DEFAULT;
			} else {
				return SlotRenderType.SKIP;
			}
		}
		return super.getSlotRenderType(slot);
	}
	
	@Override
	protected void renderScreen(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderScreen(graphics, mouseX, mouseY, partialTicks);
		graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, FILTER_SPRITE, this.leftPos + 75, this.topPos + 6, 8, 8);
		graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SORTER_SPRITE, this.leftPos + 89, this.topPos + 6, 8, 8);
		if (this.menu.getFilter() == ItemFilters.NONE && this.menu.getSorter() == ItemSorters.NONE) {
			graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, MERGER_SPRITE, this.leftPos + 200, this.topPos + 6, 8, 8);
		}
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);
		graphics.blit(BACKPACK, this.leftPos, this.topPos, 0, 0, 220, 220, 256, 256);
		int scrollPosition = this.topPos + 18 + this.scrollOffset;
		graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, this.leftPos + 198, scrollPosition, 12, 15);
	}
	
	@Override
	protected int getScrollbarWidth() {
		return 14;
	}
	
	@Override
	protected int getScrollbarHeight() {
		return 108;
	}
	
	@Override
	protected boolean isInScrollbar(double mouseX, double mouseY) {
		double topX = this.leftPos + 198.0;
		double topY = this.topPos + 16.0;
		return topX + this.getScrollbarWidth() >= mouseX && mouseX >= topX && topY + this.getScrollbarHeight() >= mouseY && mouseY >= topY;
	}
	
	@Override
	protected int clampMouseMove(double mouseY) {
		return Mth.clamp((int) (mouseY - this.topPos - 25.5), 0, this.getScrollbarHeight() - 17);
	}
	
	@Override
	protected int clampMouseScroll(double delta) {
		return Mth.clamp((int) (this.scrollOffset - delta), 0, this.getScrollbarHeight() - 17);
	}
	
	@Override
	public @Nullable Slot getHoveredSlot(double mouseX, double mouseY) {
		return super.getHoveredSlot(mouseX, mouseY);
	}
	
	@Override
	protected @NotNull RenderData getSearchData() {
		if (this.searchData == null) {
			this.searchData = new RenderData(true, this.leftPos + 103, this.topPos + 6, 86, 9);
		}
		return this.searchData;
	}
	
	@Override
	protected int getMaxSearchLength() {
		return 50;
	}
	
	@Override
	protected @NotNull RenderData getFilterData() {
		if (this.filterData == null) {
			this.filterData = new RenderData(true, this.leftPos + 73, this.topPos + 4, 12, 12);
		}
		return this.filterData;
	}
	
	@Override
	protected @NotNull RenderData getSorterData() {
		if (this.sorterData == null) {
			this.sorterData = new RenderData(true, this.leftPos + 87, this.topPos + 4, 12, 12);
		}
		return this.sorterData;
	}
	
	@Override
	protected @NotNull RenderData getMergerData() {
		if (this.mergerData == null) {
			this.mergerData = new RenderData(true, this.leftPos + 198, this.topPos + 4, 12, 12);
		}
		return this.mergerData;
	}
	
	@Override
	protected @NotNull TooltipFlag getTooltipFlag() {
		return Objects.requireNonNull(BackpackProvider.get(Objects.requireNonNull(Objects.requireNonNull(this.minecraft).player)).getConfig().getClientConfig()).shouldShowModifierInfo() ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
	}
	
	@Override
	protected void updateSearchTerm(@NotNull String searchBoxValue) {
		XBNetworkHandler.INSTANCE.sendToServer(new UpdateSearchTermPacket(searchBoxValue));
	}
	
	@Override
	protected void updateItemModifier(@NotNull ItemModifierType modifierType) {
		XBNetworkHandler.INSTANCE.sendToServer(new ResetItemModifierPacket(modifierType));
	}
}
