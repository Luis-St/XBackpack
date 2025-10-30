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

package net.luis.xbackpack.world.inventory;

import com.google.common.collect.Lists;
import net.luis.xbackpack.BackpackConstants;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.capability.IBackpack;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.extension.*;
import net.luis.xbackpack.world.inventory.handler.ModifiableHandler;
import net.luis.xbackpack.world.inventory.modifier.filter.ItemFilters;
import net.luis.xbackpack.world.inventory.slot.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.luis.xbackpack.world.inventory.modifier.sorter.ItemSorters.*;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackMenu extends AbstractModifiableContainerMenu {
	
	private final ModifiableHandler handler;
	
	public BackpackMenu(int id, @NotNull Inventory inventory, @NotNull FriendlyByteBuf byteBuf) {
		this(id, inventory);
	}
	
	public BackpackMenu(int id, @NotNull Inventory inventory) {
		super(XBMenuTypes.BACKPACK_MENU.get(), id, inventory);
		Player player = inventory.player;
		IBackpack backpack = BackpackProvider.get(player);
		this.handler = new ModifiableHandler(backpack.getBackpackHandler());
		for (int i = 0; i < this.handler.getSlots() / 9; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new BackpackSlot(this.handler, j + i * 9, 30 + j * 18, 18 + i * 18));
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inventory, j + i * 9 + 9, 30 + j * 18, 138 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inventory, i, 30 + i * 18, 196));
		}
		this.addSlot(new BackpackToolSlot(backpack.getToolHandler(), 0, 196, 138)); // top
		this.addSlot(new BackpackToolSlot(backpack.getToolHandler(), 1, 196, 156)); // mid
		this.addSlot(new BackpackToolSlot(backpack.getToolHandler(), 2, 196, 174)); // down
		this.addSlot(new BackpackArmorSlot(inventory, EquipmentSlot.HEAD, 39, 8, 18));
		this.addSlot(new BackpackArmorSlot(inventory, EquipmentSlot.CHEST, 38, 8, 36));
		this.addSlot(new BackpackArmorSlot(inventory, EquipmentSlot.LEGS, 37, 8, 54));
		this.addSlot(new BackpackArmorSlot(inventory, EquipmentSlot.FEET, 36, 8, 72));
		this.addSlot(new BackpackOffhandSlot(inventory, 40, 8, 196));
		this.addExtensionMenu(BackpackExtensions.CRAFTING_TABLE.get(), player, CraftingExtensionMenu::new);
		this.addExtensionMenu(BackpackExtensions.FURNACE.get(), player, FurnaceExtensionMenu::new);
		this.addExtensionMenu(BackpackExtensions.ANVIL.get(), player, AnvilExtensionMenu::new);
		this.addExtensionMenu(BackpackExtensions.ENCHANTMENT_TABLE.get(), player, EnchantmentTableExtensionMenu::new);
		this.addExtensionMenu(BackpackExtensions.STONECUTTER.get(), player, StonecutterExtensionMenu::new);
		this.addExtensionMenu(BackpackExtensions.BREWING_STAND.get(), player, BrewingStandExtensionMenu::new);
		this.addExtensionMenu(BackpackExtensions.GRINDSTONE.get(), player, GrindstoneExtensionMenu::new);
		this.addExtensionMenu(BackpackExtensions.SMITHING_TABLE.get(), player, SmithingTableExtensionMenu::new);
	}
	
	public @NotNull ModifiableHandler getHandler() {
		return this.handler;
	}
	
	@Override
	public boolean stillValid(@NotNull Player player) {
		return true;
	}
	
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.getSlot(index);
		if (slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			if (872 >= index && index >= 0) { // from menu
				stack = slotStack.copy();
				if (!this.moveSpecial(slotStack)) {
					if (!this.moveExtension(slotStack, index)) { // into extension
						if (!this.moveInventory(slotStack)) { // into inventory
							return ItemStack.EMPTY;
						}
					}
				}
			} else if (908 >= index && index >= 873) { // from inventory
				stack = slotStack.copy();
				if (!this.moveSpecial(slotStack)) {
					if (!this.moveExtension(slotStack, index)) { // into extension
						if (!this.moveItemStackTo(slotStack, 0, 873)) { // into menu
							return ItemStack.EMPTY;
						}
					}
				}
			} else if (916 >= index && index >= 909) { // from tool, armor or offhand slot
				stack = slotStack.copy();
				if (!this.moveExtension(slotStack, index)) { // into extension
					if (!this.moveItemStackTo(slotStack, 0, 873)) { // into menu
						if (!this.moveInventory(slotStack)) { // into inventory
							return ItemStack.EMPTY;
						}
					}
				}
			} else if (956 >= index && index >= 917) { // from extensions
				stack = slotStack.copy();
				if (!this.moveExtension(slotStack, index)) { // into extension
					if (!this.moveItemStackTo(slotStack, 0, 873)) { // into menu
						if (!this.moveInventory(slotStack)) { // into inventory
							return ItemStack.EMPTY;
						}
					}
				}
			}
			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			slot.onTake(player, slotStack);
		}
		return stack;
	}
	
	private boolean moveInventory(@NotNull ItemStack slotStack) {
		if (!this.moveItemStackTo(slotStack, 900, 909)) { // into hotbar
			return this.moveItemStackTo(slotStack, 873, 900); // into inventory
		}
		return true;
	}
	
	private boolean moveSpecial(@NotNull ItemStack slotStack) {
		if (BackpackConstants.VALID_TOOL_SLOT_ITEMS.contains(slotStack.getItem())) {
			return this.moveItemStackTo(slotStack, 909, 912); // into tool slot
		} else if (BackpackConstants.SHIFTABLE_OFFHAND_SLOT_ITEMS.contains(slotStack.getItem())) {
			return this.moveItemStackTo(slotStack, 916, 917); // into offhand slot
		} else if (BackpackConstants.VALID_ARMOR_SLOT_ITEMS.contains(slotStack.getItem())) {
			return this.moveItemStackTo(slotStack, 912, 916); // into armor slot
		}
		return false;
	}
	
	@Override
	public boolean clickMenuButton(@NotNull Player player, int button) {
		if (this.getExtensionMenu().isEmpty()) {
			if (player instanceof ServerPlayer serverPlayer) {
				if (button == 0) {
					this.updateFilter(null, UpdateType.CYCLE, CycleDirection.FORWARDS);
					return true;
				} else if (button == 1) {
					this.updateFilter(null, UpdateType.CYCLE, CycleDirection.BACKWARDS);
					return true;
				} else if (button == 2) {
					this.updateSorter(null, UpdateType.CYCLE, CycleDirection.FORWARDS);
					return true;
				} else if (button == 3) {
					this.updateSorter(null, UpdateType.CYCLE, CycleDirection.BACKWARDS);
					return true;
				} else if (button == 4) {
					this.mergeInventory(serverPlayer);
					return true;
				}
			}
		}
		return super.clickMenuButton(player, button);
	}
	
	private void mergeInventory(@NotNull ServerPlayer player) {
		List<ItemStack> failedStacks = Lists.newArrayList();
		ModifiableHandler handler = new ModifiableHandler(this.handler.getSlots());
		this.handler.resetWrappedSlots();
		for (int i = 0; i < this.handler.getSlots(); i++) {
			ItemStack stack = this.handler.getStackInSlot(i);
			if (!stack.isEmpty()) {
				stack = handler.insertItem(stack, false);
				if (!stack.isEmpty()) {
					failedStacks.add(stack);
				}
			}
		}
		for (int i = 0; i < handler.getSlots(); i++) {
			if (this.handler.getWrappedSlot(i) != i) {
				this.handler.setWrappedSlot(i, i);
			}
			this.handler.setStackInSlot(i, handler.getStackInSlot(i));
		}
		failedStacks.removeIf(ItemStack::isEmpty);
		if (!failedStacks.isEmpty()) {
			failedStacks.forEach(stack -> player.drop(stack, false));
			failedStacks.clear();
		}
		this.broadcastChanges();
	}
	
	@Override
	protected void onItemModifiersChanged(@NotNull ServerPlayer player) {
		if (this.getFilter() == ItemFilters.NONE && this.getSorter() == NONE) {
			this.handler.resetWrappedSlots();
		} else {
			List<ItemStack> stacks = this.handler.createModifiableList();
			String searchTerm = this.getSearchTerm();
			boolean negate = this.isNegate();
			if (!searchTerm.isEmpty()) {
				switch (this.getSorter()) {
					case NAME_SEARCH -> stacks.removeIf((stack) -> !ItemFilters.NAME_SEARCH.canKeepItem(stack, searchTerm, negate));
					case NAMESPACE_SEARCH -> stacks.removeIf((stack) -> !ItemFilters.NAMESPACE_SEARCH.canKeepItem(stack, searchTerm, negate));
					case TAG_SEARCH -> stacks.removeIf((stack) -> !ItemFilters.TAG_SEARCH.canKeepItem(stack, searchTerm, negate));
					case COUNT_SEARCH -> stacks.removeIf((stack) -> !ItemFilters.COUNT_SEARCH.canKeepItem(stack, searchTerm, negate));
					default -> {break;}
				}
			}
			stacks.removeIf((stack) -> !this.getFilter().canKeepItem(stack, searchTerm, negate));
			this.handler.applyModifications(this.getSorter().sort(stacks, searchTerm, negate));
		}
		this.broadcastChanges();
	}
}
