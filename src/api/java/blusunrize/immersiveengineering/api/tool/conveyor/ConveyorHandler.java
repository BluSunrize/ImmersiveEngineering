/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.conveyor;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
	private static final Map<ResourceLocation, IConveyorType<?>> typeRegistry = new LinkedHashMap<>();
	private static final Set<BiConsumer<Entity, IConveyorBlockEntity<?>>> magnetSuppressionFunctions = new HashSet<>();
	private static final Set<BiConsumer<Entity, IConveyorBlockEntity<?>>> magnetSuppressionReverse = new HashSet<>();
	public static final SetRestrictedField<ItemAgeAccessor> ITEM_AGE_ACCESS = SetRestrictedField.common();

	public static final SetRestrictedField<Function<IConveyorType<?>, Block>> CONVEYOR_BLOCKS = SetRestrictedField.common();
	public static final SetRestrictedField<Function<IConveyorType<?>, BlockEntityType<?>>> BLOCK_ENTITY_TYPES = SetRestrictedField.common();
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
		if(e.level().isClientSide)
		{
			currentTick = currentTickClient;
			entitiesHandledInCurrentTick = entitiesHandledInCurrentTickClient;
		}
		else
		{
			currentTick = currentTickServer;
			entitiesHandledInCurrentTick = entitiesHandledInCurrentTickServer;
		}
		long now = e.level().getGameTime();
		if(now!=currentTick.getValue())
		{
			currentTick.setValue(now);
			entitiesHandledInCurrentTick.clear();
		}
		return entitiesHandledInCurrentTick.add(e.getId());
	}

	public static boolean registerConveyorType(IConveyorType<?> type)
	{
		ResourceLocation key = type.getId();
		if(typeRegistry.containsKey(key))
			return false;
		typeRegistry.put(key, type);
		return true;
	}

	/**
	 * @return a new instance of the given conveyor type
	 */
	public static <T extends IConveyorBelt>
	T getConveyor(IConveyorType<T> type, @Nonnull BlockEntity tile)
	{
		if(tile instanceof IConveyorBlockEntity<?> convBE)
		{
			IConveyorBelt fromTile = convBE.getConveyorInstance();
			if(fromTile!=null)
				return (T)fromTile;
		}
		return type.makeInstance(tile);
	}

	public static IConveyorType<?> getConveyorType(ResourceLocation key)
	{
		return typeRegistry.get(key);
	}

	public static Collection<IConveyorType<?>> getConveyorTypes()
	{
		return typeRegistry.values();
	}

	public static BlockEntityType<? extends BlockEntity> getBEType(IConveyorType<?> type)
	{
		return BLOCK_ENTITY_TYPES.getValue().apply(type);
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

	public static IConveyorType<?> getType(Block b)
	{
		if(b instanceof IConveyorBlock conveyorBlock)
			return conveyorBlock.getType();
		else
			return null;
	}

	public static Block getBlock(IConveyorType<?> type)
	{
		return CONVEYOR_BLOCKS.getValue().apply(type);
	}

	public static boolean isConveyorBlock(Block b)
	{
		for(IConveyorType<?> type : typeRegistry.values())
			if(b==getBlock(type))
				return true;
		return false;
	}

	/**
	 * registers a consumer/function to suppress magnets while they are on the conveyors
	 * the reversal function is optional, to revert possible NBT changes
	 * the tileentity parsed is an instanceof
	 */
	public static void registerMagnetSuppression(
			BiConsumer<Entity, IConveyorBlockEntity<?>> function, @Nullable BiConsumer<Entity, IConveyorBlockEntity<?>> revert
	)
	{
		magnetSuppressionFunctions.add(function);
		if(revert!=null)
			magnetSuppressionReverse.add(revert);
	}

	/**
	 * applies all registered magnets suppressors to the entity
	 */
	public static void applyMagnetSuppression(Entity entity, IConveyorBlockEntity<?> tile)
	{
		if(entity!=null)
			for(BiConsumer<Entity, IConveyorBlockEntity<?>> func : magnetSuppressionFunctions)
				func.accept(entity, tile);
	}

	/**
	 * applies all registered magnet suppression removals
	 */
	public static void revertMagnetSuppression(Entity entity, IConveyorBlockEntity<?> tile)
	{
		if(entity!=null)
			for(BiConsumer<Entity, IConveyorBlockEntity<?>> func : magnetSuppressionReverse)
				func.accept(entity, tile);
	}

	public static boolean connectsToConveyor(Level level, BlockPos pos, Direction side)
	{
		BlockEntity te = SafeChunkUtils.getSafeBE(level, pos);
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
			te = SafeChunkUtils.getSafeBE(level, pos.offset(0, -1, 0));
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
	public interface IConveyorBlockEntity<T extends IConveyorBelt> extends IConveyorAttachable
	{
		T getConveyorInstance();

		@Override
		default Direction[] sigOutputDirections()
		{
			T subtype = getConveyorInstance();
			if(subtype!=null)
				return subtype.sigTransportDirections();
			return new Direction[0];
		}
	}

	public interface IConveyorBlock
	{
		IConveyorType<?> getType();
	}

	public interface ItemAgeAccessor
	{
		void setAge(ItemEntity entity, int newAge);
	}
}