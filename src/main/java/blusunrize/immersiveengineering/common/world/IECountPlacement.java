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
import blusunrize.immersiveengineering.common.world.IECountPlacement.IEFeatureSpreadConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.RepeatingDecorator;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class IECountPlacement extends RepeatingDecorator<IEFeatureSpreadConfig>
{
	public IECountPlacement()
	{
		super(IEFeatureSpreadConfig.CODEC);
	}

	//TODO why is this constant? Was it constant before or did I mess up the port?
	@Override
	protected int count(@Nonnull Random random, IEFeatureSpreadConfig config, @Nonnull BlockPos pos)
	{
		return config.getCount();
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

		public int getCount()
		{
			return IEServerConfig.getRawConfig().getInt(count);
		}
	}
}

