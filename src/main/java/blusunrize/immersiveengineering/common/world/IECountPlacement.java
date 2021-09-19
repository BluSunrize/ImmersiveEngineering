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
import net.minecraft.core.BlockPos;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IECountPlacement extends SimpleFeatureDecorator<IECountPlacement.IEFeatureSpreadConfig>
{
	public IECountPlacement()
	{
		super(IEFeatureSpreadConfig.CODEC);
	}

	@Override
	public Stream<BlockPos> place(Random random, IEFeatureSpreadConfig config, BlockPos pos)
	{
		return IntStream.range(0, config.getSpreadFeature().sample(random))
				.mapToObj(count -> pos);
	}

	public static class IEFeatureSpreadConfig implements DecoratorConfiguration, FeatureConfiguration
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
			this(config.veinsPerChunk.getBase().getPath());
		}

		public IEFeatureSpreadConfig(List<String> path)
		{
			count = path;
		}

		public UniformInt getSpreadFeature()
		{
			return UniformInt.fixed(IEServerConfig.getRawConfig().getInt(count));
		}
	}
}

