/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class CapabilityReference<T>
{
	private static final Logger LOGGER = LogManager.getLogger();

	public static <T> CapabilityReference<T> forTileEntityAt(
			BlockEntity local, Supplier<DirectionalBlockPos> pos, Capability<T> cap
	)
	{
		return new TECapReference<>(local::getLevel, pos, cap);
	}

	public static <T> CapabilityReference<T> forRelative(BlockEntity local, Capability<T> cap, Vec3i offset, Direction side)
	{
		return forTileEntityAt(local, () -> new DirectionalBlockPos(local.getBlockPos().offset(offset), side.getOpposite()), cap);
	}

	public static <T> CapabilityReference<T> forNeighbor(BlockEntity local, Capability<T> cap, NonNullSupplier<Direction> side)
	{
		return forTileEntityAt(
				local,
				() -> {
					Direction d = side.get();
					return new DirectionalBlockPos(local.getBlockPos().relative(d), d.getOpposite());
				},
				cap
		);
	}

	public static <T> CapabilityReference<T> forNeighbor(BlockEntity local, Capability<T> cap, @Nonnull Direction side)
	{
		return forRelative(local, cap, BlockPos.ZERO.relative(side), side);
	}

	protected final Capability<T> cap;

	protected CapabilityReference(Capability<T> cap)
	{
		this.cap = Objects.requireNonNull(cap);
	}

	@Nullable
	public abstract T getNullable();

	@Nonnull
	public T get()
	{
		return Objects.requireNonNull(getNullable());
	}

	public abstract boolean isPresent();

	private static class TECapReference<T> extends CapabilityReference<T>
	{
		private final Supplier<Level> world;
		private final Supplier<DirectionalBlockPos> pos;
		@Nonnull
		private LazyOptional<T> currentCap = LazyOptional.empty();
		private DirectionalBlockPos lastPos;
		private Level lastWorld;//TODO does this leak anywhere?
		private BlockEntity lastTE;

		public TECapReference(Supplier<Level> world, Supplier<DirectionalBlockPos> pos, Capability<T> cap)
		{
			super(cap);
			this.world = world;
			this.pos = pos;
		}

		@Nullable
		@Override
		public T getNullable()
		{
			updateLazyOptional();
			return currentCap.orElse(null);
		}

		@Override
		public boolean isPresent()
		{
			updateLazyOptional();
			return currentCap.isPresent();
		}

		private void updateLazyOptional()
		{
			Level currWorld = world.get();
			DirectionalBlockPos currPos = pos.get();
			if(currWorld==null||currPos==null)
			{
				currentCap = LazyOptional.empty();
				lastWorld = null;
				lastPos = null;
				lastTE = null;
			}
			else if(currWorld!=lastWorld
					||!currPos.equals(lastPos)
					||!currentCap.isPresent()
					||(lastTE!=null&&lastTE.isRemoved()))
			{
				if(currentCap.isPresent()&&lastTE!=null&&lastTE.isRemoved())
				{
					LOGGER.warn(
							"The tile entity {} (class {}) was removed, but the value {} provided by it "+
									"for the capability {} is still marked as valid. This is likely a bug in the mod(s) adding "+
									"the tile entity/the capability",
							lastTE,
							lastTE.getClass(),
							currentCap.orElseThrow(RuntimeException::new),
							cap.getName());
				}
				lastTE = SafeChunkUtils.getSafeTE(currWorld, currPos.getPosition());
				if(lastTE!=null)
					currentCap = lastTE.getCapability(cap, currPos.getSide());
				else
					currentCap = LazyOptional.empty();
				lastWorld = currWorld;
				lastPos = currPos;
			}
		}
	}
}