/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.fluids.FluidStack;

public class TextUtils
{
	public static <T extends StringRepresentable> Component[] sideConfigWithOpposite(
			String descBase,
			T thisConfig,
			T otherConfig
	)
	{
		return new Component[]{
				Component.translatable(Lib.DESC_INFO+"blockSide.facing")
						.append(": ")
						.append(Component.translatable(descBase+thisConfig.getSerializedName())),
				Component.translatable(Lib.DESC_INFO+"blockSide.opposite")
						.append(": ")
						.append(Component.translatable(descBase+otherConfig.getSerializedName()))
		};
	}

	public static Component formatFluidStack(FluidStack fluid)
	{
		String s;
		if(!fluid.isEmpty())
			s = fluid.getDisplayName().getString()+": "+fluid.getAmount()+"mB";
		else
			s = I18n.get(Lib.GUI+"empty");
		return Component.literal(s);
	}
}
