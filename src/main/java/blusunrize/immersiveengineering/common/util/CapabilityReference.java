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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class CapabilityReference<T>
{
	public static <T> CapabilityReference<T> forTileEntity(TileEntity local, Supplier<DirectionalBlockPos> pos,
														   Capability<T> cap)
	{
		return new TECapReference<>(local::getWorld, pos, cap);
	}

	public static <T> CapabilityReference<T> forRelative(TileEntity local, Capability<T> cap, Vec3i offset, EnumFacing side)
	{
		return forTileEntity(local, () -> new DirectionalBlockPos(local.getPos().add(offset), side.getOpposite()), cap);
	}

	public static <T> CapabilityReference<T> forNeighbor(TileEntity local, Capability<T> cap, @Nonnull EnumFacing side)
	{
		return forRelative(local, cap, BlockPos.ORIGIN.offset(side), side);
	}

	protected final Capability<T> cap;

	protected CapabilityReference(Capability<T> cap)
	{
		this.cap = cap;
	}

	@Nullable
	public abstract T get();

	public abstract boolean isPresent();

	private static class TECapReference<T> extends CapabilityReference<T>
	{
		private final Supplier<World> world;
		private final Supplier<DirectionalBlockPos> pos;
		@Nonnull
		private LazyOptional<T> currentCap = LazyOptional.empty();
		private DirectionalBlockPos lastPos;
		private World lastWorld;//TODO does this leak anywhere?

		public TECapReference(Supplier<World> world, Supplier<DirectionalBlockPos> pos, Capability<T> cap)
		{
			super(cap);
			this.world = world;
			this.pos = pos;
		}

		@Nullable
		@Override
		public T get()
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
			}
			else if(currWorld!=lastWorld||!currPos.equals(lastPos)||!currentCap.isPresent())
			{
				TileEntity te = Utils.getExistingTileEntity(currWorld, currPos);
				if(te!=null)
					currentCap = te.getCapability(cap, currPos.direction);
				else
					currentCap = LazyOptional.empty();
				lastWorld = currWorld;
				lastPos = currPos;
			}
		}
	}
}