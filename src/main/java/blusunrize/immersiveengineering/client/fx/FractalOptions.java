package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.common.register.IEParticles;
import blusunrize.immersiveengineering.common.util.IECodecs;
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
}
