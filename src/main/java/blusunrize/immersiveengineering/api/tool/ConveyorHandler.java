/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBeltTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * @author BluSunrize - 17.08.2016
 * A handler for custom conveyor types
 */
public class ConveyorHandler
{
	public static final Map<ResourceLocation, Class<? extends IConveyorBelt>> classRegistry = new LinkedHashMap<ResourceLocation, Class<? extends IConveyorBelt>>();
	public static final Map<ResourceLocation, Set<ResourceLocation>> substituteRegistry = new HashMap<>();
	public static final Map<ResourceLocation, Function<TileEntity, ? extends IConveyorBelt>> functionRegistry = new LinkedHashMap<ResourceLocation, Function<TileEntity, ? extends IConveyorBelt>>();
	public static final Map<ResourceLocation, TileEntityType<? extends TileEntity>> tileEntities = new LinkedHashMap<>();
	public static final Map<Class<? extends IConveyorBelt>, ResourceLocation> reverseClassRegistry = new LinkedHashMap<Class<? extends IConveyorBelt>, ResourceLocation>();
	public static final Set<BiConsumer<Entity, IConveyorTile>> magnetSupressionFunctions = new HashSet<BiConsumer<Entity, IConveyorTile>>();
	public static final Set<BiConsumer<Entity, IConveyorTile>> magnetSupressionReverse = new HashSet<BiConsumer<Entity, IConveyorTile>>();

	public static final Map<ResourceLocation, Block> conveyorBlocks = MetalDevices.CONVEYORS;
	public static final ResourceLocation textureConveyorColour = new ResourceLocation("immersiveengineering:blocks/conveyor_colour");

	/**
	 * @param key           A unique ResourceLocation to identify the conveyor by
	 * @param conveyorClass the conveyor class
	 * @param function      a function used to create a new instance. Note that the TileEntity may be null for the inventory model. Handle accordingly.
	 */
	public static <T extends IConveyorBelt> boolean registerConveyorHandler(ResourceLocation key, Class<T> conveyorClass, Function<TileEntity, T> function)
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
	 * @param key			A unique ResourceLocation to identify the conveyor by
	 * @param substitute	A unique ResourceLocation to identify the substitute
	 */
	public static void registerSubstitute(ResourceLocation key, ResourceLocation substitute)
	{
		Set<ResourceLocation> registeredSubstitutes = substituteRegistry.computeIfAbsent(key, k -> new HashSet<>());
		registeredSubstitutes.add(substitute);
	}

	/**
	 * @return a new instance of the given conveyor type
	 */
	public static IConveyorBelt getConveyor(ResourceLocation key, @Nullable TileEntity tile)
	{
		Function<TileEntity, ? extends IConveyorBelt> func = functionRegistry.get(key);
		if(func!=null)
			return func.apply(tile);
		return null;
	}

	public static void registerConveyorTEs(RegistryEvent.Register<TileEntityType<?>> evt)
	{
		for(ResourceLocation rl : classRegistry.keySet())
		{
			TileEntityType<ConveyorBeltTileEntity> te = new TileEntityType<>(() -> new ConveyorBeltTileEntity(rl),
					ImmutableSet.of(conveyorBlocks.get(rl)),
					null);
			te.setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, "conveyor_"
					+rl.toString().replace(':', '_')));
			tileEntities.put(rl, te);
			evt.getRegistry().register(te);
		}
	}

	public static TileEntityType<? extends TileEntity> getTEType(ResourceLocation typeName)
	{
		return tileEntities.get(typeName);
	}

	public static void registerConveyorBlocks(RegistryEvent.Register<Block> evt)
	{
		for(ResourceLocation rl : classRegistry.keySet())
		{
			Block b = new ConveyorBlock(rl);
			evt.getRegistry().register(b);
			conveyorBlocks.put(rl, b);
		}
	}

	public static Block getBlock(ResourceLocation typeName)
	{
		return conveyorBlocks.get(typeName);
	}

	/**
	 * @return whether the given subtype key can be found at the location. Useful for multiblocks
	 */
	public static boolean isConveyor(World world, BlockPos pos, @Nonnull String key, @Nullable Direction facing)
	{
		TileEntity tile = world.getTileEntity(pos);
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
	public static void registerMagnetSupression(BiConsumer<Entity, IConveyorTile> function, @Nullable BiConsumer<Entity, IConveyorTile> revert)
	{
		magnetSupressionFunctions.add(function);
		if(revert!=null)
			magnetSupressionReverse.add(revert);
	}

	/**
	 * applies all registered magnets supressors to the entity
	 */
	public static void applyMagnetSupression(Entity entity, IConveyorTile tile)
	{
		if(entity!=null)
			for(BiConsumer<Entity, IConveyorTile> func : magnetSupressionFunctions)
				func.accept(entity, tile);
	}

	/**
	 * applies all registered magnet supression removals
	 */
	public static void revertMagnetSupression(Entity entity, IConveyorTile tile)
	{
		if(entity!=null)
			for(BiConsumer<Entity, IConveyorTile> func : magnetSupressionReverse)
				func.accept(entity, tile);
	}

	/**
	 * An interface for the external handling of conveyorbelts
	 */
	//TODO redesign the interface to make it either a singleton or obviously link each instance to a fixed TE
	public interface IConveyorBelt
	{
		/**
		 * @return the string by which unique models would be cached. Override for additional appended information*
		 * The model class will also append to this key for rendered walls and facing
		 */
		default String getModelCacheKey(TileEntity tile, Direction facing)
		{
			String key = reverseClassRegistry.get(this.getClass()).toString();
			key += "f"+facing.ordinal();
			key += "d"+getConveyorDirection().ordinal();
			key += "a"+(isActive(tile)?1: 0);
			key += "w0"+(renderWall(tile, facing, 0)?1: 0);
			key += "w1"+(renderWall(tile, facing, 1)?1: 0);
			key += "c"+getDyeColour();
			return key;
		}

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
		boolean isActive(TileEntity tile);

		/**
		 * @return true if the conveyor can be dyed
		 */
		boolean canBeDyed();

		/**
		 * sets the colour of the conveyor when rightclicked with a dye
		 * parsed value is a hex RGB
		 *
		 * @return true if renderupdate should happen
		 * @param colour
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
		default boolean playerInteraction(TileEntity tile, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
		{
			return false;
		}

		/**
		 * @param wall 0 is left, 1 is right
		 * @return whether the wall should be drawn on the model. Also used for they cache key
		 */
		default boolean renderWall(TileEntity tile, Direction facing, int wall)
		{
			if(getConveyorDirection()!=ConveyorDirection.HORIZONTAL)
				return true;
			Direction side = wall==0?facing.rotateYCCW(): facing.rotateY();
			BlockPos pos = tile.getPos().offset(side);
			TileEntity te = Utils.getExistingTileEntity(tile.getWorld(), pos);
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
				te = Utils.getExistingTileEntity(tile.getWorld(), pos.add(0, -1, 0));
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
		default Direction[] sigTransportDirections(TileEntity conveyorTile, Direction facing)
		{
			if(getConveyorDirection()==ConveyorDirection.UP)
				return new Direction[]{facing, Direction.UP};
			else if(getConveyorDirection()==ConveyorDirection.DOWN)
				return new Direction[]{facing, Direction.DOWN};
			return new Direction[]{facing};
		}

		/**
		 * @return a vector representing the movement applied to the entity
		 */
		default Vec3d getDirection(TileEntity conveyorTile, Entity entity, Direction facing)
		{
			ConveyorDirection conveyorDirection = getConveyorDirection();
			BlockPos pos = conveyorTile.getPos();

			double vBase = 1.15;
			double vX = 0.1*vBase*facing.getXOffset();
			double vY = entity.getMotion().y;
			double vZ = 0.1*vBase*facing.getZOffset();

			if(conveyorDirection==ConveyorDirection.UP)
				vY = 0.17D*vBase;
			else if(conveyorDirection==ConveyorDirection.DOWN)
				vY = -0.07*vBase;

			if(conveyorDirection!=ConveyorDirection.HORIZONTAL)
				entity.onGround = false;

			if(facing==Direction.WEST||facing==Direction.EAST)
			{
				if(entity.posZ > pos.getZ()+0.55D)
					vZ = -0.1D*vBase;
				else if(entity.posZ < pos.getZ()+0.45D)
					vZ = 0.1D*vBase;
			}
			else if(facing==Direction.NORTH||facing==Direction.SOUTH)
			{
				if(entity.posX > pos.getX()+0.55D)
					vX = -0.1D*vBase;
				else if(entity.posX < pos.getX()+0.45D)
					vX = 0.1D*vBase;
			}

			return new Vec3d(vX, vY, vZ);
		}

		default void onEntityCollision(TileEntity tile, Entity entity, Direction facing)
		{
			if(!isActive(tile))
				return;
			BlockPos pos = tile.getPos();
			ConveyorDirection conveyorDirection = getConveyorDirection();
			float heightLimit = conveyorDirection==ConveyorDirection.HORIZONTAL?.25f: 1f;
			if(entity!=null&&entity.isAlive()&&!(entity instanceof PlayerEntity&&entity.isSneaking())&&entity.posY-pos.getY() >= 0&&entity.posY-pos.getY() < heightLimit)
			{
				Vec3d vec = this.getDirection(tile, entity, facing);
				if(entity.fallDistance < 3)
					entity.fallDistance = 0;
				entity.setMotion(vec);
				double distX = Math.abs(pos.offset(facing).getX()+.5-entity.posX);
				double distZ = Math.abs(pos.offset(facing).getZ()+.5-entity.posZ);
				double treshold = .9;
				boolean contact = facing.getAxis()==Axis.Z?distZ < treshold: distX < treshold;
				World w = tile.getWorld();
				BlockPos upPos = pos.offset(facing).up();
				if(contact&&conveyorDirection==ConveyorDirection.UP&&
						!Block.doesSideFillSquare(w.getBlockState(upPos).getShape(w, upPos), Direction.DOWN))
				{
					double move = .4;
					entity.setPosition(entity.posX+move*facing.getXOffset(), entity.posY+1*move, entity.posZ+move*facing.getZOffset());
				}
				if(!contact)
					ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
				else
				{
					BlockPos nextPos = tile.getPos().offset(facing);
					if(!(Utils.getExistingTileEntity(tile.getWorld(), nextPos) instanceof IConveyorTile))
						ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile);
				}

				// In the first tick this could be an entity the conveyor belt just dropped, causing #3023
				if(entity instanceof ItemEntity&&entity.ticksExisted > 1)
				{
					ItemEntity item = (ItemEntity)entity;
					if(!contact)
					{
						if(item.getAge() > item.lifespan-60*20)
							item.setAgeToCreativeDespawnTime();
					}
					else
						handleInsertion(tile, item, facing, conveyorDirection, distX, distZ);
				}
			}
		}

		/**
		 * Called when an item is inserted into the conveyor and deployed as an entity
		 */
		default void onItemDeployed(TileEntity tile, ItemEntity entity, Direction facing)
		{
		}

		default void handleInsertion(TileEntity tile, ItemEntity entity, Direction facing, ConveyorDirection conDir, double distX, double distZ)
		{
			BlockPos invPos = tile.getPos().offset(facing).add(0, (conDir==ConveyorDirection.UP?1: conDir==ConveyorDirection.DOWN?-1: 0), 0);
			World world = tile.getWorld();
			TileEntity inventoryTile = Utils.getExistingTileEntity(world, invPos);
			boolean contact = facing.getAxis()==Axis.Z?distZ < .7: distX < .7;
			if(!tile.getWorld().isRemote)
			{
				if(contact&&inventoryTile!=null&&!(inventoryTile instanceof IConveyorTile))
				{
					ItemStack stack = entity.getItem();
					if(!stack.isEmpty())
					{
						ItemStack ret = ApiUtils.insertStackIntoInventory(inventoryTile, stack, facing.getOpposite());
						if(ret.isEmpty())
							entity.remove();
						else if(ret.getCount() < stack.getCount())
							entity.setItem(ret);
					}
				}
			}

		}

		default boolean isTicking(TileEntity tile)
		{
			return false;
		}

		default void onUpdate(TileEntity tile, Direction facing)
		{
		}

		AxisAlignedBB conveyorBounds = new AxisAlignedBB(0, 0, 0, 1, .125f, 1);
		AxisAlignedBB highConveyorBounds = new AxisAlignedBB(0, 0, 0, 1, 1.125f, 1);
		AxisAlignedBB FULL_BLOCK = new AxisAlignedBB(0, 0, 0, 1, 1, 1);

		default List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, Direction facing)
		{
			return getConveyorDirection()==ConveyorDirection.HORIZONTAL?Lists.newArrayList(conveyorBounds): Lists.newArrayList(highConveyorBounds);
		}

		default List<AxisAlignedBB> getColisionBoxes(TileEntity tile, Direction facing)
		{
			return Lists.newArrayList(conveyorBounds);
		}

		CompoundNBT writeConveyorNBT();

		void readConveyorNBT(CompoundNBT nbt);

		@OnlyIn(Dist.CLIENT)
		default Matrix4f modifyBaseRotationMatrix(Matrix4f matrix, @Nullable TileEntity tile, Direction facing)
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
		default List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, Direction facing)
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
				return subtype.sigTransportDirections((TileEntity)this, this.getFacing());
			return new Direction[0];
		}
	}
}