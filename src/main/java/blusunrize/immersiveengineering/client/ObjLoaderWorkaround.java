/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class ObjLoaderWorkaround
{
	private static final Gson GSON = new Gson();

	private static Set<ResourceLocation> requestedTextures = new HashSet<>();
	private static Multimap<ResourceLocation, ModelResourceLocation> requestedModels = HashMultimap.create();

	@SubscribeEvent
	public static void modelRegistry(ModelRegistryEvent evt)
	{
		IELogger.logger.debug("Registring models");
		final IResourceManager manager = Minecraft.getInstance().getResourceManager();
		for(Block b : IEContent.registeredIEBlocks)
		{
			ResourceLocation blockName = b.getRegistryName();
			Preconditions.checkNotNull(blockName);
			ResourceLocation jsonLoc = new ResourceLocation(blockName.getNamespace(), "blockstates/"+blockName.getPath()+".json");
			try
			{

				IResource jsonResource = manager.getResource(jsonLoc);
				InputStream in = jsonResource.getInputStream();
				JsonObject json = JSONUtils.fromJson(new InputStreamReader(in));
				//TODO support multipart?
				if(!json.has("variants"))
					continue;
				JsonObject variants = json.getAsJsonObject("variants");
				for(Entry<String, JsonElement> entry : variants.entrySet())
				{
					if(!entry.getValue().isJsonObject())
						continue;
					JsonObject val = entry.getValue().getAsJsonObject();
					ResourceLocation model = new ResourceLocation(val.get("model").getAsString());
					if(model.getPath().endsWith(".obj"))
						requestedModels.put(model, new ModelResourceLocation(blockName, entry.getKey()));
				}
			} catch(IOException x)
			{

			}
		}
		try
		{
			for(ResourceLocation reqModel : requestedModels.keySet())
			{
				IResource asResource = manager.getResource(new ResourceLocation(reqModel.getNamespace(), "models/"+reqModel.getPath()));
				IUnbakedModel unbaked = new OBJModel.Parser(asResource, manager).parse();
				requestedTextures.addAll(unbaked.getTextures(ModelLoader.defaultModelGetter(), ImmutableSet.of()));
			}
		} catch(IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@SubscribeEvent
	public static void modelBake(ModelBakeEvent evt)
	{
		IELogger.logger.debug("Baking models");
		final IResourceManager manager = Minecraft.getInstance().getResourceManager();
		try
		{
			for(ResourceLocation reqModel : requestedModels.keySet())
			{
				IResource asResource = manager.getResource(new ResourceLocation(reqModel.getNamespace(), "models/"+reqModel.getPath()));
				IUnbakedModel unbaked = new OBJModel.Parser(asResource, manager).parse();
				unbaked = unbaked.process(ImmutableMap.of("flip-v", "true"));
				//TODO parse rotation+uvlock from JSON
				IBakedModel baked = unbaked.bake(evt.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(ModelRotation.X0_Y0, false), DefaultVertexFormats.ITEM);
				for(ModelResourceLocation mrl : requestedModels.get(reqModel))
					evt.getModelRegistry().put(mrl, baked);
			}
		} catch(IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@SubscribeEvent
	public static void textureStitch(TextureStitchEvent.Pre evt)
	{
		IELogger.logger.debug("Stitching textures!");
		for(ResourceLocation rl : requestedTextures)
		{
			evt.addSprite(rl);
		}
	}
}
