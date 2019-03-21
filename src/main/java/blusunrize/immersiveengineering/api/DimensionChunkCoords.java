/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;

public class DimensionChunkCoords extends ChunkPos
{
	public int dimension;

	public DimensionChunkCoords(int dimension, int x, int z)
	{
		super(x, z);
		this.dimension = dimension;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o)
			return true;
		else if(!(o instanceof DimensionChunkCoords))
			return false;
		else
		{
			DimensionChunkCoords coordPair = (DimensionChunkCoords)o;
			return this.dimension==coordPair.dimension&&this.x==coordPair.x&&this.z==coordPair.z;
		}
	}

	@Override
	public String toString()
	{
		return "[dim:"+this.dimension+"; "+this.x+", "+this.z+"]";
	}

	public DimensionChunkCoords withOffset(int offsetX, int offsetZ)
	{
		return new DimensionChunkCoords(this.dimension, this.x+offsetX, this.z+offsetZ);
	}

	public NBTTagCompound writeToNBT()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("dim", dimension);
		tag.setInteger("x", this.x);
		tag.setInteger("z", this.z);
		return tag;
	}

	public static DimensionChunkCoords readFromNBT(NBTTagCompound tag)
	{
		if(tag.hasKey("dim", 3)&&tag.hasKey("x", 3)&&tag.hasKey("z", 3))
			return new DimensionChunkCoords(tag.getInteger("dim"), tag.getInteger("x"), tag.getInteger("z"));
		return null;
	}
}