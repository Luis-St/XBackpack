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

import net.luis.xbackpack.network.XBNetworkHandler;
import net.luis.xbackpack.network.packet.extension.UpdateStonecutterPacket;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class StonecutterExtensionMenu extends AbstractExtensionMenu {
	
	private final CraftingHandler handler;
	private SelectableRecipe.SingleInputSet<StonecutterRecipe> recipesForInput = SelectableRecipe.SingleInputSet.empty();
	private ItemStack input = ItemStack.EMPTY;
	private int selectedRecipe = -1;
	private @Nullable RecipeHolder<StonecutterRecipe> recipe;
	
	public StonecutterExtensionMenu(@NotNull AbstractExtensionContainerMenu menu, @NotNull Player player) {
		super(menu, player, BackpackExtensions.STONECUTTER.get());
		this.handler = BackpackProvider.get(this.player).getStonecutterHandler();
	}
	
	@Override
	public void open() {
		this.slotsChanged();
	}
	
	@Override
	public void addSlots(@NotNull Consumer<Slot> consumer) {
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 0, 249, 121));
		consumer.accept(new ExtensionSlot(this, this.handler.getResultHandler(), 0, 249, 207) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return false;
			}
			
			@Override
			public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
				StonecutterExtensionMenu.this.onTake(player, stack);
				super.onTake(player, stack);
			}
		});
	}
	
	private void onTake(@NotNull Player player, @NotNull ItemStack stack) {
		stack.onCraftedBy(player, stack.getCount());
		if (this.recipe != null && !this.recipe.value().isSpecial()) {
			player.awardRecipes(Collections.singleton(this.recipe));
		}
		ItemStack inputStack = this.handler.getInputHandler().extractItem(0, 1, false);
		if (!inputStack.isEmpty() && !this.handler.getInputHandler().getStackInSlot(0).isEmpty()) {
			this.setupResult();
		}
		this.menu.broadcastChanges();
		if (player instanceof ServerPlayer serverPlayer) {
			this.playSound(serverPlayer, serverPlayer.level());
		}
	}
	
	private void playSound(@NotNull ServerPlayer player, @NotNull ServerLevel level) {
		player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_STONECUTTER_TAKE_RESULT), SoundSource.BLOCKS, player.getX(), player.getY(), player.getZ(), 1.0F, level.random.nextFloat() * 0.1F + 0.9F, level.random.nextLong()));
	}
	
	public boolean requiresTickUpdate() {
		ItemStack input = this.handler.getInputHandler().getStackInSlot(0);
		ItemStack result = this.handler.getResultHandler().getStackInSlot(0);
		if (result.isEmpty() && !this.recipesForInput.isEmpty()) {
			return true;
		}
		return !input.isEmpty() && this.recipesForInput.isEmpty();
	}
	
	@Override
	public void slotsChanged() {
		ItemStack stack = this.handler.getInputHandler().getStackInSlot(0);
		if (stack.is(this.input.getItem())) {
			XBNetworkHandler.INSTANCE.sendToPlayer(this.player, new UpdateStonecutterPacket(false));
		} else {
			this.input = stack.copy();
			this.setupRecipes(stack);
		}
	}
	
	private void setupRecipes(@NotNull ItemStack stack) {
		this.selectedRecipe = -1;
		this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
		if (stack.isEmpty()) {
			this.recipesForInput = SelectableRecipe.SingleInputSet.empty();
		} else {
			this.recipesForInput = this.player.level().recipeAccess().stonecutterRecipes().selectByInput(stack);
		}
		XBNetworkHandler.INSTANCE.sendToPlayer(this.player, new UpdateStonecutterPacket(true));
	}
	
	@Override
	public boolean clickMenuButton(@NotNull Player player, int button) {
		if (this.isValidIndex(button)) {
			this.selectedRecipe = button;
			this.setupResult();
			return true;
		}
		return true;
	}
	
	private void setupResult() {
		Optional<RecipeHolder<StonecutterRecipe>> optional = Optional.empty();
		if (!this.recipesForInput.isEmpty() && this.isValidIndex(this.selectedRecipe)) {
			SelectableRecipe.SingleInputEntry<StonecutterRecipe> selectedRecipe = this.recipesForInput.entries().get(this.selectedRecipe);
			optional = selectedRecipe.recipe().recipe();
		}
		optional.ifPresentOrElse(recipe -> {
			this.recipe = recipe;
			this.handler.getResultHandler().setStackInSlot(0, recipe.value().assemble(new SingleRecipeInput(this.handler.getInputHandler().getStackInSlot(0)), this.player.level().registryAccess()));
		}, () -> {
			this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
			this.recipe = null;
		});
		this.menu.broadcastChanges();
	}
	
	private boolean isValidIndex(int index) {
		return this.recipesForInput.size() > index && index >= 0;
	}
	
	@Override
	public boolean quickMoveStack(@NotNull ItemStack slotStack, int index) {
		if (908 >= index && index >= 0) { // from container
			return this.menu.moveItemStackTo(slotStack, 944, 945); // into input
		} else if (index == 945) { // from result
			return this.movePreferredMenu(slotStack); // into container
		}
		return false;
	}
}
