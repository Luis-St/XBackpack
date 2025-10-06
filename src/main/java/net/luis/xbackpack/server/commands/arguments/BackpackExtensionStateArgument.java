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

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.luis.xbackpack.world.extension.BackpackExtensionState;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 * @author Luis-St
 *
 */

public class BackpackExtensionStateArgument implements ArgumentType<BackpackExtensionState> {
	
	private static final Dynamic2CommandExceptionType INVALID_EXTENSION_STATE = new Dynamic2CommandExceptionType((name, dummyObject) -> {
		return Component.translatable("xbackpack.commands.arguments.state.invalid", name);
	});
	
	private BackpackExtensionStateArgument() {}
	
	public static @NotNull BackpackExtensionStateArgument state() {
		return new BackpackExtensionStateArgument();
	}
	
	public static <S> BackpackExtensionState get(@NotNull CommandContext<S> context, String name) {
		return context.getArgument(name, BackpackExtensionState.class);
	}
	
	@Override
	public BackpackExtensionState parse(@NotNull StringReader reader) throws CommandSyntaxException {
		BackpackExtensionState state = BackpackExtensionState.fromString(reader.readUnquotedString());
		if (state != null) {
			return state;
		}
		throw INVALID_EXTENSION_STATE.create(null, "");
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(Lists.newArrayList(BackpackExtensionState.values()).stream().map(BackpackExtensionState::getName), builder);
	}
	
	@Override
	public Collection<String> getExamples() {
		return Lists.newArrayList(BackpackExtensionState.values()).stream().map(BackpackExtensionState::getName).collect(Collectors.toList());
	}
	
	//region Argument info
	public static class Info implements ArgumentTypeInfo<BackpackExtensionStateArgument, Info.Template> {
		
		@Override
		public void serializeToNetwork(@NotNull Template template, @NotNull FriendlyByteBuf buffer) {}
		
		@Override
		public @NotNull Template deserializeFromNetwork(@NotNull FriendlyByteBuf buffer) {
			return new Template(new BackpackExtensionStateArgument());
		}
		
		@Override
		public void serializeToJson(@NotNull Template template, @NotNull JsonObject object) {}
		
		@Override
		public @NotNull Template unpack(@NotNull BackpackExtensionStateArgument argument) {
			return new Template(argument);
		}
		
		public class Template implements ArgumentTypeInfo.Template<BackpackExtensionStateArgument> {
			
			private final BackpackExtensionStateArgument argument;
			
			public Template(BackpackExtensionStateArgument argument) {
				this.argument = argument;
			}
			
			@Override
			public @NotNull BackpackExtensionStateArgument instantiate(@NotNull CommandBuildContext context) {
				return this.argument;
			}
			
			@Override
			public @NotNull ArgumentTypeInfo<BackpackExtensionStateArgument, ?> type() {
				return Info.this;
			}
		}
	}
	//endregion
}
