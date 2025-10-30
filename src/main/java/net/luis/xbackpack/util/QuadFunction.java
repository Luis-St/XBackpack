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

package net.luis.xbackpack.util;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author Luis-St
 *
 */

@FunctionalInterface
public interface QuadFunction<T, U, V, W, R> {
	
	R apply(T t, U u, V v, W w);
	
	default <S> QuadFunction<T, U, V, W, S> andThen(Function<? super R, ? extends S> after) {
		Objects.requireNonNull(after);
		return (T t, U u, V v, W w) -> after.apply(this.apply(t, u, v, w));
	}
}
