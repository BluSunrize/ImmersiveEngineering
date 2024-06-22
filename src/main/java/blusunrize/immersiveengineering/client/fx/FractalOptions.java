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
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEParticles;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.phys.Vec3;

public record FractalOptions(
		Vec3 direction, double scale, int maxAge, int points, Color4 colourOut, Color4 colourIn
) implements ParticleOptions
{
	public static DualMapCodec<ByteBuf, FractalOptions> CODECS = DualMapCodec.composite(
			DualCodecs.VEC3.fieldOf("direction"), d -> d.direction,
			DualCodecs.DOUBLE.fieldOf("scale"), d -> d.scale,
			DualCodecs.INT.fieldOf("maxAge"), d -> d.maxAge,
			DualCodecs.INT.fieldOf("points"), d -> d.points,
			Color4.CODECS.fieldOf("outerColor"), d -> d.colourOut,
			Color4.CODECS.fieldOf("innerColor"), d -> d.colourIn,
			FractalOptions::new
	);

	@Override
	public ParticleType<?> getType()
	{
		return IEParticles.FRACTAL.get();
	}
}
