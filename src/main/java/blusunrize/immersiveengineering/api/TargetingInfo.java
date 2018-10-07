/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

/**
 * @author BluSunrize - 11.03.2015
 * <p>
 * Similar too MovingObjectPosition.class, but this is specifically designed for sub-targets on a block
 */
public class TargetingInfo
{
	public final EnumFacing side;
	public final float hitX;
	public final float hitY;
	public final float hitZ;

	public TargetingInfo(EnumFacing side, float hitX, float hitY, float hitZ)
	{
		this.side = side;
		this.hitX = hitX;
		this.hitY = hitY;
		this.hitZ = hitZ;
	}

	public void writeToNBT(NBTTagCompound tag)
	{
		tag.setInteger("side", side.ordinal());
		tag.setFloat("hitX", hitX);
		tag.setFloat("hitY", hitY);
		tag.setFloat("hitZ", hitZ);
	}

	public static TargetingInfo readFromNBT(NBTTagCompound tag)
	{
		return new TargetingInfo(EnumFacing.byIndex(tag.getInteger("side")), tag.getFloat("hitX"), tag.getFloat("hitY"), tag.getFloat("hitZ"));
	}
}