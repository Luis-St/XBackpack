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

package net.luis.xbackpack.event.client;

import net.luis.xbackpack.BackpackConstants;
import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.client.XBKeyMappings;
import net.luis.xbackpack.client.commands.TooltipCommand;
import net.luis.xbackpack.network.XBNetworkHandler;
import net.luis.xbackpack.network.packet.OpenBackpackPacket;
import net.luis.xbackpack.network.packet.tool.direct.*;
import net.luis.xbackpack.network.packet.tool.next.*;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.capability.IBackpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 * @author Luis-St
 *
 */

@EventBusSubscriber(modid = XBackpack.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
	
	public static int lastPacket = 0;
	
	@SubscribeEvent
	public static void registerClientCommands(@NotNull RegisterClientCommandsEvent event) {
		TooltipCommand.register(event.getDispatcher());
	}
	
	@SubscribeEvent
	public static void clientTick(ClientTickEvent.@NotNull Pre event) {
		if (0 >= lastPacket) {
			if (XBKeyMappings.BACKPACK_OPEN.isDown()) {
				XBNetworkHandler.INSTANCE.sendToServer(new OpenBackpackPacket());
				lastPacket = 4;
			} else if (XBKeyMappings.BACKPACK_NEXT.isDown()) {
				XBNetworkHandler.INSTANCE.sendToServer(new NextToolPacket());
				lastPacket = 4;
			} else if (XBKeyMappings.BACKPACK_SLOT_TOP.isDown()) {
				XBNetworkHandler.INSTANCE.sendToServer(new ToolTopPacket());
				lastPacket = 4;
			} else if (XBKeyMappings.BACKPACK_SLOT_MID.isDown()) {
				XBNetworkHandler.INSTANCE.sendToServer(new ToolMidPacket());
				lastPacket = 4;
			} else if (XBKeyMappings.BACKPACK_SLOT_DOWN.isDown()) {
				XBNetworkHandler.INSTANCE.sendToServer(new ToolDownPacket());
				lastPacket = 4;
			}
		} else {
			lastPacket--;
		}
	}
	
	@SubscribeEvent
	public static void mouseScroll(@NotNull MouseScrollingEvent event) {
		double delta = event.getScrollDeltaY();
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = Objects.requireNonNull(minecraft.player);
		if (player.isShiftKeyDown() && Objects.requireNonNull(minecraft.gameMode).getPlayerMode() != GameType.SPECTATOR) {
			ItemStack main = player.getMainHandItem();
			if (BackpackConstants.VALID_TOOL_SLOT_ITEMS.contains(main.getItem())) {
				IBackpack backpack = BackpackProvider.get(player);
				ItemStack top = backpack.getToolHandler().getStackInSlot(0).copy();
				ItemStack down = backpack.getToolHandler().getStackInSlot(2).copy();
				if (!top.isEmpty() && !down.isEmpty()) {
					if (delta > 0) {
						event.setCanceled(true);
						XBNetworkHandler.INSTANCE.sendToServer(new NextToolTopPacket());
					} else if (delta < 0) {
						event.setCanceled(true);
						XBNetworkHandler.INSTANCE.sendToServer(new NextToolDownPacket());
					}
				}
			}
		}
	}
}
