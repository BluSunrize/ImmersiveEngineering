/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public record SimpleCapProvider<T>(Supplier<Capability<T>> cap, LazyOptional<T> value) implements ICapabilityProvider
{
	public SimpleCapProvider(Supplier<Capability<T>> cap, T value)
	{
		this(cap, CapabilityUtils.constantOptional(value));
	}

	@Nonnull
	@Override
	public <T2> LazyOptional<T2> getCapability(@Nonnull Capability<T2> cap, @Nullable Direction side)
	{
		return Objects.requireNonNull(this.cap.get()).orEmpty(cap, value);
	}
}
