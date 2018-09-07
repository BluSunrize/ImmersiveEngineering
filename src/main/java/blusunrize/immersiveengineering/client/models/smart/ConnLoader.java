/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.smart;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.ModelData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class ConnLoader implements ICustomModelLoader
{
	public static final String RESOURCE_LOCATION = "models/block/smartmodel/conn_";
	public static final ResourceLocation DATA_BASED_LOC = new ResourceLocation(ImmersiveEngineering.MODID, "models/block/smartmodel/connector");
	//Do not manually write to these in IE 0.12-77+. Use blusunrize.immersiveengineering.api.energy.wires.WireApi.registerConnectorForRender()
	public static Map<String, ImmutableMap<String, String>> textureReplacements = new HashMap<>();
	public static Map<String, ResourceLocation> baseModels = new HashMap<>();

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
		ConnModelReal.cache.invalidateAll();
	}

	@Override
	public boolean accepts(@Nonnull ResourceLocation modelLocation)
	{
		return modelLocation.equals(DATA_BASED_LOC)||modelLocation.getResourcePath().contains(RESOURCE_LOCATION);
	}

	@Nonnull
	@Override
	public IModel loadModel(@Nonnull ResourceLocation modelLocation)
	{
		if(modelLocation.equals(DATA_BASED_LOC))
			return new ConnModelBase();
		String resourcePath = modelLocation.getResourcePath();
		int pos = resourcePath.indexOf("conn_");
		if(pos >= 0)
		{
			pos += 5;// length of "conn_"
			String name = resourcePath.substring(pos);
			ResourceLocation r = baseModels.get(name);
			if(r!=null)
			{
				if(textureReplacements.containsKey(name))
					return new ConnModelBase(r, textureReplacements.get(name), ImmutableMap.of());
				else
					return new ConnModelBase(r);
			}
		}
		return ModelLoaderRegistry.getMissingModel();
	}

	private static class ConnModelBase implements IModel
	{
		private static final ResourceLocation WIRE_LOC = new ResourceLocation(ImmersiveEngineering.MODID.toLowerCase(Locale.ENGLISH)+":blocks/wire");
		@Nullable
		private final ModelData baseData;

		public ConnModelBase(@Nonnull ResourceLocation b, @Nonnull ImmutableMap<String, String> t,
							 @Nonnull ImmutableMap<String, String> customBase)
		{
			this(new ModelData(b, ModelData.asJsonObject(customBase), t));
		}

		public ConnModelBase(@Nonnull ResourceLocation b)
		{
			this(b, ImmutableMap.of(), ImmutableMap.of());
		}

		public ConnModelBase()
		{
			baseData = null;
		}

		public ConnModelBase(@Nullable ModelData newData)
		{
			this.baseData = newData;
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			if(baseData==null)
				return ImmutableList.of();
			baseData.attemptToLoad(false);
			if(baseData.getModel()!=null)
			{
				List<ResourceLocation> ret = new ArrayList<>(baseData.getModel().getDependencies());
				ret.add(0, baseData.location);
				return ret;
			}
			else
				return ImmutableList.of(baseData.location);
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getTextures()
		{
			if(baseData==null)
				return ImmutableList.of();
			baseData.attemptToLoad(false);
			if(baseData.getModel()!=null)
			{
				List<ResourceLocation> ret = new ArrayList<>(baseData.getModel().getTextures());
				ret.add(WIRE_LOC);
				return ret;
			}
			else
				return ImmutableList.of(WIRE_LOC);
		}

		@Nonnull
		@Override
		public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
		{
			assert baseData!=null;
			baseData.attemptToLoad(true);
			assert baseData.getModel()!=null;
			return new ConnModelReal(baseData.getModel().bake(state, format, bakedTextureGetter));
		}

		private static final ImmutableSet<String> ownKeys = ImmutableSet.of("base", "custom", "textures");

		@Nonnull
		@Override
		public IModel process(ImmutableMap<String, String> customData)
		{
			ModelData newData = ModelData.fromMap(customData, ownKeys, "base");
			if(!newData.equals(baseData))
				return new ConnModelBase(newData);
			return this;
		}
	}
}
