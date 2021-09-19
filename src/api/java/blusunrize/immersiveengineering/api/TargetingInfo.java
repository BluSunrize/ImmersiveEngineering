/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.context.UseOnContext;

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

	public TargetingInfo(UseOnContext ctx)
	{
		this(ctx.getClickedFace(), (float)ctx.getClickLocation().x, (float)ctx.getClickLocation().y, (float)ctx.getClickLocation().z);
	}

	public TargetingInfo(Direction side, float hitX, float hitY, float hitZ)
	{
		this.side = side;
		this.hitX = hitX;
		this.hitY = hitY;
		this.hitZ = hitZ;
	}

	public void writeToNBT(CompoundTag tag)
	{
		tag.putInt("side", side.ordinal());
		tag.putFloat("hitX", hitX);
		tag.putFloat("hitY", hitY);
		tag.putFloat("hitZ", hitZ);
	}

	public static TargetingInfo readFromNBT(CompoundTag tag)
	{
		return new TargetingInfo(Direction.from3DDataValue(tag.getInt("side")), tag.getFloat("hitX"), tag.getFloat("hitY"), tag.getFloat("hitZ"));
	}
}