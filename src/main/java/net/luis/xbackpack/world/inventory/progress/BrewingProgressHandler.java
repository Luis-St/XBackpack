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
import net.luis.xbackpack.network.packet.extension.UpdateBrewingStandPacket;
import net.luis.xbackpack.world.inventory.handler.CraftingFuelHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

public class BrewingProgressHandler implements ProgressHandler {
	
	private final Player player;
	private final PotionBrewing potionBrewing;
	private final CraftingFuelHandler handler;
	private Item input;
	private int fuel;
	private int brewTime;
	
	public BrewingProgressHandler(@NotNull Player player, @NotNull CraftingFuelHandler handler) {
		this.player = player;
		this.potionBrewing = player.level().potionBrewing();
		this.handler = handler;
	}
	
	@Override
	public void tick() {
		ItemStack fuelStack = this.getFuelItem();
		if (0 >= this.fuel && fuelStack.is(Items.BLAZE_POWDER)) {
			this.fuel = 20;
			this.handler.getFuelHandler().extractItem(0, 1, false);
			this.broadcastChanges();
		}
		ItemStack inputStack = this.getInputItem();
		if (this.brewTime > 0) {
			--this.brewTime;
			if (this.brewTime == 0 && this.isBrewable()) {
				this.brewPotion();
			} else if (!this.isBrewable() || !inputStack.is(this.input)) {
				this.brewTime = 0;
			}
			this.broadcastChanges();
		} else if (this.isBrewable() && this.fuel > 0) {
			--this.fuel;
			this.brewTime = 400;
			this.input = inputStack.getItem();
			this.broadcastChanges();
		}
	}
	
	private boolean isBrewable() {
		ItemStack inputStack = this.getInputItem();
		if (!inputStack.isEmpty()) {
			for (int i = 0; i < 3; i++) {
				if (this.potionBrewing.hasMix(this.getResultHandler().getStackInSlot(i), inputStack)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void brewPotion() {
		if (!this.onPotionAttemptBrew()) {
			ItemStack inputStack = this.getInputItem();
			for (int i = 0; i < 3; i++) {
				this.getResultHandler().setStackInSlot(i, this.potionBrewing.mix(inputStack, this.getResultHandler().getStackInSlot(i)));
			}
			this.onPotionBrewed(this.asList());
			if (this.player instanceof ServerPlayer player) {
				this.playSound(player, player.level());
			}
			ItemStack remainingStack = inputStack.getCraftingRemainder();
			if (remainingStack.isEmpty()) {
				inputStack.shrink(1);
			} else {
				inputStack.shrink(1);
				if (inputStack.isEmpty()) {
					inputStack = remainingStack;
				} else {
					Containers.dropItemStack(this.player.level(), this.player.getX(), this.player.getY(), this.player.getZ(), remainingStack);
				}
			}
			this.handler.getInputHandler().setStackInSlot(0, inputStack);
		}
	}
	
	private @NotNull ItemStack getInputItem() {
		return this.handler.getInputHandler().getStackInSlot(0);
	}
	
	private @NotNull NonNullList<ItemStack> getInputList() {
		NonNullList<ItemStack> stacks = NonNullList.withSize(this.handler.getInputHandler().getSlots(), ItemStack.EMPTY);
		for (int i = 0; i < stacks.size(); i++) {
			stacks.set(i, this.handler.getInputHandler().getStackInSlot(i));
		}
		return stacks;
	}
	
	private @NotNull ItemStack getFuelItem() {
		return this.handler.getFuelHandler().getStackInSlot(0);
	}
	
	private @NotNull ItemStackHandler getResultHandler() {
		return this.handler.getResultHandler();
	}
	
	private void playSound(@NotNull ServerPlayer player, @NotNull ServerLevel level) {
		player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BREWING_STAND_BREW), SoundSource.BLOCKS, player.getX(), player.getY(), player.getZ(), 1.0F, level.random.nextFloat() * 0.1F + 0.9F, level.random.nextLong()));
	}
	
	private @NotNull NonNullList<ItemStack> asList() {
		NonNullList<ItemStack> stacks = NonNullList.withSize(5, ItemStack.EMPTY);
		for (int i = 0; i < 3; i++) {
			stacks.set(i, this.getResultHandler().getStackInSlot(i));
		}
		stacks.set(3, this.getInputItem());
		stacks.set(4, this.getFuelItem());
		return stacks;
	}
	
	private boolean onPotionAttemptBrew() {
		NonNullList<ItemStack> inputStacks = this.getInputList();
		NonNullList<ItemStack> eventStacks = this.getInputList();
		PotionBrewEvent.Pre event = new PotionBrewEvent.Pre(eventStacks);
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
			boolean changed = false;
			for (int x = 0; x < inputStacks.size(); x++) {
				changed |= ItemStack.matches(eventStacks.get(x), inputStacks.get(x));
				inputStacks.set(x, event.getItem(x));
			}
			if (changed) {
				this.onPotionBrewed(inputStacks);
			}
			return true;
		}
		return false;
	}
	
	private void onPotionBrewed(@NotNull NonNullList<ItemStack> stacks) {
		EventHooks.onPotionBrewed(stacks);
	}
	
	@Override
	public void broadcastChanges() {
		XBNetworkHandler.INSTANCE.sendToPlayer(this.player, new UpdateBrewingStandPacket(this.fuel, this.brewTime));
	}
	
	@Override
	public @NotNull CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.putString("input", Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(this.input)).toString());
		tag.putInt("fuel", this.fuel);
		tag.putInt("brew_time", this.brewTime);
		return tag;
	}

	@Override
	public void deserialize(@NotNull CompoundTag tag) {
		this.input = BuiltInRegistries.ITEM.getValue(ResourceLocation.tryParse(tag.getStringOr("input", "")));
		this.fuel = tag.getIntOr("fuel", 0);
		this.brewTime = tag.getIntOr("brew_time", 0);
	}
}
