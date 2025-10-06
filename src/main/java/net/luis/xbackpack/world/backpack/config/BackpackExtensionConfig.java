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

package net.luis.xbackpack.world.backpack.config;

import com.google.common.collect.Maps;
import net.luis.xbackpack.world.extension.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackExtensionConfig {
	
	private final Map<BackpackExtension, Data> states = Maps.newHashMap();
	
	public BackpackExtensionConfig() {
		for (BackpackExtension extension : BackpackExtensions.REGISTRY) {
			this.states.put(extension, new Data(BackpackExtensionState.LOCKED, 0));
		}
	}
	
	private @NotNull Data getData(@NotNull BackpackExtension extension) {
		return this.states.getOrDefault(extension, new Data(BackpackExtensionState.LOCKED, 0));
	}
	
	public @NotNull BackpackExtensionState getState(@NotNull BackpackExtension extension) {
		return this.getData(extension).state();
	}
	
	public @NotNull List<BackpackExtension> getWithState(@NotNull BackpackExtensionState state) {
		return this.states.entrySet().stream().filter((entry) -> {
			return entry.getValue().state() == state;
		}).map(Entry::getKey).collect(Collectors.toList());
	}
	
	public void setState(@NotNull ServerPlayer player, @NotNull BackpackExtension extension, @NotNull BackpackExtensionState state) {
		this.states.put(extension, new Data(state, player.getStats().getValue(Stats.ITEM_CRAFTED, extension.getUnlockItem().getItem())));
	}
	
	public void update(@NotNull ServerPlayer player) {
		for (BackpackExtension extension : BackpackExtensions.REGISTRY) {
			Data data = this.getData(extension);
			BackpackExtensionState state = data.state();
			if (state != BackpackExtensionState.BLOCKED) {
				int count = player.getStats().getValue(Stats.ITEM_CRAFTED, extension.getUnlockItem().getItem());
				if (0 >= data.unlockCount() && count > 0) {
					this.setState(player, extension, BackpackExtensionState.UNLOCKED);
				} else if (data.unlockCount() > 0 && count > data.unlockCount()) {
					this.setState(player, extension, BackpackExtensionState.UNLOCKED);
				}
			}
		}
	}
	
	//region Serialization
	public @NotNull CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		ListTag statesTag = new ListTag();
		for (Entry<BackpackExtension, Data> entry : this.states.entrySet()) {
			CompoundTag stateTag = new CompoundTag();
			stateTag.putString("key", Objects.requireNonNull(BackpackExtensions.REGISTRY.getKey(entry.getKey())).toString());
			stateTag.putString("value", entry.getValue().state().getName());
			stateTag.putInt("unlock_count", entry.getValue().unlockCount());
			statesTag.add(stateTag);
		}
		tag.put("states", statesTag);
		return tag;
	}
	
	public void deserialize(@NotNull CompoundTag tag) {
		ListTag statesTag = tag.getList("states").orElse(new ListTag());
		for (int i = 0; i < statesTag.size(); i++) {
			CompoundTag stateTag = statesTag.getCompound(i).orElse(new CompoundTag());
			BackpackExtension extension = BackpackExtensions.REGISTRY.getValue(ResourceLocation.tryParse(stateTag.getString("key").orElse("")));
			BackpackExtensionState state = BackpackExtensionState.fromString(stateTag.getString("value").orElse(""), BackpackExtensionState.LOCKED);
			int unlockCount = stateTag.getInt("unlock_count").orElse(0);
			if (extension != null) {
				this.states.put(extension, new Data(state, unlockCount));
			}
		}
	}
	//endregion
	
	private record Data(@NotNull BackpackExtensionState state, int unlockCount) {}
}
