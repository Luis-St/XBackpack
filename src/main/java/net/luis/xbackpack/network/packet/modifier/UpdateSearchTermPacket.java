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

package net.luis.xbackpack.network.packet.modifier;

import io.netty.buffer.ByteBuf;
import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.network.NetworkPacket;
import net.luis.xbackpack.world.inventory.BackpackMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public record UpdateSearchTermPacket(@NotNull String searchBoxValue) implements NetworkPacket {

	public static final CustomPacketPayload.Type<UpdateSearchTermPacket> TYPE =
		new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "update_search_term"));

	public static final StreamCodec<ByteBuf, UpdateSearchTermPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, UpdateSearchTermPacket::searchBoxValue,
		UpdateSearchTermPacket::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public void handle(@NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) context.player();
			if (player.containerMenu instanceof BackpackMenu menu) {
				menu.setSearchTerm(this.searchBoxValue);
			}
		});
	}
}
