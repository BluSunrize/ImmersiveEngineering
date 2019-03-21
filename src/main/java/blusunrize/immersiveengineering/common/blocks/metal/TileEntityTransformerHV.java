/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.WireType;
import com.google.common.collect.ImmutableSet;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.HV_CATEGORY;

public class TileEntityTransformerHV extends TileEntityTransformer
{
	{
		acceptableLowerWires = ImmutableSet.of(WireType.LV_CATEGORY, WireType.MV_CATEGORY);
	}

	@Override
	protected boolean canTakeHV()
	{
		return true;
	}

	@Override
	protected float getLowerOffset()
	{
		return super.getHigherOffset();
	}

	@Override
	protected float getHigherOffset()
	{
		return .75F;
	}

	@Override
	public String getHigherWiretype()
	{
		return HV_CATEGORY;
	}
}