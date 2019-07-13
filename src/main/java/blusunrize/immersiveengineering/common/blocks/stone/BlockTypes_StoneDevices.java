/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_StoneDevices implements IStringSerializable, IEBaseBlock.IBlockEnum
{
	//Instances of StoneMultiBlock with appropiate TEType
	COKE_OVEN,
	BLAST_FURNACE,
	ALLOY_SMELTER,
	//BlockBFAdvanced
	BLAST_FURNACE_ADVANCED,
	//PartialConcreteBlock
	CONCRETE_SHEET,
	CONCRETE_QUARTER,
	CONCRETE_THREEQUARTER,
	//CoresampleBlock
	CORESAMPLE;

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