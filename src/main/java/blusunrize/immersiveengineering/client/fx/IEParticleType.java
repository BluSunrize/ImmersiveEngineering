/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import com.mojang.serialization.MapCodec;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class IEParticleType<T extends ParticleOptions> extends ParticleType<T>
{
	private final MapCodec<T> codec;
	private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

	public IEParticleType(
			boolean alwaysShow,
			MapCodec<T> codec,
			StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
	)
	{
		super(alwaysShow);
		this.codec = codec;
		this.streamCodec = streamCodec;
	}

	public IEParticleType(boolean alwaysShow, DualMapCodec<? super RegistryFriendlyByteBuf, T> codecs)
	{
		this(alwaysShow, codecs.mapCodec(), codecs.streamCodec());
	}

	@Override
	public MapCodec<T> codec()
	{
		return codec;
	}

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec()
	{
		return streamCodec;
	}
}
