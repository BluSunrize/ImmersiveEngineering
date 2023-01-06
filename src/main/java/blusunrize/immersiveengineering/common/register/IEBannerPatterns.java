/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEItems.ItemRegObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class IEBannerPatterns
{
	private static final DeferredRegister<BannerPattern> REGISTER = DeferredRegister.create(
			Registries.BANNER_PATTERN, Lib.MODID
	);
	public static final List<BannerEntry> ALL_BANNERS = new ArrayList<>();
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
		TagKey<BannerPattern> tag = TagKey.create(Registries.BANNER_PATTERN, pattern.getId());
		ItemRegObject<BannerPatternItem> item = IEItems.register("bannerpattern_"+name, () -> new BannerPatternItem(
				tag, new Properties()
		));
		BannerEntry result = new BannerEntry(pattern, tag, item);
		ALL_BANNERS.add(result);
		return result;
	}

	public record BannerEntry(
			RegistryObject<BannerPattern> pattern,
			TagKey<BannerPattern> tag,
			IEItems.ItemRegObject<BannerPatternItem> item
	)
	{
	}
}
