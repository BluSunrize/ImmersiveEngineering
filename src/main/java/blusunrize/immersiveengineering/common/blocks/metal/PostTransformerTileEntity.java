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
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableTileEntity;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Direction;
import net.minecraft.util.Unit;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostTransformerTileEntity extends AbstractTransformerTileEntity implements IBlockBounds
{
	public PostTransformerTileEntity()
	{
		super(IETileTypes.POST_TRANSFORMER.get());
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	protected Vector3d getConnectionOffset(Connection con, boolean right)
	{
		if(right)
			return new Vector3d(.5+(getFacing()==Direction.EAST?.4375: getFacing()==Direction.WEST?-.4375: 0), 1.4375, .5+(getFacing()==Direction.SOUTH?.4375: getFacing()==Direction.NORTH?-.4375: 0));
		else
			return new Vector3d(.5+(getFacing()==Direction.EAST?-.0625: getFacing()==Direction.WEST?.0625: 0), .25, .5+(getFacing()==Direction.SOUTH?-.0625: getFacing()==Direction.NORTH?.0625: 0));
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo target, Vector3i offset)
	{
		if(target.hitY >= .5)
			return new ConnectionPoint(pos, RIGHT_INDEX);
		else
			return new ConnectionPoint(pos, LEFT_INDEX);
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_PREFER_SIDE;
	}

	private static final CachedShapesWithTransform<Unit, Direction> SHAPES = CachedShapesWithTransform.createDirectional(
			$ -> ImmutableList.of(new AxisAlignedBB(.25F, 0, -.375F, .75F, 1, .3125F))
	);

	@Nonnull
	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return SHAPES.get(Unit.INSTANCE, getFacing());
	}
}