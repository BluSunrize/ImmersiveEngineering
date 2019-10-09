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
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator.ConfiguredModel;
import blusunrize.immersiveengineering.common.data.model.ModelFile.ExistingModelFile;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
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
import java.util.*;
import java.util.Map.Entry;

@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class ObjLoaderWorkaround
{
	private static Set<ResourceLocation> requestedTextures = new HashSet<>();
	private static Multimap<ConfiguredModel, ModelResourceLocation> requestedModels = HashMultimap.create();
	private static Map<ConfiguredModel, IUnbakedModel> unbakedModels = new HashMap<>();

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
					ResourceLocation name = new ResourceLocation(val.get("model").getAsString());
					if(name.getPath().endsWith(".obj"))
					{
						int xRot = Optional.ofNullable(val.get("x")).map(JsonElement::getAsInt).orElse(0);
						int yRot = Optional.ofNullable(val.get("y")).map(JsonElement::getAsInt).orElse(0);
						boolean uvLock = Optional.ofNullable(val.get("uvlock")).map(JsonElement::getAsBoolean).orElse(false);
						ImmutableMap.Builder<String, Object> remaining = new Builder<>();
						for(Entry<String, JsonElement> e : val.entrySet())
						{
							String key = e.getKey();
							if(!"model".equals(key)&&!"x".equals(key)&&!"y".equals(key)&&!"uvlock".equals(key))
								remaining.put(key, e.getValue());
						}
						requestedModels.put(
								new ConfiguredModel(new ExistingModelFile(name), xRot, yRot, uvLock, remaining.build()),
								new ModelResourceLocation(blockName, entry.getKey())
						);
					}
				}
			} catch(IOException x)
			{
			}
		}
		try
		{
			for(ConfiguredModel reqModel : requestedModels.keySet())
			{
				ResourceLocation name = reqModel.name.getLocation();
				IResource asResource = manager.getResource(new ResourceLocation(name.getNamespace(), "models/"+name.getPath()));
				IUnbakedModel unbaked = new OBJModel.Parser(asResource, manager).parse();
				unbaked = unbaked.process(reqModel.getAddtionalDataAsStrings());
				requestedTextures.addAll(unbaked.getTextures(ModelLoader.defaultModelGetter(), ImmutableSet.of()));
				unbakedModels.put(reqModel, unbaked);
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
		for(Entry<ConfiguredModel, IUnbakedModel> unbaked : unbakedModels.entrySet())
		{
			ConfiguredModel conf = unbaked.getKey();
			IBakedModel baked = unbaked.getValue().bake(evt.getModelLoader(), ModelLoader.defaultTextureGetter(),
					new BasicState(ModelRotation.getModelRotation(conf.rotationX, conf.rotationY), conf.uvLock),
					DefaultVertexFormats.ITEM);
			for(ModelResourceLocation mrl : requestedModels.get(unbaked.getKey()))
				evt.getModelRegistry().put(mrl, baked);
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
