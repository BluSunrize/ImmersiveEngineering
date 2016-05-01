package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_MetalDecoration1 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	STEEL_FENCE,
	STEEL_SCAFFOLDING_0,
	STEEL_SCAFFOLDING_1,
	STEEL_SCAFFOLDING_2,
	ALUMINUM_FENCE,
	ALUMINUM_SCAFFOLDING_0,
	ALUMINUM_SCAFFOLDING_1,
	ALUMINUM_SCAFFOLDING_2;

	@Override
	public String getName()
	{
		return this.toString();
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