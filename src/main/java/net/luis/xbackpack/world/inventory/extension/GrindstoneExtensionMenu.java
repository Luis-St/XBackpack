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
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GrindstoneEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public class GrindstoneExtensionMenu extends AbstractExtensionMenu {
	
	private final CraftingHandler handler;
	private int xp = -1;
	
	public GrindstoneExtensionMenu(@NotNull AbstractExtensionContainerMenu menu, @NotNull Player player) {
		super(menu, player, BackpackExtensions.GRINDSTONE.get());
		this.handler = BackpackProvider.get(this.player).getGrindstoneHandler();
	}
	
	@Override
	public void open() {
		this.createResult();
	}
	
	@Override
	public void addSlots(@NotNull Consumer<Slot> consumer) {
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 0, 243, 172) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return true;
			}
		});
		consumer.accept(new ExtensionSlot(this, this.handler.getInputHandler(), 1, 243, 193) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return true;
			}
		});
		consumer.accept(new ExtensionSlot(this, this.handler.getResultHandler(), 0, 305, 187, false) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return false;
			}
			
			@Override
			public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
				GrindstoneExtensionMenu.this.onTake(player, stack);
			}
		});
	}
	
	private void onTake(@NotNull Player player, @NotNull ItemStack stack) {
		if (player instanceof ServerPlayer serverPlayer) {
			GrindstoneEvent.OnTakeItem event = new GrindstoneEvent.OnTakeItem(this.handler.getInputHandler().getStackInSlot(0), this.handler.getInputHandler().getStackInSlot(1), this.getExperienceAmount(player.level()));
			if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
				return;
			}
			player.giveExperiencePoints(event.getXp());
			this.playSound(serverPlayer, serverPlayer.level());
			this.handler.getInputHandler().setStackInSlot(0, event.getNewTopItem());
			this.handler.getInputHandler().setStackInSlot(1, event.getNewBottomItem());
		}
		this.menu.broadcastChanges();
	}
	
	private int getExperienceAmount(@NotNull Level level) {
		if (this.xp > -1) {
			return this.xp;
		}
		int amount = 0;
		amount += this.getExperienceFromItem(this.handler.getInputHandler().getStackInSlot(0));
		amount += this.getExperienceFromItem(this.handler.getInputHandler().getStackInSlot(1));
		if (amount > 0) {
			int experience = (int) Math.ceil((double) amount / 2.0D);
			return experience + level.random.nextInt(experience);
		} else {
			return 0;
		}
	}
	
	private int getExperienceFromItem(@NotNull ItemStack stack) {
		int experience = 0;
		ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
			if (!entry.getKey().is(EnchantmentTags.CURSE)) {
				experience += entry.getKey().value().getMinCost(entry.getIntValue());
			}
		}
		return experience;
	}
	
	private void playSound(@NotNull ServerPlayer player, @NotNull ServerLevel level) {
		player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.GRINDSTONE_USE), SoundSource.BLOCKS, player.getX(), player.getY(), player.getZ(), 1.0F, level.random.nextFloat() * 0.1F + 0.9F, level.random.nextLong()));
	}
	
	@Override
	public void slotsChanged() {
		this.createResult();
	}
	
	private void createResult() {
		ItemStack topStack = this.handler.getInputHandler().getStackInSlot(0);
		ItemStack bottomStack = this.handler.getInputHandler().getStackInSlot(1);
		boolean hasInput = !topStack.isEmpty() || !bottomStack.isEmpty();
		boolean hasInputs = !topStack.isEmpty() && !bottomStack.isEmpty();
		this.xp = this.onGrindstoneChange(topStack, bottomStack, this.xp);
		if (this.xp != Integer.MIN_VALUE) {
			return;
		}
		if (hasInput && topStack.getCount() <= 1 && bottomStack.getCount() <= 1) {
			if (hasInputs) {
				this.handler.getResultHandler().setStackInSlot(0, this.mergeItems(topStack, bottomStack));
				this.menu.broadcastChanges();
				return;
			} else {
				ItemStack resultStack = !topStack.isEmpty() ? topStack : bottomStack;
				if (EnchantmentHelper.hasAnyEnchantments(resultStack)) {
					this.handler.getResultHandler().setStackInSlot(0, this.removeNonCursesFrom(resultStack.copy()));
					this.menu.broadcastChanges();
					return;
				}
			}
		}
		this.handler.getResultHandler().setStackInSlot(0, ItemStack.EMPTY);
		this.menu.broadcastChanges();
	}
	
	private @NotNull ItemStack mergeItems(@NotNull ItemStack topStack, @NotNull ItemStack bottomStack) {
		if (topStack.is(bottomStack.getItem())) {
			int maxDamage = Math.max(topStack.getMaxDamage(), bottomStack.getMaxDamage());
			int topDurability = topStack.getMaxDamage() - topStack.getDamageValue();
			int bottomDurability = bottomStack.getMaxDamage() - bottomStack.getDamageValue();
			int resultDurability = topDurability + bottomDurability + maxDamage * 5 / 100;
			int count = 1;
			if (!topStack.isDamageableItem()) {
				if (topStack.getMaxStackSize() < 2 || !ItemStack.matches(topStack, bottomStack)) {
					return ItemStack.EMPTY;
				}
				count = 2;
			}
			ItemStack resultStack = topStack.copyWithCount(count);
			if (resultStack.isDamageableItem()) {
				resultStack.set(DataComponents.MAX_DAMAGE, maxDamage);
				resultStack.setDamageValue(Math.max(maxDamage - resultDurability, 0));
			}
			this.mergeEnchantsFrom(resultStack, bottomStack);
			return this.removeNonCursesFrom(resultStack);
		} else {
			return ItemStack.EMPTY;
		}
	}
	
	private void mergeEnchantsFrom(ItemStack topStack, ItemStack bottomStack) {
		EnchantmentHelper.updateEnchantments(topStack, mutable -> {
			for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(bottomStack).entrySet()) {
				Holder<Enchantment> holder = entry.getKey();
				if (!holder.is(EnchantmentTags.CURSE) || mutable.getLevel(holder) == 0) {
					mutable.upgrade(holder, entry.getIntValue());
				}
			}
		});
	}
	
	private @NotNull ItemStack removeNonCursesFrom(@NotNull ItemStack stack) {
		ItemEnchantments enchantments = EnchantmentHelper.updateEnchantments(stack, mutable -> {
			mutable.removeIf(holder -> {
				return !holder.is(EnchantmentTags.CURSE);
			});
		});
		if (stack.is(Items.ENCHANTED_BOOK) && enchantments.isEmpty()) {
			stack = stack.transmuteCopy(Items.BOOK);
		}
		int repairCost = 0;
		for (int i = 0; i < enchantments.size(); i++) {
			repairCost = AnvilMenu.calculateIncreasedRepairCost(repairCost);
		}
		stack.set(DataComponents.REPAIR_COST, repairCost);
		return stack;
	}
	
	private int onGrindstoneChange(@NotNull ItemStack topStack, @NotNull ItemStack bottomStack, int xp) {
		GrindstoneEvent.OnPlaceItem event = new GrindstoneEvent.OnPlaceItem(topStack, bottomStack, xp);
		if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
			return event.getXp();
		}
		if (event.getOutput().isEmpty()) {
			return Integer.MIN_VALUE;
		}
		this.handler.getResultHandler().setStackInSlot(0, event.getOutput());
		return event.getXp();
	}
	
	@Override
	public boolean quickMoveStack(@NotNull ItemStack slotStack, int index) {
		if (908 >= index && index >= 0) { // from container
			if (slotStack.isDamageableItem() || slotStack.is(Items.ENCHANTED_BOOK) || slotStack.isEnchanted()) {
				return this.menu.moveItemStackTo(slotStack, 951, 953); // into input
			}
		} else if (index == 953) { // from result
			return this.movePreferredMenu(slotStack); // into container
		}
		return false;
	}
}
