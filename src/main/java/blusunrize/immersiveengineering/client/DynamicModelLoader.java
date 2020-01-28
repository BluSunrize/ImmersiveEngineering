/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.ModelConfigurableSides;
import blusunrize.immersiveengineering.client.models.ModelConveyor.RawConveyorModel;
import blusunrize.immersiveengineering.client.models.ModelCoresample.RawCoresampleModel;
import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader.ConnectorModel;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader;
import blusunrize.immersiveengineering.client.models.obj.IEOBJModel;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator.ConfiguredModel;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

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
		if(evt.getMap()!=mc().getTextureMap())
			return;
		for(ResourceLocation rl : manualTextureRequests)
			evt.addSprite(rl);
		for(ResourceLocation rl : requestedTextures)
			evt.addSprite(rl);
	}

	private static Class<? extends IUnbakedModel> VANILLA_MODEL_WRAPPER;
	private static Field BASE_MODEL;

	static
	{
		try
		{
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
		final IResourceManager manager = Minecraft.getInstance().getResourceManager();
		requestedTextures.clear();
		unbakedModels.clear();
		try
		{
			for(ModelWithTransforms reqModel : requestedModels.keySet())
			{
				ResourceLocation name = reqModel.model.name.getLocation();
				IUnbakedModel unbaked;
				if(name.equals(new ResourceLocation(ImmersiveEngineering.MODID, "connector")))
					unbaked = new ConnectorModel();
				else if(name.equals(new ResourceLocation(ImmersiveEngineering.MODID, "coresample")))
					unbaked = new RawCoresampleModel();
				else if(name.equals(new ResourceLocation(ImmersiveEngineering.MODID, "feedthrough")))
					unbaked = new FeedthroughLoader.FeedthroughModelRaw();
				else if(name.equals(new ResourceLocation(ImmersiveEngineering.MODID, "conveyor")))
					unbaked = new RawConveyorModel();
				else if(name.getPath().contains(ModelConfigurableSides.RESOURCE_LOCATION))
					unbaked = new ModelConfigurableSides.Loader().loadModel(name);
				else if(new ResourceLocation("forge", "dynbucket").equals(name))
					unbaked = new UnbakedDynBucket(new ModelDynBucket());
				else if(name.getPath().contains(".obj"))
				{
					IResource asResource = manager.getResource(new ResourceLocation(name.getNamespace(), "models/"+name.getPath()));
					unbaked = new OBJModel.Parser(asResource, manager).parse();
					if(name.getPath().endsWith(".obj.ie"))
						unbaked = new IEOBJModel(((OBJModel)unbaked).getMatLib(), name);
				}
				else
				{
					IResource asResource = manager.getResource(new ResourceLocation(name.getNamespace(), "models/"+name.getPath()+".json"));
					BlockModel model = BlockModel.deserialize(new InputStreamReader(asResource.getInputStream()));
					if(model.getParentLocation()!=null)
					{
						if(model.getParentLocation().getPath().equals("builtin/generated"))
							model.parent = Util.make(BlockModel.deserialize("{}"), (p_209273_0_) -> {
								p_209273_0_.name = "generation marker";
							});
						else
						{
							IUnbakedModel parent = ModelLoaderRegistry.getModelOrLogError(model.getParentLocation(), "Could not load vanilla model parent '"+model.getParentLocation()+"' for '"+model);
							if(VANILLA_MODEL_WRAPPER.isInstance(parent))
								model.parent = (BlockModel)BASE_MODEL.get(parent);
							else
								throw new IllegalStateException("vanilla model '"+model+"' can't have non-vanilla parent");
						}
					}
					unbaked = model;
				}
				unbaked = unbaked
						.process(reqModel.model.getAddtionalDataAsStrings())
						.retexture(reqModel.model.retexture);
				Set<String> missingTexErrors = new HashSet<>();
				requestedTextures.addAll(unbaked.getTextures(ModelLoader.defaultModelGetter(), missingTexErrors));
				if(!missingTexErrors.isEmpty())
					throw new RuntimeException("Missing textures: "+missingTexErrors);
				unbakedModels.put(reqModel, unbaked);
			}
		} catch(Exception x)
		{
			x.printStackTrace();
			//TODO mostly for dev
			System.exit(1);
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

	//Forge's one crashes because the RL is quoted.
	//TODO is this a bug in Forge or on our side?
	private static class UnbakedDynBucket implements IUnbakedModel
	{
		private final ModelDynBucket actual;

		private UnbakedDynBucket(ModelDynBucket actual)
		{
			this.actual = actual;
		}

		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			return actual.getDependencies();
		}

		@Override
		public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors)
		{
			return actual.getTextures(modelGetter, missingTextureErrors);
		}

		@Nullable
		@Override
		public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format)
		{
			return actual.bake(bakery, spriteGetter, sprite, format);
		}

		@Override
		public IUnbakedModel process(ImmutableMap<String, String> customData)
		{
			Map<String, String> fixedData = new HashMap<>(customData);
			fixedData.compute("fluid", (key, value) -> value.replace("\"", ""));
			return actual.process(ImmutableMap.copyOf(fixedData));
		}

		@Override
		public IUnbakedModel retexture(ImmutableMap<String, String> textures)
		{
			return new UnbakedDynBucket(actual.retexture(textures));
		}
	}
}
