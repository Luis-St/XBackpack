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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.luis.xbackpack.network.XBNetworkHandler;
import net.luis.xbackpack.network.packet.extension.UpdateAnvilPacket;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.AbstractExtensionContainerMenu;
import net.luis.xbackpack.world.inventory.extension.slot.ExtensionSlot;
import net.luis.xbackpack.world.inventory.handler.CraftingHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class AnvilExtensionMenu extends AbstractExtensionMenu {
	
	private final CraftingHandler handler;
	private int repairItemCountCost;
	private int cost;
	
	public AnvilExtensionMenu(@NotNull AbstractExtensionContainerMenu menu, @NotNull Player player) {
		super(menu, player, BackpackExtensions.ANVIL.get());
		this.handler = BackpackProvider.get(this.player).getAnvilHandler();
	}
	
	private static int calculateIncreasedRepairCost(int cost) {
		return cost * 2 + 1;
	}
	
	@Override
	public void open() {
		if (!this.handler.getInputHandler().getStackInSlot(0).isEmpty() || !this.handler.getInputHandler().getStackInSlot(1).isEmpty()) {
			this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
			this.createResult();
		}
	}
	
	@Override
	public void addSlots(@NotNull Consumer<Slot> consumer) {
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 0, 225, 73));
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 1, 260, 73));
		consumer.accept(new ExtensionSlot(this, this.handler.getResultHandler(), 0, 304, 73) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return false;
			}
			
			@Override
			public boolean mayPickup(@NotNull Player player) {
				return AnvilExtensionMenu.this.mayPickup(player);
			}
			
			@Override
			public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
				AnvilExtensionMenu.this.onTake(player);
				super.onTake(player, stack);
			}
		});
	}
	
	public boolean mayPickup(@NotNull Player player) {
		return (player.getAbilities().instabuild || player.experienceLevel >= this.cost) && this.cost > 0;
	}
	
	private void onTake(@NotNull Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.getAbilities().instabuild) {
				serverPlayer.giveExperienceLevels(-this.cost);
			}
			this.handler.getInputHandler().setStackInSlot(0, ItemStack.EMPTY);
			if (this.repairItemCountCost > 0) {
				ItemStack rightStack = this.handler.getInputHandler().getStackInSlot(1);
				if (!rightStack.isEmpty() && rightStack.getCount() > this.repairItemCountCost) {
					rightStack.shrink(this.repairItemCountCost);
					this.handler.getInputHandler().setStackInSlot(1, rightStack);
				} else {
					this.handler.getInputHandler().setStackInSlot(1, ItemStack.EMPTY);
				}
			} else {
				this.handler.getInputHandler().setStackInSlot(1, ItemStack.EMPTY);
			}
			this.cost = 0;
			this.playSound(serverPlayer, serverPlayer.level());
		}
		this.menu.broadcastChanges();
		this.broadcastChanges();
		this.createResult();
	}
	
	private void playSound(@NotNull ServerPlayer player, @NotNull ServerLevel level) {
		player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ANVIL_USE), SoundSource.BLOCKS, player.getX(), player.getY(), player.getZ(), 1.0F, level.random.nextFloat() * 0.1F + 0.9F, level.random.nextLong()));
	}
	
	@Override
	public void slotsChanged() {
		this.createResult();
	}
	
	private void createResult() {
		ItemStack leftStack = this.handler.getInputHandler().getStackInSlot(0);
		this.cost = 1;
		int enchantCost = 0;
		int repairCost = 0;
		if (leftStack.isEmpty() || !EnchantmentHelper.canStoreEnchantments(leftStack)) {
			this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
			this.cost = 0;
			this.broadcastChanges();
		} else {
			ItemStack resultStack = leftStack.copy();
			ItemStack rightStack = this.handler.getInputHandler().getStackInSlot(1);
			ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(resultStack));
			repairCost += leftStack.getOrDefault(DataComponents.REPAIR_COST, 0) + rightStack.getOrDefault(DataComponents.REPAIR_COST, 0);
			this.repairItemCountCost = 0;
			boolean enchantedBook = false;
			if (!this.onAnvilUpdate(leftStack, rightStack, repairCost)) {
				return;
			}
			if (!rightStack.isEmpty()) {
				enchantedBook = rightStack.has(DataComponents.STORED_ENCHANTMENTS);
				if (resultStack.isDamageableItem() && resultStack.isValidRepairItem(rightStack)) {
					int damage = Math.min(resultStack.getDamageValue(), resultStack.getMaxDamage() / 4);
					if (damage <= 0) {
						this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
						this.cost = 0;
						this.broadcastChanges();
						return;
					}
					
					int currentRepairCost;
					for (currentRepairCost = 0; damage > 0 && currentRepairCost < rightStack.getCount(); ++currentRepairCost) {
						int currentDamage = resultStack.getDamageValue() - damage;
						resultStack.setDamageValue(currentDamage);
						++enchantCost;
						damage = Math.min(resultStack.getDamageValue(), resultStack.getMaxDamage() / 4);
					}
					
					this.repairItemCountCost = currentRepairCost;
				} else {
					if (!enchantedBook && (!resultStack.is(rightStack.getItem()) || !resultStack.isDamageableItem())) {
						this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
						this.cost = 0;
						this.broadcastChanges();
						return;
					}
					if (resultStack.isDamageableItem() && !enchantedBook) {
						int leftDamage = leftStack.getMaxDamage() - leftStack.getDamageValue();
						int rightDamage = rightStack.getMaxDamage() - rightStack.getDamageValue();
						int resultDamage = rightDamage + resultStack.getMaxDamage() * 12 / 100;
						int combindDamage = leftDamage + resultDamage;
						int damage = resultStack.getMaxDamage() - combindDamage;
						if (damage < 0) {
							damage = 0;
						}
						if (damage < resultStack.getDamageValue()) {
							resultStack.setDamageValue(damage);
							enchantCost += 2;
						}
					}
					//Issue start
					ItemEnchantments rightEnchantments = rightStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
					boolean canEnchant = false;
					boolean survival = false;
					for (Object2IntMap.Entry<Holder<Enchantment>> entry : rightEnchantments.entrySet()) {
						Holder<Enchantment> rightHolder = entry.getKey();
						int resultLevel = mutable.getLevel(rightHolder);
						int rightLevel = entry.getIntValue();
						rightLevel = resultLevel == rightLevel ? rightLevel + 1 : Math.max(rightLevel, resultLevel);
						boolean canEnchantOrCreative = rightHolder.value().canEnchant(leftStack);
						if (this.player.getAbilities().instabuild || leftStack.is(Items.ENCHANTED_BOOK)) {
							canEnchantOrCreative = true;
						}
						for (Holder<Enchantment> holder : mutable.keySet()) {
							if (!holder.equals(rightHolder) && !Enchantment.areCompatible(rightHolder, holder)) {
								canEnchantOrCreative = false;
								++enchantCost;
							}
						}
						if (canEnchantOrCreative) {
							canEnchant = true;
							if (rightLevel > rightHolder.value().getMaxLevel()) {
								rightLevel = rightHolder.value().getMaxLevel();
							}
							mutable.set(rightHolder, rightLevel);
							int anvilCost = rightHolder.value().getAnvilCost();
							if (enchantedBook) {
								anvilCost = Math.max(1, anvilCost / 2);
							}
							enchantCost += anvilCost * rightLevel;
							if (leftStack.getCount() > 1) {
								enchantCost = 40;
							}
						} else {
							survival = true;
						}
					}
					//Issue end
					if (survival && !canEnchant) { // Issue encounter here
						this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
						this.cost = 0;
						this.broadcastChanges();
						return;
					}
				}
			}
			// isBookEnchantable was removed - the check is now integrated into vanilla logic
			// if (enchantedBook && !resultStack.isBookEnchantable(rightStack)) {
			//	resultStack = ItemStack.EMPTY;
			// }
			this.cost = repairCost + enchantCost;
			if (enchantCost <= 0) {
				resultStack = ItemStack.EMPTY;
			}
			if (this.cost >= 40 && !this.player.getAbilities().instabuild) {
				resultStack = ItemStack.EMPTY;
			}
			if (!resultStack.isEmpty()) {
				int baseRepairCost = resultStack.getOrDefault(DataComponents.REPAIR_COST, 0);
				if (!rightStack.isEmpty() && baseRepairCost < rightStack.getOrDefault(DataComponents.REPAIR_COST, 0)) {
					baseRepairCost = rightStack.getOrDefault(DataComponents.REPAIR_COST, 0);
				}
				baseRepairCost = calculateIncreasedRepairCost(baseRepairCost);
				resultStack.set(DataComponents.REPAIR_COST, baseRepairCost);
				EnchantmentHelper.setEnchantments(resultStack, mutable.toImmutable());
			}
			this.handler.getResultHandler().setStackInSlot(0, resultStack);
			this.menu.broadcastChanges();
			this.broadcastChanges();
		}
	}
	
	private void broadcastChanges() {
		XBNetworkHandler.INSTANCE.sendToPlayer(this.player, new UpdateAnvilPacket(this.cost));
	}
	
	public int getCost() {
		return this.cost;
	}
	
	private boolean onAnvilUpdate(@NotNull ItemStack leftStack, @NotNull ItemStack rightStack, int repairCost) {
		AnvilUpdateEvent event = new AnvilUpdateEvent(leftStack, rightStack, leftStack.getDisplayName().getString(), ItemStack.EMPTY, repairCost, 0, this.player);
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
			return false;
		} else if (event.getOutput().isEmpty()) {
			return true;
		}
		this.handler.getResultHandler().setStackInSlot(0, event.getOutput());
		this.cost = event.getXpCost();
		this.repairItemCountCost = event.getMaterialCost();
		this.broadcastChanges();
		return false;
	}
	
	@Override
	public boolean quickMoveStack(@NotNull ItemStack slotStack, int index) {
		if (908 >= index && index >= 0) { // from container
			return this.menu.moveItemStackTo(slotStack, 938, 940); // into input
		}/* else if (index == 940) { // from result
			return this.movePreferredMenu(slotStack); // into container
		}*/ // Not possible due issue in vanilla code
		return false;
	}
	
	@Override
	public void close() {
		this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
	}
}
