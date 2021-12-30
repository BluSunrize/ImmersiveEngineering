/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.fluid;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidAttributes;

public interface IFluidPipe
{
	/**
	 * Amount to be transferred through pipes per tick,
	 * if the fluid has been pressurized by a machine (such as a pump)
	 */
	int AMOUNT_PRESSURIZED = FluidAttributes.BUCKET_VOLUME;

	/**
	 * Amount to be transferred through pipes per tick,
	 * if the fluid is not pressurized
	 */
	int AMOUNT_UNPRESSURIZED = FluidAttributes.BUCKET_VOLUME/20;

	/**
	 * NBT Key to indicate a pressurized fluid, increasing its transfer rate in IFluidPipes
	 */
	String NBT_PRESSURIZED = "pressurized";

	static int getTransferableAmount(boolean pressurized)
	{
		return pressurized?AMOUNT_PRESSURIZED: AMOUNT_UNPRESSURIZED;
	}

	default boolean stripPressureTag()
	{
		return false;
	}

	boolean canOutputPressurized(boolean consumePower);

	boolean hasOutputConnection(Direction side);
}
