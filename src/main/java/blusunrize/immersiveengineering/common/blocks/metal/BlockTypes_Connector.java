package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

public enum BlockTypes_Connector implements IStringSerializable, BlockIEBase.IBlockEnum
{
	CONNECTOR_LV,
	RELAY_LV,
	CONNECTOR_MV,
	RELAY_MV,
	CONNECTOR_HV,
	RELAY_HV,
	CONNECTOR_STRUCTURAL,
	TRANSFORMER,
	TRANSFORMER_HV,
	BREAKERSWITCH,
	REDSTONE_BREAKER,
	ENERGY_METER;

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