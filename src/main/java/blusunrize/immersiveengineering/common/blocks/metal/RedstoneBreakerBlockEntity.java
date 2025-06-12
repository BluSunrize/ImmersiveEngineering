/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RedstoneBreakerBlockEntity extends BreakerSwitchBlockEntity implements IEServerTickableBE
{
	public RedstoneBreakerBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.REDSTONE_BREAKER.get(), pos, state);
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
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		Matrix4 mat = new Matrix4(getFacing());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		boolean isLeft = here.index()==LEFT_INDEX;
		return mat.apply(isLeft?new Vec3(.125, .5, 1): new Vec3(.875, .5, 1));
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
	public ItemInteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public boolean canConnectRedstone(@Nonnull Direction side)
	{
		return false;
	}

	private static final Pair<DyeColor, Byte>[] NO_OVERRIDE = new Pair[0];
	@Override
	public Pair<DyeColor, Byte>[] overrideVoltmeterRead()
	{
		return NO_OVERRIDE;
	}
}