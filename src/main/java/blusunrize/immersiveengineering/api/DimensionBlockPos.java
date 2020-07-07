/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

//Intentionally does not inherit from BlockPos to prevent TEs etc being created with dimensional positions nad causing weird issues
public class DimensionBlockPos
{
	public final RegistryKey<World> dimension;
	public final BlockPos pos;

	public DimensionBlockPos(int x, int y, int z, RegistryKey<World> dim)
	{
		pos = new BlockPos(x, y, z);
		if(dim==null)
			dimension = World.field_234918_g_;
		else
			dimension = dim;
	}

	public DimensionBlockPos(int x, int y, int z, World w)
	{
		this(x, y, z, w.func_234923_W_());
	}

	public DimensionBlockPos(BlockPos pos, World w)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), w.func_234923_W_());
	}

	public DimensionBlockPos(BlockPos pos, RegistryKey<World> dim)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), dim);
	}

	public DimensionBlockPos(TileEntity te)
	{
		this(te.getPos(), te.getWorld());
	}

	public DimensionBlockPos(CompoundNBT nbt)
	{
		this(NBTUtil.readBlockPos(nbt), RegistryKey.func_240903_a_(
				Registry.WORLD_KEY,
				new ResourceLocation(nbt.getString("dimension")
				)));
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		DimensionBlockPos that = (DimensionBlockPos)o;
		return dimension.equals(that.dimension)&&
				pos.equals(that.pos);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(dimension, pos);
	}

	@Nonnull
	@Override
	public String toString()
	{
		return "Dimension: "+dimension+" Pos: "+super.toString();
	}

	public CompoundNBT toNBT()
	{
		CompoundNBT ret = NBTUtil.writeBlockPos(pos);
		ret.putString("dimension", dimension.func_240901_a_().toString());
		return ret;
	}
}
