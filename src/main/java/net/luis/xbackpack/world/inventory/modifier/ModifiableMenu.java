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

package net.luis.xbackpack.world.inventory.modifier;

import net.luis.xbackpack.world.inventory.modifier.filter.ItemFilter;
import net.luis.xbackpack.world.inventory.modifier.sorter.ItemSorter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

public interface ModifiableMenu {
	
	@NotNull String getSearchTerm();
	
	void setSearchTerm(String searchTerm);
	
	@NotNull ItemFilter getFilter();
	
	void updateFilter(@Nullable ItemFilter filter, @NotNull UpdateType type, @Nullable CycleDirection direction);
	
	@NotNull ItemSorter getSorter();
	
	void updateSorter(@Nullable ItemSorter sorter, @NotNull UpdateType type, @Nullable CycleDirection direction);
	
	enum UpdateType {
		
		NONE("none", false),
		SET("set", true),
		SET_NO_UPDATE("set_no_update", false),
		RESET("reset", true),
		RESET_NO_UPDATE("reset_no_update", false),
		CYCLE("cycle", true),
		CYCLE_NO_UPDATE("cycle_no_update", false);
		
		private final String name;
		private final boolean shouldUpdate;
		
		UpdateType(@NotNull String name, boolean shouldUpdate) {
			this.name = name;
			this.shouldUpdate = shouldUpdate;
		}
		
		public @NotNull String getName() {
			return this.name;
		}
		
		public int getId() {
			return this.ordinal();
		}
		
		public boolean shouldSet() {
			return this == SET || this == SET_NO_UPDATE;
		}
		
		public boolean shouldReset() {
			return this == RESET || this == RESET_NO_UPDATE;
		}
		
		public boolean shouldCycle() {
			return this == CYCLE || this == CYCLE_NO_UPDATE;
		}
		
		public boolean shouldUpdate() {
			return this.shouldUpdate;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
	
	enum CycleDirection {
		
		FORWARDS("forwards") {
			@Override
			public <T extends ItemModifier> T cycle(@NotNull List<T> itemModifiers, T itemModifier) {
				return itemModifiers.get((itemModifiers.indexOf(itemModifier) + 1) % itemModifiers.size());
			}
		},
		BACKWARDS("backwards") {
			@Override
			public <T extends ItemModifier> T cycle(@NotNull List<T> itemModifiers, T itemModifier) {
				int index = itemModifiers.indexOf(itemModifier);
				if (index == 0) {
					return itemModifiers.getLast();
				} else {
					return itemModifiers.get(index - 1);
				}
			}
		};
		
		private final String name;
		
		CycleDirection(@NotNull String name) {
			this.name = name;
		}
		
		public @NotNull String getName() {
			return this.name;
		}
		
		public int getId() {
			return this.ordinal();
		}
		
		public abstract <T extends ItemModifier> T cycle(@NotNull List<T> itemModifiers, T itemModifier);
		
		@Override
		public String toString() {
			return this.name;
		}
	}
}
