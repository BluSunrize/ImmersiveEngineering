package blusunrize.immersiveengineering.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;

public class IEBiomeModifier implements BiomeModifier
{
	public static final Codec<IEBiomeModifier> CODEC = Codec.unit(new IEBiomeModifier());

	@Override
	public void modify(Holder<Biome> biome, Phase phase, Builder builder)
	{
		if(phase==Phase.ADD)
		{

		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec()
	{
		return CODEC;
	}
}
