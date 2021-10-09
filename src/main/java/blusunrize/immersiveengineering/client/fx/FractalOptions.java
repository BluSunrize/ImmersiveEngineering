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
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record FractalOptions(Vec3 direction, double scale, int maxAge, int points, float[] colourOut,
							 float[] colourIn) implements ParticleOptions
{
	public static Codec<FractalOptions> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					IECodecs.VECTOR3D.fieldOf("direction").forGetter(d -> d.direction),
					Codec.DOUBLE.fieldOf("scale").forGetter(d -> d.scale),
					Codec.INT.fieldOf("maxAge").forGetter(d -> d.maxAge),
					Codec.INT.fieldOf("points").forGetter(d -> d.points),
					IECodecs.COLOR4.fieldOf("outerColor").forGetter(d -> d.colourOut),
					IECodecs.COLOR4.fieldOf("innerColor").forGetter(d -> d.colourIn)
			).apply(instance, FractalOptions::new)
	);

	@Override
	public ParticleType<?> getType()
	{
		return IEParticles.FRACTAL.get();
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buffer)
	{
		buffer.writeDouble(direction.x).writeDouble(direction.y).writeDouble(direction.z);
		buffer.writeDouble(scale);
		buffer.writeInt(maxAge)
				.writeInt(points);
		for(int i = 0; i < 4; ++i)
			buffer.writeFloat(colourOut[i]);
		for(int i = 0; i < 4; ++i)
			buffer.writeFloat(colourIn[i]);
	}

	@Override
	public String writeToString()
	{
		String ret = direction.x+" "+
				direction.y+" "+
				direction.z+" "+
				scale+" "+
				maxAge+" "+
				points;
		for(int i = 0; i < 4; ++i)
			ret += " "+colourOut[i];
		for(int i = 0; i < 4; ++i)
			ret += " "+colourIn[i];
		return ret;
	}

	public static class DataDeserializer implements Deserializer<FractalOptions>
	{
		@Override
		public FractalOptions fromCommand(ParticleType<FractalOptions> particleTypeIn, StringReader reader) throws CommandSyntaxException
		{
			double dX = reader.readDouble();
			reader.expect(' ');
			double dY = reader.readDouble();
			reader.expect(' ');
			double dZ = reader.readDouble();
			reader.expect(' ');
			double scale = reader.readDouble();
			reader.expect(' ');
			int maxAge = reader.readInt();
			reader.expect(' ');
			int points = reader.readInt();
			reader.expect(' ');
			float[] colourOut = new float[4];
			float[] colourIn = new float[4];
			for(int i = 0; i < 4; ++i)
			{
				colourOut[i] = reader.readFloat();
				reader.expect(' ');
			}
			for(int i = 0; i < 4; ++i)
			{
				colourIn[i] = reader.readFloat();
				reader.expect(' ');
			}

			return new FractalOptions(new Vec3(dX, dY, dZ), scale, maxAge, points, colourOut, colourIn);
		}

		@Override
		public FractalOptions fromNetwork(ParticleType<FractalOptions> particleTypeIn, FriendlyByteBuf buffer)
		{
			Vec3 dir = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
			double scale = buffer.readDouble();
			int maxAge = buffer.readInt();
			int points = buffer.readInt();
			float[] colourOut = new float[4];
			float[] colourIn = new float[4];
			for(int i = 0; i < 4; ++i)
				colourOut[i] = buffer.readFloat();
			for(int i = 0; i < 4; ++i)
				colourIn[i] = buffer.readFloat();
			return new FractalOptions(dir, scale, maxAge, points, colourOut, colourIn);
		}
	}
}
