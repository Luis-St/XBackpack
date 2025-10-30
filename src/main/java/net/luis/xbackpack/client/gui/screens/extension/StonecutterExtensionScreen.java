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

package net.luis.xbackpack.client.gui.screens.extension;

import com.google.common.collect.Lists;
import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.client.gui.screens.AbstractExtensionContainerScreen;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.handler.CraftingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class StonecutterExtensionScreen extends AbstractExtensionScreen {
	
	private static final ResourceLocation RECIPE_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/stonecutter/recipe");
	private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/stonecutter/recipe_highlighted");
	private static final ResourceLocation RECIPE_SELECTED_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/stonecutter/recipe_selected");
	private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/stonecutter/scroller");
	private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/stonecutter/scroller_disabled");
	
	private final List<ItemStack> recipesForInput = Lists.newArrayList();
	private Player player;
	private CraftingHandler handler;
	private double scrollOffset;
	private boolean scrolling;
	private int startIndex;
	private int selectedRecipe;
	
	public StonecutterExtensionScreen(@NotNull AbstractExtensionContainerScreen<?> screen, @NotNull List<BackpackExtension> extensions) {
		super(screen, BackpackExtensions.STONECUTTER.get(), extensions);
	}
	
	@Override
	protected void init() {
		this.player = Objects.requireNonNull(this.minecraft.player);
		this.handler = BackpackProvider.get(this.player).getStonecutterHandler();
	}
	
	@Override
	protected void renderAdditional(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, boolean open) {
		if (open) {
			ResourceLocation sprite = this.isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
			graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, this.leftPos + this.imageWidth + 72, this.topPos + 143 + (int) (39.0 * this.scrollOffset), 12, 15);
			this.renderButtons(graphics, mouseX, mouseY);
			this.renderRecipes(graphics);
		}
	}

	private void renderButtons(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
		int maxRecipes = this.startIndex + 12;
		for (int index = this.startIndex; index < maxRecipes && index < this.recipesForInput.size(); ++index) {
			int i = index - this.startIndex;
			int x = this.leftPos + 225 + index % 4 * 16;
			int y = this.topPos + 142 + (i / 4) * 18 + 2;
			ResourceLocation sprite = RECIPE_SPRITE;
			if (index == this.selectedRecipe) {
				sprite = RECIPE_SELECTED_SPRITE;
			} else if (mouseX >= x && x + 16 > mouseX && mouseY + 1 >= y && y + 18 > mouseY + 1) {
				sprite = RECIPE_HIGHLIGHTED_SPRITE;
			}
			graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, x, y - 1, 16, 18);
		}
	}
	
	private void renderRecipes(@NotNull GuiGraphics graphics) {
		for (int index = this.startIndex; index < this.startIndex + 12 && index < this.recipesForInput.size(); ++index) {
			int i = index - this.startIndex;
			int x = this.leftPos + 225 + index % 4 * 16;
			int y = this.topPos + 142 + (i / 4) * 18 + 2;
			graphics.renderItem(this.recipesForInput.get(index), x, y);
		}
	}
	
	@Override
	public void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY, boolean open, boolean renderable, @NotNull Consumer<ItemStack> tooltipRenderer) {
		super.renderTooltip(graphics, mouseX, mouseY, open, renderable, tooltipRenderer);
		if (open && this.shouldDisplayRecipes()) {
			for (int index = this.startIndex; index < this.startIndex + 12 && index < this.recipesForInput.size(); ++index) {
				int i = index - this.startIndex;
				double x = mouseX - (double) (this.leftPos + 225 + i % 4 * 16);
				double y = mouseY - (double) (this.topPos + 142 + i / 4 * 18);
				if (x >= 0.0 && y >= 0.0 && x < 16.0 && y < 18.0) {
					tooltipRenderer.accept(this.recipesForInput.get(index));
				}
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.shouldDisplayRecipes()) {
			for (int index = this.startIndex; index < this.startIndex + 12; ++index) {
				int i = index - this.startIndex;
				double x = mouseX - (this.leftPos + 225 + i % 4 * 16);
				double y = mouseY - (this.topPos + 142 + i / 4.0 * 18);
				if (x >= 0.0 && y >= 0.0 && x < 16.0 && y < 18.0) {
					Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
					Objects.requireNonNull(this.minecraft.gameMode).handleInventoryButtonClick(this.screen.getMenu().containerId, index);
					this.selectedRecipe = index;
					return true;
				}
			}
			double x = this.leftPos + this.imageWidth + 72;
			double y = this.topPos + 143;
			if (mouseX >= x && x + 12 >= mouseX && mouseY >= y && y + 54 >= mouseY) {
				this.scrolling = true;
				return true;
			}
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
		if (this.scrolling && this.isScrollBarActive()) {
			int top = this.topPos + 141;
			int bottom = top + 54;
			this.scrollOffset = (mouseY - top - 7.5) / ((bottom - top) - 15.0);
			this.scrollOffset = Mth.clamp(this.scrollOffset, 0.0, 1.0);
			this.startIndex = (int) ((this.scrollOffset * this.getOffScreenRows()) + 0.5) * 4;
			return true;
		} else {
			return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (this.isScrollBarActive()) {
			double offset = delta / this.getOffScreenRows();
			this.scrollOffset = Mth.clamp(this.scrollOffset - offset, 0.0F, 1.0F);
			this.startIndex = (int) ((this.scrollOffset * this.getOffScreenRows()) + 0.5) * 4;
		}
		return true;
	}
	
	private @NotNull ItemStack getInputStack() {
		return this.handler.getInputHandler().getStackInSlot(0);
	}
	
	private boolean shouldDisplayRecipes() {
		return !this.getInputStack().isEmpty();
	}
	
	private boolean isScrollBarActive() {
		return this.shouldDisplayRecipes() && this.recipesForInput.size() > 12;
	}
	
	private int getOffScreenRows() {
		return (this.recipesForInput.size() + 4 - 1) / 4 - 3;
	}
	
	public void updateRecipes(boolean resetSelected) {
		this.recipesForInput.clear();
		if (!this.getInputStack().isEmpty()) {
			ContextMap context = SlotDisplayContext.fromLevel(this.player.level());
			SelectableRecipe.SingleInputSet<StonecutterRecipe> allRecipes = this.player.level().recipeAccess().stonecutterRecipes();
			SelectableRecipe.SingleInputSet<StonecutterRecipe> recipesForInput = allRecipes.selectByInput(this.getInputStack());
			recipesForInput.entries().forEach((entry) -> {
				SlotDisplay slotDisplay = entry.recipe().optionDisplay();
				this.recipesForInput.add(slotDisplay.resolveForFirstStack(context));
			});
		}
		if (resetSelected) {
			this.scrollOffset = 0.0;
			this.startIndex = 0;
			this.selectedRecipe = -1;
		}
	}
}
