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
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.VerticalDecorator;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class IERangePlacement extends VerticalDecorator<IETopSolidRangeConfig>
{
	public static final Codec<List<String>> STRING_LIST = Codec.list(Codec.STRING);

	public IERangePlacement()
	{
		super(IETopSolidRangeConfig.CODEC);
	}

	@Override
	protected int y(@Nonnull DecorationContext ctx, Random random, IETopSolidRangeConfig config, int baseY)
	{
		return random.nextInt(config.getMax()-config.getMin())+config.getMin();
	}

	public static class IETopSolidRangeConfig implements DecoratorConfiguration
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
			this(config.minY.getBase().getPath(), config.maxY.getBase().getPath());
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
