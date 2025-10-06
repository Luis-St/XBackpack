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

package net.luis.xbackpack.server.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.luis.xbackpack.world.extension.BackpackExtension;
import net.luis.xbackpack.world.extension.BackpackExtensions;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackExtensionArgument implements ArgumentType<BackpackExtension> {
	
	private static final Dynamic2CommandExceptionType INVALID_BACKPACK_EXTENSION = new Dynamic2CommandExceptionType((name, dummyObject) -> {
		return Component.translatable("xbackpack.commands.arguments.extension.invalid", name);
	});
	
	private final Supplier<Registry<BackpackExtension>> registrySupplier;

	private BackpackExtensionArgument() {
		this.registrySupplier = () -> BackpackExtensions.REGISTRY;
	}
	
	public static @NotNull BackpackExtensionArgument extension() {
		return new BackpackExtensionArgument();
	}
	
	public static <S> BackpackExtension get(@NotNull CommandContext<S> context, String name) {
		return context.getArgument(name, BackpackExtension.class);
	}
	
	@Override
	public BackpackExtension parse(StringReader reader) throws CommandSyntaxException {
		ResourceLocation location = ResourceLocation.read(reader);
		Registry<BackpackExtension> registry = this.registrySupplier.get();
		ResourceLocation noExtensionKey = BackpackExtensions.REGISTRY.getKey(BackpackExtensions.NO.get());
		if (registry.containsKey(location) && !location.equals(noExtensionKey)) {
			return registry.getValue(location);
		}
		throw INVALID_BACKPACK_EXTENSION.create(location, "");
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(this.values().stream().map(ResourceLocation::toString), builder);
	}
	
	@Override
	public Collection<String> getExamples() {
		return this.values().stream().map(ResourceLocation::toString).collect(Collectors.toList());
	}
	
	private Collection<ResourceLocation> values() {
		Registry<BackpackExtension> registry = this.registrySupplier.get();
		return registry.keySet().stream()
			.filter(key -> !key.equals(BackpackExtensions.REGISTRY.getKey(BackpackExtensions.NO.get())))
			.collect(Collectors.toList());
	}
	
	//region Argument info
	public static class Info implements ArgumentTypeInfo<BackpackExtensionArgument, Info.Template> {
		
		@Override
		public void serializeToNetwork(@NotNull Template template, @NotNull FriendlyByteBuf buffer) {}
		
		@Override
		public @NotNull Template deserializeFromNetwork(@NotNull FriendlyByteBuf buffer) {
			return new Template(new BackpackExtensionArgument());
		}
		
		@Override
		public void serializeToJson(@NotNull Template template, @NotNull JsonObject object) {}
		
		@Override
		public @NotNull Template unpack(@NotNull BackpackExtensionArgument argument) {
			return new Template(argument);
		}
		
		public class Template implements ArgumentTypeInfo.Template<BackpackExtensionArgument> {
			
			private final BackpackExtensionArgument argument;
			
			public Template(BackpackExtensionArgument argument) {
				this.argument = argument;
			}
			
			@Override
			public @NotNull BackpackExtensionArgument instantiate(@NotNull CommandBuildContext context) {
				return this.argument;
			}
			
			@Override
			public @NotNull ArgumentTypeInfo<BackpackExtensionArgument, ?> type() {
				return Info.this;
			}
		}
	}
	//endregion
}
