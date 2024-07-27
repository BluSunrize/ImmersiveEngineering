/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns.BannerEntry;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.io.BufferedReader;
import java.io.IOException;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class Language extends LanguageProvider
{
	final ExistingFileHelper existingFileHelper;
	private final ResourceLocation baseLangFile;

	public Language(PackOutput output, ExistingFileHelper existingFileHelper, String locale)
	{
		super(output, Lib.MODID, locale);
		this.existingFileHelper = existingFileHelper;
		this.baseLangFile = ieLoc(locale);
	}

	@Override
	protected void addTranslations()
	{
		// Load our existing resource file
		try
		{
			final Gson gson = new Gson();
			final Resource resource = existingFileHelper.getResource(baseLangFile, PackType.CLIENT_RESOURCES, ".json", "lang_base");
			final BufferedReader reader = resource.openAsReader();
			JsonObject object = GsonHelper.fromJson(gson, reader, JsonObject.class);
			for(String key : object.keySet())
				add(key, object.get(key).getAsString());
		} catch(IOException e)
		{
			throw new RuntimeException("Failure to read base language file", e);
		}

		// Add banners with their coloration
		for(BannerEntry banner : IEBannerPatterns.ALL_BANNERS)
			for(var pattern : banner.patterns())
			{
				final String key = pattern.location().getPath();
				final String[] keyParts = key.split("_");
				String bannerName = Utils.toCamelCase(keyParts[0]);
				if(keyParts.length > 1)
					bannerName += " ("+Utils.toCamelCase(keyParts[1])+")";
				for(DyeColor dye : DyeColor.values())
				{
					String color = dye.getSerializedName();
					add("block.minecraft.banner.immersiveengineering."+key+"."+color, I18n.get("color.minecraft."+color)+" "+bannerName);
				}
			}
	}
}