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
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.FeatureSpread;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.SimplePlacement;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IECountPlacement extends SimplePlacement<IECountPlacement.IEFeatureSpreadConfig>
{
	public IECountPlacement()
	{
		super(IEFeatureSpreadConfig.CODEC);
	}

	public Stream<BlockPos> getPositions(Random random, IEFeatureSpreadConfig p_212852_2_, BlockPos pos)
	{
		return IntStream.range(0, p_212852_2_.getSpreadFeature().func_242259_a(random))
				.mapToObj(p_242878_1_ -> pos);
	}

	public static class IEFeatureSpreadConfig implements IPlacementConfig, IFeatureConfig
	{
		public static final Codec<IEFeatureSpreadConfig> CODEC = RecordCodecBuilder.create(
				app -> app.group(
						Codec.list(Codec.STRING).fieldOf("count")
								.forGetter(f -> f.count)
				).apply(app, IEFeatureSpreadConfig::new)
		);
		private final List<String> count;

		public IEFeatureSpreadConfig(Ores.OreConfig config)
		{
			this(config.veinsPerChunk.getPath());
		}

		public IEFeatureSpreadConfig(List<String> path)
		{
			count = path;
		}

		public FeatureSpread getSpreadFeature()
		{
			return FeatureSpread.func_242252_a(IEServerConfig.getRawConfig().getInt(count));
		}
	}
}

