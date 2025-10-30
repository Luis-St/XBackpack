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

package net.luis.xbackpack;

import net.luis.xbackpack.commands.XBCommandArgumentTypes;
import net.luis.xbackpack.core.components.XBDataComponents;
import net.luis.xbackpack.network.XBNetworkHandler;
import net.luis.xbackpack.network.packet.OpenBackpackPacket;
import net.luis.xbackpack.network.packet.UpdateBackpackPacket;
import net.luis.xbackpack.network.packet.extension.*;
import net.luis.xbackpack.network.packet.modifier.*;
import net.luis.xbackpack.network.packet.tool.direct.*;
import net.luis.xbackpack.network.packet.tool.next.*;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.luis.xbackpack.world.inventory.XBMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

@Mod(XBackpack.MOD_ID)
public class XBackpack {

	public static final String MOD_ID = "xbackpack";
	public static final String MOD_NAME = "XBackpack";
	public static final Logger LOGGER = LogManager.getLogger(XBackpack.class);

	public XBackpack(@NotNull ModContainer container) {
		IEventBus modEventBus = container.getEventBus();
		XBMenuTypes.MENU_TYPES.register(modEventBus);
		BackpackExtensions.BACKPACK_EXTENSIONS.register(modEventBus);
		XBCommandArgumentTypes.COMMAND_ARGUMENT_TYPES.register(modEventBus);
		XBDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);

		// Register attachment types
		DeferredRegister<AttachmentType<?>> attachments = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);
		attachments.register("backpack", () -> BackpackProvider.BACKPACK);
		attachments.register(modEventBus);

		XBNetworkHandler.INSTANCE.initChannel();
		XBNetworkHandler.INSTANCE.registerPackets();
	}

	@EventBusSubscriber(modid = MOD_ID)
	public static class NetworkEvents {

		@SubscribeEvent
		public static void registerPayloads(@NotNull RegisterPayloadHandlersEvent event) {
			PayloadRegistrar registrar = event.registrar("1");

			// Server-bound packets
			registrar.playToServer(OpenBackpackPacket.TYPE, OpenBackpackPacket.STREAM_CODEC, OpenBackpackPacket::handle);
			registrar.playToServer(NextToolPacket.TYPE, NextToolPacket.STREAM_CODEC, NextToolPacket::handle);
			registrar.playToServer(ToolTopPacket.TYPE, ToolTopPacket.STREAM_CODEC, ToolTopPacket::handle);
			registrar.playToServer(ToolMidPacket.TYPE, ToolMidPacket.STREAM_CODEC, ToolMidPacket::handle);
			registrar.playToServer(ToolDownPacket.TYPE, ToolDownPacket.STREAM_CODEC, ToolDownPacket::handle);
			registrar.playToServer(NextToolTopPacket.TYPE, NextToolTopPacket.STREAM_CODEC, NextToolTopPacket::handle);
			registrar.playToServer(NextToolDownPacket.TYPE, NextToolDownPacket.STREAM_CODEC, NextToolDownPacket::handle);
			registrar.playToServer(UpdateExtensionPacket.TYPE, UpdateExtensionPacket.STREAM_CODEC, UpdateExtensionPacket::handle);
			registrar.playToServer(UpdateSearchTermPacket.TYPE, UpdateSearchTermPacket.STREAM_CODEC, UpdateSearchTermPacket::handle);
			registrar.playToServer(ResetItemModifierPacket.TYPE, ResetItemModifierPacket.STREAM_CODEC, ResetItemModifierPacket::handle);

			// Client-bound packets
			registrar.playToClient(UpdateBackpackPacket.TYPE, UpdateBackpackPacket.STREAM_CODEC, UpdateBackpackPacket::handle);
			registrar.playToClient(UpdateFurnacePacket.TYPE, UpdateFurnacePacket.STREAM_CODEC, UpdateFurnacePacket::handle);
			registrar.playToClient(UpdateAnvilPacket.TYPE, UpdateAnvilPacket.STREAM_CODEC, UpdateAnvilPacket::handle);
			registrar.playToClient(UpdateEnchantmentTablePacket.TYPE, UpdateEnchantmentTablePacket.STREAM_CODEC, UpdateEnchantmentTablePacket::handle);
			registrar.playToClient(UpdateStonecutterPacket.TYPE, UpdateStonecutterPacket.STREAM_CODEC, UpdateStonecutterPacket::handle);
			registrar.playToClient(UpdateBrewingStandPacket.TYPE, UpdateBrewingStandPacket.STREAM_CODEC, UpdateBrewingStandPacket::handle);
			registrar.playToClient(UpdateItemModifiersPacket.TYPE, UpdateItemModifiersPacket.STREAM_CODEC, UpdateItemModifiersPacket::handle);
		}
	}
}
