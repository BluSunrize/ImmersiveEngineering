package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_WoodenDevice0 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	CRATE,
	BARREL,
	WORKBENCH,
	SORTER,
	GUNPOWDER_BARREL,
	REINFORCED_CRATE;

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