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
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

import java.util.List;
import java.util.Random;

public class IERangePlacement extends HeightProvider
{
	public static final Codec<List<String>> STRING_LIST = Codec.list(Codec.STRING);
	public static Codec<IERangePlacement> CODEC = RecordCodecBuilder.create(
			app -> app.group(
					STRING_LIST.fieldOf("min").forGetter(config -> config.min),
					STRING_LIST.fieldOf("max").forGetter(config -> config.max)
			).apply(app, IERangePlacement::new)
	);

	private final List<String> min;
	private final List<String> max;

	public IERangePlacement(List<String> min, List<String> max)
	{
		this.min = min;
		this.max = max;
	}

	public IERangePlacement(OreConfig config)
	{
		this(config.minY.getBase().getPath(), config.maxY.getBase().getPath());
	}

	@Override
	public int sample(Random random, WorldGenerationContext p_161978_)
	{
		int min = IEServerConfig.getRawConfig().getInt(this.min);
		int max = IEServerConfig.getRawConfig().getInt(this.max);
		return random.nextInt(max - min)+min;
	}

	@Override
	public HeightProviderType<?> getType()
	{
		return IEWorldGen.IE_RANGE_PLACEMENT;
	}
}
