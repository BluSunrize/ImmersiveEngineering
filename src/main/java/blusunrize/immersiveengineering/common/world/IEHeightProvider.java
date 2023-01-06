/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.OreConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.OreDistribution;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.VeinType;
import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraftforge.common.util.Lazy;

public class IEHeightProvider extends HeightProvider
{
	public static final Codec<IEHeightProvider> CODEC = VeinType.CODEC.xmap(IEHeightProvider::new, p -> p.type);

	private final VeinType type;
	private final Lazy<HeightProvider> internalProvider;

	public IEHeightProvider(VeinType type)
	{
		this.type = type;

		this.internalProvider = Lazy.of(() -> {
			OreConfig config = IEServerConfig.ORES.ores.get(type);
			VerticalAnchor vaMin = pContext -> config.minY.get();
			VerticalAnchor vaMax = pContext -> config.maxY.get();
			return config.distribution.get()==OreDistribution.TRAPEZOID?
					TrapezoidHeight.of(vaMin, vaMax):
					UniformHeight.of(vaMin, vaMax);
		});
	}

	@Override
	public int sample(RandomSource random, WorldGenerationContext context)
	{
		return this.internalProvider.get().sample(random, context);
	}

	@Override
	public HeightProviderType<?> getType()
	{
		return IEWorldGen.IE_HEIGHT_PROVIDER.get();
	}
}
