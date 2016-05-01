package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.util.IStringSerializable;

public enum BlockTypes_Ore implements IStringSerializable, BlockIEBase.IBlockEnum
{
	COPPER,
	ALUMINUM,
	LEAD,
	SILVER,
	NICKEL,
	URANIUM;
	
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