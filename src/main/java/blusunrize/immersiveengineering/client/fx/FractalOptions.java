/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.common.register.IEParticles;
import blusunrize.immersiveengineering.common.util.IECodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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

	public record Color4(float r, float g, float b, float a)
	{
		public static final Color4 WHITE = new Color4(1, 1, 1, 1);
		public static final Codec<Color4> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("r").forGetter(Color4::r),
				Codec.FLOAT.fieldOf("g").forGetter(Color4::g),
				Codec.FLOAT.fieldOf("b").forGetter(Color4::b),
				Codec.FLOAT.fieldOf("a").forGetter(Color4::a)
		).apply(instance, Color4::new));
		public static final StreamCodec<ByteBuf, Color4> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.FLOAT, Color4::r,
				ByteBufCodecs.FLOAT, Color4::g,
				ByteBufCodecs.FLOAT, Color4::b,
				ByteBufCodecs.FLOAT, Color4::a,
				Color4::new
		);

		public static Color4 load(Tag nbt)
		{
			return CODEC.decode(NbtOps.INSTANCE, nbt).getOrThrow().getFirst();
		}

		public Tag save()
		{
			return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
		}

		public int toInt()
		{
			final int rInt = (int)(255*r);
			final int gInt = (int)(255*g);
			final int bInt = (int)(255*b);
			final int aInt = (int)(255*a);
			return (aInt<<24)|(rInt<<16)|(gInt<<8)|bInt;
		}
	}
}
