/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.OreConfig;
import blusunrize.immersiveengineering.common.world.IERangePlacement.IETopSolidRangeConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.SimplePlacement;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class IERangePlacement extends SimplePlacement<IETopSolidRangeConfig>
{
	public static final Codec<List<String>> STRING_LIST = Codec.list(Codec.STRING);

	public IERangePlacement()
	{
		super(IETopSolidRangeConfig.CODEC);
	}

	public Stream<BlockPos> getPositions(Random random, IETopSolidRangeConfig config, BlockPos pos)
	{
		int i = pos.getX();
		int j = pos.getZ();
		int k = random.nextInt(config.getMax()-config.getMin())+config.getMin();
		return Stream.of(new BlockPos(i, k, j));
	}

	public static class IETopSolidRangeConfig implements IPlacementConfig
	{
		public static Codec<IETopSolidRangeConfig> CODEC = RecordCodecBuilder.create(
				app -> app.group(
						STRING_LIST.fieldOf("min").forGetter(config -> config.min),
						STRING_LIST.fieldOf("max").forGetter(config -> config.max)
				).apply(app, IETopSolidRangeConfig::new)
		);

		final List<String> min;
		final List<String> max;

		public IETopSolidRangeConfig(OreConfig config)
		{
			this(config.minY.getPath(), config.maxY.getPath());
		}

		public IETopSolidRangeConfig(List<String> minPath, List<String> maxPath)
		{
			this.min = minPath;
			this.max = maxPath;
		}

		public int getMin()
		{
			return IEServerConfig.getRawConfig().getInt(min);
		}

		public int getMax()
		{
			return IEServerConfig.getRawConfig().getInt(max);
		}
	}
}
