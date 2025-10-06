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

package net.luis.xbackpack.world.inventory.extension;

import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.capability.IBackpack;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.AbstractExtensionContainerMenu;
import net.luis.xbackpack.world.inventory.extension.slot.ExtensionSlot;
import net.luis.xbackpack.world.inventory.handler.CraftingFuelHandler;
import net.luis.xbackpack.world.inventory.progress.ProgressHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class BrewingStandExtensionMenu extends AbstractExtensionMenu {
	
	private final PotionBrewing potionBrewing;
	private final CraftingFuelHandler handler;
	private final ProgressHandler progressHandler;
	
	public BrewingStandExtensionMenu(@NotNull AbstractExtensionContainerMenu menu, @NotNull Player player) {
		super(menu, player, BackpackExtensions.BREWING_STAND.get());
		this.potionBrewing = player.level().potionBrewing();
		IBackpack backpack = BackpackProvider.get(player);
		this.handler = backpack.getBrewingHandler();
		this.progressHandler = backpack.getBrewHandler();
	}
	
	@Override
	public void open() {
		this.progressHandler.broadcastChanges();
	}
	
	@Override
	public void addSlots(@NotNull Consumer<Slot> consumer) {
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 0, 277, 146) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return BrewingStandExtensionMenu.this.potionBrewing.isIngredient(stack);
			}
		});
		consumer.accept(new ExtensionSlot(this, this.handler.getFuelHandler(), 0, 225, 146) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return stack.is(Items.BLAZE_POWDER);
			}
		});
		for (int i = 0; i < 3; i++) {
			consumer.accept(new ExtensionSlot(this, this.handler.getResultHandler(), i, 254 + i * 23, i == 1 ? 187 : 180) {
				@Override
				public boolean mayPlace(@NotNull ItemStack stack) {
					return BrewingStandExtensionMenu.this.potionBrewing.isInput(stack);
				}
				
				@Override
				public int getMaxStackSize() {
					return 1;
				}
				
				@Override
				public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
					BrewingStandExtensionMenu.this.onTake(player, stack);
					super.onTake(player, stack);
				}
			});
		}
	}
	
	private void onTake(@NotNull Player player, @NotNull ItemStack stack) {
		Optional<Holder<Potion>> optional = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
		if (optional.isPresent() && player instanceof ServerPlayer serverPlayer) {
			net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent(player, stack));
			CriteriaTriggers.BREWED_POTION.trigger(serverPlayer, optional.get());
		}
	}
	
	@Override
	public boolean quickMoveStack(@NotNull ItemStack slotStack, int index) {
		if (908 >= index && index >= 0) { // from container
			if (slotStack.is(Items.BLAZE_POWDER) && this.canQuickMovePowder()) {
				return this.menu.moveItemStackTo(slotStack, 947, 948); // into fuel
			} else if (this.potionBrewing.isIngredient(slotStack)) {
				return this.menu.moveItemStackTo(slotStack, 946, 947); // into input
			} else if (this.potionBrewing.isInput(slotStack)) {
				return this.menu.moveItemStackTo(slotStack, 948, 451); // into result
			}
		} else if (950 >= index && index >= 946) { // from extension
			return this.movePreferredMenu(slotStack); // into container
		}
		return false;
	}
	
	private boolean canQuickMovePowder() {
		ItemStack stack = this.handler.getFuelHandler().getStackInSlot(0);
		return stack.isEmpty() || (stack.is(Items.BLAZE_POWDER) && stack.getMaxStackSize() > stack.getCount());
	}
}
