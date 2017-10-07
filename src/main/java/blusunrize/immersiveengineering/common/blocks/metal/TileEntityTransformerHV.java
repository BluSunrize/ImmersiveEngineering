/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

public class TileEntityTransformerHV extends TileEntityTransformer
{
	@Override
	protected boolean canTakeHV()
	{
		return true;
	}
	@Override
	protected boolean canTakeLV()
	{
		return false;
	}

	@Override
	protected float getLowerOffset() {
		return super.getHigherOffset();
	}

	@Override
	protected float getHigherOffset() {
		return .75F;
	}
}