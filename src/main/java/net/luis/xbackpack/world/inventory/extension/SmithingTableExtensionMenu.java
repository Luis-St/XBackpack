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
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.AbstractExtensionContainerMenu;
import net.luis.xbackpack.world.inventory.extension.slot.ExtensionSlot;
import net.luis.xbackpack.world.inventory.handler.CraftingHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class SmithingTableExtensionMenu extends AbstractExtensionMenu {
	
	private final List<RecipeHolder<SmithingRecipe>> recipes = new ArrayList<>();
	private final CraftingHandler handler;
	private final Level level;
	private RecipeHolder<SmithingRecipe> selectedRecipe;
	
	@SuppressWarnings("unchecked")
	public SmithingTableExtensionMenu(@NotNull AbstractExtensionContainerMenu menu, @NotNull Player player) {
		super(menu, player, BackpackExtensions.SMITHING_TABLE.get());
		this.handler = BackpackProvider.get(this.player).getSmithingHandler();
		this.level = this.player.level();
		if (this.level instanceof ServerLevel level) {
			level.recipeAccess().getRecipes().stream().filter((recipe) -> recipe.value().getType() == RecipeType.SMITHING).forEach(recipe -> {
				this.recipes.add((RecipeHolder<SmithingRecipe>) recipe);
			});
		}
	}
	
	@Override
	public void open() {
		this.createResult();
	}
	
	@Override
	public void addSlots(@NotNull Consumer<Slot> consumer) {
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 0, 225, 193) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return SmithingTableExtensionMenu.this.recipes.stream().anyMatch((recipe) -> {
					return recipe.value().templateIngredient().map(ingredient -> ingredient.test(stack)).orElse(false);
				});
			}
		});
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 1, 243, 193) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return SmithingTableExtensionMenu.this.recipes.stream().anyMatch((recipe) -> {
					return recipe.value().baseIngredient().map(ingredient -> ingredient.test(stack)).orElse(false);
				});
			}
		});
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 2, 260, 193) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return SmithingTableExtensionMenu.this.recipes.stream().anyMatch((recipe) -> {
					return recipe.value().additionIngredient().map(ingredient -> ingredient.test(stack)).orElse(false);
				});
			}
		});
		consumer.accept(new ExtensionSlot(this, this.handler.getResultHandler(), 0, 304, 193, false) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return false;
			}
			
			@Override
			public boolean mayPickup(@NotNull Player player) {
				return SmithingTableExtensionMenu.this.mayPickup(player);
			}
			
			@Override
			public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
				SmithingTableExtensionMenu.this.onTake(player, stack);
				super.onTake(player, stack);
			}
		});
	}
	
	private boolean mayPickup(@NotNull Player player) {
		return this.selectedRecipe != null && this.selectedRecipe.value().matches(this.asContainer(), this.level);
	}
	
	private void onTake(@NotNull Player player, @NotNull ItemStack stack) {
		stack.onCraftedBy(player, stack.getCount());
		if (this.selectedRecipe != null) {
			player.awardRecipes(Collections.singleton(this.selectedRecipe));
		}
		this.shrinkStackInSlot(0);
		this.shrinkStackInSlot(1);
		this.shrinkStackInSlot(2);
		if (player instanceof ServerPlayer serverPlayer) {
			this.playSound(serverPlayer, serverPlayer.level());
		}
	}
	
	private void shrinkStackInSlot(int slot) {
		ItemStack stack = this.handler.getInputHandler().getStackInSlot(slot);
		stack.shrink(1);
		this.handler.getInputHandler().setStackInSlot(slot, stack);
	}
	
	private void playSound(@NotNull ServerPlayer player, @NotNull ServerLevel level) {
		player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SMITHING_TABLE_USE), SoundSource.BLOCKS, player.getX(), player.getY(), player.getZ(), 1.0F, level.random.nextFloat() * 0.1F + 0.9F, level.random.nextLong()));
	}
	
	@Override
	public void slotsChanged() {
		this.createResult();
	}
	
	private void createResult() {
		Optional<RecipeHolder<SmithingRecipe>> recipes = Optional.empty();
		if (this.level instanceof ServerLevel level) {
			recipes = level.recipeAccess().getRecipeFor(RecipeType.SMITHING, this.asContainer(), level);
		}
		if (recipes.isEmpty()) {
			this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
		} else {
			this.selectedRecipe = recipes.orElseThrow();
			ItemStack stack = this.selectedRecipe.value().assemble(this.asContainer(), this.level.registryAccess());
			this.handler.getResultHandler().setStackInSlot(0, stack);
		}
		
	}
	
	private @NotNull SmithingRecipeInput asContainer() {
		return new SmithingRecipeInput(this.handler.getInputHandler().getStackInSlot(0), this.handler.getInputHandler().getStackInSlot(1), this.handler.getInputHandler().getStackInSlot(2));
	}
	
	@Override
	public boolean quickMoveStack(@NotNull ItemStack slotStack, int index) {
		if (908 >= index && index >= 0) { // from container
			int slot = 954 + this.getSlotOffset(slotStack);
			if (954 > slot || slot > 956) {
				return false;
			}
			return this.menu.moveItemStackTo(slotStack, slot, slot + 1);
		} else if (index == 957) { // from result
			return this.movePreferredMenu(slotStack); // into addition
		}
		return false;
	}
	
	private int getSlotOffset(@NotNull ItemStack stack) {
		return this.recipes.stream().map((recipe) -> {
			if (recipe.value().templateIngredient().map(ingredient -> ingredient.test(stack)).orElse(false)) {
				return 0;
			} else if (recipe.value().baseIngredient().map(ingredient -> ingredient.test(stack)).orElse(false)) {
				return 1;
			} else {
				return recipe.value().additionIngredient().map(ingredient -> ingredient.test(stack)).orElse(false) ? 2 : -1;
			}
		}).findFirst().orElse(-1);
	}
}
