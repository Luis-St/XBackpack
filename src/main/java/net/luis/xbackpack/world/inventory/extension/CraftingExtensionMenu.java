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

package net.luis.xbackpack.world.inventory.extension;

import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.AbstractExtensionContainerMenu;
import net.luis.xbackpack.world.inventory.extension.slot.ExtensionResultSlot;
import net.luis.xbackpack.world.inventory.extension.slot.ExtensionSlot;
import net.luis.xbackpack.world.inventory.wrapper.CraftingContainerWrapper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class CraftingExtensionMenu extends AbstractExtensionMenu {
	
	private final CraftingContainerWrapper craftingWrapper;
	private final ResultContainer resultWrapper;
	
	public CraftingExtensionMenu(@NotNull AbstractExtensionContainerMenu menu, @NotNull Player player) {
		super(menu, player, BackpackExtensions.CRAFTING_TABLE.get());
		this.craftingWrapper = new CraftingContainerWrapper(this.menu, BackpackProvider.get(this.player).getCraftingHandler(), 3, 3);
		this.resultWrapper = new ResultContainer();
	}
	
	@Override
	public void open() {
		this.slotChangedCraftingGrid();
	}
	
	@Override
	public void addSlots(@NotNull Consumer<Slot> consumer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				consumer.accept(new ExtensionSlot(this, this.craftingWrapper, j + i * 3, 225 + j * 18, 25 + i * 18));
			}
		}
		consumer.accept(new ExtensionResultSlot(this, this.player, this.craftingWrapper, this.resultWrapper, 0, 243, 110));
	}
	
	@Override
	public void slotsChanged(@NotNull Container container) {
		if (container == this.craftingWrapper) {
			this.slotChangedCraftingGrid();
		}
	}
	
	private void slotChangedCraftingGrid() {
		Level level = this.player.level();
		if (!level.isClientSide && this.player instanceof ServerPlayer player) {
			ItemStack stack = ItemStack.EMPTY;
			Optional<RecipeHolder<CraftingRecipe>> optional = Objects.requireNonNull(level.getServer()).getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.craftingWrapper.asCraftInput(), level);
			if (optional.isPresent()) {
				RecipeHolder<CraftingRecipe> recipe = optional.get();
				this.resultWrapper.setRecipeUsed(recipe);
				stack = recipe.value().assemble(this.craftingWrapper.asCraftInput(), player.level().registryAccess());
			}
			this.resultWrapper.setItem(0, stack);
			BackpackProvider.get(this.player).broadcastChanges();
		}
	}
	
	@Override
	public boolean quickMoveStack(@NotNull ItemStack slotStack, int index) {
		if (908 >= index && index >= 0) { // from container
			return this.menu.moveItemStackTo(slotStack, 917, 926); // into input
		} else if (index == 926) { // from result
			return this.movePreferredMenu(slotStack); // into container
		}
		return false;
	}
}
