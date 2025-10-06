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

package net.luis.xbackpack.world.inventory.slot;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class SlotWrapper {
	
	private final int mainSlot;
	private int slot;
	
	private SlotWrapper(int mainSlot, int slot) {
		this.mainSlot = mainSlot;
		this.slot = slot;
	}
	
	public static @NotNull SlotWrapper of(int mainSlot, int slot) {
		return new SlotWrapper(mainSlot, slot);
	}
	
	public static @NotNull SlotWrapper of(@NotNull FriendlyByteBuf buffer) {
		int mainSlot = buffer.readInt();
		int slot = buffer.readInt();
		return new SlotWrapper(mainSlot, slot);
	}
	
	public static @NotNull SlotWrapper ofUnwrapped(int mainSlot) {
		return of(mainSlot, mainSlot);
	}
	
	public static @NotNull SlotWrapper ofDisabled(int mainSlot) {
		return of(mainSlot, -1);
	}
	
	public int getMainSlot() {
		return this.mainSlot;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public void encode(@NotNull FriendlyByteBuf buffer) {
		buffer.writeInt(this.mainSlot);
		buffer.writeInt(this.slot);
	}
}
