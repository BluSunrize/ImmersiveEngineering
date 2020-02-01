/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.WrappedUnbakedModel;
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
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
				if(new ResourceLocation("forge", "dynbucket").equals(name))
					unbaked = new UnbakedDynBucket(new ModelDynBucket());
				else
					unbaked = ModelLoader.defaultModelGetter().apply(name);
				if(VANILLA_MODEL_WRAPPER.isInstance(unbaked))
				{
					IResource asResource = manager.getResource(new ResourceLocation(name.getNamespace(), "models/"+name.getPath()+".json"));
					BlockModel model = BlockModel.deserialize(new InputStreamReader(asResource.getInputStream()));
					if(model.getParentLocation()!=null)
						model.parent = (BlockModel)getVanillaModel(model.getParentLocation());
					unbaked = new BlockModelWrapper(model);
				}
				unbaked = unbaked
						.process(reqModel.model.getAddtionalDataAsStrings())
						.retexture(reqModel.model.retexture);
				Set<String> missingTexErrors = new HashSet<>();
				requestedTextures.addAll(unbaked.getTextures(DynamicModelLoader::getVanillaModel, missingTexErrors));
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
	private static class UnbakedDynBucket extends WrappedUnbakedModel
	{
		private UnbakedDynBucket(IUnbakedModel actual)
		{
			super(actual);
		}

		@Override
		public IUnbakedModel process(ImmutableMap<String, String> customData)
		{
			Map<String, String> fixedData = new HashMap<>(customData);
			fixedData.compute("fluid", (key, value) -> value.replace("\"", ""));
			return super.process(ImmutableMap.copyOf(fixedData));
		}

		@Override
		protected WrappedUnbakedModel newInstance(IUnbakedModel base)
		{
			return new UnbakedDynBucket(base);
		}
	}

	private static class BlockModelWrapper extends WrappedUnbakedModel
	{

		public BlockModelWrapper(IUnbakedModel base)
		{
			super(base);
		}

		@Nullable
		@Override
		public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format)
		{
			IBakedModel base = super.bake(bakery, spriteGetter, sprite, format);
			if(base!=null)
				return new PerspectiveMapWrapper(
						base, sprite.getState()
				);
			else
				return base;
		}

		@Override
		public IUnbakedModel retexture(ImmutableMap<String, String> newTextures)
		{
			BlockModel b = (BlockModel)base;
			Map<String, String> textures = new HashMap<>(b.textures);
			for(Map.Entry<String, String> e : textures.entrySet())
				if(e.getValue().charAt(0)=='#')
				{
					String key = e.getValue().substring(1);
					if(newTextures.containsKey(key))
						textures.put(e.getKey(), newTextures.get(key));
				}
			textures.putAll(newTextures);
			return newInstance(new BlockModel(
					b.getParentLocation(), b.getElements(), textures, b.isAmbientOcclusion(), b.isGui3d(),
					b.getAllTransforms(), b.getOverrides()
			));
		}

		@Override
		protected WrappedUnbakedModel newInstance(IUnbakedModel base)
		{
			return new BlockModelWrapper(base);
		}
	}
}
