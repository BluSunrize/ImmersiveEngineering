package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.common.config.CachedConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.OreConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.OreDistribution;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraftforge.common.util.Lazy;

import java.util.List;
import java.util.Random;

public class IEHeightProvider extends HeightProvider
{
	public static final Codec<IEHeightProvider> CODEC = RecordCodecBuilder.create(
			app -> app.group(
					Codec.list(Codec.STRING).fieldOf("distribution")
							.forGetter(f -> f.distribution),
					Codec.list(Codec.STRING).fieldOf("min")
							.forGetter(f -> f.min),
					Codec.list(Codec.STRING).fieldOf("max")
							.forGetter(f -> f.max)
			).apply(app, IEHeightProvider::new)
	);

	private final List<String> distribution;
	private final List<String> min;
	private final List<String> max;
	private final Lazy<HeightProvider> internalProvider;

	public IEHeightProvider(List<String> distribution, List<String> min, List<String> max)
	{
		super();
		this.distribution = distribution;
		this.min = min;
		this.max = max;

		this.internalProvider = Lazy.of(() -> {
			OreDistribution dist = IEServerConfig.getRawConfig().get(this.distribution);
			VerticalAnchor vaMin = new VerticalAnchor(0)
			{
				@Override
				public int resolveY(WorldGenerationContext pContext)
				{
					return IEServerConfig.getRawConfig().getInt(min);
				}
			};
			VerticalAnchor vaMax = new VerticalAnchor(0)
			{
				@Override
				public int resolveY(WorldGenerationContext pContext)
				{
					return IEServerConfig.getRawConfig().getInt(max);
				}
			};
			return dist==OreDistribution.TRAPEZOID?TrapezoidHeight.of(vaMin, vaMax): UniformHeight.of(vaMin, vaMax);
		});
	}

	public IEHeightProvider(CachedConfig.ConfigValue<OreDistribution> distribution, CachedConfig.IntValue min, CachedConfig.IntValue max)
	{
		this(distribution.getBase().getPath(), min.getBase().getPath(), max.getBase().getPath());
	}

	public IEHeightProvider(OreConfig config)
	{
		this(config.distribution, config.minY, config.maxY);
	}

	@Override
	public int sample(Random random, WorldGenerationContext context)
	{
		return this.internalProvider.get().sample(random, context);
	}

	@Override
	public HeightProviderType<?> getType()
	{
		return IEWorldGen.IE_HEIGHT_PROVIDER;
	}
}
