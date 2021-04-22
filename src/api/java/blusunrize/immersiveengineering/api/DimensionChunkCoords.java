/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

// TODO replace with GlobalPos in next world breaking update
public class DimensionChunkCoords extends ChunkPos
{
	public RegistryKey<World> dimension;

	public DimensionChunkCoords(RegistryKey<World> dimension, int x, int z)
	{
		super(x, z);
		this.dimension = dimension;
	}

	public DimensionChunkCoords(RegistryKey<World> dimension, ChunkPos pos)
	{
		this(dimension, pos.x, pos.z);
	}

	public DimensionChunkCoords(World world, int chunkX, int chunkZ)
	{
		this(world.getDimensionKey(), chunkX, chunkZ);
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

	public CompoundNBT writeToNBT()
	{
		CompoundNBT tag = new CompoundNBT();
		tag.putString("dim", dimension.getLocation().toString());
		tag.putInt("x", this.x);
		tag.putInt("z", this.z);
		return tag;
	}

	@Nullable
	public static DimensionChunkCoords readFromNBT(CompoundNBT tag)
	{
		if(tag.contains("dim", NBT.TAG_STRING)&&tag.contains("x", NBT.TAG_INT)&&tag.contains("z", NBT.TAG_INT))
		{
			String dimNameStr = tag.getString("dim");
			ResourceLocation dimName = new ResourceLocation(dimNameStr);
			RegistryKey<World> dimType = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dimName);
			return new DimensionChunkCoords(dimType, tag.getInt("x"), tag.getInt("z"));
		}
		return null;
	}
}