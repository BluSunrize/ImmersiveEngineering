/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.component.DataComponentType;

public class IEApiDataComponents
{
	static {
		if (true)
			throw new IllegalStateException("Need to be registered!");
	}

	public static DataComponentType<WireLink> WIRE_LINK;
	public static DataComponentType<String> BLUEPRINT_TYPE;
	public static DataComponentType<Unit> FLUID_PRESSURIZED;
}
