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

package net.luis.xbackpack.world.inventory.slot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackArmorSlot extends Slot {
	
	private final Player player;
	private final EquipmentSlot equipmentSlot;
	
	public BackpackArmorSlot(@NotNull Inventory inventory, @NotNull EquipmentSlot equipmentSlot, int index, int xPosition, int yPosition) {
		super(inventory, index, xPosition, yPosition);
		this.player = inventory.player;
		this.equipmentSlot = equipmentSlot;
	}
	
	@Override
	public int getMaxStackSize() {
		return 1;
	}
	
	@Override
	public int getMaxStackSize(@NotNull ItemStack stack) {
		return 1;
	}
	
	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return stack.canEquip(this.equipmentSlot, this.player);
	}
	
	@Override
	public boolean mayPickup(@NotNull Player player) {
		ItemStack stack = this.getItem();
		return (stack.isEmpty() || player.isCreative() || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(player);
	}
	
	@Override
	public @NotNull ResourceLocation getNoItemIcon() {
		return switch (this.equipmentSlot) {
			case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
			case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
			case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
			case FEET -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
			default -> super.getNoItemIcon();
		};
	}
}
