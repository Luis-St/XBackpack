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
import net.luis.xbackpack.network.packet.extension.UpdateEnchantmentTablePacket;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.AbstractExtensionContainerMenu;
import net.luis.xbackpack.world.inventory.extension.slot.ExtensionSlot;
import net.luis.xbackpack.world.inventory.handler.EnchantingHandler;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class EnchantmentTableExtensionMenu extends AbstractExtensionMenu {
	
	public static final ResourceLocation EMPTY_ENCHANTMENT = ResourceLocation.withDefaultNamespace("enchantment_empty");
	
	private final EnchantingHandler handler;
	private final RandomSource rng = RandomSource.create();
	private final ResourceLocation[] enchantments = {
		EMPTY_ENCHANTMENT, EMPTY_ENCHANTMENT, EMPTY_ENCHANTMENT
	};
	private final int[] enchantmentLevels = {
		-1, -1, -1
	};
	private final int[] enchantingCosts = {
		0, 0, 0
	};
	private int enchantmentSeed;
	
	public EnchantmentTableExtensionMenu(@NotNull AbstractExtensionContainerMenu menu, @NotNull Player player) {
		super(menu, player, BackpackExtensions.ENCHANTMENT_TABLE.get());
		this.handler = BackpackProvider.get(this.player).getEnchantingHandler();
		this.enchantmentSeed = player.getEnchantmentSeed();
	}
	
	@Override
	public void open() {
		this.slotsChanged();
	}
	
	@Override
	public void addSlots(@NotNull Consumer<Slot> consumer) {
		consumer.accept(new ExtensionSlot(this, this.handler.getPowerHandler(), 0, 235, 108) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return stack.is(Tags.Items.BOOKSHELVES) || stack.getItem() == Items.BOOK;
			}
		});
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 0, 225, 130) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return stack.isEnchantable() || stack.getItem() == Items.BOOK;
			}
			
			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		consumer.accept(new ExtensionSlot(this, this.handler.getFuelHandler(), 0, 245, 130) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return stack.is(Tags.Items.ENCHANTING_FUELS);
			}
		});
	}
	
	@Override
	public void slotsChanged() {
		ItemStack inputStack = this.handler.getInputHandler().getStackInSlot(0);
		if (!inputStack.isEmpty() && inputStack.isEnchantable()) {
			Registry<Enchantment> registry = this.player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			int power = this.calculatePower();
			this.rng.setSeed(this.enchantmentSeed);
			for (int row = 0; row < 3; ++row) {
				this.enchantingCosts[row] = EnchantmentHelper.getEnchantmentCost(this.rng, row, power, inputStack);
				this.enchantments[row] = EMPTY_ENCHANTMENT;
				this.enchantmentLevels[row] = -1;
				if (this.enchantingCosts[row] < row + 1) {
					this.enchantingCosts[row] = 0;
				}
				// EventHooks.onEnchantmentLevelSet was removed - just use the cost value directly
			}
			for (int row = 0; row < 3; ++row) {
				if (this.enchantingCosts[row] > 0) {
					List<EnchantmentInstance> enchantments = this.getEnchantmentList(inputStack, row, this.enchantingCosts[row]);
					if (!enchantments.isEmpty()) {
						EnchantmentInstance instance = Util.getRandom(enchantments, this.rng);
						this.enchantments[row] = registry.getKey(instance.enchantment().value());
						this.enchantmentLevels[row] = instance.level();
					}
				}
			}
		} else {
			for (int row = 0; row < 3; ++row) {
				this.enchantingCosts[row] = 0;
				this.enchantments[row] = EMPTY_ENCHANTMENT;
				this.enchantmentLevels[row] = -1;
			}
		}
		XBNetworkHandler.INSTANCE.sendToPlayer(this.player, new UpdateEnchantmentTablePacket(this.enchantments, this.enchantmentLevels, this.enchantingCosts, this.enchantmentSeed));
	}
	
	private int calculatePower() {
		ItemStack stack = this.handler.getPowerHandler().getStackInSlot(0);
		if (stack.isEmpty()) {
			return 0;
		} else if (stack.is(Items.BOOKSHELF)) {
			return Mth.clamp(stack.getCount(), 0, 15);
		} else if (stack.is(Items.BOOK)) {
			return Mth.clamp(stack.getCount() / 3, 0, 15);
		}
		return 0;
	}
	
	@Override
	public boolean clickMenuButton(@NotNull Player player, int button) {
		if (2 >= button && button >= 0) {
			ItemStack inputStack = this.handler.getInputHandler().getStackInSlot(0);
			ItemStack fuelStack = this.handler.getFuelHandler().getStackInSlot(0);
			int requiredFuel = button + 1;
			if ((fuelStack.isEmpty() || fuelStack.getCount() < requiredFuel) && !player.getAbilities().instabuild) {
				return false;
			} else if (this.enchantingCosts[button] <= 0 || inputStack.isEmpty() || (player.experienceLevel < requiredFuel || player.experienceLevel < this.enchantingCosts[button]) && !player.getAbilities().instabuild) {
				return false;
			} else {
				ItemStack resultStack = inputStack;
				List<EnchantmentInstance> enchantments = this.getEnchantmentList(inputStack, button, this.enchantingCosts[button]);
				if (!enchantments.isEmpty()) {
					player.onEnchantmentPerformed(inputStack, requiredFuel);
					boolean isBook = inputStack.is(Items.BOOK);
					if (isBook) {
						resultStack = inputStack.transmuteCopy(Items.ENCHANTED_BOOK);
						this.handler.getInputHandler().setStackInSlot(0, resultStack);
					}
					for (EnchantmentInstance enchantment : enchantments) {
						resultStack.enchant(enchantment.enchantment(), enchantment.level());
					}
					if (!player.getAbilities().instabuild) {
						fuelStack.shrink(requiredFuel);
						if (fuelStack.isEmpty()) {
							this.handler.getFuelHandler().setStackInSlot(0, ItemStack.EMPTY);
						}
					}
					player.awardStat(Stats.ENCHANT_ITEM);
					if (player instanceof ServerPlayer serverPlayer) {
						CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, resultStack, requiredFuel);
						this.playSound(serverPlayer, serverPlayer.level());
					}
					this.enchantmentSeed = player.getEnchantmentSeed();
					this.slotsChanged();
				}
				return true;
			}
		} else {
			Util.logAndPauseIfInIde(player.getName() + " pressed invalid button id: " + button);
			return false;
		}
	}
	
	private @NotNull List<EnchantmentInstance> getEnchantmentList(@NotNull ItemStack inputStack, int row, int enchantingCost) {
		Optional<HolderSet.Named<Enchantment>> optional = this.player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
		if (optional.isEmpty()) {
			return List.of();
		}
		this.rng.setSeed(this.enchantmentSeed + row);
		List<EnchantmentInstance> enchantments = EnchantmentHelper.selectEnchantment(this.rng, inputStack, enchantingCost, optional.get().stream());
		if (inputStack.is(Items.BOOK) && enchantments.size() > 1) {
			enchantments.remove(this.rng.nextInt(enchantments.size()));
		}
		return enchantments;
	}
	
	private void playSound(@NotNull ServerPlayer player, @NotNull ServerLevel level) {
		player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ENCHANTMENT_TABLE_USE), SoundSource.BLOCKS, player.getX(), player.getY(), player.getZ(), 1.0F, level.random.nextFloat() * 0.1F + 0.9F, level.random.nextLong()));
	}
	
	@Override
	public boolean quickMoveStack(@NotNull ItemStack slotStack, int index) {
		if (908 >= index && index >= 0) { // from container
			if (slotStack.is(Tags.Items.BOOKSHELVES) || this.canQuickMoveBook()) {
				return this.menu.moveItemStackTo(slotStack, 941, 942); // into power
			} else if (slotStack.isEnchantable() || slotStack.getItem() == Items.BOOK) {
				return this.menu.moveItemStackTo(slotStack, 942, 943); // into input
			} else if (slotStack.is(Tags.Items.ENCHANTING_FUELS)) {
				return this.menu.moveItemStackTo(slotStack, 943, 944); // into fuel
			}
		} else if (index >= 943) { // from extension
			return this.movePreferredMenu(slotStack); // into container
		}
		return false;
	}
	
	private boolean canQuickMoveBook() {
		if (!this.handler.getInputHandler().getStackInSlot(0).isEmpty()) {
			ItemStack stack = this.handler.getPowerHandler().getStackInSlot(0);
			return stack.isEmpty() || (stack.is(Items.BOOK) && stack.getMaxStackSize() > stack.getCount());
		}
		return false;
	}
}
