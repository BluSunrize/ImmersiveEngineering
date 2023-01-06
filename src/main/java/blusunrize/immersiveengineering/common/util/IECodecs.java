/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public class IECodecs
{
	public static Codec<float[]> COLOR4 = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.FLOAT.fieldOf("r").forGetter(a -> a[0]),
					Codec.FLOAT.fieldOf("g").forGetter(a -> a[1]),
					Codec.FLOAT.fieldOf("b").forGetter(a -> a[2]),
					Codec.FLOAT.fieldOf("a").forGetter(a -> a[3])
			).apply(instance, (r, g, b, a) -> new float[]{r, g, b, a})
	);
	public static Codec<Vec3> VECTOR3D = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
					Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
					Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
			).apply(instance, Vec3::new)
	);
}
