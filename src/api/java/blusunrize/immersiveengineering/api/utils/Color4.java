/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

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

	public Color4(int rgba)
	{
		this(((rgba>>16)&255)/255f, ((rgba>>8)&255)/255f, (rgba&255)/255f, ((rgba>>24)&255)/255f);
	}

	public static Color4 from(DyeColor dyeColor)
	{
		if(dyeColor==null)
			return new Color4(1, 1, 1, 1);
		int rgb = dyeColor.getTextureDiffuseColor();
		return new Color4(((rgb>>16)&255)/255f, ((rgb>>8)&255)/255f, (rgb&255)/255f, 1);
	}

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
