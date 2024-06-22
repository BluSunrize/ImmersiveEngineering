/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.register.IEItems.ItemRegObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.ArrayList;
import java.util.List;

public class IEBannerPatterns
{
	public static final List<BannerEntry> ALL_BANNERS = new ArrayList<>();
	public static final BannerEntry HAMMER = addBanner("hammer");
	public static final BannerEntry BEVELS = addBanner("bevels");
	public static final BannerEntry ORNATE = addBanner("ornate");
	public static final BannerEntry TREATED_WOOD = addBanner("treated_wood");
	public static final BannerEntry WINDMILL = addBanner("windmill");
	public static final BannerEntry WOLF_R = addBanner("wolf_r");
	public static final BannerEntry WOLF_L = addBanner("wolf_l");
	public static final BannerEntry WOLF = addBanner("wolf");

	private static BannerEntry addBanner(String name)
	{
		var key = ResourceKey.create(Registries.BANNER_PATTERN, IEApi.ieLoc(name));
		TagKey<BannerPattern> tag = TagKey.create(Registries.BANNER_PATTERN, IEApi.ieLoc(name));
		ItemRegObject<BannerPatternItem> item = IEItems.register(
				"bannerpattern_"+name, () -> new BannerPatternItem(tag, new Properties())
		);
		BannerEntry result = new BannerEntry(key, tag, item);
		ALL_BANNERS.add(result);
		return result;
	}

	public static void init()
	{
	}

	public record BannerEntry(
			ResourceKey<BannerPattern> key,
			TagKey<BannerPattern> tag,
			IEItems.ItemRegObject<BannerPatternItem> item
	)
	{
	}
}
