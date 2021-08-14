package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.common.register.IEParticles;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public record FluidSplashOptions(Fluid fluid) implements ParticleOptions
{
	public static final Codec<FluidSplashOptions> CODEC = ResourceLocation.CODEC.xmap(
			FluidSplashOptions::new, d -> d.fluid.getRegistryName()
	);

	public FluidSplashOptions(ResourceLocation name)
	{
		this(ForgeRegistries.FLUIDS.getValue(name));
	}

	@Nonnull
	@Override
	public ParticleType<?> getType()
	{
		return IEParticles.FLUID_SPLASH.get();
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buffer)
	{
		buffer.writeResourceLocation(fluid.getRegistryName());
	}

	@Nonnull
	@Override
	public String writeToString()
	{
		return fluid.getRegistryName().toString();
	}
}
