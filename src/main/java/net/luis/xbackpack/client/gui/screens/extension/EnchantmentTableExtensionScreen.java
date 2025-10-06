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

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.client.gui.screens.AbstractExtensionContainerScreen;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.handler.EnchantingHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
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

public class EnchantmentTableExtensionScreen extends AbstractExtensionScreen {
	
	private static final ResourceLocation ENCHANTMENT_SLOT_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/enchantment_table/enchantment_slot");
	private static final ResourceLocation ENCHANTMENT_SLOT_DISABLED_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/enchantment_table/enchantment_slot_disabled");
	private static final ResourceLocation ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/enchantment_table/enchantment_slot_highlighted");
	private static final ResourceLocation LEVEL_1_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/enchantment_table/level_1");
	private static final ResourceLocation LEVEL_1_DISABLED_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/enchantment_table/level_1_disabled");
	private static final ResourceLocation LEVEL_2_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/enchantment_table/level_2");
	private static final ResourceLocation LEVEL_2_DISABLED_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/enchantment_table/level_2_disabled");
	private static final ResourceLocation LEVEL_3_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/enchantment_table/level_3");
	private static final ResourceLocation LEVEL_3_DISABLED_SPRITE = ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "extensions/enchantment_table/level_3_disabled");
	
	private final ResourceLocation[] enchantments = new ResourceLocation[3];
	private final int[] enchantmentLevels = new int[3];
	private final int[] enchantingCosts = new int[3];
	private EnchantingHandler handler;
	private int enchantmentSeed;
	
	public EnchantmentTableExtensionScreen(@NotNull AbstractExtensionContainerScreen<?> screen, @NotNull List<BackpackExtension> extensions) {
		super(screen, BackpackExtensions.ENCHANTMENT_TABLE.get(), extensions);
	}
	
	@Override
	protected void init() {
		this.handler = BackpackProvider.get(Objects.requireNonNull(this.minecraft.player)).getEnchantingHandler();
	}
	
	@Override
	protected void renderAdditional(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, boolean open) {
		for (int row = 0; row < 3 && open; row++) {
			this.renderRow(graphics, mouseX, mouseY, row, Objects.requireNonNull(this.minecraft.player), this.enchantments[row], this.enchantingCosts[row]);
		}
	}
	
	private void renderRow(@NotNull GuiGraphics graphics, int mouseX, int mouseY, int row, @NotNull LocalPlayer player, @Nullable ResourceLocation enchantment, int enchantingCost) {
		if (enchantment != null && this.enchantingCosts[row] > 0) {
			int costColor;
			int enchantmentColor;
			RenderSystem.setShaderTexture(0, this.getTexture());
			if ((player.experienceLevel >= enchantingCost && this.hasFuel(row)) || player.getAbilities().instabuild) {
				ResourceLocation sprite = this.isHoveringRow(row, mouseX, mouseY) ? ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE : ENCHANTMENT_SLOT_SPRITE;
				graphics.blitSprite(sprite, this.leftPos + this.imageWidth + 47, this.topPos + 97 + row * 19, 78, 19);
				this.renderLevel(graphics, row, true);
				costColor = 8453920;
				enchantmentColor = 6839882;
			} else {
				graphics.blitSprite(ENCHANTMENT_SLOT_DISABLED_SPRITE, this.leftPos + this.imageWidth + 47, this.topPos + 97 + row * 19, 78, 19);
				this.renderLevel(graphics, row, false);
				costColor = 4226832;
				enchantmentColor = 3419941;
			}
			this.renderLabels(graphics, row, costColor, enchantmentColor);
		} else {
			graphics.blitSprite(ENCHANTMENT_SLOT_DISABLED_SPRITE, this.leftPos + this.imageWidth + 47, this.topPos + 97 + row * 19, 78, 19);
		}
	}

	private void renderLevel(@NotNull GuiGraphics graphics, int row, boolean active) {
		ResourceLocation sprite = switch (row) {
			case 0 -> active ? LEVEL_1_SPRITE : LEVEL_1_DISABLED_SPRITE;
			case 1 -> active ? LEVEL_2_SPRITE : LEVEL_2_DISABLED_SPRITE;
			case 2 -> active ? LEVEL_3_SPRITE : LEVEL_3_DISABLED_SPRITE;
			default -> throw new IllegalStateException("Expected row to be 0, 1 or 2, but got: " + row);
		};
		graphics.blitSprite(sprite, this.leftPos + this.imageWidth + 47, this.topPos + 98 + row * 19, 16, 16);
	}
	
	private void renderLabels(@NotNull GuiGraphics graphics, int row, int costColor, int enchantmentColor) {
		String cost = Integer.toString(this.enchantingCosts[row]);
		graphics.drawString(this.font, cost, this.leftPos + this.imageWidth + 123 - this.font.width(cost), this.topPos + 106 + 19 * row, costColor);
		int length = 50 - this.font.width(cost);
		EnchantmentNames.getInstance().initSeed(this.enchantmentSeed + row);
		FormattedText enchantmentName = EnchantmentNames.getInstance().getRandomName(this.font, length);
		graphics.drawWordWrap(this.font, enchantmentName, this.leftPos + this.imageWidth + 67, this.topPos + 99 + 19 * row, length, enchantmentColor);
	}
	
	@Override
	public void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY, boolean open, boolean renderable, @NotNull Consumer<ItemStack> tooltipRenderer) {
		super.renderTooltip(graphics, mouseX, mouseY, open, renderable, tooltipRenderer);
		for (int row = 0; row < 3 && open; row++) {
			this.renderTooltip(graphics, mouseX, mouseY, row, Objects.requireNonNull(this.minecraft.player), this.enchantments[row], this.enchantmentLevels[row], this.enchantingCosts[row]);
		}
	}
	
	private void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY, int row, @NotNull LocalPlayer player, @Nullable ResourceLocation enchantment, int enchantmentLevel, int enchantingCost) {
		int fuel = this.getFuel();
		int rowIndex = row + 1;
		if (this.isHoveringRow(row, mouseX, mouseY) && enchantingCost > 0) {
			List<Component> components = Lists.newArrayList();
			if (enchantment != null) {
				Registry<Enchantment> registry = Objects.requireNonNull(this.minecraft.level).registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
				Holder<Enchantment> holder = registry.get(enchantment).orElseThrow();
				components.add((Component.translatable("container.enchant.clue", Enchantment.getFullname(holder, enchantmentLevel))).withStyle(ChatFormatting.WHITE));
			} else {
				components.add(Component.translatable("container.enchant.clue", ""));
			}
			if (enchantment == null) {
				components.add(CommonComponents.EMPTY);
				components.add(Component.translatable("forge.container.enchant.limitedEnchantability").withStyle(ChatFormatting.RED));
			} else if (!player.getAbilities().instabuild) {
				components.add(CommonComponents.EMPTY);
				if (enchantmentLevel >= player.experienceLevel) {
					components.add(Component.translatable("container.enchant.level.requirement", enchantmentLevel).withStyle(ChatFormatting.RED));
				} else {
					MutableComponent lapisComponent = rowIndex == 1 ? Component.translatable("container.enchant.lapis.one") : Component.translatable("container.enchant.lapis.many", rowIndex);
					components.add(lapisComponent.withStyle(fuel >= rowIndex ? ChatFormatting.GRAY : ChatFormatting.RED));
					MutableComponent levelComponent = rowIndex == 1 ? Component.translatable("container.enchant.level.one") : Component.translatable("container.enchant.level.many", rowIndex);
					components.add(levelComponent.withStyle(ChatFormatting.GRAY));
				}
			}
			graphics.renderComponentTooltip(this.font, components, mouseX, mouseY);
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.minecraft != null) {
			for (int row = 0; row < 3; row++) {
				if (this.isHoveringRow(row, mouseX, mouseY)) {
					Objects.requireNonNull(this.minecraft.gameMode).handleInventoryButtonClick(this.screen.getMenu().containerId, row);
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	private boolean isHoveringRow(int row, double mouseX, double mouseY) {
		int x = this.leftPos + this.imageWidth + 47;
		int y = this.topPos + 97 + row * 19;
		return x + 77 >= mouseX && mouseX >= x && y + 18 >= mouseY && mouseY >= y;
	}
	
	private int getFuel() {
		return this.handler.getFuelHandler().getStackInSlot(0).getCount();
	}
	
	private boolean hasFuel(int row) {
		return this.getFuel() >= row + 1;
	}
	
	public void update(ResourceLocation @NotNull [] enchantments, int @NotNull [] enchantmentLevels, int @NotNull [] enchantingCosts, int enchantmentSeed) {
		System.arraycopy(enchantments, 0, this.enchantments, 0, this.enchantments.length);
		System.arraycopy(enchantmentLevels, 0, this.enchantmentLevels, 0, this.enchantmentLevels.length);
		System.arraycopy(enchantingCosts, 0, this.enchantingCosts, 0, this.enchantingCosts.length);
		this.enchantmentSeed = enchantmentSeed;
	}
}
