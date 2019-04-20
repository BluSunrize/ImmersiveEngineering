/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class CapabilityReference<T>
{
	public static <T> CapabilityReference<T> forNeighbor(TileEntity local, Capability<T> cap, @Nonnull EnumFacing side)
	{
		return new CapabilityReference<>(
				() -> Objects.requireNonNull(local.getWorld()).getTileEntity(local.getPos().offset(side)),
				cap, side.getOpposite());
	}

	public static <T> CapabilityReference<T> forTE(World w, BlockPos pos, Capability<T> cap, @Nullable EnumFacing side)
	{
		return new CapabilityReference<>(() -> w.getTileEntity(pos), cap, side);
	}

	private final Supplier<ICapabilityProvider> provider;
	private Capability<T> cap;
	@Nullable
	private final EnumFacing side;

	private LazyOptional<T> currentCap = LazyOptional.empty();

	public CapabilityReference(Supplier<ICapabilityProvider> provider, Capability<T> cap, @Nullable EnumFacing side)
	{

		this.provider = provider;
		this.cap = cap;
		this.side = side;
	}

	@Nullable
	public T get()
	{
		if(!currentCap.isPresent())
		{
			ICapabilityProvider source = provider.get();
			if(source!=null)
				currentCap = source.getCapability(cap, side);
		}
		return currentCap.orElse(null);
	}
}
