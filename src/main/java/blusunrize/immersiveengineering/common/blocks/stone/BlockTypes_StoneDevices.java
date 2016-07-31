package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_StoneDevices implements IStringSerializable, BlockIEBase.IBlockEnum
{
	COKE_OVEN,
	BLAST_FURNACE,
	BLAST_FURNACE_ADVANCED;

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