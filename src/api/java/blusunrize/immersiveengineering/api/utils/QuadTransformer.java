/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraftforge.client.model.IQuadTransformer;

public class QuadTransformer
{
	public static IQuadTransformer color(Int2IntFunction colorTransform)
	{
		return quad -> {
			if(!quad.isTinted())
				return;
			int multiplier = colorTransform.apply(quad.getTintIndex());
			if(multiplier==0)
				return;
			int[] data = quad.getVertices();
			for(int i = 0; i < 4; ++i)
			{
				int colorId = i*IQuadTransformer.STRIDE+IQuadTransformer.COLOR;
				data[colorId] = modifyColor(data[colorId], 0, multiplier);
				data[colorId] = modifyColor(data[colorId], 8, multiplier);
				data[colorId] = modifyColor(data[colorId], 16, multiplier);
			}
		};
	}

	private static int modifyColor(int oldColor, int offsetBits, int packedMultiplier)
	{
		final int mask = 255<<offsetBits;
		final int oldSubColor = mask&oldColor;
		final float subMultiplier = ((packedMultiplier>>offsetBits)&255)/255f;
		final int newSubColor = ((int)(oldSubColor*subMultiplier))&mask;
		return (oldColor&~mask)|newSubColor;
	}
}
