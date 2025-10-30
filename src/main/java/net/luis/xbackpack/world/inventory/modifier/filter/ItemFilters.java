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

package net.luis.xbackpack.world.inventory.modifier.filter;

import com.google.common.collect.Lists;
import net.luis.xbackpack.util.Util;
import net.luis.xbackpack.world.item.CustomBackpackFilterItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DamageResistant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.StreamSupport;

/**
 *
 * @author Luis-St
 *
 */

public enum ItemFilters implements ItemFilter {
	
	NONE("none") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return true;
		}
		
		@Override
		public @NotNull List<MutableComponent> getInfo() {
			List<MutableComponent> infoTooltip = Lists.newArrayList();
			infoTooltip.add(Component.translatable(this.getInfoDescriptorId() + "0"));
			infoTooltip.add(Component.translatable(this.getInfoDescriptorId() + "1"));
			return infoTooltip;
		}
	},
	NAME_SEARCH("name_search", false) {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			String itemName = stack.getDisplayName().getString().replace("[", "").replace("]", "").trim().toLowerCase();
			if (searchTerm.isEmpty()) {
				return true;
			} else if (itemName.equals(searchTerm)) {
				return true;
			} else if (itemName.contains(searchTerm)) {
				return true;
			}
			return itemName.startsWith(searchTerm);
		}
	},
	NAMESPACE_SEARCH("namespace_search", false) {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			String namespace = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())).getNamespace().trim().toLowerCase();
			if (searchTerm.isEmpty()) {
				return true;
			} else if (!searchTerm.startsWith("@")) {
				return false;
			}
			String string = searchTerm.substring(1);
			if (string.isEmpty()) {
				return true;
			} else if (namespace.equals(string)) {
				return true;
			} else if (namespace.contains(string)) {
				return true;
			}
			return namespace.startsWith(string);
		}
	},
	TAG_SEARCH("tag_search", false) {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			if (searchTerm.isEmpty()) {
				return true;
			} else if (!searchTerm.startsWith("#")) {
				return false;
			}
			String string = searchTerm.substring(1);
			if (string.isEmpty()) {
				return true;
			} else {
				TagKey<Item> tag = this.getTag(string);
				if (tag == null) {
					return false;
				} else {
					List<Item> tagItems = StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false)
							.map(net.minecraft.core.Holder::value)
							.toList();
					return tagItems.contains(stack.getItem());
				}
			}
		}

		private @Nullable TagKey<Item> getTag(String searchTerm) {
			List<TagKey<Item>> tags = BuiltInRegistries.ITEM.getTags().map(named -> named.key()).toList();
			for (TagKey<Item> tag : tags) {
				if (searchTerm.replace(" ", "_").equals(tag.location().getPath())) {
					return tag;
				}
			}
			return null;
		}
	},
	COUNT_SEARCH("count_search", false) {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			if (searchTerm.isEmpty()) {
				return true;
			}
			int count = Util.tryParseInteger(searchTerm, -1);
			if (count == -1) {
				return false;
			}
			char firstChar = searchTerm.charAt(0);
			if (firstChar == '=') {
				return stack.getCount() == count;
			} else if (firstChar == '<') {
				return stack.getCount() <= count;
			} else if (firstChar == '>') {
				return stack.getCount() >= count;
			}
			char lastChar = searchTerm.charAt(searchTerm.length() - 1);
			if (lastChar == '<') {
				return stack.getCount() >= count;
			} else if (lastChar == '>') {
				return stack.getCount() <= count;
			}
			return stack.getCount() == count;
		}
	},
	STACKABLE("stackable") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return this.checkCustom(stack, CustomBackpackFilterItem::isStackable) || stack.isStackable();
		}
	},
	NONE_STACKABLE("none_stackable") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return this.checkCustom(stack, (custom, itemStack) -> !custom.isStackable(itemStack)) || !stack.isStackable();
		}
	},
	MAX_COUNT("max_count") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return this.checkCustom(stack, CustomBackpackFilterItem::isMaxCount) || stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 64) == stack.getCount();
		}
	},
	ENCHANTABLE("enchantable") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return stack.getItem() == Items.BOOK || this.checkCustom(stack, CustomBackpackFilterItem::isEnchantable) || stack.isEnchantable();
		}
	},
	ENCHANTED("enchanted") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return stack.has(DataComponents.STORED_ENCHANTMENTS) || this.checkCustom(stack, CustomBackpackFilterItem::isEnchanted) || stack.isEnchanted();
		}
	},
	DAMAGEABLE("damageable") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return this.checkCustom(stack, CustomBackpackFilterItem::isDamageable) || stack.isDamageableItem();
		}
	},
	DAMAGED("damaged") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return this.checkCustom(stack, CustomBackpackFilterItem::isDamaged) || stack.isDamaged();
		}
	},
	FIRE_RESISTANT("fire_resistant") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			DamageResistant damageResistant = stack.get(DataComponents.DAMAGE_RESISTANT);
			if (damageResistant != null) {
				return damageResistant.types() == DamageTypeTags.IS_FIRE;
			}
			return this.checkCustom(stack, CustomBackpackFilterItem::isFireResistant);
		}
	},
	FOOD("food") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return this.checkCustom(stack, CustomBackpackFilterItem::isFood) || stack.has(DataComponents.FOOD);
		}
	},
	WEAPON("weapon") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			Item item = stack.getItem();
			return this.checkCustom(stack, CustomBackpackFilterItem::isWeapon) || stack.is(ItemTags.SWORDS) || item instanceof BowItem || item instanceof CrossbowItem;
		}
	},
	TOOL("tool") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			Item item = stack.getItem();
			return this.checkCustom(stack, CustomBackpackFilterItem::isTool) || stack.has(DataComponents.TOOL) || item instanceof FishingRodItem || item instanceof FlintAndSteelItem || item instanceof CompassItem || item == Items.CLOCK;
		}
	},
	ARMOR("armor") {
		@Override
		protected boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm) {
			return this.checkCustom(stack, CustomBackpackFilterItem::isArmor) || stack.has(DataComponents.EQUIPPABLE);
		}
	};
	
	private final String name;
	private final boolean selectable;
	
	ItemFilters(@NotNull String name) {
		this(name, true);
	}
	
	ItemFilters(@NotNull String name, boolean selectable) {
		this.name = name;
		this.selectable = selectable;
	}
	
	public static @NotNull ItemFilter byId(int id, ItemFilter fallback) {
		for (ItemFilter filter : ItemFilters.values()) {
			if (filter.getId() == id) {
				return filter;
			}
		}
		return fallback;
	}
	
	@Override
	public @NotNull String getName() {
		return this.name;
	}
	
	@Override
	public int getId() {
		return this.ordinal();
	}
	
	@Override
	public boolean isSelectable() {
		return this.selectable;
	}
	
	protected abstract boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm);
	
	@Override
	public boolean canKeepItem(@NotNull ItemStack stack, @NotNull String searchTerm, boolean negate) {
		if (this == NONE) {
			return NONE.canKeepItem(stack, searchTerm);
		}
		return negate != this.canKeepItem(stack, searchTerm);
	}
	
	protected boolean checkCustom(@NotNull ItemStack stack, @NotNull BiPredicate<CustomBackpackFilterItem, ItemStack> predicate) {
		if (stack.getItem() instanceof CustomBackpackFilterItem custom) {
			return predicate.test(custom, stack);
		}
		return false;
	}
	
	@Override
	public @NotNull MutableComponent getDisplayName() {
		return Component.translatable("xbackpack.backpack_action.filter." + this.name);
	}
	
	protected @NotNull String getInfoDescriptorId() {
		return "xbackpack.backpack_action.filter." + this.name + ".info.";
	}
	
	@Override
	public @NotNull List<MutableComponent> getInfo() {
		List<MutableComponent> infoTooltip = Lists.newArrayList();
		infoTooltip.add(Component.translatable(this.getInfoDescriptorId() + "0"));
		return infoTooltip;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
