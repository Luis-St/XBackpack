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
import net.luis.xbackpack.world.inventory.extension.EnchantmentTableExtensionMenu;
import net.luis.xbackpack.world.inventory.handler.EnchantingHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
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
	private static final ResourceLocation EMPTY_ENCHANTMENT = EnchantmentTableExtensionMenu.EMPTY_ENCHANTMENT;
	
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
		int slotX = this.leftPos + this.imageWidth + 47;
		int slotY = this.topPos + 97 + row * 19;
		int textX = slotX + 20;
		int textY = this.topPos + 99 + row * 19;
		int costXOffset = 76;
		int costY = this.topPos + 106 + row * 19;
		if (enchantingCost <= 0) {
			graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_SPRITE, slotX, slotY, 78, 19);
			return;
		}
		boolean hasEnchantment = this.hasEnchantment(enchantment);
		String costText = Integer.toString(enchantingCost);
		int availableWidth = Math.max(0, 50 - this.font.width(costText));
		EnchantmentNames.getInstance().initSeed(this.enchantmentSeed + row);
		FormattedText enchantmentName = EnchantmentNames.getInstance().getRandomName(this.font, availableWidth);
		boolean insufficient = ((this.getFuel() < row + 1 || player.experienceLevel < enchantingCost) && !player.getAbilities().instabuild) || !hasEnchantment;
		int color = -9937334;
		if (insufficient) {
			graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_SPRITE, slotX, slotY, 78, 19);
			if (hasEnchantment) {
				this.renderLevel(graphics, row, false);
			}
			int disabledColor = ARGB.opaque((color & 16711422) >> 1);
			graphics.drawWordWrap(this.font, enchantmentName, textX, textY, availableWidth, disabledColor, false);
			color = -12550384;
		} else {
			boolean hovering = this.isHoveringRow(row, mouseX, mouseY);
			ResourceLocation sprite = hovering ? ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE : ENCHANTMENT_SLOT_SPRITE;
			graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, slotX, slotY, 78, 19);
			this.renderLevel(graphics, row, true);
			int activeColor = hovering ? -128 : color;
			graphics.drawWordWrap(this.font, enchantmentName, textX, textY, availableWidth, activeColor, false);
			color = -8323296;
		}
		int costX = textX + costXOffset - this.font.width(costText);
		graphics.drawString(this.font, costText, this.leftPos + this.imageWidth + 123 - this.font.width(costText), this.topPos + 106 + 19 * row, ARGB.opaque(color));
	}

	private void renderLevel(@NotNull GuiGraphics graphics, int row, boolean active) {
		ResourceLocation sprite = switch (row) {
			case 0 -> active ? LEVEL_1_SPRITE : LEVEL_1_DISABLED_SPRITE;
			case 1 -> active ? LEVEL_2_SPRITE : LEVEL_2_DISABLED_SPRITE;
			case 2 -> active ? LEVEL_3_SPRITE : LEVEL_3_DISABLED_SPRITE;
			default -> throw new IllegalStateException("Expected row to be 0, 1 or 2, but got: " + row);
		};
		graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, this.leftPos + this.imageWidth + 47, this.topPos + 98 + row * 19, 16, 16);
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
			if (this.hasEnchantment(enchantment)) {
				Registry<Enchantment> registry = Objects.requireNonNull(this.minecraft.level).registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
				registry.get(enchantment).map(holder -> (Component.translatable("container.enchant.clue", Enchantment.getFullname(holder, enchantmentLevel))).withStyle(ChatFormatting.WHITE)).ifPresentOrElse(components::add, () -> components.add(Component.translatable("container.enchant.clue", "")));
			} else {
				components.add(Component.translatable("container.enchant.clue", ""));
			}
			if (!this.hasEnchantment(enchantment)) {
				components.add(CommonComponents.EMPTY);
				components.add(Component.translatable("forge.container.enchant.limitedEnchantability").withStyle(ChatFormatting.RED));
			} else if (!player.getAbilities().instabuild) {
				components.add(CommonComponents.EMPTY);
				if (player.experienceLevel < enchantingCost) {
					components.add(Component.translatable("container.enchant.level.requirement", enchantingCost).withStyle(ChatFormatting.RED));
				} else {
					MutableComponent lapisComponent = rowIndex == 1 ? Component.translatable("container.enchant.lapis.one") : Component.translatable("container.enchant.lapis.many", rowIndex);
					components.add(lapisComponent.withStyle(fuel >= rowIndex ? ChatFormatting.GRAY : ChatFormatting.RED));
					MutableComponent levelComponent = rowIndex == 1 ? Component.translatable("container.enchant.level.one") : Component.translatable("container.enchant.level.many", rowIndex);
					components.add(levelComponent.withStyle(ChatFormatting.GRAY));
				}
			}
			List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> tooltipComponents = components.stream()
				.map(component -> net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(component.getVisualOrderText()))
				.toList();
			graphics.renderTooltip(this.font, tooltipComponents, mouseX, mouseY, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null);
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
	
	private boolean hasEnchantment(@Nullable ResourceLocation enchantment) {
		return enchantment != null && !EMPTY_ENCHANTMENT.equals(enchantment);
	}
	
	public void update(ResourceLocation @NotNull [] enchantments, int @NotNull [] enchantmentLevels, int @NotNull [] enchantingCosts, int enchantmentSeed) {
		System.arraycopy(enchantments, 0, this.enchantments, 0, this.enchantments.length);
		System.arraycopy(enchantmentLevels, 0, this.enchantmentLevels, 0, this.enchantmentLevels.length);
		System.arraycopy(enchantingCosts, 0, this.enchantingCosts, 0, this.enchantingCosts.length);
		this.enchantmentSeed = enchantmentSeed;
	}
}
