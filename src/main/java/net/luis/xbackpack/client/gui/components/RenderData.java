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

package net.luis.xbackpack.client.gui.components;

import net.luis.xbackpack.util.QuadFunction;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 *
 * @author Luis-St
 *
 */

public record RenderData(boolean exists, int xPosition, int yPosition, int width, int height) {
	
	@Nullable
	public <T extends AbstractWidget> T addIfExists(@NotNull QuadFunction<Integer, Integer, Integer, Integer, T> function, @NotNull Consumer<T> action) {
		if (this.exists) {
			T renderable = function.apply(this.xPosition, this.yPosition, this.width, this.height);
			action.accept(renderable);
			return renderable;
		}
		return null;
	}
}
