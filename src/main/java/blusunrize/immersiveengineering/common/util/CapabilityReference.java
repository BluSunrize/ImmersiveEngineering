/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class CapabilityReference<T>
{
	public static <T> CapabilityReference<T> forTileEntity(TileEntity local, Supplier<DirectionalBlockPos> pos,
														   Capability<T> cap)
	{
		return new TECapReference<>(local::getWorld, pos, cap);
	}

	public static <T> CapabilityReference<T> forRelative(TileEntity local, Capability<T> cap, Vector3i offset, Direction side)
	{
		return forTileEntity(local, () -> new DirectionalBlockPos(local.getPos().add(offset), side.getOpposite()), cap);
	}

	public static <T> CapabilityReference<T> forNeighbor(TileEntity local, Capability<T> cap, NonNullSupplier<Direction> side)
	{
		return forTileEntity(
				local,
				() -> {
					Direction d = side.get();
					return new DirectionalBlockPos(local.getPos().offset(d), d.getOpposite());
				},
				cap
		);
	}

	public static <T> CapabilityReference<T> forNeighbor(TileEntity local, Capability<T> cap, @Nonnull Direction side)
	{
		return forRelative(local, cap, BlockPos.ZERO.offset(side), side);
	}

	protected final Capability<T> cap;

	protected CapabilityReference(Capability<T> cap)
	{
		this.cap = cap;
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
		private final Supplier<World> world;
		private final Supplier<DirectionalBlockPos> pos;
		@Nonnull
		private LazyOptional<T> currentCap = LazyOptional.empty();
		private DirectionalBlockPos lastPos;
		private World lastWorld;//TODO does this leak anywhere?
		private TileEntity lastTE;

		public TECapReference(Supplier<World> world, Supplier<DirectionalBlockPos> pos, Capability<T> cap)
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
			World currWorld = world.get();
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
					IELogger.logger.warn(
							"The tile entity {} (class {}) was removed, but the value {} provided by it "+
									"for the capability {} is still marked as valid. This is likely a bug in the mod(s) adding "+
									"the tile entity/the capability",
							lastTE,
							lastTE.getClass(),
							currentCap.orElseThrow(RuntimeException::new),
							cap.getName());
				}
				lastTE = Utils.getExistingTileEntity(currWorld, currPos);
				if(lastTE!=null)
					currentCap = lastTE.getCapability(cap, currPos.direction);
				else
					currentCap = LazyOptional.empty();
				lastWorld = currWorld;
				lastPos = currPos;
			}
		}
	}
}