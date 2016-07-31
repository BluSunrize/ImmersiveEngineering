package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_TreatedWood implements IStringSerializable, BlockIEBase.IBlockEnum
{
	HORIZONTAL,
	VERTICAL,
	PACKAGED;

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
		return true;
	}
}