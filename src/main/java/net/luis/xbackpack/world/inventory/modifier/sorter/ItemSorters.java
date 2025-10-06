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

package net.luis.xbackpack.world.inventory.modifier.sorter;

import com.google.common.collect.Lists;
import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.StreamSupport;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Luis-St
 *
 */

public enum ItemSorters implements ItemSorter {
	
	NONE("none") {
		@Override
		public @NotNull List<ItemStack> sort(@NotNull List<ItemStack> stacks, @NotNull String searchTerm, boolean negate) {
			return stacks;
		}
	},
	NAME_SEARCH("name_search", false) {
		@Override
		public @NotNull List<ItemStack> sort(@NotNull List<ItemStack> stacks, @NotNull String searchTerm, boolean negate) {
			if (!searchTerm.isEmpty()) {
				List<ItemStack> returnList = Lists.newArrayList();
				List<ItemStack> equalsList = Lists.newArrayList();
				List<ItemStack> startsList = Lists.newArrayList();
				List<ItemStack> containsList = Lists.newArrayList();
				Iterator<ItemStack> iterator = stacks.iterator();
				while (iterator.hasNext()) {
					ItemStack stack = iterator.next();
					String name = stack.getDisplayName().getString().replace("[", "").replace("]", "").trim().toLowerCase();
					if (negate != name.equals(searchTerm)) {
						equalsList.add(stack);
						iterator.remove();
					} else if (negate != name.startsWith(searchTerm)) {
						startsList.add(stack);
						iterator.remove();
					} else if (negate != name.contains(searchTerm)) {
						containsList.add(stack);
						iterator.remove();
					} else {
						XBackpack.LOGGER.error("[Filter Failed] The filtered item list contains an item with the name '{}' that does not match the search term '{}' in any form", name, searchTerm);
					}
				}
				returnList.addAll(ItemSorters.ALPHABETICALLY.sort(equalsList, searchTerm, negate));
				returnList.addAll(ItemSorters.ALPHABETICALLY.sort(startsList, searchTerm, negate));
				returnList.addAll(ItemSorters.ALPHABETICALLY.sort(containsList, searchTerm, negate));
				return returnList;
			}
			XBackpack.LOGGER.info("An attempt is made to apply a search term sorter to an empty search term");
			return stacks;
		}
	},
	NAMESPACE_SEARCH("namespace_search", false) {
		@Override
		public @NotNull List<ItemStack> sort(@NotNull List<ItemStack> stacks, @NotNull String searchTerm, boolean negate) {
			if (searchTerm.startsWith("@")) {
				String string = searchTerm.substring(1);
				if (!string.isEmpty()) {
					List<ItemStack> equalsList = Lists.newArrayList();
					List<ItemStack> startsList = Lists.newArrayList();
					List<ItemStack> containsList = Lists.newArrayList();
					Iterator<ItemStack> iterator = stacks.iterator();
					while (iterator.hasNext()) {
						ItemStack stack = iterator.next();
						String namespace = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())).getNamespace();
						if (negate != namespace.equals(string)) {
							equalsList.add(stack);
							iterator.remove();
						} else if (negate != namespace.startsWith(string)) {
							startsList.add(stack);
							iterator.remove();
						} else if (negate != namespace.contains(string)) {
							containsList.add(stack);
							iterator.remove();
						} else {
							XBackpack.LOGGER.error("[Filter Failed] The filtered item list contains an item with the namespace '{}' that does not match the search term '{}' in any form", namespace, string);
						}
					}
					equalsList.sort(Comparator.comparing(stack -> Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())).getNamespace()));
					startsList.sort(Comparator.comparing(stack -> Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())).getNamespace()));
					containsList.sort(Comparator.comparing(stack -> Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())).getNamespace()));
					return Stream.of(equalsList, startsList, containsList).flatMap(List::stream).collect(Collectors.toList());
				}
				return stacks;
			}
			XBackpack.LOGGER.info("An attempt is made to apply a search term sorter to an empty search term");
			return stacks;
		}
	},
	TAG_SEARCH("tag_search", false) {
		@Override
		public @NotNull List<ItemStack> sort(@NotNull List<ItemStack> stacks, @NotNull String searchTerm, boolean negate) {
			if (searchTerm.startsWith("#")) {
				TagKey<Item> tag = this.getTag(searchTerm);
				if (tag != null) {
					List<ItemStack> returnList = Lists.newArrayList();
					for (ItemStack stack : stacks) {
						List<Item> tagItems = StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false)
								.map(net.minecraft.core.Holder::value)
								.toList();
						if (negate != tagItems.contains(stack.getItem())) {
							returnList.add(stack);
						} else {
							XBackpack.LOGGER.error("[Filter Failed] The filtered item list contains an item '{}' which is not subordinated to the tag '{}'", stack.getItem(), tag.location());
						}
					}
					return ItemSorters.ALPHABETICALLY.sort(returnList, searchTerm, negate);
				}
				return stacks;
			}
			XBackpack.LOGGER.info("An attempt is made to apply a search term sorter to an empty search term");
			return stacks;
		}

		@Nullable
		private TagKey<Item> getTag(String searchTerm) {
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
		public @NotNull List<ItemStack> sort(@NotNull List<ItemStack> stacks, @NotNull String searchTerm, boolean negate) {
			if (!searchTerm.isEmpty()) {
				int count = Util.tryParseInteger(searchTerm, -1);
				if (count == -1) {
					XBackpack.LOGGER.error("There was a problem sorting the items by the stack size, the search term is invalid");
				}
				return ItemSorters.ALPHABETICALLY.sort(stacks, searchTerm, negate);
			}
			XBackpack.LOGGER.info("An attempt is made to apply a search term sorter to an empty search term");
			return stacks;
		}
		
		@Override
		public @NotNull List<MutableComponent> getInfo() {
			List<MutableComponent> infoTooltip = Lists.newArrayList();
			infoTooltip.add(Component.translatable(this.getInfoDescriptorId() + "0"));
			return infoTooltip;
		}
	},
	ALPHABETICALLY("alphabetically") {
		@Override
		public @NotNull List<ItemStack> sort(@NotNull List<ItemStack> stacks, @NotNull String searchTerm, boolean negate) {
			stacks.sort(Comparator.comparing(stack -> stack.getDisplayName().getString().toLowerCase()));
			return stacks;
		}
		
		@Override
		public @NotNull List<MutableComponent> getInfo() {
			List<MutableComponent> infoTooltip = Lists.newArrayList();
			infoTooltip.add(Component.translatable(this.getInfoDescriptorId() + "0"));
			return infoTooltip;
		}
	},
	COUNT_DOWNWARDS("count_downwards") {
		@Override
		public @NotNull List<ItemStack> sort(@NotNull List<ItemStack> stacks, @NotNull String searchTerm, boolean negate) {
			stacks.sort((firstStack, secondStack) -> Integer.compare(secondStack.getCount(), firstStack.getCount()));
			return stacks;
		}
	},
	COUNT_UPWARDS("count_upwards") {
		@Override
		public @NotNull List<ItemStack> sort(@NotNull List<ItemStack> stacks, @NotNull String searchTerm, boolean negate) {
			stacks.sort(Comparator.comparingInt(ItemStack::getCount));
			return stacks;
		}
	};
	
	private final String name;
	private final boolean selectable;
	
	ItemSorters(String name) {
		this(name, true);
	}
	
	ItemSorters(String name, boolean selectable) {
		this.name = name;
		this.selectable = selectable;
	}
	
	public static @NotNull ItemSorter byId(int id) {
		return byId(id, null);
	}
	
	public static @NotNull ItemSorter byId(int id, ItemSorter fallback) {
		for (ItemSorter sorter : ItemSorters.values()) {
			if (sorter.getId() == id) {
				return sorter;
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
	
	@Override
	public abstract @NotNull List<ItemStack> sort(@NotNull List<ItemStack> stacks, @NotNull String searchTerm, boolean negate);
	
	@Override
	public @NotNull MutableComponent getDisplayName() {
		return Component.translatable("xbackpack.backpack_action.sorter." + this.name);
	}
	
	protected @NotNull String getInfoDescriptorId() {
		return "xbackpack.backpack_action.sorter." + this.name + ".info.";
	}
	
	@Override
	public @NotNull List<MutableComponent> getInfo() {
		List<MutableComponent> infoTooltip = Lists.newArrayList();
		infoTooltip.add(Component.translatable(this.getInfoDescriptorId() + "0"));
		infoTooltip.add(Component.translatable(this.getInfoDescriptorId() + "1"));
		return infoTooltip;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
