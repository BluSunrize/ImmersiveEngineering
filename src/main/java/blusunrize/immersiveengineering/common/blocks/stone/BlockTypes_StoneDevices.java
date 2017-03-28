package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_StoneDevices implements IStringSerializable, BlockIEBase.IBlockEnum
{
	COKE_OVEN,
	BLAST_FURNACE,
	BLAST_FURNACE_ADVANCED,
	CONCRETE_SHEET,
	CONCRETE_QUARTER,
	CONCRETE_THREEQUARTER,
	CORESAMPLE;

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
		return ordinal()>2&&ordinal()!=6;
	}
}