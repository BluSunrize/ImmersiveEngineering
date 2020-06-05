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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nonnull;
import java.util.Objects;

//Intentionally does not inherit from BlockPos to prevent TEs etc being created with dimensional positions nad causing weird issues
public class DimensionBlockPos
{
	public final DimensionType dimension;
	public final BlockPos pos;

	public DimensionBlockPos(int x, int y, int z, DimensionType dim)
	{
		pos = new BlockPos(x, y, z);
		if(dim==null)
			dimension = DimensionType.OVERWORLD;
		else
			dimension = dim;
	}

	public DimensionBlockPos(int x, int y, int z, World w)
	{
		this(x, y, z, w.getDimension().getType());
	}

	public DimensionBlockPos(BlockPos pos, World w)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), w.getDimension().getType());
	}

	public DimensionBlockPos(BlockPos pos, DimensionType dim)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), dim);
	}

	public DimensionBlockPos(TileEntity te)
	{
		this(te.getPos(), te.getWorld());
	}

	public DimensionBlockPos(CompoundNBT nbt)
	{
		this(NBTUtil.readBlockPos(nbt), DimensionType.byName(new ResourceLocation(nbt.getString("dimension"))));
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
		ret.putString("dimension", dimension.getRegistryName().toString());
		return ret;
	}
}
