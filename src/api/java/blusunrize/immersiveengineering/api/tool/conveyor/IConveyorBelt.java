/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.conveyor;

import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorBlockEntity;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ItemAgeAccessor;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.utils.PlayerUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * An interface for the external handling of conveyorbelts
 */
public interface IConveyorBelt
{
	BlockEntity getBlockEntity();

	Direction getFacing();

	/**
	 * @return the transport direction; HORIZONTAL for flat conveyors, UP and DOWN for diagonals
	 */
	default ConveyorDirection getConveyorDirection()
	{
		return ConveyorDirection.HORIZONTAL;
	}

	/**
	 * Switch to the next possible ConveyorDirection
	 *
	 * @return true if renderupdate should happen
	 */
	boolean changeConveyorDirection();

	/**
	 * Set the ConveyorDirection to given
	 *
	 * @return false if the direction is not possible for this conveyor
	 */
	boolean setConveyorDirection(ConveyorDirection dir);

	/**
	 * Called after the conveyor has been rotated with a hammer
	 */
	default void afterRotation(Direction oldDir, Direction newDir)
	{
	}

	/**
	 * @return false if the conveyor is deactivated (for instance by a redstone signal)
	 */
	boolean isActive();

	/**
	 * sets the colour of the conveyor when rightclicked with a dye
	 * parsed value is a hex RGB
	 *
	 * @param colour
	 * @return true if renderupdate should happen
	 */
	boolean setDyeColour(DyeColor colour);

	/**
	 * @return the dyed colour
	 */
	@Nullable
	DyeColor getDyeColour();

	/**
	 * when the player rightclicks the block, after direction changes or dye have been handled
	 *
	 * @return true if anything happened, cancelling item use
	 */
	default boolean playerInteraction(Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		return false;
	}

	/**
	 * a rough indication of where this conveyor will transport things. Relevant for vertical conveyors, to see if they need to render the groundpiece below them.
	 */
	default Direction[] sigTransportDirections()
	{
		if(getConveyorDirection()==ConveyorDirection.UP)
			return new Direction[]{getFacing(), Direction.UP};
		else if(getConveyorDirection()==ConveyorDirection.DOWN)
			return new Direction[]{getFacing(), Direction.DOWN};
		return new Direction[]{getFacing()};
	}

	/**
	 * @return a vector representing the movement applied to the entity
	 */
	default Vec3 getDirection(Entity entity, boolean outputBlocked)
	{
		ConveyorDirection conveyorDirection = getConveyorDirection();
		BlockPos pos = getBlockEntity().getBlockPos();

		final Direction facing = getFacing();
		double vBase = 1.15;
		double vX = 0.1*vBase*facing.getStepX();
		double vY = entity.getDeltaMovement().y;
		double vZ = 0.1*vBase*facing.getStepZ();

		if(facing==Direction.WEST||facing==Direction.EAST)
		{
			if(entity.getZ() > pos.getZ()+0.55D)
				vZ = -0.1D*vBase;
			else if(entity.getZ() < pos.getZ()+0.45D)
				vZ = 0.1D*vBase;
		}
		else if(facing==Direction.NORTH||facing==Direction.SOUTH)
		{
			if(entity.getX() > pos.getX()+0.55D)
				vX = -0.1D*vBase;
			else if(entity.getX() < pos.getX()+0.45D)
				vX = 0.1D*vBase;
		}

		if(conveyorDirection!=ConveyorDirection.HORIZONTAL)
		{
			// Attempt to fix entity to the highest point under it
			final Vec3 centerRelative = entity.position()
					.subtract(new Vec3(pos.getX(), pos.getY(), pos.getZ()))
					.subtract(0.5+vX, 0.5, 0.5+vZ);
			final double conveyorHeight = 2/16.;
			final Vec3i directionVector = facing.getNormal();
			final double centerOffsetInDirection = centerRelative.dot(new Vec3(directionVector.getX(), directionVector.getY(), directionVector.getZ()));
			final double radius = entity.getDimensions(entity.getPose()).width/2;
			final double maxEntityPos = centerOffsetInDirection+radius;
			double maxCenterHeightUnderEntity = maxEntityPos+conveyorHeight;
			if(conveyorDirection==ConveyorDirection.DOWN)
				maxCenterHeightUnderEntity = -maxCenterHeightUnderEntity;
			if(conveyorDirection==ConveyorDirection.UP)
			{
				if(maxCenterHeightUnderEntity > centerRelative.y||!outputBlocked)
					vY = 0.17D*vBase;
			}
			else
				vY = Math.signum(maxCenterHeightUnderEntity-centerRelative.y)*0.07*vBase;
			entity.setOnGround(false);
		}

		return new Vec3(vX, vY, vZ);
	}

	default void onEntityCollision(@Nonnull Entity entity)
	{
		if(!isActive()||!entity.isAlive())
			return;
		if(entity instanceof Player&&entity.isShiftKeyDown())
			return;
		PlayerUtils.resetFloatingState(entity);
		ConveyorDirection conveyorDirection = getConveyorDirection();
		float heightLimit = conveyorDirection==ConveyorDirection.HORIZONTAL?.25f: 1f;
		BlockPos pos = getBlockEntity().getBlockPos();
		final double relativeHeight = entity.getY()-pos.getY();
		if(relativeHeight >= 0&&relativeHeight < heightLimit)
		{
			boolean hasBeenHandled = !ConveyorHandler.markEntityAsHandled(entity);
			final boolean outputBlocked = isOutputBlocked();
			Vec3 vec = this.getDirection(entity, outputBlocked);
			if(entity.fallDistance < 3)
				entity.fallDistance = 0;
			if(outputBlocked)
			{
				double replacementX;
				double replacementZ;
				if(hasBeenHandled)
				{
					replacementX = entity.getDeltaMovement().x;
					replacementZ = entity.getDeltaMovement().z;
				}
				else
				{
					replacementX = 0;
					replacementZ = 0;
				}
				vec = new Vec3(
						replacementX,
						vec.y,
						replacementZ
				);
			}
			entity.setDeltaMovement(vec);
			double distX = Math.abs(pos.relative(getFacing()).getX()+.5-entity.getX());
			double distZ = Math.abs(pos.relative(getFacing()).getZ()+.5-entity.getZ());
			double threshold = .9;
			boolean contact = getFacing().getAxis()==Axis.Z?distZ < threshold: distX < threshold;
			Level w = Preconditions.checkNotNull(getBlockEntity().getLevel());
			BlockPos upPos = pos.relative(getFacing()).above();
			if(contact&&conveyorDirection==ConveyorDirection.UP&&
					!Block.isFaceFull(w.getBlockState(upPos).getShape(w, upPos), Direction.DOWN))
			{
				double move = .4;
				entity.setPos(entity.getX()+move*getFacing().getStepX(), entity.getY()+1*move, entity.getZ()+move*getFacing().getStepZ());
			}
			if(!contact)
				ConveyorHandler.applyMagnetSuppression(entity, (IConveyorBlockEntity<?>)getBlockEntity());
			else
			{
				BlockPos nextPos = getBlockEntity().getBlockPos().relative(getFacing());
				if(!(SafeChunkUtils.getSafeBE(getBlockEntity().getLevel(), nextPos) instanceof IConveyorBlockEntity))
					ConveyorHandler.revertMagnetSuppression(entity, (IConveyorBlockEntity<?>)getBlockEntity());
			}

			// In the first tick this could be an entity the conveyor belt just dropped, causing #3023
			if(entity instanceof ItemEntity item&&entity.tickCount > 1)
			{
				if(!contact)
				{
					ItemAgeAccessor access = ConveyorHandler.ITEM_AGE_ACCESS.getValue();
					if(item.getAge() > item.lifespan-60*20&&!outputBlocked)
						access.setAge(item, item.lifespan-60*20);
				}
				else if(!w.isClientSide)
					handleInsertion(item, conveyorDirection, distX, distZ);
			}
		}
	}

	default boolean isBlocked()
	{
		return false;
	}

	default boolean isOutputBlocked()
	{
		Level level = Objects.requireNonNull(getBlockEntity().getLevel());
		for(BlockPos pos : getNextConveyorCandidates())
		{
			LevelChunk chunk = SafeChunkUtils.getSafeChunk(level, pos);
			// Do not export entities into non-ticking chunks, where they would pile up at the boundary
			if(chunk==null||(!level.isClientSide&&!chunk.getFullStatus().isOrAfter(FullChunkStatus.ENTITY_TICKING)))
				return true;
			BlockEntity outputTile = chunk.getBlockEntity(pos);
			if(outputTile instanceof IConveyorBlockEntity<?> convOut)
				return convOut.getConveyorInstance().isBlocked();
		}
		return false;
	}

	/**
	 * Called when an item is inserted into the conveyor and deployed as an entity
	 */
	default void onItemDeployed(ItemEntity entity)
	{
		ConveyorHandler.applyMagnetSuppression(entity, (IConveyorBlockEntity<?>)getBlockEntity());
	}

	default void handleInsertion(ItemEntity entity, ConveyorDirection conDir, double distX, double distZ)
	{
		BlockPos invPos = getOutputInventory();
		Level world = getBlockEntity().getLevel();
		boolean contact = getFacing().getAxis()==Axis.Z?distZ < .7: distX < .7;
		BlockEntity inventoryTile = SafeChunkUtils.getSafeBE(world, invPos);
		if(!contact||inventoryTile instanceof IConveyorBlockEntity)
			return;

		ItemUtils.tryInsertEntity(world, invPos, getFacing().getOpposite(), entity);
	}

	default BlockPos getOutputInventory()
	{
		ConveyorDirection conDir = getConveyorDirection();
		return getBlockEntity().getBlockPos()
				.relative(getFacing())
				.offset(0, (conDir==ConveyorDirection.UP?1: conDir==ConveyorDirection.DOWN?-1: 0), 0);
	}

	default List<BlockPos> getNextConveyorCandidates()
	{
		ConveyorDirection conDir = getConveyorDirection();
		BlockPos basePos = getOutputInventory();
		// Up and horizontal conveyors: handles changes to down
		// Down conveyors: Handles changes to horizontal
		BlockPos alternative = conDir==ConveyorDirection.DOWN?basePos.above(): basePos.below();
		return ImmutableList.of(
				basePos,
				alternative
		);
	}

	@Nullable
	default IConveyorBelt getOutputConveyor()
	{
		for(BlockPos pos : getNextConveyorCandidates())
		{
			BlockEntity outputTile = SafeChunkUtils.getSafeBE(getBlockEntity().getLevel(), pos);
			if(outputTile instanceof IConveyorBlockEntity<?> convOut)
				return convOut.getConveyorInstance();
		}
		return null;
	}

	default void tickServer()
	{
	}

	VoxelShape conveyorBounds = Shapes.box(0, 0, 0, 1, .125f, 1);
	VoxelShape highConveyorBounds = Shapes.box(0, 0, 0, 1, 1.125f, 1);
	VoxelShape FULL_BLOCK = Shapes.box(0, 0, 0, 1, 1, 1);

	default VoxelShape getSelectionShape()
	{
		return getConveyorDirection()==ConveyorDirection.HORIZONTAL?conveyorBounds: highConveyorBounds;
	}

	default VoxelShape getCollisionShape()
	{
		return conveyorBounds;
	}

	CompoundTag writeConveyorNBT();

	void readConveyorNBT(CompoundTag nbt);

	IConveyorType<?> getType();

	Block getCover();

	static Block getCoverOrDefault(@Nullable IConveyorBelt belt, Block fallback)
	{
		return belt!=null?belt.getCover(): fallback;
	}

	static boolean isCovered(@Nullable IConveyorBelt belt, Block fallback)
	{
		return getCoverOrDefault(belt, fallback)!=Blocks.AIR;
	}

	void setCover(Block cover);
}
