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

package net.luis.xbackpack.world.extension;

import net.luis.xbackpack.XBackpack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackExtensions {
	
	public static final ResourceKey<Registry<BackpackExtension>> REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "backpack_extension"));
	public static final DeferredRegister<BackpackExtension> BACKPACK_EXTENSIONS = DeferredRegister.create(REGISTRY_KEY, XBackpack.MOD_ID);
	public static final Registry<BackpackExtension> REGISTRY = BACKPACK_EXTENSIONS.makeRegistry(builder -> {
		// Configure the registry builder if needed
		// builder.sync(true); // example configuration
	});

	public static final DeferredHolder<BackpackExtension, BackpackExtension> NO = BACKPACK_EXTENSIONS.register("no", () -> {
		return new BackpackExtension(ItemStack.EMPTY, 0, 0, 0, 0, true);
	});
	public static final DeferredHolder<BackpackExtension, BackpackExtension> CRAFTING_TABLE = BACKPACK_EXTENSIONS.register("crafting_table", () -> {
		return new BackpackExtension(new ItemStack(Items.CRAFTING_TABLE), 20, 22, 68, 135);
	});
	public static final DeferredHolder<BackpackExtension, BackpackExtension> FURNACE = BACKPACK_EXTENSIONS.register("furnace", () -> {
		return new BackpackExtension(new ItemStack(Items.FURNACE), 20, 22, 86, 126);
	});
	public static final DeferredHolder<BackpackExtension, BackpackExtension> ANVIL = BACKPACK_EXTENSIONS.register("anvil", () -> {
		return new BackpackExtension(new ItemStack(Items.ANVIL), 20, 22, 111, 46);
	});
	public static final DeferredHolder<BackpackExtension, BackpackExtension> ENCHANTMENT_TABLE = BACKPACK_EXTENSIONS.register("enchantment_table", () -> {
		return new BackpackExtension(new ItemStack(Items.ENCHANTING_TABLE), 20, 22, 136, 88);
	});
	public static final DeferredHolder<BackpackExtension, BackpackExtension> STONECUTTER = BACKPACK_EXTENSIONS.register("stonecutter", () -> {
		return new BackpackExtension(new ItemStack(Items.STONECUTTER), 20, 22, 95, 136);
	});
	public static final DeferredHolder<BackpackExtension, BackpackExtension> BREWING_STAND = BACKPACK_EXTENSIONS.register("brewing_stand", () -> {
		return new BackpackExtension(new ItemStack(Items.BREWING_STAND), 20, 22, 106, 88);
	});
	public static final DeferredHolder<BackpackExtension, BackpackExtension> GRINDSTONE = BACKPACK_EXTENSIONS.register("grindstone", () -> {
		return new BackpackExtension(new ItemStack(Items.GRINDSTONE), 20, 22, 112, 84);
	});
	public static final DeferredHolder<BackpackExtension, BackpackExtension> SMITHING_TABLE = BACKPACK_EXTENSIONS.register("smithing_table", () -> {
		return new BackpackExtension(new ItemStack(Items.SMITHING_TABLE), 20, 22, 111, 46);
	});
}
