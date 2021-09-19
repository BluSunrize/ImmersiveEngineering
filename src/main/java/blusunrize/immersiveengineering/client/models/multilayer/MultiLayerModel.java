/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.multilayer;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class MultiLayerModel implements IModelGeometry<MultiLayerModel>
{

	private final Map<String, IModelGeometry<?>> subModels;

	public MultiLayerModel(Map<String, IModelGeometry<?>> subModels)
	{
		this.subModels = subModels;
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
	{
		List<Material> ret = new ArrayList<>();
		for(IModelGeometry<?> geometry : subModels.values())
			ret.addAll(geometry.getTextures(owner, modelGetter, missingTextureErrors));
		return ret;
	}

	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
	{
		Map<String, BakedModel> baked = new HashMap<>();
		for(Entry<String, IModelGeometry<?>> e : subModels.entrySet())
			//TODO sprite getters?
			baked.put(e.getKey(), e.getValue().bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation));
		return new BakedMultiLayerModel(baked);
	}
}
