package blusunrize.immersiveengineering.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.vector.Vector3d;

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
	public static Codec<Vector3d> VECTOR3D = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.DOUBLE.fieldOf("x").forGetter(Vector3d::getX),
					Codec.DOUBLE.fieldOf("y").forGetter(Vector3d::getY),
					Codec.DOUBLE.fieldOf("z").forGetter(Vector3d::getZ)
			).apply(instance, Vector3d::new)
	);
}
