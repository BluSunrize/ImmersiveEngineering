/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.world.item.DyeColor;

import java.util.Arrays;
import java.util.stream.Stream;

public class SignalColors
{

	public static final int COUNT = 16;

	public static Stream<DyeColor> colors() {
		return Arrays.stream(DyeColor.values()).filter(it -> it.getId() < COUNT);
	}

	public static DyeColor[] colorsArray() {
		return colors().toArray(DyeColor[]::new);
	}

}
