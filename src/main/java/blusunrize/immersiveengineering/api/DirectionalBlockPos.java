/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import com.google.common.base.MoreObjects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DirectionalBlockPos extends BlockPos
{
	public EnumFacing direction;

	public DirectionalBlockPos(BlockPos pos)
	{
		this(pos, EnumFacing.DOWN);
	}

	public DirectionalBlockPos(BlockPos pos, EnumFacing direction)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), direction);
	}

	public DirectionalBlockPos(int x, int y, int z, EnumFacing direction)
	{
		super(x, y, z);
		this.direction = direction;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).add("direction", this.direction.toString()).toString();
	}

	public TileEntity getTile(World world)
	{
		return world.getTileEntity(this);
	}
}
