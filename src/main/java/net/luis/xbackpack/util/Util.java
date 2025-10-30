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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author Luis-St
 *
 */

public class Util {
	
	public static int tryParseInteger(String value, int fallback) {
		int number = fallback;
		try {
			char firstChar = value.charAt(0);
			char lastChar = value.charAt(value.length() - 1);
			if (firstChar == '=' || firstChar == '<' || firstChar == '>') {
				value = value.substring(1);
			} else if (lastChar == '<' || lastChar == '>') {
				value = value.substring(0, value.length() - 1);
			}
			number = Integer.parseInt(value);
		} catch (Exception ignored) {
			
		}
		return number;
	}
	
	public static <T extends Enum<T>> T tryParseEnum(String value, T[] enumValues) {
		return tryParseEnum(value, enumValues, null);
	}
	
	public static <T extends Enum<T>> T tryParseEnum(String value, T @NotNull [] enumValues, @Nullable T fallback) {
		for (T enumValue : enumValues) {
			if (enumValue.name().toLowerCase().equals(value)) {
				return enumValue;
			}
		}
		return fallback;
	}
}
