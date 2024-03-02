/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class IEBlockCapabilityCaches
{
	public static <T, C> IEBlockCapabilityCache<T> create(
			BlockCapability<T, C> capability,
			Supplier<BlockPos> getPosition,
			Supplier<C> getContext,
			Supplier<@Nullable Level> getLevel
	)
	{
		return new IEBlockCapCacheImpl<>(capability, getPosition, getContext, getLevel);
	}

	public static <T> IEBlockCapabilityCache<T> forNeighbor(
			BlockCapability<T, Direction> capability, BlockEntity be, Supplier<Direction> neighborDirection
	)
	{
		return create(
				capability,
				() -> be.getBlockPos().relative(neighborDirection.get()),
				() -> neighborDirection.get().getOpposite(),
				be::getLevel
		);
	}

	public static <T> Map<Direction, IEBlockCapabilityCache<T>> allNeighbors(
			BlockCapability<T, Direction> capability, BlockEntity be
	)
	{
		Map<Direction, IEBlockCapabilityCache<T>> result = new EnumMap<>(Direction.class);
		for(Direction offset : Direction.values())
			result.put(offset, forNeighbor(capability, be, () -> offset));
		return result;
	}

	// Intermediate interface to hide context argument
	public interface IEBlockCapabilityCache<T>
	{
		T getCapability();
	}

	private static class IEBlockCapCacheImpl<T, C> implements IEBlockCapabilityCache<T>
	{
		private final BlockCapability<T, C> capability;
		private final Supplier<BlockPos> getPosition;
		private final Supplier<C> getContext;
		private final Supplier<@Nullable Level> getLevel;
		private BlockCapabilityCache<T, C> cache;

		private IEBlockCapCacheImpl(
				BlockCapability<T, C> capability,
				Supplier<BlockPos> getPosition,
				Supplier<C> getContext,
				Supplier<@Nullable Level> getLevel
		)
		{
			this.capability = capability;
			this.getPosition = getPosition;
			this.getContext = getContext;
			this.getLevel = getLevel;
		}

		@Override
		public T getCapability()
		{
			C currentCtx = getContext.get();
			BlockPos currentPos = getPosition.get();
			Level level = getLevel.get();
			if(level instanceof ServerLevel serverLevel)
			{
				if(isCacheInvalid(level, currentPos, currentCtx))
					this.cache = BlockCapabilityCache.create(this.capability, serverLevel, currentPos, currentCtx);
				return this.cache.getCapability();
			}
			else if(level==null)
				return null;
			else
			{
				// Cap caching is not available on non-server levels
				// TODO do we ever intentionally query on client levels? If yes, do we want:
				// Preconditions.checkState(!level.isClientSide);
				this.cache = null;
				return level.getCapability(this.capability, currentPos, currentCtx);
			}
		}

		private boolean isCacheInvalid(Level level, BlockPos pos, C context)
		{
			if(cache==null)
				return true;
			else if(!Objects.equals(cache.context(), context))
				return true;
			else if(!Objects.equals(cache.pos(), pos))
				return true;
			else if(level!=cache.level())
				return true;
			return false;
		}
	}
}
