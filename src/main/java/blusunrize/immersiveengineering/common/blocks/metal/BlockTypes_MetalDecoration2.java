package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_MetalDecoration2 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	STEEL_POST,
	STEEL_WALLMOUNT,
	ALUMINUM_POST,
	ALUMINUM_WALLMOUNT,
	LANTERN;

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