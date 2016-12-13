package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_FakeLight implements IStringSerializable, BlockIEBase.IBlockEnum
{
	AIR;
	
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
		return false;
	}
}