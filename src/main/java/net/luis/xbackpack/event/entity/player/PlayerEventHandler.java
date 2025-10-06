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

package net.luis.xbackpack.event.entity.player;

import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.world.capability.BackpackProvider;
import net.luis.xbackpack.world.capability.IBackpack;
import net.luis.xbackpack.world.inventory.BackpackMenu;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

@EventBusSubscriber(modid = XBackpack.MOD_ID)
public class PlayerEventHandler {
	
	@SubscribeEvent
	public static void itemCrafted(@NotNull ItemCraftedEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			BackpackProvider.get(player).broadcastChanges();
		}
	}
	
	@SubscribeEvent
	public static void playerChangedDimension(PlayerEvent.@NotNull PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			BackpackProvider.get(player).broadcastChanges();
		}
	}
	
	@SubscribeEvent
	public static void playerClone(PlayerEvent.@NotNull Clone event) {
		Player original = event.getOriginal();
		Player player = event.getEntity();
		if (event.isWasDeath()) {
			IBackpack oldBackpack = BackpackProvider.get(original);
			IBackpack newBackpack = BackpackProvider.get(player);
			RegistryAccess access = original.registryAccess();
			newBackpack.deserialize(access, oldBackpack.serialize(access));
		}
	}
	
	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.@NotNull PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			BackpackProvider.get(player).broadcastChanges();
		}
	}
	
	@SubscribeEvent
	public static void playerTick(PlayerTickEvent.@NotNull Pre event) {
		if (!event.getEntity().level().isClientSide()) {
			BackpackProvider.get(event.getEntity()).tick();
			if (event.getEntity().containerMenu instanceof BackpackMenu menu) {
				menu.tick();
			}
		}
	}
}