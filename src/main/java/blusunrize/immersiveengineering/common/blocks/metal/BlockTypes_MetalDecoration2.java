package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_MetalDecoration2 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	STEEL_POST,
	STEEL_WALLMOUNT,
	ALUMINUM_POST,
	ALUMINUM_WALLMOUNT,
	LANTERN,
	RAZOR_WIRE;

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