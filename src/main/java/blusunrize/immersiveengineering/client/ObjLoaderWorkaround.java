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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeBlockStateV1.TRSRDeserializer;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IRegistryDelegate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static net.minecraftforge.eventbus.api.EventPriority.LOW;

@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class ObjLoaderWorkaround
{
	@SubscribeEvent
	public static void modelRegistry(ModelRegistryEvent evt)
	{
		IELogger.logger.debug("Registring obj models");
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
					if(shouldLoad(name))
					{
						int xRot = Optional.ofNullable(val.get("x")).map(JsonElement::getAsInt).orElse(0);
						int yRot = Optional.ofNullable(val.get("y")).map(JsonElement::getAsInt).orElse(0);
						boolean uvLock = Optional.ofNullable(val.get("uvlock")).map(JsonElement::getAsBoolean).orElse(false);
						ImmutableMap.Builder<String, Object> custom = new Builder<>();
						ImmutableMap.Builder<String, String> textures = new Builder<>();
						if(val.has("custom"))
							for(Entry<String, JsonElement> e : val.getAsJsonObject("custom").entrySet())
								custom.put(e.getKey(), e.getValue());
						if(val.has("textures"))
							for(Entry<String, JsonElement> e : val.getAsJsonObject("textures").entrySet())
								textures.put(e.getKey(), e.getValue().getAsString());
						DynamicModelLoader.requestModel(
								new ConfiguredModel(new ExistingModelFile(name), xRot, yRot, uvLock, custom.build(), textures.build()),
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
			//TODO obfuscation?
			Function<Item, ModelResourceLocation> toLoc;
			ItemModelMesher mesher = ClientUtils.mc().getItemRenderer().getItemModelMesher();
			if(mesher instanceof ItemModelMesherForge)
			{
				Field modelLocField = ItemModelMesherForge.class.getDeclaredField("locations");
				modelLocField.setAccessible(true);
				Map<IRegistryDelegate<Item>, ModelResourceLocation> idMap = (Map<IRegistryDelegate<Item>, ModelResourceLocation>)modelLocField.get(mesher);
				toLoc = item -> idMap.get(item.delegate);
			}
			else
			{
				Field modelLocField = ItemModelMesher.class.getDeclaredField("modelLocations");
				modelLocField.setAccessible(true);
				Int2ObjectMap<ModelResourceLocation> idMap = (Int2ObjectMap<ModelResourceLocation>)modelLocField.get(mesher);
				toLoc = item -> idMap.get(Item.getIdFromItem(item));
			}
			Gson GSON = new GsonBuilder().registerTypeAdapter(TRSRTransformation.class, TRSRDeserializer.INSTANCE).create();
			for(Item item : IEContent.registeredIEItems)
			{
				try
				{
					ModelResourceLocation modelLoc = toLoc.apply(item);
					ResourceLocation itemName = item.getRegistryName();
					ResourceLocation jsonLoc = new ResourceLocation(itemName.getNamespace(), "models/item/"+itemName.getPath()+".json");
					IResource jsonResource = manager.getResource(jsonLoc);
					InputStream in = jsonResource.getInputStream();
					JsonObject json = JSONUtils.fromJson(new InputStreamReader(in));
					if(json.has("parent"))
					{
						ResourceLocation parent = new ResourceLocation(json.get("parent").getAsString());
						if(shouldLoad(parent))
						{
							Map<TransformType, TRSRTransformation> perspectives = new HashMap<>();
							if(json.has("display-trsr"))
							{
								JsonObject displayJson = json.getAsJsonObject("display-trsr");
								for(TransformType type : TransformType.values())
								{
									String name = type.name().toLowerCase(Locale.ENGLISH);
									if(displayJson.has(name))
									{
										JsonObject obj = displayJson.getAsJsonObject(name);
										TRSRTransformation transform = GSON.fromJson(obj, TRSRTransformation.class);
										Preconditions.checkNotNull(transform);
										perspectives.put(type, transform);
									}
								}
							}
							ImmutableMap.Builder<String, Object> remaining = new Builder<>();
							for(Entry<String, JsonElement> e : json.entrySet())
							{
								String key = e.getKey();
								if(!key.equals("parent")&&!key.equals("display-trsr"))
									remaining.put(key, e.getValue());
							}
							remaining.put("flip-v", true);
							DynamicModelLoader.requestModel(new ConfiguredModel(new ExistingModelFile(parent), 0, 0, false,
									remaining.build()), modelLoc, perspectives);
						}
					}
				} catch(IOException ioxcp)
				{
				}
			}
		} catch(NoSuchFieldException|IllegalAccessException x)
		{
			x.printStackTrace();
			System.exit(3);
			throw new RuntimeException(x);
		}
	}

	@SubscribeEvent(priority = LOW)
	public static void modelBake(ModelBakeEvent evt)
	{
		ClientUtils.mc().getItemRenderer().getItemModelMesher().rebuildCache();
	}

	private static boolean shouldLoad(ResourceLocation name)
	{
		return name.getPath().endsWith(".obj")||name.getPath().endsWith(".obj.ie")||"connector".equals(name.getPath());
	}
}
