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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;

import java.util.List;
import java.util.Optional;

public class IEOreFeature extends Feature<IEOreFeature.IEOreFeatureConfig>
{
	public IEOreFeature()
	{
		super(IEOreFeatureConfig.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<IEOreFeatureConfig> ctx)
	{
		IEOreFeatureConfig config = ctx.config();
		OreConfiguration vanillaConfig = new OreConfiguration(config.targetList, config.getSize(), (float)config.getAirExposure());
		return Feature.ORE.place(new FeaturePlaceContext<>(
				Optional.empty(), ctx.level(), ctx.chunkGenerator(), ctx.random(), ctx.origin(), vanillaConfig
		));
	}

	public static class IEOreFeatureConfig implements FeatureConfiguration
	{
		public static final Codec<IEOreFeatureConfig> CODEC = RecordCodecBuilder.create(
				app -> app.group(
						Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets")
								.forGetter(cfg -> cfg.targetList),
						Codec.list(Codec.STRING).fieldOf("size")
								.forGetter(cfg -> cfg.size),
						Codec.list(Codec.STRING).fieldOf("air_exposure")
								.forGetter(cfg -> cfg.airExposure)
				).apply(app, IEOreFeatureConfig::new)
		);
		public final List<TargetBlockState> targetList;
		public final List<String> size;
		public final List<String> airExposure;

		public IEOreFeatureConfig(List<TargetBlockState> targetList, List<String> size, List<String> airExposure)
		{
			this.targetList = targetList;
			this.size = size;
			this.airExposure = airExposure;
		}

		public IEOreFeatureConfig(List<TargetBlockState> targetList, OreConfig config)
		{
			this(targetList, config.veinSize.getBase().getPath(), config.airExposure.getBase().getPath());
		}

		public int getSize()
		{
			return IEServerConfig.getRawConfig().get(size);
		}

		public double getAirExposure()
		{
			return IEServerConfig.getRawConfig().get(airExposure);
		}
	}
}
