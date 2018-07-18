/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_MetalsAll implements IStringSerializable, BlockIEBase.IBlockEnum
{
	COPPER,
	ALUMINUM,
	LEAD,
	SILVER,
	NICKEL,
	URANIUM,
	CONSTANTAN,
	ELECTRUM,
	STEEL,
	IRON,
	GOLD;

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