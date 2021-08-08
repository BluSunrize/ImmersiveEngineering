package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record SimpleCapProvider<T>(Capability<T> cap, LazyOptional<T> value) implements ICapabilityProvider
{
	public SimpleCapProvider(Capability<T> cap, T value)
	{
		this(cap, CapabilityUtils.constantOptional(value));
	}

	@Nonnull
	@Override
	public <T2> LazyOptional<T2> getCapability(@Nonnull Capability<T2> cap, @Nullable Direction side)
	{
		return this.cap.orEmpty(cap, value);
	}
}
