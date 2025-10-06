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

package net.luis.xbackpack.client;

import net.luis.xbackpack.XBackpack;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 *
 * @author Luis-St
 *
 */

public class XBKeyMappings {
	
	public static final String BACKPACK_CATEGORY = XBackpack.MOD_ID + ".key.categories.backpack";
	public static final KeyMapping BACKPACK_OPEN = new KeyMapping(XBackpack.MOD_ID + ".key.backpack_open", GLFW.GLFW_KEY_B, BACKPACK_CATEGORY);
	public static final KeyMapping BACKPACK_NEXT = new KeyMapping(XBackpack.MOD_ID + ".key.backpack_next", GLFW.GLFW_KEY_N, BACKPACK_CATEGORY);
	public static final KeyMapping BACKPACK_SLOT_TOP = new KeyMapping(XBackpack.MOD_ID + ".key.backpack_slot_top", GLFW.GLFW_KEY_G, BACKPACK_CATEGORY);
	public static final KeyMapping BACKPACK_SLOT_MID = new KeyMapping(XBackpack.MOD_ID + ".key.backpack_slot_mid", GLFW.GLFW_KEY_H, BACKPACK_CATEGORY);
	public static final KeyMapping BACKPACK_SLOT_DOWN = new KeyMapping(XBackpack.MOD_ID + ".key.backpack_slot_down", GLFW.GLFW_KEY_J, BACKPACK_CATEGORY);
}
