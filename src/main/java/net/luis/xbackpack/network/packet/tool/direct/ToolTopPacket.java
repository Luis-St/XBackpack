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

package net.luis.xbackpack.network.packet.tool.direct;

import io.netty.buffer.ByteBuf;
import net.luis.xbackpack.BackpackConstants;
import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.network.NetworkPacket;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.capability.IBackpack;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public record ToolTopPacket() implements NetworkPacket {

	public static final CustomPacketPayload.Type<ToolTopPacket> TYPE =
		new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "tool_top"));

	public static final StreamCodec<ByteBuf, ToolTopPacket> STREAM_CODEC =
		StreamCodec.unit(new ToolTopPacket());

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public void handle(@NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) context.player();
			IBackpack backpack = BackpackProvider.get(player);
			ItemStack main = player.getMainHandItem().copy();
			ItemStack top = backpack.getToolHandler().getStackInSlot(0).copy();
			if (BackpackConstants.VALID_TOOL_SLOT_ITEMS.contains(main.getItem())) {
				player.setItemInHand(InteractionHand.MAIN_HAND, top);
				backpack.getToolHandler().setStackInSlot(0, main);
			}
		});
	}
}
