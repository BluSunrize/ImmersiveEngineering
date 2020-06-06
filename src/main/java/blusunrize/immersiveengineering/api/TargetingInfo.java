/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

/**
 * @author BluSunrize - 11.03.2015
 * <p>
 * Similar too MovingObjectPosition.class, but this is specifically designed for sub-targets on a block
 */
public class TargetingInfo
{
	public final Direction side;
	public final float hitX;
	public final float hitY;
	public final float hitZ;

	public TargetingInfo(ItemUseContext ctx)
	{
		this(ctx.getFace(), (float)ctx.getHitVec().x, (float)ctx.getHitVec().y, (float)ctx.getHitVec().z);
	}

	public TargetingInfo(Direction side, float hitX, float hitY, float hitZ)
	{
		this.side = side;
		this.hitX = hitX;
		this.hitY = hitY;
		this.hitZ = hitZ;
	}

	public void writeToNBT(CompoundNBT tag)
	{
		tag.putInt("side", side.ordinal());
		tag.putFloat("hitX", hitX);
		tag.putFloat("hitY", hitY);
		tag.putFloat("hitZ", hitZ);
	}

	public static TargetingInfo readFromNBT(CompoundNBT tag)
	{
		return new TargetingInfo(Direction.byIndex(tag.getInt("side")), tag.getFloat("hitX"), tag.getFloat("hitY"), tag.getFloat("hitZ"));
	}
}