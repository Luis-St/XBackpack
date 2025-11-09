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

package net.luis.xbackpack.network.packet.tool.next;

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

import java.util.*;

/**
 *
 * @author Luis-St
 *
 */

public record NextToolDownPacket() implements NetworkPacket {

	public static final CustomPacketPayload.Type<NextToolDownPacket> TYPE =
		new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "next_tool_down"));

	public static final StreamCodec<ByteBuf, NextToolDownPacket> STREAM_CODEC =
		StreamCodec.unit(new NextToolDownPacket());

	@Override
	public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public void handle(@NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) context.player();
			IBackpack backpack = BackpackProvider.get(player);
			ItemStack main = player.getMainHandItem().copy();
			ItemStack top = backpack.getToolHandler().getStackInSlot(0).copy();
			ItemStack mid = backpack.getToolHandler().getStackInSlot(1).copy();
			ItemStack down = backpack.getToolHandler().getStackInSlot(2).copy();
			if (BackpackConstants.VALID_TOOL_SLOT_ITEMS.contains(main.getItem())) {
				List<Integer> occupiedSlots = new ArrayList<>();
				Map<Integer, ItemStack> slotItems = new HashMap<>();

				occupiedSlots.add(-1);
				slotItems.put(-1, main);

				if (!down.isEmpty()) {
					occupiedSlots.add(2);
					slotItems.put(2, down);
				}
				if (!mid.isEmpty()) {
					occupiedSlots.add(1);
					slotItems.put(1, mid);
				}
				if (!top.isEmpty()) {
					occupiedSlots.add(0);
					slotItems.put(0, top);
				}

				if (occupiedSlots.size() >= 2) {
					Map<Integer, ItemStack> newSlotItems = new HashMap<>();
					for (int i = 0; i < occupiedSlots.size(); i++) {
						int currentSlot = occupiedSlots.get(i);
						int previousSlot = occupiedSlots.get((i - 1 + occupiedSlots.size()) % occupiedSlots.size());
						newSlotItems.put(previousSlot, slotItems.get(currentSlot));
					}

					player.setItemInHand(InteractionHand.MAIN_HAND, newSlotItems.get(-1));
					backpack.getToolHandler().setStackInSlot(0, newSlotItems.getOrDefault(0, ItemStack.EMPTY));
					backpack.getToolHandler().setStackInSlot(1, newSlotItems.getOrDefault(1, ItemStack.EMPTY));
					backpack.getToolHandler().setStackInSlot(2, newSlotItems.getOrDefault(2, ItemStack.EMPTY));
				}
			}
		});
	}
}
