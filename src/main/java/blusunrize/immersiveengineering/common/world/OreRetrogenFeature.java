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
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import javax.annotation.Nonnull;
import java.util.Random;

public class OreRetrogenFeature extends OreFeature
{
	public OreRetrogenFeature(Codec<OreConfiguration> configFactory)
	{
		super(configFactory);
	}

	@Override
	public boolean place(@Nonnull WorldGenLevel world, ChunkGenerator generator, Random rand, BlockPos pos, OreConfiguration config)
	{
		float angle = rand.nextFloat()*(float)Math.PI;
		float f1 = (float)config.size/8.0F;
		int i = Mth.ceil((f1+1.0F)/2.0F);
		float sin = Mth.sin(angle)*f1;
		float cos = Mth.cos(angle)*f1;
		double xStart = pos.getX()+sin;
		double xEnd = pos.getX()-sin;
		double zStart = pos.getZ()+cos;
		double zEnd = pos.getZ()-cos;
		double yStart = pos.getY()+rand.nextInt(3)-2;
		double yEnd = pos.getY()+rand.nextInt(3)-2;
		int k = pos.getX()-Mth.ceil(f1)-i;
		int l = pos.getY()-2-i;
		int i1 = pos.getZ()-Mth.ceil(f1)-i;
		int j1 = 2*(Mth.ceil(f1)+i);
		int k1 = 2*(2+i);
		for(int l1 = k; l1 <= k+j1; ++l1)
		{
			for(int i2 = i1; i2 <= i1+j1; ++i2)
			{
				//Use OCEAN_FLOOR instead of OCEAN_FLOOR_WG as the chunks are already generated
				if(l <= world.getHeight(Heightmap.Types.OCEAN_FLOOR, l1, i2))
				{
					return this.doPlace(world, rand, config, xStart, xEnd, zStart, zEnd, yStart, yEnd, k, l, i1, j1, k1);
				}
			}
		}
		return false;
	}
}