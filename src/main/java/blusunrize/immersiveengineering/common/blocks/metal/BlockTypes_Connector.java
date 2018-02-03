/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

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
	ENERGY_METER,
	CONNECTOR_REDSTONE,
	CONNECTOR_PROBE,
	FEEDTHROUGH;

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