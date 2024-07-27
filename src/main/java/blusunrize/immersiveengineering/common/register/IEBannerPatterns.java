/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.common.blocks.metal.WarningSignBlock.WarningSignIcon;
import blusunrize.immersiveengineering.common.register.IEItems.ItemRegObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class IEBannerPatterns
{
	public static final List<BannerEntry> ALL_BANNERS = new ArrayList<>();
	public static final BannerEntry HAMMER = addBanner("hammer", "hmr", "head", "grip");
	public static final BannerEntry WIRECUTTER = addBanner("wirecutter", "wct", "head", "grip");
	public static final BannerEntry SCREWDRIVER = addBanner("screwdriver", "scd", "head", "grip");
	public static final BannerEntry BEVELS = addBanner("bevels", "bvl");
	public static final BannerEntry ORNATE = addBanner("ornate", "orn");
	public static final BannerEntry TREATED_WOOD = addBanner("treated_wood", "twd");
	public static final BannerEntry WINDMILL = addBanner("windmill", "wnd");
	public static final BannerEntry WARNING = addBanner("warning", "wrn",
			Arrays.stream(WarningSignIcon.values())
					.filter(WarningSignIcon::hasBanner)
					.map(WarningSignIcon::getSerializedName)
					.toArray(String[]::new)
	);
	public static final BannerEntry WOLF = addBanner("wolf", "wlf", "r", "l");

	private static BannerEntry addBanner(String name, String hashName, String... subdesigns)
	{
		ResourceKey<BannerPattern> pattern = ResourceKey.create(Registries.BANNER_PATTERN, ieLoc(name));
		TagKey<BannerPattern> tag = TagKey.create(Registries.BANNER_PATTERN, pattern.location());
		ItemRegObject<BannerPatternItem> item = IEItems.register("bannerpattern_"+name, () -> new BannerPatternItem(
				tag, new Properties()
		));
		BannerEntry result = new BannerEntry(name, pattern, tag, item, hashName);
		for(String design : subdesigns)
			result.patterns().add(
					ResourceKey.create(Registries.BANNER_PATTERN, ieLoc(name+"_"+design))
			);
		ALL_BANNERS.add(result);
		return result;
	}

	public static void init()
	{
	}

	public record BannerEntry(
			String name,
			List<ResourceKey<BannerPattern>> patterns,
			TagKey<BannerPattern> tag,
			IEItems.ItemRegObject<BannerPatternItem> item,
			String hashName
	)
	{
		public BannerEntry(
				String name,
				ResourceKey<BannerPattern> pattern,
				TagKey<BannerPattern> tag,
				ItemRegObject<BannerPatternItem> item,
				String hashName
		)
		{
			this(name, new ArrayList<>(), tag, item, hashName);
			this.patterns.add(pattern);
		}
	}
}
