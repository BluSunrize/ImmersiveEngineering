/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.api.utils.IECodecs;
import blusunrize.immersiveengineering.common.register.IEParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record FractalOptions(
		Vec3 direction, double scale, int maxAge, int points, Color4 colourOut, Color4 colourIn
) implements ParticleOptions
{
	public static MapCodec<FractalOptions> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					Vec3.CODEC.fieldOf("direction").forGetter(d -> d.direction),
					Codec.DOUBLE.fieldOf("scale").forGetter(d -> d.scale),
					Codec.INT.fieldOf("maxAge").forGetter(d -> d.maxAge),
					Codec.INT.fieldOf("points").forGetter(d -> d.points),
					Color4.CODEC.fieldOf("outerColor").forGetter(d -> d.colourOut),
					Color4.CODEC.fieldOf("innerColor").forGetter(d -> d.colourIn)
			).apply(instance, FractalOptions::new)
	);
	public static StreamCodec<ByteBuf, FractalOptions> STREAM_CODEC = StreamCodec.composite(
			IECodecs.VEC3_STREAM_CODEC, FractalOptions::direction,
			ByteBufCodecs.DOUBLE, FractalOptions::scale,
			ByteBufCodecs.INT, FractalOptions::maxAge,
			ByteBufCodecs.INT, FractalOptions::points,
			Color4.STREAM_CODEC, FractalOptions::colourOut,
			Color4.STREAM_CODEC, FractalOptions::colourIn,
			FractalOptions::new
	);

	@Override
	public ParticleType<?> getType()
	{
		return IEParticles.FRACTAL.get();
	}
}
