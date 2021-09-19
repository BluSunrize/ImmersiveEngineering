/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.utils.PlayerUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder.FullChunkStatus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.mutable.MutableLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * @author BluSunrize - 17.08.2016
 * A handler for custom conveyor types
 */
public class ConveyorHandler
{
	public static final Map<ResourceLocation, Class<? extends IConveyorBelt>> classRegistry = new LinkedHashMap<>();
	public static final Map<ResourceLocation, Set<ResourceLocation>> substituteRegistry = new HashMap<>();
	public static final Map<ResourceLocation, Function<BlockEntity, ? extends IConveyorBelt>> functionRegistry = new LinkedHashMap<>();
	public static final Map<ResourceLocation, BlockEntityType<? extends BlockEntity>> tileEntities = new LinkedHashMap<>();
	public static final Map<Class<? extends IConveyorBelt>, ResourceLocation> reverseClassRegistry = new LinkedHashMap<>();
	public static final Set<BiConsumer<Entity, IConveyorTile>> magnetSuppressionFunctions = new HashSet<>();
	public static final Set<BiConsumer<Entity, IConveyorTile>> magnetSuppressionReverse = new HashSet<>();
	public static final SetRestrictedField<ItemAgeAccessor> ITEM_AGE_ACCESS = SetRestrictedField.common();

	public static final Map<ResourceLocation, Block> conveyorBlocks = new HashMap<>();
	public static final ResourceLocation textureConveyorColour = new ResourceLocation("immersiveengineering:block/conveyor/colour");

	// Should work for multiple dimensions since the calls aren't "interleaved" for multiple dimensions
	private static final IntSet entitiesHandledInCurrentTickClient = new IntOpenHashSet();
	private static final MutableLong currentTickClient = new MutableLong();
	private static final IntSet entitiesHandledInCurrentTickServer = new IntOpenHashSet();
	private static final MutableLong currentTickServer = new MutableLong();

	// Returns true iff the entity has not been handled before in this tick
	public static boolean markEntityAsHandled(Entity e)
	{
		MutableLong currentTick;
		IntSet entitiesHandledInCurrentTick;
		if(e.level.isClientSide)
		{
			currentTick = currentTickClient;
			entitiesHandledInCurrentTick = entitiesHandledInCurrentTickClient;
		}
		else
		{
			currentTick = currentTickServer;
			entitiesHandledInCurrentTick = entitiesHandledInCurrentTickServer;
		}
		long now = e.level.getGameTime();
		if(now!=currentTick.getValue())
		{
			currentTick.setValue(now);
			entitiesHandledInCurrentTick.clear();
		}
		return entitiesHandledInCurrentTick.add(e.getId());
	}

	/**
	 * @param key           A unique ResourceLocation to identify the conveyor by
	 * @param conveyorClass the conveyor class
	 * @param function      a function used to create a new instance. Note that the TileEntity may be null for the inventory model. Handle accordingly.
	 */
	public static <T extends IConveyorBelt> boolean registerConveyorHandler(ResourceLocation key, Class<T> conveyorClass, Function<BlockEntity, T> function)
	{
		if(classRegistry.containsKey(key))
			return false;
		classRegistry.put(key, conveyorClass);
		reverseClassRegistry.put(conveyorClass, key);
		functionRegistry.put(key, function);
		return true;
	}

	/**
	 * Registers a valid substitute for the given key conveyor. This substitute is allowed in the construction of multiblocks in place of the key
	 *
	 * @param key        A unique ResourceLocation to identify the conveyor by
	 * @param substitute A unique ResourceLocation to identify the substitute
	 */
	//TODO this is probably broken?
	public static void registerSubstitute(ResourceLocation key, ResourceLocation substitute)
	{
		Set<ResourceLocation> registeredSubstitutes = substituteRegistry.computeIfAbsent(key, k -> new HashSet<>());
		registeredSubstitutes.add(substitute);
	}

	/**
	 * @return a new instance of the given conveyor type
	 */
	public static IConveyorBelt getConveyor(ResourceLocation key, @Nullable BlockEntity tile)
	{
		if(tile instanceof IConveyorTile)
		{
			IConveyorBelt fromTile = ((IConveyorTile)tile).getConveyorSubtype();
			if(fromTile!=null)
				return fromTile;
		}
		Function<BlockEntity, ? extends IConveyorBelt> func = functionRegistry.get(key);
		if(func!=null)
			return func.apply(tile);
		return null;
	}

	public static BlockEntityType<? extends BlockEntity> getTEType(ResourceLocation typeName)
	{
		return tileEntities.get(typeName);
	}

	public static ResourceLocation getRegistryNameFor(ResourceLocation conveyorLoc)
	{
		String path;
		if(Lib.MODID.equals(conveyorLoc.getNamespace()))
			path = conveyorLoc.getPath();
		else
			path = conveyorLoc.getNamespace()+"_"+conveyorLoc.getPath();
		return new ResourceLocation(Lib.MODID, "conveyor_"+path);
	}

	public static ResourceLocation getType(Block b)
	{
		if(b instanceof IConveyorBlock)
			return ((IConveyorBlock)b).getTypeName();
		else
			return null;
	}

	public static Block getBlock(ResourceLocation typeName)
	{
		return conveyorBlocks.get(typeName);
	}

	/**
	 * @return whether the given subtype key can be found at the location. Useful for multiblocks
	 */
	public static boolean isConveyor(Level world, BlockPos pos, @Nonnull String key, @Nullable Direction facing)
	{
		BlockEntity tile = world.getBlockEntity(pos);
		if(!(tile instanceof IConveyorTile))
			return false;
		if(facing!=null&&!facing.equals(((IConveyorTile)tile).getFacing()))
			return false;
		IConveyorBelt conveyor = ((IConveyorTile)tile).getConveyorSubtype();
		if(conveyor==null)
			return false;
		ResourceLocation rl = reverseClassRegistry.get(conveyor.getClass());
		if(rl==null)
			return false;
		ResourceLocation rlKey = new ResourceLocation(key);
		if(key.equalsIgnoreCase(rl.toString()))
			return true;
		else if(substituteRegistry.containsKey(rlKey))
			return substituteRegistry.get(rlKey).contains(rl);
		return false;
	}

	/**
	 * registers a consumer/function to suppress magnets while they are on the conveyors
	 * the reversal function is optional, to revert possible NBT changes
	 * the tileentity parsed is an instanceof
	 */
	public static void registerMagnetSuppression(BiConsumer<Entity, IConveyorTile> function, @Nullable BiConsumer<Entity, IConveyorTile> revert)
	{
		magnetSuppressionFunctions.add(function);
		if(revert!=null)
			magnetSuppressionReverse.add(revert);
	}

	/**
	 * applies all registered magnets suppressors to the entity
	 */
	public static void applyMagnetSuppression(Entity entity, IConveyorTile tile)
	{
		if(entity!=null)
			for(BiConsumer<Entity, IConveyorTile> func : magnetSuppressionFunctions)
				func.accept(entity, tile);
	}

	/**
	 * applies all registered magnet suppression removals
	 */
	public static void revertMagnetSuppression(Entity entity, IConveyorTile tile)
	{
		if(entity!=null)
			for(BiConsumer<Entity, IConveyorTile> func : magnetSuppressionReverse)
				func.accept(entity, tile);
	}

	/**
	 * An interface for the external handling of conveyorbelts
	 */
	public interface IConveyorBelt
	{
		/**
		 * @return the string by which unique models would be cached. Override for additional appended information*
		 * The model class will also append to this key for rendered walls and facing
		 */
		default String getModelCacheKey()
		{
			String key = reverseClassRegistry.get(this.getClass()).toString();
			key += "f"+getFacing().ordinal();
			key += "d"+getConveyorDirection().ordinal();
			key += "a"+(isActive()?1: 0);
			key += "w0"+(renderWall(getFacing(), 0)?1: 0);
			key += "w1"+(renderWall(getFacing(), 1)?1: 0);
			key += "c"+getDyeColour();
			return key;
		}

		BlockEntity getTile();

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
		 * @return true if the conveyor can be dyed
		 */
		boolean canBeDyed();

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
		 * @param wall 0 is left, 1 is right
		 * @return whether the wall should be drawn on the model. Also used for they cache key
		 */
		default boolean renderWall(Direction facing, int wall)
		{
			if(getConveyorDirection()!=ConveyorDirection.HORIZONTAL)
				return true;
			Direction side = wall==0?facing.getCounterClockWise(): facing.getClockWise();
			BlockPos pos = getTile().getBlockPos().relative(side);
			BlockEntity te = SafeChunkUtils.getSafeTE(getTile().getLevel(), pos);
			if(te instanceof IConveyorAttachable)
			{
				boolean b = false;
				for(Direction f : ((IConveyorAttachable)te).sigOutputDirections())
					if(f==side.getOpposite())
						b = true;
					else if(f==Direction.UP)
						b = false;
				return !b;
			}
			else
			{
				te = SafeChunkUtils.getSafeTE(getTile().getLevel(), pos.offset(0, -1, 0));
				if(te instanceof IConveyorAttachable)
				{
					int b = 0;
					for(Direction f : ((IConveyorAttachable)te).sigOutputDirections())
						if(f==side.getOpposite())
							b++;
						else if(f==Direction.UP)
							b++;
					return b < 2;
				}
			}
			return true;
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
			BlockPos pos = getTile().getBlockPos();

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
			BlockPos pos = getTile().getBlockPos();
			final double relativeHeight = entity.getY()-pos.getY();
			if(relativeHeight >= 0&&relativeHeight < heightLimit)
			{
				boolean hasBeenHandled = !markEntityAsHandled(entity);
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
				Level w = Preconditions.checkNotNull(getTile().getLevel());
				BlockPos upPos = pos.relative(getFacing()).above();
				if(contact&&conveyorDirection==ConveyorDirection.UP&&
						!Block.isFaceFull(w.getBlockState(upPos).getShape(w, upPos), Direction.DOWN))
				{
					double move = .4;
					entity.setPos(entity.getX()+move*getFacing().getStepX(), entity.getY()+1*move, entity.getZ()+move*getFacing().getStepZ());
				}
				if(!contact)
					ConveyorHandler.applyMagnetSuppression(entity, (IConveyorTile)getTile());
				else
				{
					BlockPos nextPos = getTile().getBlockPos().relative(getFacing());
					if(!(SafeChunkUtils.getSafeTE(getTile().getLevel(), nextPos) instanceof IConveyorTile))
						ConveyorHandler.revertMagnetSuppression(entity, (IConveyorTile)getTile());
				}

				// In the first tick this could be an entity the conveyor belt just dropped, causing #3023
				if(entity instanceof ItemEntity&&entity.tickCount > 1)
				{
					ItemEntity item = (ItemEntity)entity;
					if(!contact)
					{
						ItemAgeAccessor access = ITEM_AGE_ACCESS.getValue();
						if(access.getAgeNonsided(item) > item.lifespan-60*20&&!outputBlocked)
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
			Level level = Objects.requireNonNull(getTile().getLevel());
			for(BlockPos pos : getNextConveyorCandidates())
			{
				LevelChunk chunk = SafeChunkUtils.getSafeChunk(level, pos);
				// Do not export entities into non-ticking chunks, where they would pile up at the boundary
				if(chunk==null||(!level.isClientSide&&!chunk.getFullStatus().isOrAfter(FullChunkStatus.ENTITY_TICKING)))
					return true;
				BlockEntity outputTile = chunk.getBlockEntity(pos);
				if(outputTile instanceof IConveyorTile)
					return ((IConveyorTile)outputTile).getConveyorSubtype().isBlocked();
			}
			return false;
		}

		/**
		 * Called when an item is inserted into the conveyor and deployed as an entity
		 */
		default void onItemDeployed(ItemEntity entity)
		{
			ConveyorHandler.applyMagnetSuppression(entity, (IConveyorTile)getTile());
		}

		default void handleInsertion(ItemEntity entity, ConveyorDirection conDir, double distX, double distZ)
		{
			BlockPos invPos = getOutputInventory();
			Level world = getTile().getLevel();
			boolean contact = getFacing().getAxis()==Axis.Z?distZ < .7: distX < .7;
			BlockEntity inventoryTile = SafeChunkUtils.getSafeTE(world, invPos);
			if(!contact||inventoryTile instanceof IConveyorTile)
				return;

			LazyOptional<IItemHandler> cap = CapabilityUtils.findItemHandlerAtPos(world, invPos, getFacing().getOpposite(), true);
			cap.ifPresent(itemHandler -> {
				ItemStack stack = entity.getItem();
				ItemStack temp = ItemHandlerHelper.insertItem(itemHandler, stack.copy(), true);
				if(temp.isEmpty()||temp.getCount() < stack.getCount())
				{
					temp = ItemHandlerHelper.insertItem(itemHandler, stack, false);
					if(temp.isEmpty())
						entity.remove();
					else if(temp.getCount() < stack.getCount())
						entity.setItem(temp);
				}
			});
		}

		default BlockPos getOutputInventory()
		{
			ConveyorDirection conDir = getConveyorDirection();
			return getTile().getBlockPos()
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
				BlockEntity outputTile = SafeChunkUtils.getSafeTE(getTile().getLevel(), pos);
				if(outputTile instanceof IConveyorTile)
					return ((IConveyorTile)outputTile).getConveyorSubtype();
			}
			return null;
		}

		default boolean isTicking()
		{
			return false;
		}

		default void onUpdate()
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

		@OnlyIn(Dist.CLIENT)
		default Transformation modifyBaseRotationMatrix(Transformation matrix)
		{
			return matrix;
		}

		@OnlyIn(Dist.CLIENT)
		ResourceLocation getActiveTexture();

		@OnlyIn(Dist.CLIENT)
		ResourceLocation getInactiveTexture();

		@OnlyIn(Dist.CLIENT)
		default ResourceLocation getColouredStripesTexture()
		{
			return textureConveyorColour;
		}

		@OnlyIn(Dist.CLIENT)
		default List<BakedQuad> modifyQuads(List<BakedQuad> baseModel)
		{
			return baseModel;
		}
	}

	public enum ConveyorDirection
	{
		HORIZONTAL,
		UP,
		DOWN
	}

	/**
	 * An interface to prevent conveyors from rendering a wall in the direction of this tile
	 */
	public interface IConveyorAttachable
	{
		Direction getFacing();

		/**
		 * @return a rough indication of where this block will output things. Will determine if attached conveyors render a wall in the opposite direction
		 */
		Direction[] sigOutputDirections();
	}

	/**
	 * This interface solely exists to mark a tile as conveyor, and have it ignored for insertion
	 */
	public interface IConveyorTile extends IConveyorAttachable
	{
		IConveyorBelt getConveyorSubtype();

		@Override
		default Direction[] sigOutputDirections()
		{
			IConveyorBelt subtype = getConveyorSubtype();
			if(subtype!=null)
				return subtype.sigTransportDirections();
			return new Direction[0];
		}
	}

	public interface IConveyorBlock
	{
		ResourceLocation getTypeName();
	}

	public interface ItemAgeAccessor
	{
		int getAgeNonsided(ItemEntity entity);

		void setAge(ItemEntity entity, int newAge);
	}
}