/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_MetalDevice1 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	BLAST_FURNACE_PREHEATER,
	FURNACE_HEATER,
	DYNAMO,
	THERMOELECTRIC_GEN,
	ELECTRIC_LANTERN,
	CHARGING_STATION,
	FLUID_PIPE,
	SAMPLE_DRILL,
	TESLA_COIL,
	FLOODLIGHT,
	TURRET_CHEM,
	TURRET_GUN,
	TURRET_RAIL,
	BELLJAR;

	@Override
	public String getName()
	{
		return this.toString().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public int getMeta()
	{
		return ordinal();
	}

	@Override
	public boolean listForCreative()
	{
		return ordinal()!=12;
	}
}