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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

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
		OreConfiguration vanillaConfig = new OreConfiguration(config.target, config.state, config.getSize());
		return Feature.ORE.place(new FeaturePlaceContext<>(
				Optional.empty(), ctx.level(), ctx.chunkGenerator(), ctx.random(), ctx.origin(), vanillaConfig
		));
	}

	public static class IEOreFeatureConfig implements FeatureConfiguration
	{
		public static final Codec<IEOreFeatureConfig> CODEC = RecordCodecBuilder.create(
				app -> app.group(
						RuleTest.CODEC.fieldOf("target")
								.forGetter(cfg -> cfg.target),
						BlockState.CODEC.fieldOf("state")
								.forGetter(cfg -> cfg.state),
						Codec.list(Codec.STRING).fieldOf("size")
								.forGetter(cfg -> cfg.size)
				).apply(app, IEOreFeatureConfig::new)
		);
		public final RuleTest target;
		public final List<String> size;
		public final BlockState state;

		public IEOreFeatureConfig(RuleTest target, BlockState state, List<String> size)
		{
			this.size = size;
			this.state = state;
			this.target = target;
		}

		public IEOreFeatureConfig(RuleTest target, BlockState state, OreConfig config)
		{
			this(target, state, config.veinSize.getBase().getPath());
		}

		public int getSize()
		{
			return IEServerConfig.getRawConfig().get(size);
		}
	}
}
