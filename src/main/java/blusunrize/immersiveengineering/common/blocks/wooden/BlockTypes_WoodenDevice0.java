/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_WoodenDevice0 implements IStringSerializable, IEBaseBlock.IBlockEnum
{

	WORKBENCH,
	//GunpowderBarrelBlock
	GUNPOWDER_BARREL,
	//BarrelBlock
	BARREL,
	//TurntableBlock
	TURNTABLE,
	//CrateBlock
	CRATE,
	REINFORCED_CRATE,
	//SorterBlock
	SORTER,
	FLUID_SORTER;

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
		return true;
	}
}