/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.smart;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;

import java.util.*;
import java.util.function.Function;

public class ConnLoader implements ICustomModelLoader
{
	public static final String RESOURCE_LOCATION = "models/block/smartmodel/conn_";
	public static Map<String, ImmutableMap<String, String>> textureReplacements = new HashMap<>();
	public static Map<String, ResourceLocation> baseModels = new HashMap<>();
	static
	{
		baseModels.put("conn_lv", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"));
		baseModels.put("rel_lv", new ResourceLocation("immersiveengineering:block/connector/connector_lv.obj"));
		textureReplacements.put("rel_lv", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_lv",
				"immersiveengineering:blocks/connector_relay_lv"));

		baseModels.put("conn_mv", new ResourceLocation("immersiveengineering:block/connector/connector_mv.obj"));
		baseModels.put("rel_mv", new ResourceLocation("immersiveengineering:block/connector/connector_mv.obj"));
		textureReplacements.put("rel_mv", ImmutableMap.of("#immersiveengineering:blocks/connector_connector_mv",
				"immersiveengineering:blocks/connector_relay_mv"));

		baseModels.put("conn_hv", new ResourceLocation("immersiveengineering:block/connector/connector_hv.obj"));
		baseModels.put("rel_hv", new ResourceLocation("immersiveengineering:block/connector/relay_hv.obj"));

		baseModels.put("conn_struct",
				new ResourceLocation("immersiveengineering:block/connector/connector_structural.obj.ie"));

		baseModels.put("conn_redstone", new ResourceLocation("immersiveengineering:block/connector/connector_redstone.obj.ie"));
		baseModels.put("conn_probe", new ResourceLocation("immersiveengineering:block/connector/connector_probe.obj.ie"));

		baseModels.put("breaker_off",
				new ResourceLocation("immersiveengineering:block/connector/breaker_switch_off.obj.ie"));
		baseModels.put("breaker_on", new ResourceLocation("immersiveengineering:block/connector/breaker_switch_on.obj.ie"));

		baseModels.put("e_meter", new ResourceLocation("immersiveengineering:block/connector/e_meter.obj"));
		baseModels.put("redstone_breaker",
				new ResourceLocation("immersiveengineering:block/connector/redstone_breaker.obj.ie"));

		baseModels.put("transformer_hv_left",
				new ResourceLocation("immersiveengineering:block/connector/transformer_hv_left.obj"));
		baseModels.put("transformer_hv_right",
				new ResourceLocation("immersiveengineering:block/connector/transformer_hv_right.obj"));
		baseModels.put("transformer_mv_left",
				new ResourceLocation("immersiveengineering:block/connector/transformer_mv_left.obj"));
		baseModels.put("transformer_mv_right",
				new ResourceLocation("immersiveengineering:block/connector/transformer_mv_right.obj"));
		baseModels.put("transformer_mv_post",
				new ResourceLocation("immersiveengineering:block/connector/transformer_post.obj"));

		baseModels.put("e_lantern",
				new ResourceLocation("immersiveengineering:block/metal_device/e_lantern.obj"));
		baseModels.put("e_lantern_on",
				new ResourceLocation("immersiveengineering:block/metal_device/e_lantern.obj"));
		textureReplacements.put("e_lantern_on",
				ImmutableMap.of("#immersiveengineering:blocks/metal_device1_electric_lantern",
						"immersiveengineering:blocks/metal_device1_electric_lantern_on"));
		baseModels.put("floodlight",
				new ResourceLocation("immersiveengineering:block/metal_device/floodlight.obj.ie"));
		baseModels.put("floodlight_on",
				new ResourceLocation("immersiveengineering:block/metal_device/floodlight.obj.ie"));
		textureReplacements.put("floodlight_on",
				ImmutableMap.of("#immersiveengineering:blocks/metal_device1_floodlight",
						"immersiveengineering:blocks/metal_device1_floodlight_on"));
		
		baseModels.put("balloon",
				new ResourceLocation("immersiveengineering:block/balloon.obj.ie"));

		baseModels.put("razor_wire",
				new ResourceLocation("immersiveengineering:block/razor_wire.obj.ie"));
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{
		ConnModelReal.cache.clear();
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation)
	{
		return modelLocation.getResourcePath().contains(RESOURCE_LOCATION);
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation)
	{
		String resourcePath = modelLocation.getResourcePath();
		int pos = resourcePath.indexOf("conn_");
		if (pos >= 0)
		{
			pos += 5;// length of "conn_"
			String name = resourcePath.substring(pos);
			ResourceLocation r = baseModels.get(name);
			if (r != null)
			{
				if (textureReplacements.containsKey(name))
					return new ConnModelBase(r, textureReplacements.get(name));
				else
					return new ConnModelBase(r);
			}
		}
		return ModelLoaderRegistry.getMissingModel();
	}

	private class ConnModelBase implements IModel
	{
		ResourceLocation base;
		ImmutableMap<String, String> texReplace;

		public ConnModelBase(ResourceLocation b, ImmutableMap<String, String> t)
		{
			base = b;
			texReplace = t;
		}

		public ConnModelBase(ResourceLocation b)
		{
			this(b, ImmutableMap.of("", ""));
		}

		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			return ImmutableList.of(base);
		}

		@Override
		public Collection<ResourceLocation> getTextures()
		{
			try
			{
				List<ResourceLocation> ret = new ArrayList<>(ModelLoaderRegistry.getModel(base).getTextures());
				for (String tex:texReplace.values())
					ret.add(new ResourceLocation(tex));
				ret.add(new ResourceLocation(ImmersiveEngineering.MODID.toLowerCase(Locale.ENGLISH) + ":blocks/wire"));
				return ret;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public IBakedModel bake(IModelState state, VertexFormat format,	Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
		{
			try
			{
				IModel model = ModelLoaderRegistry.getModel(base);
				if (model instanceof OBJModel)
				{
					model = model.retexture(texReplace);
					OBJModel obj = (OBJModel) model;
					model = obj.process(ImmutableMap.of("flip-v", "true"));
				}
				return new ConnModelReal(model.bake(state, format, bakedTextureGetter));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public IModelState getDefaultState()
		{
			return null;
		}

	}
}
