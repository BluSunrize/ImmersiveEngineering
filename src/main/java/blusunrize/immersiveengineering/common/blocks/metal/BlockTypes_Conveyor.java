package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_Conveyor implements IStringSerializable, BlockIEBase.IBlockEnum
{
	CONVEYOR,
	CONVEYOR_DROPPER,
	CONVEYOR_VERTICAL;

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