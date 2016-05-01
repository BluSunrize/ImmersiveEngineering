package blusunrize.immersiveengineering.common.blocks.plant;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase.IBlockEnum;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_Hemp implements IStringSerializable, IBlockEnum
{
	BOTTOM0,
	BOTTOM1,
	BOTTOM2,
	BOTTOM3,
	BOTTOM4,
	TOP0;
	
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