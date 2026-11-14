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

public class SmeltingHandler extends CraftingFuelHandler {

	private final DynamicItemStacksResourceHandler inputStorageHandler;
	private final DynamicItemStacksResourceHandler resultStorageHandler;

	public SmeltingHandler(int input, int inputStorage, int resultStorage) {
		this(input, 1, 1, inputStorage, resultStorage);
	}

	public SmeltingHandler(int input, int fuel, int result, int inputStorage, int resultStorage) {
		this(new DynamicItemStacksResourceHandler(input), new DynamicItemStacksResourceHandler(fuel), new DynamicItemStacksResourceHandler(result), new DynamicItemStacksResourceHandler(inputStorage), new DynamicItemStacksResourceHandler(resultStorage));
	}

	public SmeltingHandler(DynamicItemStacksResourceHandler inputHandler, DynamicItemStacksResourceHandler fuelHandler, DynamicItemStacksResourceHandler resultHandler, DynamicItemStacksResourceHandler inputStorageHandler, DynamicItemStacksResourceHandler resultStorageHandler) {
		super(inputHandler, fuelHandler, resultHandler);
		this.inputStorageHandler = inputStorageHandler;
		this.resultStorageHandler = resultStorageHandler;
	}

	public DynamicItemStacksResourceHandler getInputStorageHandler() {
		return this.inputStorageHandler;
	}

	public DynamicItemStacksResourceHandler getResultStorageHandler() {
		return this.resultStorageHandler;
	}
	
	@Override
	public @NotNull CompoundTag serialize(HolderLookup.@NotNull Provider provider) {
		CompoundTag tag = super.serialize(provider);
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
		output.putChild("input_storage_handler", this.inputStorageHandler);
		output.putChild("result_storage_handler", this.resultStorageHandler);
		tag.merge(output.buildResult());
		return tag;
	}

	@Override
	public void deserialize(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
		super.deserialize(provider, tag);
		ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, provider, tag);
		input.child("input_storage_handler").ifPresent(this.inputStorageHandler::deserialize);
		input.child("result_storage_handler").ifPresent(this.resultStorageHandler::deserialize);
	}
}
