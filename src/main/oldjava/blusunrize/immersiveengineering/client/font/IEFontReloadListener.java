/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.font;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.fonts.providers.DefaultGlyphProvider;
import net.minecraft.client.gui.fonts.providers.GlyphProviderTypes;
import net.minecraft.client.gui.fonts.providers.IGlyphProvider;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

//TODO more async
public class IEFontReloadListener extends ReloadListener<Void>
{
	@Override
	protected Void prepare(IResourceManager resourceManagerIn, IProfiler profilerIn)
	{
		return null;
	}

	@Override
	protected void apply(Void splashList, IResourceManager resourceManagerIn, IProfiler profilerIn)
	{
		if(ClientProxy.nixieFontOptional==null)
		{
			ClientProxy.nixieFontOptional = new NixieFontRender(false, new ResourceLocation(ImmersiveEngineering.MODID, "nixie_opt"));
			ClientProxy.nixieFont = new NixieFontRender(false, new ResourceLocation(ImmersiveEngineering.MODID, "nixie"));
			ClientProxy.itemFont = new IEFontRender(false, new ResourceLocation(ImmersiveEngineering.MODID, "item_font"));
		}
		reloadFontRender(ClientProxy.itemFont, resourceManagerIn);
		reloadFontRender(ClientProxy.nixieFont, resourceManagerIn);
		reloadFontRender(ClientProxy.nixieFontOptional, resourceManagerIn);
	}

	private void reloadFontRender(IEFontRender render, IResourceManager resourceManagerIn)
	{
		// Based on code in FontResourceManager
		//TODO this could be in prepare
		ResourceLocation resourcelocation = render.getBaseID();

		Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
		List<IGlyphProvider> list = Lists.newArrayList(new DefaultGlyphProvider());
		try
		{
			for(IResource iresource : resourceManagerIn.getAllResources(new ResourceLocation(resourcelocation.getNamespace(), "font/"+resourcelocation.getPath()+".json")))
			{
				try(
						InputStream inputstream = iresource.getInputStream();
						Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
				)
				{
					JsonArray jsonarray = JSONUtils.getJsonArray(JSONUtils.fromJson(gson, reader, JsonObject.class), "providers");

					for(int i = jsonarray.size()-1; i >= 0; --i)
					{
						JsonObject jsonobject = JSONUtils.getJsonObject(jsonarray.get(i), "providers["+i+"]");

						try
						{
							String s1 = JSONUtils.getString(jsonobject, "type");
							GlyphProviderTypes glyphprovidertypes = GlyphProviderTypes.byName(s1);
							boolean shouldAdd = glyphprovidertypes==GlyphProviderTypes.LEGACY_UNICODE;
							shouldAdd |= !resourcelocation.equals(Minecraft.DEFAULT_FONT_RENDERER_NAME);
							shouldAdd |= !Minecraft.getInstance().getForceUnicodeFont();
							if(shouldAdd)
							{
								list.add(glyphprovidertypes.getFactory(jsonobject).create(resourceManagerIn));
							}
						} catch(RuntimeException runtimeexception)
						{
							IELogger.logger.warn("Unable to read definition '{}' in fonts.json in resourcepack: '{}': {}", resourcelocation, iresource.getPackName(), runtimeexception.getMessage());
						}
					}

				} catch(RuntimeException runtimeexception1)
				{
					IELogger.logger.warn("Unable to load font '{}' in fonts.json in resourcepack: '{}': {}", resourcelocation, iresource.getPackName(), runtimeexception1.getMessage());
				}

			}
		} catch(IOException ioexception)
		{
			IELogger.logger.warn("Unable to load font '{}' in fonts.json: {}", resourcelocation, ioexception.getMessage());
		}
		//This is the sync part
		Collections.reverse(list);
		render.setGlyphProviders(list);
	}
}
