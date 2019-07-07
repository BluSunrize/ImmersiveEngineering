/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

public class TileEntityRedstoneBreaker extends TileEntityBreakerSwitch implements ITickable
{
	public static TileEntityType<TileEntityRedstoneBreaker> TYPE;

	public TileEntityRedstoneBreaker()
	{
		super(TYPE);
	}
	@Override
	public void tick()
	{
		//TODO use block updates to detect RS changes?
		if(!world.isRemote&&(world.getRedstonePowerFromNeighbors(getPos()) > 0)==active)
		{
			active = !active;
			updateConductivity();
		}
	}

	@Override
	protected boolean canTakeHV()
	{
		return true;
	}

	protected boolean allowEnergyToPass()
	{
		return active^inverted;
	}

	@Override
	public float[] getBlockBounds()
	{
		Vec3d start = new Vec3d(0, .125f, 0);
		Vec3d end = new Vec3d(1, .875f, 1);
		Matrix4 mat = new Matrix4(facing);
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		start = mat.apply(start);
		end = mat.apply(end);
		return new float[]{(float)start.x, (float)start.y, (float)start.z,
				(float)end.x, (float)end.y, (float)end.z};
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Matrix4 mat = new Matrix4(facing);
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		boolean isLeft = here.getIndex()==LEFT_INDEX;
		Vec3d ret = mat.apply(isLeft?new Vec3d(.125, .5, 1): new Vec3d(.875, .5, 1));
		return ret;
	}

	@Override
	public int getWeakRSOutput(BlockState state, Direction side)
	{
		return 0;
	}

	@Override
	public int getStrongRSOutput(BlockState state, Direction side)
	{
		return 0;
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, Direction side)
	{
		return false;
	}
}