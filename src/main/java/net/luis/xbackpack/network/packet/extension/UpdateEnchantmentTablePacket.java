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

package net.luis.xbackpack.network.packet.extension;

import io.netty.buffer.ByteBuf;
import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.client.XBClientPacketHandler;
import net.luis.xbackpack.network.NetworkPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 * @author Luis-St
 *
 */

public record UpdateEnchantmentTablePacket(@NotNull List<ResourceLocation> enchantments, int @NotNull [] enchantmentLevels, int @NotNull [] enchantingCosts, int enchantmentSeed) implements NetworkPacket {

	public static final CustomPacketPayload.Type<UpdateEnchantmentTablePacket> TYPE =
		new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XBackpack.MOD_ID, "update_enchantment_table"));

	public static final StreamCodec<ByteBuf, UpdateEnchantmentTablePacket> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public void encode(@NotNull ByteBuf buffer, @NotNull UpdateEnchantmentTablePacket packet) {
			ByteBufCodecs.VAR_INT.encode(buffer, packet.enchantments.size());
			for (ResourceLocation enchantment : packet.enchantments) {
				ResourceLocation.STREAM_CODEC.encode(buffer, enchantment);
			}
			ByteBufCodecs.VAR_INT.encode(buffer, packet.enchantmentLevels.length);
			for (int level : packet.enchantmentLevels) {
				ByteBufCodecs.VAR_INT.encode(buffer, level);
			}
			ByteBufCodecs.VAR_INT.encode(buffer, packet.enchantingCosts.length);
			for (int cost : packet.enchantingCosts) {
				ByteBufCodecs.VAR_INT.encode(buffer, cost);
			}
			ByteBufCodecs.VAR_INT.encode(buffer, packet.enchantmentSeed);
		}

		@Override
		public @NotNull UpdateEnchantmentTablePacket decode(@NotNull ByteBuf buffer) {
			int enchantmentCount = ByteBufCodecs.VAR_INT.decode(buffer);
			ResourceLocation[] enchantments = new ResourceLocation[enchantmentCount];
			for (int i = 0; i < enchantmentCount; i++) {
				enchantments[i] = ResourceLocation.STREAM_CODEC.decode(buffer);
			}
			int levelCount = ByteBufCodecs.VAR_INT.decode(buffer);
			int[] levels = new int[levelCount];
			for (int i = 0; i < levelCount; i++) {
				levels[i] = ByteBufCodecs.VAR_INT.decode(buffer);
			}
			int costCount = ByteBufCodecs.VAR_INT.decode(buffer);
			int[] costs = new int[costCount];
			for (int i = 0; i < costCount; i++) {
				costs[i] = ByteBufCodecs.VAR_INT.decode(buffer);
			}
			int seed = ByteBufCodecs.VAR_INT.decode(buffer);
			return new UpdateEnchantmentTablePacket(List.of(enchantments), levels, costs, seed);
		}
	};

	public UpdateEnchantmentTablePacket(ResourceLocation @NotNull [] enchantments, int @NotNull [] enchantmentLevels, int @NotNull [] enchantingCosts, int enchantmentSeed) {
		this(List.of(enchantments), enchantmentLevels, enchantingCosts, enchantmentSeed);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public void handle(@NotNull IPayloadContext context) {
		context.enqueueWork(() -> {
			XBClientPacketHandler.updateEnchantmentTableExtension(this.enchantments.toArray(new ResourceLocation[0]), this.enchantmentLevels, this.enchantingCosts, this.enchantmentSeed);
		});
	}
}
