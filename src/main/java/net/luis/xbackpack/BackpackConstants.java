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

package net.luis.xbackpack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackConstants {
	
	private static final List<Item> ITEMS = ImmutableList.copyOf(BuiltInRegistries.ITEM.stream().toList());
	
	/**
	 * Add recipe types to this list in {@link FMLCommonSetupEvent},<br>
	 * if you want to allow the recipe type in the {@link BackpackExtensions#FURNACE Furnace Extension}
	 */
	public static final List<RecipeType<? extends AbstractCookingRecipe>> FURNACE_RECIPE_TYPES = Lists.newArrayList(RecipeType.SMELTING, RecipeType.BLASTING, RecipeType.SMOKING);
	
	/**
	 * Add items to this list in {@link FMLCommonSetupEvent},<br>
	 * if you want to add valid items for the tool slot
	 */
	public static final List<Item> VALID_TOOL_SLOT_ITEMS = ITEMS.stream().filter(item -> {
		return item.components().has(DataComponents.TOOL) || item instanceof ShearsItem || item instanceof FlintAndSteelItem || item instanceof ProjectileWeaponItem;
	}).collect(Collectors.toList());
	
	/**
	 * Add items to this list in {@link FMLCommonSetupEvent},<br>
	 * if you want to add valid items for the armor slot<br>
	 * <br>
	 * note: the item can only be place in the Slot if {@link ItemStack#canEquip(EquipmentSlot, Entity)} returns {@code true}
	 */
	public static final List<Item> VALID_ARMOR_SLOT_ITEMS = ITEMS.stream().filter(item -> {
		return item.components().has(DataComponents.EQUIPPABLE) || item.components().has(DataComponents.GLIDER) || item == Items.CARVED_PUMPKIN;
	}).collect(Collectors.toList());
	
	/**
	 * Add items to this list in {@link FMLCommonSetupEvent},<br>
	 * if you want to quick move the item via shift into the offhand slot
	 */
	public static final List<Item> SHIFTABLE_OFFHAND_SLOT_ITEMS = ITEMS.stream().filter(item -> {
		return item instanceof ShieldItem || item == Items.TORCH || item == Items.SOUL_TORCH || item.components().has(DataComponents.FOOD);
	}).collect(Collectors.toList());
}
