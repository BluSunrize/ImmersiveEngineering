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

public enum BlockTypes_MetalDecoration1 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	STEEL_FENCE(false),
	STEEL_SCAFFOLDING_0(true),
	STEEL_SCAFFOLDING_1(true),
	STEEL_SCAFFOLDING_2(true),
	ALUMINUM_FENCE(false),
	ALUMINUM_SCAFFOLDING_0(true),
	ALUMINUM_SCAFFOLDING_1(true),
	ALUMINUM_SCAFFOLDING_2(true);

	private boolean isScaffold;

	BlockTypes_MetalDecoration1(boolean isScaffold)
	{
		this.isScaffold = isScaffold;
	}

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

	public boolean isScaffold()
	{
		return isScaffold;
	}
}