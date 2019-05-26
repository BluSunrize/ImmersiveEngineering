/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//TODO can this be done with just blockstates now?
public class TileEntityLadder extends TileEntityIEBase implements IDirectionalTile
{
	public static TileEntityType<TileEntityLadder> TYPE;
	public EnumFacing facing = EnumFacing.NORTH;

	public TileEntityLadder()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.byIndex(nbt.getInt("facing"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInt("facing", facing.ordinal());
	}

	@Override
	public EnumFacing getFacingForPlacement(EntityLivingBase placer, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		World world = placer.getEntityWorld();
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof BlockMetalLadder)
		{
			BlockMetalLadder ladder = (BlockMetalLadder)state.getBlock();
			if(side.getAxis().isHorizontal()&&ladder.canAttachTo(world, pos.offset(side.getOpposite()), facing))
				return side;
			else if(ladder.getMetaFromState(state) > 0)
				return placer.getAdjustedHorizontalFacing().getOpposite();
			else
			{
				for(EnumFacing enumfacing : Plane.HORIZONTAL)
					if(ladder.canAttachTo(world, pos.offset(enumfacing.getOpposite()), enumfacing))
						return enumfacing;
			}
		}
		return EnumFacing.NORTH;
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return -1;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}
}