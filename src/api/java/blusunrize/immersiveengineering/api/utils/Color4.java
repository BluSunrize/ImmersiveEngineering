/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;

public record Color4(float r, float g, float b, float a)
{
	public static final Color4 WHITE = new Color4(1f, 1, 1, 1);
	public static final DualCodec<ByteBuf, Color4> CODECS = DualCompositeCodecs.composite(
			DualCodecs.FLOAT.fieldOf("r"), Color4::r,
			DualCodecs.FLOAT.fieldOf("g"), Color4::g,
			DualCodecs.FLOAT.fieldOf("b"), Color4::b,
			DualCodecs.FLOAT.fieldOf("a"), Color4::a,
			Color4::new
	);

	public static Color4 fromARGB(int rgba)
	{
		return new Color4(rgba>>16&255, rgba>>8&255, rgba&255, rgba>>24&255);
	}

	public static Color4 fromRGB(int rgb)
	{
		return new Color4(rgb>>16&255, rgb>>8&255, rgb&255, 255);
	}

	public Color4(int r, int g, int b, int a)
	{
		this(r/255f, g/255f, b/255f, a/255f);
	}

	public static Color4 from(DyeColor dyeColor)
	{
		if(dyeColor==null)
			return WHITE;
		int rgb = dyeColor.getTextureDiffuseColor();
		return new Color4(((rgb>>16)&255)/255f, ((rgb>>8)&255)/255f, (rgb&255)/255f, 1);
	}

	public static Color4 load(Tag nbt)
	{
		return CODECS.fromNBT(nbt);
	}

	public Tag save()
	{
		return CODECS.toNBT(this);
	}

	// TODO split into RGB and ARGB
	public int toInt()
	{
		final int rInt = (int)(255*r);
		final int gInt = (int)(255*g);
		final int bInt = (int)(255*b);
		final int aInt = (int)(255*a);
		return (aInt<<24)|(rInt<<16)|(gInt<<8)|bInt;
	}
}
