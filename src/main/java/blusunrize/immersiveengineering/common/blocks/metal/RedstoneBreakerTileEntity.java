/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RedstoneBreakerTileEntity extends BreakerSwitchTileEntity implements IETickableBlockEntity
{
	public RedstoneBreakerTileEntity(BlockPos pos, BlockState state)
	{
		super(IETileTypes.REDSTONE_BREAKER.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		final boolean activeOld = getIsActive();
		if(isRSPowered()==activeOld)
		{
			setActive(!activeOld);
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
		return getIsActive()^inverted;
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		Vec3 start = new Vec3(0, .125f, 0);
		Vec3 end = new Vec3(1, .875f, 1);
		Matrix4 mat = new Matrix4(getFacing());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		start = mat.apply(start);
		end = mat.apply(end);
		return Shapes.create(new AABB(start, end));
	}

	@Override
	public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Matrix4 mat = new Matrix4(getFacing());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		boolean isLeft = here.getIndex()==LEFT_INDEX;
		Vec3 ret = mat.apply(isLeft?new Vec3(.125, .5, 1): new Vec3(.875, .5, 1));
		return ret;
	}

	@Override
	public int getWeakRSOutput(@Nonnull Direction side)
	{
		return 0;
	}

	@Override
	public int getStrongRSOutput(@Nonnull Direction side)
	{
		return 0;
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	@Override
	public boolean canConnectRedstone(@Nonnull Direction side)
	{
		return false;
	}
}