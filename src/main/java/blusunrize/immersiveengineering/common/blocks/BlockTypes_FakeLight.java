package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.util.IStringSerializable;

public enum BlockTypes_FakeLight implements IStringSerializable, BlockIEBase.IBlockEnum
{
	AIR;
	
	@Override
	public String getName()
	{
		return this.toString().toLowerCase();
	}
	@Override
	public int getMeta()
	{
		return ordinal();
	}
	@Override
	public boolean listForCreative()
	{
		return false;
	}
}