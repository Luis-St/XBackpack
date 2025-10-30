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

package net.luis.xbackpack.world.inventory.progress;

import net.luis.xbackpack.network.XBNetworkHandler;
import net.luis.xbackpack.network.packet.extension.UpdateFurnacePacket;
import net.luis.xbackpack.world.inventory.handler.SmeltingHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Luis-St
 *
 */

public class SmeltingProgressHandler implements ProgressHandler {
	
	private final Player player;
	private final SmeltingHandler handler;
	private final List<RecipeType<? extends AbstractCookingRecipe>> recipeTypes;
	private int cookingProgress;
	private int cookingTime;
	private @Nullable AbstractCookingRecipe progressingRecipe;
	private int fuelTime;
	private int maxFuel;
	
	public SmeltingProgressHandler(@NotNull Player player, @NotNull SmeltingHandler handler, @NotNull List<RecipeType<? extends AbstractCookingRecipe>> recipeTypes) {
		this.player = player;
		this.handler = handler;
		this.recipeTypes = recipeTypes;
	}
	
	@Override
	public void tick() {
		int oldCookingProgress = this.getCookingProgress();
		int oldFuelTime = this.getFuelProgress();
		this.checkRecipe();
		this.forceStorages();
		if (this.fuelTime > 0) {
			--this.fuelTime;
		}
		this.checkFuel();
		if (this.progressingRecipe == null && !this.getInputItem().isEmpty()) {
			ItemStack stack = this.getInputItem();
			if (this.canSmelt(stack)) {
				this.progressingRecipe = this.getRecipe(stack);
				if (this.progressingRecipe != null) {
					this.cookingTime = this.progressingRecipe.cookingTime();
					this.cookingProgress = 0;
				}
			}
		}
		AbstractCookingRecipe progressingRecipe = this.progressingRecipe;
		if (progressingRecipe != null) {
			if (this.getInputItem().isEmpty()) {
				this.resetRecipe();
				this.broadcastChanges();
			} else if (this.fuelTime > 0) {
				this.cookingProgress++;
			} else if (this.cookingProgress > 0) {
				this.cookingProgress--;
			}
			ItemStackHandler inputHandler = this.handler.getInputHandler();
			if (this.cookingProgress >= this.cookingTime && !inputHandler.extractItem(0, 1, true).isEmpty()) {
				ItemStack stack = this.handler.getResultHandler().insertItem(0, progressingRecipe.assemble(new SingleRecipeInput(inputHandler.extractItem(0, 1, false)), this.player.level().registryAccess()), false);
				if (!stack.isEmpty()) {
					this.player.drop(stack, true, true);
				}
				this.giveExperience();
				this.resetRecipe();
				this.broadcastChanges();
			}
		}
		if (oldCookingProgress != this.getCookingProgress() || oldFuelTime != this.getFuelProgress()) {
			this.broadcastChanges();
		}
	}
	
	private void checkRecipe() {
		if (this.cookingProgress > 0 && this.cookingTime > 0 && this.progressingRecipe == null) {
			this.progressingRecipe = this.getRecipe(this.getInputItem());
			this.broadcastChanges();
		} else if (this.progressingRecipe == null) {
			this.resetRecipe();
		}
	}
	
	private void forceStorages() {
		this.forceInputStorage();
		this.forceResultStorage();
	}
	
	private void forceInputStorage() {
		ItemStackHandler handler = this.getInputStorage();
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			} else if (this.getInputItem().isEmpty()) {
				this.handler.getInputHandler().setStackInSlot(0, stack);
				handler.setStackInSlot(i, ItemStack.EMPTY);
			} else if (this.areMergable(this.getInputItem(), stack)) {
				ItemStack input = this.getInputItem();
				int count = input.getCount() + stack.getCount();
				input.setCount(Math.min(count, input.getMaxStackSize()));
				this.handler.getInputHandler().setStackInSlot(0, input);
				ItemStack remainder = input.copy();
				remainder.setCount(Math.max(count - input.getMaxStackSize(), 0));
				handler.setStackInSlot(i, remainder);
			}
			ItemStack input = this.getInputItem();
			if (input.getCount() >= input.getMaxStackSize()) {
				break;
			}
		}
	}
	
	private void forceResultStorage() {
		ItemStackHandler handler = this.getResultStorage();
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack result = this.getResultItem();
			if (result.isEmpty()) {
				break;
			} else {
				ItemStack stack = handler.getStackInSlot(i);
				if (stack.isEmpty()) {
					handler.setStackInSlot(i, result);
					this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
				} else if (this.areMergable(stack, result)) {
					int count = stack.getCount() + result.getCount();
					int remaining = Math.min(count, stack.getMaxStackSize());
					result.setCount(Math.max(count - stack.getMaxStackSize(), 0));
					this.handler.getResultHandler().setStackInSlot(0, result);
					ItemStack merged = stack.copy();
					merged.setCount(remaining);
					handler.setStackInSlot(i, merged);
				}
			}
		}
	}
	
	private boolean areMergable(@NotNull ItemStack toStack, @NotNull ItemStack stack) {
		if (toStack.getCount() >= toStack.getMaxStackSize()) {
			return false;
		} else if (!toStack.is(stack.getItem())) {
			return false;
		} else {
			return ItemStack.isSameItemSameComponents(toStack, stack);
		}
	}
	
	private void checkFuel() {
		if (0 >= this.fuelTime && !this.getInputItem().isEmpty()) {
			int fuelTime = this.getFuelTime(this.getFuelItem());
			if (fuelTime > 0) {
				this.consumeFuel(fuelTime);
			}
		}
	}
	
	private void consumeFuel(int fuelTime) {
		ItemStack stack = this.handler.getFuelHandler().extractItem(0, 1, false);
		if (!stack.isEmpty()) {
			ItemStack remainingStack = stack.getCraftingRemainder();
			if (this.getFuelItem().isEmpty() && !stack.isEmpty()) {
				this.handler.getFuelHandler().insertItem(0, remainingStack, false);
			}
			this.maxFuel = fuelTime;
			this.fuelTime = fuelTime;
		}
	}
	
	private void giveExperience() {
		RandomSource rng = this.player.getRandom();
		float experience;
		if (this.progressingRecipe != null) {
			experience = this.progressingRecipe.experience();
		} else {
			experience = rng.nextFloat() + rng.nextFloat();
		}
		int usedRecipes = rng.nextInt(3);
		this.player.giveExperiencePoints((int) (experience * usedRecipes));
	}
	
	private void resetRecipe() {
		this.cookingProgress = 0;
		this.cookingTime = 0;
		this.progressingRecipe = null;
	}
	
	@SuppressWarnings("unchecked")
	private @Nullable AbstractCookingRecipe getRecipe(@NotNull ItemStack stack) {
		AbstractCookingRecipe cookingRecipe = null;
		for (RecipeType<? extends AbstractCookingRecipe> recipeType : this.recipeTypes) {
			Optional<RecipeHolder<AbstractCookingRecipe>> optional = Optional.empty();
			if (this.player instanceof ServerPlayer player) {
				optional = player.level().recipeAccess().getRecipeFor((RecipeType<AbstractCookingRecipe>) recipeType, new SingleRecipeInput(stack), player.level());
			}
			if (optional.isPresent()) {
				AbstractCookingRecipe recipe = optional.get().value();
				if (cookingRecipe == null) {
					cookingRecipe = recipe;
				} else if (cookingRecipe.cookingTime() > recipe.cookingTime()) {
					cookingRecipe = recipe;
				}
			}
		}
		return cookingRecipe;
	}
	
	private boolean canSmelt(@NotNull ItemStack stack) {
		return 0 >= this.cookingProgress && 0 >= this.cookingTime && this.fuelTime > 0 && this.canProgress(stack);
	}
	
	private boolean canProgress(@NotNull ItemStack stack) {
		AbstractCookingRecipe recipe = this.getRecipe(stack);
		if (recipe != null) {
			ItemStack result = recipe.assemble(new SingleRecipeInput(stack), this.player.level().registryAccess());
			return ItemStack.isSameItemSameComponents(result, this.getResultItem()) || this.getResultItem().isEmpty();
		}
		return false;
	}
	
	private int getFuelTime(@NotNull ItemStack stack) {
		int fuelTime = 0;
		for (RecipeType<? extends AbstractCookingRecipe> recipeType : this.recipeTypes) {
			fuelTime = Math.max(this.player.level().fuelValues().burnDuration(stack), fuelTime);
		}
		return fuelTime;
	}
	
	public @NotNull ItemStack getInputItem() {
		return this.handler.getInputHandler().getStackInSlot(0);
	}
	
	public @NotNull ItemStack getFuelItem() {
		return this.handler.getFuelHandler().getStackInSlot(0);
	}
	
	public @NotNull ItemStack getResultItem() {
		return this.handler.getResultHandler().getStackInSlot(0);
	}
	
	public @NotNull ItemStackHandler getInputStorage() {
		return this.handler.getInputStorageHandler();
	}
	
	public @NotNull ItemStackHandler getResultStorage() {
		return this.handler.getResultStorageHandler();
	}
	
	public int getCookingProgress() {
		int cookingProgress = this.cookingProgress;
		int cookingTime = this.cookingTime;
		return cookingTime != 0 && cookingProgress != 0 ? cookingProgress * 24 / cookingTime : 0;
	}
	
	public int getFuelProgress() {
		int maxFuel = this.maxFuel == 0 ? 200 : this.maxFuel;
		return this.fuelTime * 14 / maxFuel;
	}
	
	@Override
	public void broadcastChanges() {
		XBNetworkHandler.INSTANCE.sendToPlayer(this.player, new UpdateFurnacePacket(this.getCookingProgress(), this.getFuelProgress()));
	}
	
	@Override
	public @NotNull CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("cooking_progress", this.cookingProgress);
		tag.putInt("cooking_time", this.cookingTime);
		tag.putInt("fuel_time", this.fuelTime);
		tag.putInt("max_fuel", this.maxFuel);
		return tag;
	}
	
	@Override
	public void deserialize(@NotNull CompoundTag tag) {
		this.cookingProgress = tag.getIntOr("cooking_progress", 0);
		this.cookingTime = tag.getIntOr("cooking_time", 0);
		this.fuelTime = tag.getIntOr("fuel_time", 0);
		this.maxFuel = tag.getIntOr("max_fuel", 0);
	}
}
