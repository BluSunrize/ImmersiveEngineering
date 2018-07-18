/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_StoneDevices implements IStringSerializable, BlockIEBase.IBlockEnum
{
	COKE_OVEN,
	BLAST_FURNACE,
	BLAST_FURNACE_ADVANCED,
	CONCRETE_SHEET,
	CONCRETE_QUARTER,
	CONCRETE_THREEQUARTER,
	CORESAMPLE,
	ALLOY_SMELTER;

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
		return ordinal() > 2&&ordinal() < 6;
	}
}