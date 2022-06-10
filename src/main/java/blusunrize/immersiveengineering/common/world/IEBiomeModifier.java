package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.api.Lib;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.RegistryObject;

public class IEBiomeModifier implements BiomeModifier
{
	private static final DeferredRegister<Codec<? extends BiomeModifier>> REGISTER = DeferredRegister.create(
			Keys.BIOME_MODIFIER_SERIALIZERS, Lib.MODID
	);
	// TODO we may want to move configuration from this to the actual datapack
	public static final RegistryObject<Codec<IEBiomeModifier>> IE_MODIFIER = REGISTER.register(
			"config_driven", () -> Codec.unit(new IEBiomeModifier())
	);

	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	@Override
	public void modify(Holder<Biome> biome, Phase phase, Builder builder)
	{
		if(phase==Phase.ADD)
			IEWorldGen.addOresTo(builder.getGenerationSettings());
	}

	@Override
	public Codec<? extends BiomeModifier> codec()
	{
		return IE_MODIFIER.get();
	}
}
