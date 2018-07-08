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

public enum BlockTypes_StoneDecoration implements IStringSerializable, BlockIEBase.IBlockEnum
{
	COKEBRICK,
	BLASTBRICK,
	BLASTBRICK_REINFORCED,
	COKE,
	HEMPCRETE,
	CONCRETE,
	CONCRETE_TILE,
	CONCRETE_LEADED,
	INSULATING_GLASS,
	CONCRETE_SPRAYED,
	ALLOYBRICK;

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