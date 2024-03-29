/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static blusunrize.immersiveengineering.api.wires.WireType.HV_CATEGORY;

public class TransformerHVBlockEntity extends TransformerBlockEntity
{
	public TransformerHVBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.TRANSFORMER_HV.get(), pos, state);
		acceptableLowerWires = ImmutableSet.of(WireType.LV_CATEGORY, WireType.MV_CATEGORY);
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