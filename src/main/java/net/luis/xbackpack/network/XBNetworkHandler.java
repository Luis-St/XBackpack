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

package net.luis.xbackpack.network;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 *
 * @author Luis-St
 *
 */

public enum XBNetworkHandler {

	INSTANCE();

	public void initChannel() {
		// No longer needed with new packet system
	}

	public void registerPackets() {
		// Packets are now registered via RegisterPayloadHandlersEvent in XBackpack.java
	}

	public <T extends NetworkPacket> void sendToServer(T packet) {
		if (Minecraft.getInstance().getConnection() != null) {
			Minecraft.getInstance().getConnection().send(packet);
		}
	}

	public <T extends NetworkPacket> void sendToPlayer(Player player, T packet) {
		if (player instanceof ServerPlayer serverPlayer) {
			this.sendToPlayer(serverPlayer, packet);
		}
	}

	public <T extends NetworkPacket> void sendToPlayer(ServerPlayer player, T packet) {
		PacketDistributor.sendToPlayer(player, packet);
	}
}
