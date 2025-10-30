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

package net.luis.xbackpack.data;

import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.data.provider.language.XBLanguageProvider;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

@EventBusSubscriber(modid = XBackpack.MOD_ID)
public class GatherDataEventHandler {

	@SubscribeEvent
	public static void gatherClientData(@NotNull GatherDataEvent.Client event) {
		DataGenerator generator = event.getGenerator();
		event.addProvider(new XBLanguageProvider(generator.getPackOutput()));
	}
}
