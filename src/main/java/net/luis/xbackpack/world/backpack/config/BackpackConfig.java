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

import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.world.backpack.config.client.BackpackClientConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackConfig {
	
	private final Player player;
	private final boolean isServerSide;
	private final BackpackClientConfig clientConfig;
	private final BackpackExtensionConfig extensionConfig;
	
	public BackpackConfig(@NotNull Player player) {
		this.player = player;
		this.isServerSide = !this.player.level().isClientSide;
		this.clientConfig = new BackpackClientConfig();
		this.extensionConfig = new BackpackExtensionConfig();
	}
	
	public @Nullable BackpackClientConfig getClientConfig() {
		if (this.isServerSide) {
			XBackpack.LOGGER.warn("The backpack client config is not accessible from the server");
			return null;
		}
		return this.clientConfig;
	}
	
	public @NotNull BackpackExtensionConfig getExtensionConfig() {
		return this.extensionConfig;
	}
	
	public void updateServer() {
		if (this.player instanceof ServerPlayer player) {
			this.extensionConfig.update(player);
		} else {
			XBackpack.LOGGER.warn("The backpack config cannot be updated from the client");
		}
	}
	
	//region Serialization
	public @NotNull CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.put("extension_config", this.extensionConfig.serialize());
		return tag;
	}
	
	public void deserialize(@NotNull CompoundTag tag) {
		this.extensionConfig.deserialize(tag.getCompound("extension_config").orElse(new CompoundTag()));
	}
	//endregion
}
