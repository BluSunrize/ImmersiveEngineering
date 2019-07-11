/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.INeighbourChangeTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class TileEntityTurntable extends TileEntityIEBase implements IDirectionalTile, INeighbourChangeTile, IHammerInteraction
{
	public static TileEntityType<TileEntityTurntable> TYPE;
	private Direction facing = Direction.UP;
	private boolean redstone = false;
	public boolean invert = false;

	public TileEntityTurntable()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		facing = Direction.byIndex(nbt.getInt("facing"));
		redstone = nbt.getBoolean("redstone");
		invert = nbt.getBoolean("invert");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("facing", facing.ordinal());
		nbt.putBoolean("redstone", redstone);
		nbt.putBoolean("invert", invert);
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		boolean r = this.world.isBlockPowered(pos);
		if(r!=this.redstone)
		{
			this.redstone = r;
			if(this.redstone)
			{
				BlockPos target = pos.offset(facing);
				RotationUtil.rotateBlock(this.world, target, invert?facing: facing.getOpposite());
			}
		}
	}

	@Override
	public Direction getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 1;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return placer.isSneaking();
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return !entity.isSneaking();
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			invert = !invert;
			markDirty();
			world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 254, 0);
			return true;
		}
		return false;
	}
}