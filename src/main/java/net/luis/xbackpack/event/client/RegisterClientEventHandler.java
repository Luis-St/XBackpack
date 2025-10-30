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

import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.client.XBKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

@EventBusSubscriber(modid = XBackpack.MOD_ID, value = Dist.CLIENT)
public class RegisterClientEventHandler {
	
	@SubscribeEvent
	public static void registerKeyMappings(@NotNull RegisterKeyMappingsEvent event) {
		event.register(XBKeyMappings.BACKPACK_OPEN);
		event.register(XBKeyMappings.BACKPACK_NEXT);
		event.register(XBKeyMappings.BACKPACK_SLOT_TOP);
		event.register(XBKeyMappings.BACKPACK_SLOT_MID);
		event.register(XBKeyMappings.BACKPACK_SLOT_DOWN);
	}
}
