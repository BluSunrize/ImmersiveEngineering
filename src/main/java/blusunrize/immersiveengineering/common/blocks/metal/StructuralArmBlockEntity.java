/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.UP;

public class StructuralArmBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional, ICollisionBounds,
		ISelectionBounds, IBlockBounds
{
	private int totalLength = 1;
	private int slopePosition = 0;
	private Direction facing = null;
	private boolean onCeiling = false;

	public StructuralArmBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.STRUCTURAL_ARM.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		int oldLength = totalLength, oldPos = slopePosition;
		totalLength = nbt.getInt("totalLength");
		slopePosition = nbt.getInt("slopePosition");
		onCeiling = nbt.getBoolean("onCeiling");
		if(level!=null&&level.isClientSide&&(oldLength!=totalLength||slopePosition!=oldPos))
		{
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 3);
		}
		// In IE 134 and below the tile field is used instead of the blockstate property. The TE field is now only used
		// to handle worlds saved with those versions and should be removed once compat is no longer a concern.
		// Note that the blockstate is not actively replaced, so this will be the next MC version break (1.17).
		if(nbt.contains("facing", NBT.TAG_INT))
			this.facing = DirectionUtils.VALUES[nbt.getInt("facing")];
		else
			this.facing = null;
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putInt("totalLength", totalLength);
		nbt.putInt("slopePosition", slopePosition);
		if(this.facing!=null)
			nbt.putInt("facing", this.facing.ordinal());
		nbt.putBoolean("onCeiling", onCeiling);
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		super.onNeighborBlockChange(otherPos);
		if(level.isClientSide)
			return;
		boolean positive;
		if(otherPos.equals(worldPosition.relative(getFacing(), 1)))
			positive = true;
		else if(otherPos.equals(worldPosition.relative(getFacing(), -1)))
			positive = false;
		else
			return;
		StructuralArmBlockEntity slope = null;
		{
			BlockEntity atOther = level.getBlockEntity(otherPos);
			if(atOther instanceof StructuralArmBlockEntity)
			{
				StructuralArmBlockEntity tmp = (StructuralArmBlockEntity)atOther;
				BlockState stateHere = level.getBlockState(worldPosition);
				BlockState stateThere = level.getBlockState(otherPos);
				if(tmp.getFacing()==this.getFacing()&&stateHere.getBlock()==stateThere.getBlock()&&tmp.onCeiling==this.onCeiling)
					slope = (StructuralArmBlockEntity)atOther;
			}
		}
		boolean atEnd = isAtEnd(positive);
		if(atEnd==(slope==null))
			return;
		if(slope==null)
		{
			int toEnd = blocksToEnd(positive);
			forEachSlopeBlockBeyond(positive, false, true, other -> {
				other.totalLength = toEnd-1;
				if(positive)
					other.slopePosition -= slopePosition+2;
				updateNoNeighbours(other.worldPosition);
			});
			forEachSlopeBlockBeyond(!positive, true, true, other -> {
				other.totalLength = totalLength-toEnd;
				if(!positive)
					other.slopePosition -= this.slopePosition;
				updateNoNeighbours(other.worldPosition);
			});

		}
		else
		{
			int oldLength = totalLength;
			if(!positive)
				slopePosition += slope.totalLength;
			totalLength += slope.totalLength;
			forEachSlopeBlockBeyond(positive, false, false, other -> {
				other.totalLength = totalLength;
				if(positive)
					other.slopePosition += oldLength;
				updateNoNeighbours(other.worldPosition);
			});
			forEachSlopeBlockBeyond(!positive, false, false, other -> {
				other.totalLength = totalLength;
				if(!positive)
					other.slopePosition += totalLength-oldLength;
				updateNoNeighbours(other.worldPosition);
			});
		}
		updateNoNeighbours(worldPosition);
	}

	private boolean isAtEnd(boolean positive)
	{
		if(positive)
			return slopePosition==totalLength-1;
		else
			return slopePosition==0;
	}

	private int blocksToEnd(boolean positive)
	{
		if(positive)
			return totalLength-slopePosition-1;
		else
			return slopePosition;
	}

	private void forEachSlopeBlockBeyond(boolean positive, boolean includeThis, boolean removing,
										 Consumer<StructuralArmBlockEntity> out)
	{
		if(positive)
			for(int i = 1; i < totalLength-slopePosition; i++)
				acceptIfValid(i, removing, out);
		else
			for(int i = -1; i >= -slopePosition; i--)
				acceptIfValid(i, removing, out);
		if(includeThis)
			out.accept(this);
	}

	private void acceptIfValid(int offsetToHere, boolean removing, Consumer<StructuralArmBlockEntity> out)
	{
		BlockPos posI = worldPosition.relative(getFacing(), offsetToHere);
		BlockEntity teAtI = level.getBlockEntity(posI);
		if(teAtI instanceof StructuralArmBlockEntity)
		{
			StructuralArmBlockEntity slope = (StructuralArmBlockEntity)teAtI;
			int offsetAtPos = slopePosition+offsetToHere;
			BlockState stateHere = level.getBlockState(worldPosition);
			BlockState stateThere = level.getBlockState(posI);
			if((!removing||(slope.totalLength==this.totalLength&&slope.slopePosition==offsetAtPos))
					&&slope.onCeiling==this.onCeiling
					&&stateHere.getBlock()==stateThere.getBlock()
					&&slope.getFacing()==this.getFacing())
				out.accept(slope);
		}
	}

	private void updateNoNeighbours(BlockPos pos)
	{
		BlockState state = level.getBlockState(pos);
		level.sendBlockUpdated(pos, state, state, 3);
	}

	@Override
	public Direction getFacing()
	{
		if(this.facing!=null)
			return facing;
		else
			return IStateBasedDirectional.super.getFacing();
	}

	@Override
	public void setFacing(Direction facing)
	{
		IStateBasedDirectional.super.setFacing(facing);
		this.facing = null;
		totalLength = 1;
		slopePosition = 0;
		if(level!=null)
			level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return StructuralArmBlock.FACING;
	}

	@Override
	public Direction getFacingForPlacement(LivingEntity placer, BlockPos pos, Direction side, float hitX, float hitY,
										   float hitZ)
	{
		onCeiling = (side==DOWN)||(side!=UP&&hitY > .5);
		return IStateBasedDirectional.super.getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	private static final record ShapeKey(int slopePos, int slopeLength, boolean onCeiling)
	{
	}

	private static final CachedShapesWithTransform<ShapeKey, Direction> SHAPES =
			CachedShapesWithTransform.createDirectional(
					key -> getBounds(key.slopePos(), key.slopeLength(), key.onCeiling())
			);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(new ShapeKey(slopePosition, totalLength, onCeiling), getFacing());
	}

	private static List<AABB> getBounds(int slopePosition, int totalLength, boolean onCeiling)
	{
		double lowerH = (slopePosition+.5)/totalLength;
		double upperH = (slopePosition+1.)/totalLength;
		if(!onCeiling)
			return ImmutableList.of(
					new AABB(0, 0, 0, 1, lowerH, 1),
					new AABB(0, lowerH, 0, 1, upperH, .5)
			);
		else
			return ImmutableList.of(
					new AABB(0, 1-lowerH, 0, 1, 1, 1),
					new AABB(0, 1-upperH, 0, 1, 1-lowerH, .5)
			);
	}

	public int getSlopePosition()
	{
		return slopePosition;
	}

	public int getTotalLength()
	{
		return totalLength;
	}

	public boolean isOnCeiling()
	{
		return onCeiling;
	}
}
