/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.plant;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase.IBlockEnum;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_Hemp implements IStringSerializable, IBlockEnum
{
	BOTTOM0,
	BOTTOM1,
	BOTTOM2,
	BOTTOM3,
	BOTTOM4,
	TOP0;

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