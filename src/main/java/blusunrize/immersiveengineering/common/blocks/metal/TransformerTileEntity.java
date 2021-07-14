/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IMirrorAble;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class TransformerTileEntity extends AbstractTransformerTileEntity implements IMirrorAble,
		IHasDummyBlocks, IModelOffsetProvider, IBlockBounds
{
	public int dummy = 0;

	public TransformerTileEntity()
	{
		super(IETileTypes.TRANSFORMER.get());
	}

	public TransformerTileEntity(TileEntityType<? extends TransformerTileEntity> type)
	{
		super(type);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("dummy", dummy);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		dummy = nbt.getInt("dummy");
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return getPos().add(0, -dummy, 0);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
	{
		if(dummy==2)
		{
			TileEntity master = world.getTileEntity(getPos().add(0, -dummy, 0));
			return master instanceof TransformerTileEntity&&((TransformerTileEntity)master).canConnectCable(cableType, target,
					new Vector3i(0, 2, 0));
		}
		else
			return super.canConnectCable(cableType, target, offset);
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		if(dummy!=0)
		{
			TileEntity master = world.getTileEntity(getPos().add(0, -dummy, 0));
			if(master instanceof TransformerTileEntity)
				((TransformerTileEntity)master).connectCable(cableType, target, other, otherTarget);
		}
		else
			super.connectCable(cableType, target, other, otherTarget);
	}

	@Override
	protected Vector3d getConnectionOffset(Connection con, boolean right)
	{
		double conRadius = con.type.getRenderDiameter()/2;
		double offset = getHigherWiretype().equals(con.type.getCategory())?getHigherOffset(): getLowerOffset();
		if(getFacing()==Direction.NORTH)
			return new Vector3d(right?.8125: .1875, 2+offset-conRadius, .5);
		if(getFacing()==Direction.SOUTH)
			return new Vector3d(right?.1875: .8125, 2+offset-conRadius, .5);
		if(getFacing()==Direction.WEST)
			return new Vector3d(.5, 2+offset-conRadius, right?.1875: .8125);
		if(getFacing()==Direction.EAST)
			return new Vector3d(.5, 2+offset-conRadius, right?.8125: .1875);
		return new Vector3d(.5, .5, .5);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo target, Vector3i offset)
	{
		if(offset.getY()!=2)
			return null;
		Direction facing = getFacing();
		double hitPos;
		if(facing.getAxis()==Axis.X)
			hitPos = target.hitZ;
		else
			hitPos = 0.5-target.hitX;

		if((hitPos < .5)==(facing.getAxisDirection()==AxisDirection.POSITIVE))
			return new ConnectionPoint(pos, LEFT_INDEX);
		else
			return new ConnectionPoint(pos, RIGHT_INDEX);
	}

	@Override
	protected void updateMirrorState()
	{
		if(dummy!=0)
		{
			TileEntity master = world.getTileEntity(pos.down(dummy));
			if(master instanceof TransformerTileEntity)
				((TransformerTileEntity)master).updateMirrorState();
		}
		else if(rightType!=null||leftType!=null)
		{
			String higher = getHigherWiretype();
			boolean intendedState = (rightType!=null&&higher.equals(rightType.getCategory()))||
					(leftType!=null&&!higher.equals(leftType.getCategory()));
			for(int i = 0; i < 3; ++i)
			{
				TileEntity te = world.getTileEntity(pos.up(i));
				if(te instanceof TransformerTileEntity)
					((TransformerTileEntity)te).setMirrored(intendedState);
			}
		}
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean isDummy()
	{
		return dummy!=0;
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getPos().down(dummy);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		state = state.with(IEProperties.MULTIBLOCKSLAVE, true);
		for(int i = 1; i <= 2; i++)
		{
			BlockPos dummyPos = pos.up(i);
			world.setBlockState(dummyPos, IEBaseBlock.applyLocationalWaterlogging(state, world, dummyPos));
			((TransformerTileEntity)world.getTileEntity(dummyPos)).dummy = i;
			((TransformerTileEntity)world.getTileEntity(dummyPos)).setFacing(this.getFacing());
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 2; i++)
			world.removeBlock(getPos().add(0, -dummy, 0).add(0, i, 0), false);
	}

	private static final CachedShapesWithTransform<ShapeKey, Pair<Direction, Boolean>> SHAPES =
			new CachedShapesWithTransform<>(
					key -> ImmutableList.of(
							new AxisAlignedBB(0, 0, .3125, .375, key.lowerHeight, .6875),
							new AxisAlignedBB(.625, 0, .3125, 1, key.higherHeight, .6875)
					),
					(pair, aabb) -> CachedShapesWithTransform.withFacingAndMirror(aabb, pair.getFirst(), pair.getSecond())
			);

	@Nonnull
	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		if(dummy==2)
			return SHAPES.get(new ShapeKey(getLowerOffset(), getHigherOffset()), Pair.of(getFacing(), !getIsMirrored()));
		else
			return VoxelShapes.fullCube();
	}

	@Override
	public Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		return ImmutableSet.of(pos.up(2));
	}

	protected float getLowerOffset()
	{
		return .5F;
	}

	protected float getHigherOffset()
	{
		return .5625F;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		if(isDummy())
			return ImmutableList.of();
		else
			return super.getConnectionPoints();
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		if(isDummy())
			return ImmutableList.of();
		else
			return super.getInternalConnections();
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vector3i size)
	{
		return new BlockPos(0, dummy, 0);
	}

	private static class ShapeKey
	{
		private final double lowerHeight;
		private final double higherHeight;

		private ShapeKey(double lowerHeight, double higherHeight)
		{
			this.lowerHeight = lowerHeight;
			this.higherHeight = higherHeight;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			ShapeKey shapeKey = (ShapeKey)o;
			return Double.compare(shapeKey.lowerHeight, lowerHeight)==0&&Double.compare(shapeKey.higherHeight, higherHeight)==0;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(lowerHeight, higherHeight);
		}
	}
}