/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.util.EnumFacing;

public interface IFluidPipe
{
	boolean canOutputPressurized(boolean consumePower);

	boolean hasOutputConnection(EnumFacing side);
}
