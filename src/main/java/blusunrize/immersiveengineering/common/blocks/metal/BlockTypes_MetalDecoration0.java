package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_MetalDecoration0 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	COIL_LV,
	COIL_MV,
	COIL_HV,
	RS_ENGINEERING,
	LIGHT_ENGINEERING,
	HEAVY_ENGINEERING,
	GENERATOR,
	RADIATOR;

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