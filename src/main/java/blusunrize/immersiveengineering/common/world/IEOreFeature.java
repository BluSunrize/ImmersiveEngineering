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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.template.RuleTest;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class IEOreFeature extends Feature<IEOreFeature.IEOreFeatureConfig>
{
	public IEOreFeature()
	{
		super(IEOreFeatureConfig.CODEC);
	}

	@Override
	public boolean generate(@Nonnull ISeedReader world, @Nonnull ChunkGenerator gen, @Nonnull Random rand,
							@Nonnull BlockPos pos, IEOreFeatureConfig config)
	{
		OreFeatureConfig vanillaConfig = new OreFeatureConfig(config.target, config.state, config.getSize());
		return Feature.ORE.generate(world, gen, rand, pos, vanillaConfig);
	}

	public static class IEOreFeatureConfig implements IFeatureConfig
	{
		public static final Codec<IEOreFeatureConfig> CODEC = RecordCodecBuilder.create(
				app -> app.group(
						RuleTest.field_237127_c_.fieldOf("target")
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
