/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants.NBT;

public class DimensionChunkCoords extends ChunkPos
{
	public DimensionType dimension;

	public DimensionChunkCoords(DimensionType dimension, int x, int z)
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
		tag.setString("dim", dimension.getRegistryName().toString());
		tag.setInt("x", this.x);
		tag.setInt("z", this.z);
		return tag;
	}

	public static DimensionChunkCoords readFromNBT(NBTTagCompound tag)
	{
		if(tag.contains("dim", NBT.TAG_STRING)&&tag.contains("x", NBT.TAG_INT)&&tag.contains("z", NBT.TAG_INT))
		{
			String dimNameStr = tag.getString("dim");
			ResourceLocation dimName = new ResourceLocation(dimNameStr);
			DimensionType dimType = DimensionType.byName(dimName);
			return new DimensionChunkCoords(dimType, tag.getInt("x"), tag.getInt("z"));
		}
		return null;
	}
}