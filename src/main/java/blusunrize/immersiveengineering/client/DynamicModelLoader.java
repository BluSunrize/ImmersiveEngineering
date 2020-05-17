/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.SimpleUVModelTransform;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.ExpandedBlockModelDeserializer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//Loads models not referenced in any blockstates for rendering in TE(S)Rs
@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class DynamicModelLoader
{
	private static Set<Material> requestedTextures = new HashSet<>();
	private static Set<ResourceLocation> manualTextureRequests = new HashSet<>();
	private static final Multimap<ModelWithTransforms, ModelResourceLocation> requestedModels = HashMultimap.create();
	private static Map<ModelWithTransforms, IUnbakedModel> unbakedModels = new HashMap<>();

	@SubscribeEvent
	public static void modelBake(ModelBakeEvent evt)
	{
		IELogger.logger.debug("Baking models");
		for(Entry<ModelWithTransforms, IUnbakedModel> unbaked : unbakedModels.entrySet())
		{
			ModelRequest conf = unbaked.getKey().model;
			IModelTransform state;
			if(unbaked.getKey().transforms.isEmpty())
				state = ModelRotation.getModelRotation(conf.rotX, conf.rotY);
			else
				state = new SimpleUVModelTransform(ImmutableMap.copyOf(unbaked.getKey().transforms), conf.uvLock);
			IBakedModel baked = unbaked.getValue().bakeModel(evt.getModelLoader(), ModelLoader.defaultTextureGetter(),
					state, conf.name);
			synchronized(requestedModels)
			{
				for(ModelResourceLocation mrl : requestedModels.get(unbaked.getKey()))
					evt.getModelRegistry().put(mrl, baked);
			}
		}
	}

	@SubscribeEvent
	public static void textureStitch(TextureStitchEvent.Pre evt)
	{
		if(evt.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE))
			return;
		IELogger.logger.debug("Loading dynamic models");
		try
		{
			//apparently this is needed to prevent CMEs. TODO Figure out the threads involved and how to solve this in a nicer way.
			synchronized(requestedModels)
			{
				for(ModelWithTransforms reqModel : requestedModels.keySet())
				{
					BlockModel model = ExpandedBlockModelDeserializer.INSTANCE.fromJson(reqModel.model.data, BlockModel.class);
					Set<Pair<String, String>> missingTexErrors = new HashSet<>();
					requestedTextures.addAll(model.getTextures(DynamicModelLoader::getVanillaModel, missingTexErrors));
					if(!missingTexErrors.isEmpty())
						throw new RuntimeException("Missing textures: "+missingTexErrors);
					unbakedModels.put(reqModel, model);
				}
			}
		} catch(Exception x)
		{
			x.printStackTrace();
			//TODO mostly for dev
			System.exit(1);
		}
		IELogger.logger.debug("Stitching textures!");
		for(ResourceLocation rl : manualTextureRequests)
			evt.addSprite(rl);
		for(Material rl : requestedTextures)
			evt.addSprite(rl.getTextureLocation());
	}

	private static Class<? extends IUnbakedModel> VANILLA_MODEL_WRAPPER;
	private static Field BASE_MODEL;

	static
	{
		try
		{
			//TODO update?
			VANILLA_MODEL_WRAPPER = (Class<? extends IUnbakedModel>)Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
			BASE_MODEL = VANILLA_MODEL_WRAPPER.getDeclaredField("model");
			BASE_MODEL.setAccessible(true);
		} catch(ClassNotFoundException|NoSuchFieldException e)
		{
			e.printStackTrace();
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void modelRegistry(ModelRegistryEvent evt)
	{
		requestedTextures.clear();
		unbakedModels.clear();
	}

	private static IUnbakedModel getVanillaModel(ResourceLocation loc)
	{
		if(loc.getPath().equals("builtin/generated"))
			return Util.make(BlockModel.deserialize("{}"), (p_209273_0_) -> {
				p_209273_0_.name = "generation marker";
			});
		else
		{
			IUnbakedModel wrapper = ModelLoader.defaultModelGetter().apply(loc);
			if(VANILLA_MODEL_WRAPPER.isInstance(wrapper))
			{
				try
				{
					return (BlockModel)BASE_MODEL.get(wrapper);
				} catch(IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}
			}
			else
				return wrapper;
		}
	}

	public static void requestTexture(ResourceLocation name)
	{
		manualTextureRequests.add(name);
	}

	public static void requestModel(ModelRequest reqModel, ModelResourceLocation name)
	{
		requestModel(reqModel, name, ImmutableMap.of());
	}

	public static void requestModel(ModelRequest reqModel, ModelResourceLocation name,
									Map<TransformType, TransformationMatrix> transforms)
	{
		synchronized(requestedModels)
		{
			requestedModels.put(new ModelWithTransforms(reqModel, transforms), name);
		}
	}

	public static class ModelRequest
	{
		private final JsonObject data;
		private final int rotX;
		private final int rotY;
		private final boolean uvLock;
		private final ResourceLocation name;

		public ModelRequest(ResourceLocation name, ResourceLocation loader, JsonObject data, int rotX, int rotY, boolean uvLock)
		{
			this.name = name;
			//TODO copy?
			this.data = data;
			this.rotX = rotX;
			this.rotY = rotY;
			this.uvLock = uvLock;
			Preconditions.checkArgument(!data.has("loader"));
			this.data.addProperty("loader", loader.toString());
		}

		public static ModelRequest ieObj(ResourceLocation loc, int rotY) {
			return withModel(loc, new ResourceLocation(ImmersiveEngineering.MODID, "ie_obj"), rotY);
		}

		public static ModelRequest obj(ResourceLocation loc, int rotY)
		{
			return withModel(loc, new ResourceLocation("forge", "obj"), rotY);
		}

		private static ModelRequest withModel(ResourceLocation model, ResourceLocation loader, int rotY)
		{
			JsonObject json = new JsonObject();
			json.addProperty("model", new ResourceLocation(model.getNamespace(), "models/"+model.getPath()).toString());
			json.addProperty("flip-v", true);
			return new ModelRequest(model, loader, json, 0, rotY, true);
		}
	}

	private static class ModelWithTransforms
	{
		final ModelRequest model;
		final Map<TransformType, TransformationMatrix> transforms;

		private ModelWithTransforms(ModelRequest model, Map<TransformType, TransformationMatrix> transforms)
		{
			this.model = model;
			this.transforms = transforms;
		}
	}
}
