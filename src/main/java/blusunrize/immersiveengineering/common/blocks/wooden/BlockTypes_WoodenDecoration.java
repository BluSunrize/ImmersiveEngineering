package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_WoodenDecoration implements IStringSerializable, BlockIEBase.IBlockEnum
{
	FENCE,
	SCAFFOLDING;

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