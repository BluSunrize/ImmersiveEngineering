package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_WoodenDevice1 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	WATERMILL,
	WINDMILL,
	WINDMILL_ADVANCED,
	POST,
	WALLMOUNT;

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