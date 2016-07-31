package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_MetalDevice1 implements IStringSerializable, BlockIEBase.IBlockEnum
{
	BLAST_FURNACE_PREHEATER,
	FURNACE_HEATER,
	DYNAMO,
	THERMOELECTRIC_GEN,
	ELECTRIC_LANTERN,
	CHARGING_STATION,
	FLUID_PIPE,
	SAMPLE_DRILL,
	TESLA_COIL,
	FLOODLIGHT;

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