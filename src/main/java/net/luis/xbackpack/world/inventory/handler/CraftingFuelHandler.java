/*
 * XBackpack
 * Copyright (C) 2026 Luis Staudt
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

package net.luis.xbackpack.world.inventory.handler;

import net.luis.xbackpack.world.item.DynamicItemStacksResourceHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.*;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Luis-St
 *
 */

public class CraftingFuelHandler extends CraftingHandler {

	private final DynamicItemStacksResourceHandler fuelHandler;

	public CraftingFuelHandler(int input, int result) {
		this(new DynamicItemStacksResourceHandler(input), new DynamicItemStacksResourceHandler(result));
	}

	public CraftingFuelHandler(@NotNull DynamicItemStacksResourceHandler inputHandler, @NotNull DynamicItemStacksResourceHandler resultHandler) {
		this(inputHandler, new DynamicItemStacksResourceHandler(1), resultHandler);
	}

	public CraftingFuelHandler(int input, int fuel, int result) {
		this(new DynamicItemStacksResourceHandler(input), new DynamicItemStacksResourceHandler(fuel), new DynamicItemStacksResourceHandler(result));
	}

	public CraftingFuelHandler(@NotNull DynamicItemStacksResourceHandler inputHandler, @NotNull DynamicItemStacksResourceHandler fuelHandler, @NotNull DynamicItemStacksResourceHandler resultHandler) {
		super(inputHandler, resultHandler);
		this.fuelHandler = fuelHandler;
	}

	public @NotNull DynamicItemStacksResourceHandler getFuelHandler() {
		return this.fuelHandler;
	}
	
	@Override
	public @NotNull CompoundTag serialize(HolderLookup.@NotNull Provider provider) {
		CompoundTag tag = super.serialize(provider);
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
		output.putChild("fuel_handler", this.fuelHandler);
		tag.merge(output.buildResult());
		return tag;
	}

	@Override
	public void deserialize(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
		super.deserialize(provider, tag);
		ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, provider, tag);
		input.child("fuel_handler").ifPresent(this.fuelHandler::deserialize);
	}
}
