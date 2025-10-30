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

package net.luis.xbackpack.commands;

import net.luis.xbackpack.XBackpack;
import net.luis.xbackpack.server.commands.arguments.BackpackExtensionArgument;
import net.luis.xbackpack.server.commands.arguments.BackpackExtensionStateArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 *
 * @author Luis-St
 *
 */

public class XBCommandArgumentTypes {

	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, XBackpack.MOD_ID);

	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, BackpackExtensionArgument.Info> BACKPACK_EXTENSION_TYPE = COMMAND_ARGUMENT_TYPES.register("backpack_extension_type", () -> {
		return ArgumentTypeInfos.registerByClass(BackpackExtensionArgument.class, new BackpackExtensionArgument.Info());
	});
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, BackpackExtensionStateArgument.Info> BACKPACK_EXTENSION_STATE_TYPE = COMMAND_ARGUMENT_TYPES.register("backpack_extension_state_type", () -> {
		return ArgumentTypeInfos.registerByClass(BackpackExtensionStateArgument.class, new BackpackExtensionStateArgument.Info());
	});
}
