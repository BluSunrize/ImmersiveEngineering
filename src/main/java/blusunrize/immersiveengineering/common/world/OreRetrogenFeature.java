/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

/*
 * Credit to Richard Freimer @pupnewfster
 * https://github.com/mekanism/Mekanism/blob/a5c433d58b5ec51b85bc5252a566b5f8668d47f9/src/main/java/mekanism/common/world/OreRetrogenFeature.java
 */
package blusunrize.immersiveengineering.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import javax.annotation.Nonnull;

public class OreRetrogenFeature extends OreFeature
{
	public OreRetrogenFeature(Codec<OreConfiguration> configFactory)
	{
		super(configFactory);
	}

	@Override
	public boolean place(@Nonnull FeaturePlaceContext<OreConfiguration> ctx)
	{
		float angle = ctx.random().nextFloat()*(float)Math.PI;
		float f1 = (float)ctx.config().size/8.0F;
		int i = Mth.ceil((f1+1.0F)/2.0F);
		float sin = Mth.sin(angle)*f1;
		float cos = Mth.cos(angle)*f1;
		double xStart = ctx.origin().getX()+sin;
		double xEnd = ctx.origin().getX()-sin;
		double zStart = ctx.origin().getZ()+cos;
		double zEnd = ctx.origin().getZ()-cos;
		double yStart = ctx.origin().getY()+ctx.random().nextInt(3)-2;
		double yEnd = ctx.origin().getY()+ctx.random().nextInt(3)-2;
		int k = ctx.origin().getX()-Mth.ceil(f1)-i;
		int l = ctx.origin().getY()-2-i;
		int i1 = ctx.origin().getZ()-Mth.ceil(f1)-i;
		int j1 = 2*(Mth.ceil(f1)+i);
		int k1 = 2*(2+i);
		for(int l1 = k; l1 <= k+j1; ++l1)
		{
			for(int i2 = i1; i2 <= i1+j1; ++i2)
			{
				//Use OCEAN_FLOOR instead of OCEAN_FLOOR_WG as the chunks are already generated
				if(l <= ctx.level().getHeight(Heightmap.Types.OCEAN_FLOOR, l1, i2))
					return this.doPlace(ctx.level(), ctx.random(), ctx.config(), xStart, xEnd, zStart, zEnd, yStart, yEnd, k, l, i1, j1, k1);
			}
		}
		return false;
	}
}