package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEItems.ItemRegObject;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// TODO fill tags correctly!
public class IEBannerPatterns
{
	public static final DeferredRegister<BannerPattern> REGISTER = DeferredRegister.create(
			Registry.BANNER_PATTERN_REGISTRY, Lib.MODID
	);
	public static final BannerEntry HAMMER = addBanner("hammer", "hmr");
	public static final BannerEntry BEVELS = addBanner("bevels", "bvl");
	public static final BannerEntry ORNATE = addBanner("ornate", "orn");
	public static final BannerEntry TREATED_WOOD = addBanner("treated_wood", "twd");
	public static final BannerEntry WINDMILL = addBanner("windmill", "wnd");
	public static final BannerEntry WOLF_R = addBanner("wolf_r", "wlfr");
	public static final BannerEntry WOLF_L = addBanner("wolf_l", "wlfl");
	public static final BannerEntry WOLF = addBanner("wolf", "wlf");

	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	private static BannerEntry addBanner(String name, String hashName)
	{
		RegistryObject<BannerPattern> pattern = REGISTER.register(name, () -> new BannerPattern("ie_"+hashName));
		TagKey<BannerPattern> tag = TagKey.create(Registry.BANNER_PATTERN_REGISTRY, pattern.getId());
		ItemRegObject<BannerPatternItem> item = IEItems.register("pattern_"+name, () -> new BannerPatternItem(
				tag, new Properties().tab(ImmersiveEngineering.ITEM_GROUP)
		));
		return new BannerEntry(pattern, tag, item);
	}

	public record BannerEntry(
			RegistryObject<BannerPattern> pattern,
			TagKey<BannerPattern> tag,
			IEItems.ItemRegObject<BannerPatternItem> item
	)
	{
	}
}
