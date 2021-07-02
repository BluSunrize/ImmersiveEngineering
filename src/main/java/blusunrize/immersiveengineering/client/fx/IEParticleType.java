/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import com.mojang.serialization.Codec;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.IParticleData.IDeserializer;
import net.minecraft.particles.ParticleType;

import javax.annotation.Nonnull;

public class IEParticleType<T extends IParticleData> extends ParticleType<T>
{
	private final Codec<T> codec;

	public IEParticleType(boolean alwaysShow, IDeserializer<T> deserializer, Codec<T> codec)
	{
		super(alwaysShow, deserializer);
		this.codec = codec;
	}

	@Nonnull
	@Override
	public Codec<T> func_230522_e_()
	{
		return codec;
	}
}
