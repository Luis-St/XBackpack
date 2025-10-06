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

package net.luis.xbackpack.world.capability;

import net.luis.xbackpack.world.backpack.BackpackHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackProvider {

	private static final IAttachmentSerializer<BackpackHandler> SERIALIZER = new IAttachmentSerializer<>() {
		@Override
		public @NotNull BackpackHandler read(@NotNull net.neoforged.neoforge.attachment.IAttachmentHolder holder, @NotNull ValueInput input) {
			BackpackHandler handler = new BackpackHandler(null);
			// Use a codec to read the compound tag data using the new API
			input.read("data", CompoundTag.CODEC).ifPresent(tag -> {
				handler.deserialize(input.lookup(), tag);
			});
			return handler;
		}

		@Override
		public boolean write(@NotNull BackpackHandler handler, @NotNull ValueOutput output) {
			CompoundTag tag = handler.serialize(output.lookup());
			// Store the CompoundTag using the new API with a key
			output.store("data", CompoundTag.CODEC, tag);
			return true;
		}
	};

	public static final AttachmentType<BackpackHandler> BACKPACK = AttachmentType.builder(holder -> {
		if (holder instanceof Player player) {
			return new BackpackHandler(player);
		}
		throw new IllegalArgumentException("BACKPACK attachment can only be attached to players");
	}).serialize(SERIALIZER).build();

	public static @NotNull BackpackHandler get(@NotNull Player player) {
		BackpackHandler handler = player.getData(BACKPACK);
		// Ensure the player reference is set (needed after deserialization)
		handler.setPlayer(player);
		return handler;
	}
}
