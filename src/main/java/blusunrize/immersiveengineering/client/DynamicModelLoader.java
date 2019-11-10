/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.obj.IEOBJModel;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator.ConfiguredModel;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//Loads models not referenced in any blockstates for rendering in TE(S)Rs
//TODO rewrite this once Forge has native OBJ support again, this should be just a few lines afterwards
@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class DynamicModelLoader
{
	private static Set<ResourceLocation> requestedTextures = new HashSet<>();
	private static Set<ResourceLocation> manualTextureRequests = new HashSet<>();
	private static Multimap<ModelWithTransforms, ModelResourceLocation> requestedModels = HashMultimap.create();
	private static Map<ModelWithTransforms, IUnbakedModel> unbakedModels = new HashMap<>();

	@SubscribeEvent
	public static void modelBake(ModelBakeEvent evt)
	{
		IELogger.logger.debug("Baking models");
		for(Entry<ModelWithTransforms, IUnbakedModel> unbaked : unbakedModels.entrySet())
		{
			ConfiguredModel conf = unbaked.getKey().model;
			IModelState state;
			if(unbaked.getKey().transforms.isEmpty())
				state = ModelRotation.getModelRotation(conf.rotationX, conf.rotationY);
			else
				state = new SimpleModelState(ImmutableMap.copyOf(unbaked.getKey().transforms));
			IBakedModel baked = unbaked.getValue().bake(evt.getModelLoader(), ModelLoader.defaultTextureGetter(),
					new BasicState(state, conf.uvLock), DefaultVertexFormats.ITEM);
			for(ModelResourceLocation mrl : requestedModels.get(unbaked.getKey()))
				evt.getModelRegistry().put(mrl, baked);
		}
	}

	@SubscribeEvent
	public static void textureStitch(TextureStitchEvent.Pre evt)
	{
		IELogger.logger.debug("Stitching textures!");
		for(ResourceLocation rl : manualTextureRequests)
			evt.addSprite(rl);
		for(ResourceLocation rl : requestedTextures)
			evt.addSprite(rl);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void modelRegistry(ModelRegistryEvent evt)
	{
		final IResourceManager manager = Minecraft.getInstance().getResourceManager();
		requestedTextures.clear();
		unbakedModels.clear();
		try
		{
			for(ModelWithTransforms reqModel : requestedModels.keySet())
			{
				ResourceLocation name = reqModel.model.name.getLocation();
				IResource asResource = manager.getResource(new ResourceLocation(name.getNamespace(), "models/"+name.getPath()));
				IUnbakedModel unbaked = new OBJModel.Parser(asResource, manager).parse();
				if(name.getPath().endsWith(".obj.ie"))
					unbaked = new IEOBJModel(((OBJModel)unbaked).getMatLib(), name);
				unbaked = unbaked
						.process(reqModel.model.getAddtionalDataAsStrings())
						.retexture(reqModel.model.retexture);
				requestedTextures.addAll(unbaked.getTextures(ModelLoader.defaultModelGetter(), ImmutableSet.of()));
				unbakedModels.put(reqModel, unbaked);
			}
		} catch(Exception x)
		{
			throw new RuntimeException(x);
		}
	}

	public static void requestTexture(ResourceLocation name)
	{
		manualTextureRequests.add(name);
	}

	public static void requestModel(ConfiguredModel reqModel, ModelResourceLocation name)
	{
		requestModel(reqModel, name, ImmutableMap.of());
	}

	public static void requestModel(ConfiguredModel reqModel, ModelResourceLocation name,
									Map<TransformType, TRSRTransformation> transforms)
	{
		requestedModels.put(new ModelWithTransforms(reqModel, transforms), name);
	}

	private static class ModelWithTransforms
	{
		final ConfiguredModel model;
		final Map<TransformType, TRSRTransformation> transforms;

		private ModelWithTransforms(ConfiguredModel model, Map<TransformType, TRSRTransformation> transforms)
		{
			this.model = model;
			this.transforms = transforms;
		}
	}
}
