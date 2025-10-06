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

package net.luis.xbackpack.world.inventory;

import com.google.common.collect.Lists;
import net.luis.xbackpack.network.XBNetworkHandler;
import net.luis.xbackpack.network.packet.modifier.UpdateItemModifiersPacket;
import net.luis.xbackpack.util.Util;
import net.luis.xbackpack.world.inventory.modifier.ModifiableMenu;
import net.luis.xbackpack.world.inventory.modifier.filter.ItemFilter;
import net.luis.xbackpack.world.inventory.modifier.filter.ItemFilters;
import net.luis.xbackpack.world.inventory.modifier.sorter.ItemSorter;
import net.luis.xbackpack.world.inventory.modifier.sorter.ItemSorters;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Luis-St
 *
 */

public abstract class AbstractModifiableContainerMenu extends AbstractExtensionContainerMenu implements ModifiableMenu {
	
	private final Player player;
	private ItemFilter filter = ItemFilters.NONE;
	private ItemSorter sorter = ItemSorters.NONE;
	private String searchTerm = "";
	private boolean negate;
	
	protected AbstractModifiableContainerMenu(@NotNull MenuType<?> menuType, int id, @NotNull Inventory inventory) {
		super(menuType, id, inventory);
		this.player = inventory.player;
	}
	
	@Override
	public @NotNull String getSearchTerm() {
		return StringUtils.trimToEmpty(this.searchTerm).toLowerCase();
	}
	
	@Override
	public void setSearchTerm(@NotNull String searchTerm) {
		this.searchTerm = StringUtils.trimToEmpty(searchTerm).toLowerCase();
		if (this.searchTerm.startsWith("!")) {
			this.negate = true;
			this.searchTerm = this.searchTerm.substring(1);
		} else {
			this.negate = false;
		}
		if (this.searchTerm.isEmpty() && !this.sorter.isSelectable()) {
			this.sorter = ItemSorters.NONE;
		} else if (!this.searchTerm.isEmpty()) {
			if (this.isNumber(this.searchTerm) && this.sorter != ItemSorters.COUNT_SEARCH) {
				this.sorter = ItemSorters.COUNT_SEARCH;
			} else if (!this.isNumber(this.searchTerm)) {
				if (this.searchTerm.startsWith("@") && this.sorter != ItemSorters.NAMESPACE_SEARCH) {
					this.sorter = ItemSorters.NAMESPACE_SEARCH;
				} else if (this.searchTerm.startsWith("#") && this.sorter != ItemSorters.TAG_SEARCH) {
					this.sorter = ItemSorters.TAG_SEARCH;
				} else if (!this.searchTerm.startsWith("@") && !this.searchTerm.startsWith("#") && this.sorter != ItemSorters.NAME_SEARCH) {
					this.sorter = ItemSorters.NAME_SEARCH;
				}
			}
		}
		this.updateItemModifiers();
	}
	
	private boolean isNumber(@NotNull String searchTerm) {
		return Util.tryParseInteger(searchTerm, -1) >= 0;
	}
	
	public boolean isNegate() {
		return this.negate;
	}
	
	@Override
	public @NotNull ItemFilter getFilter() {
		return this.filter;
	}
	
	@Override
	public void updateFilter(@Nullable ItemFilter filter, @NotNull UpdateType type, @Nullable CycleDirection direction) {
		boolean changed = false;
		if (type.shouldSet()) {
			if (this.filter != filter) {
				this.filter = filter;
				changed = true;
			}
		} else if (type.shouldReset()) {
			if (this.filter != ItemFilters.NONE) {
				this.filter = ItemFilters.NONE;
				changed = true;
			}
		} else if (type.shouldCycle()) {
			List<ItemFilter> filters = Lists.newArrayList(ItemFilters.values()).stream().filter(ItemFilter::isSelectable).collect(Collectors.toList());
			if (this.filter.isSelectable()) {
				this.filter = Objects.requireNonNull(direction).cycle(filters, this.filter);
				changed = true;
			}
		}
		if (type.shouldUpdate() && changed) {
			this.updateItemModifiers();
		}
	}
	
	@Override
	public @NotNull ItemSorter getSorter() {
		return this.sorter;
	}
	
	@Override
	public void updateSorter(@Nullable ItemSorter sorter, @NotNull UpdateType type, @Nullable CycleDirection direction) {
		boolean changed = false;
		if (type.shouldSet()) {
			if (this.sorter != sorter) {
				this.sorter = sorter;
				changed = true;
			}
		} else if (type.shouldReset()) {
			if (this.sorter != ItemSorters.NONE) {
				this.sorter = ItemSorters.NONE;
				changed = true;
			}
		} else if (type.shouldCycle()) {
			List<ItemSorter> sorters = Lists.newArrayList(ItemSorters.values()).stream().filter(ItemSorter::isSelectable).collect(Collectors.toList());
			if (this.sorter.isSelectable()) {
				this.sorter = Objects.requireNonNull(direction).cycle(sorters, this.sorter);
				changed = true;
			}
		}
		if (type.shouldUpdate() && changed) {
			this.updateItemModifiers();
		}
	}
	
	private void updateItemModifiers() {
		if (this.player instanceof ServerPlayer player) {
			XBNetworkHandler.INSTANCE.sendToPlayer(player, new UpdateItemModifiersPacket(this.filter, this.sorter));
			this.onItemModifiersChanged(player);
		}
	}
	
	protected abstract void onItemModifiersChanged(@NotNull ServerPlayer player);
}
