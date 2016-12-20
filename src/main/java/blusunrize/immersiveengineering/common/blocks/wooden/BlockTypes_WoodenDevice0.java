package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_WoodenDevice0 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	CRATE,
	BARREL,
	WORKBENCH,
	SORTER,
	GUNPOWDER_BARREL,
	REINFORCED_CRATE,
	TURNTABLE;

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
		return true;
	}
}