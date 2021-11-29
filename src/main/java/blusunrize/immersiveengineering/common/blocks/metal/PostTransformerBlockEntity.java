/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostTransformerBlockEntity extends AbstractTransformerBlockEntity implements IBlockBounds
{
	public PostTransformerBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.POST_TRANSFORMER.get(), pos, state);
	}

	@Override
	protected Vec3 getConnectionOffset(WireType type, boolean right)
	{
		if(right)
			return new Vec3(.5+(getFacing()==Direction.EAST?.4375: getFacing()==Direction.WEST?-.4375: 0), 1.4375, .5+(getFacing()==Direction.SOUTH?.4375: getFacing()==Direction.NORTH?-.4375: 0));
		else
			return new Vec3(.5+(getFacing()==Direction.EAST?-.0625: getFacing()==Direction.WEST?.0625: 0), .25, .5+(getFacing()==Direction.SOUTH?-.0625: getFacing()==Direction.NORTH?.0625: 0));
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo target, Vec3i offset)
	{
		if(target.hitY >= .5)
			return new ConnectionPoint(worldPosition, RIGHT_INDEX);
		else
			return new ConnectionPoint(worldPosition, LEFT_INDEX);
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_PREFER_SIDE;
	}

	private static final CachedShapesWithTransform<Unit, Direction> SHAPES = CachedShapesWithTransform.createDirectional(
			$ -> ImmutableList.of(new AABB(.25F, 0, -.375F, .75F, 1, .3125F))
	);

	@Nonnull
	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(Unit.INSTANCE, getFacing());
	}
}