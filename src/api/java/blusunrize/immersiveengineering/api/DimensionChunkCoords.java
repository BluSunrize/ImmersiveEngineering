/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

// TODO replace with GlobalPos in next world breaking update
public class DimensionChunkCoords extends ChunkPos
{
	public ResourceKey<Level> dimension;

	public DimensionChunkCoords(ResourceKey<Level> dimension, int x, int z)
	{
		super(x, z);
		this.dimension = dimension;
	}

	public DimensionChunkCoords(ResourceKey<Level> dimension, ChunkPos pos)
	{
		this(dimension, pos.x, pos.z);
	}

	public DimensionChunkCoords(Level world, int chunkX, int chunkZ)
	{
		this(world.dimension(), chunkX, chunkZ);
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

	public CompoundTag writeToNBT()
	{
		CompoundTag tag = new CompoundTag();
		tag.putString("dim", dimension.location().toString());
		tag.putInt("x", this.x);
		tag.putInt("z", this.z);
		return tag;
	}

	@Nullable
	public static DimensionChunkCoords readFromNBT(CompoundTag tag)
	{
		if(tag.contains("dim", NBT.TAG_STRING)&&tag.contains("x", NBT.TAG_INT)&&tag.contains("z", NBT.TAG_INT))
		{
			String dimNameStr = tag.getString("dim");
			ResourceLocation dimName = new ResourceLocation(dimNameStr);
			ResourceKey<Level> dimType = ResourceKey.create(Registry.DIMENSION_REGISTRY, dimName);
			return new DimensionChunkCoords(dimType, tag.getInt("x"), tag.getInt("z"));
		}
		return null;
	}
}