/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class UnbakedSplitModel implements IModelGeometry<UnbakedSplitModel>
{
	private final IModelGeometry<?> baseModel;
	private final Set<Vector3i> parts;
	private final boolean dynamic;
	private final Vector3i size;

	public UnbakedSplitModel(IModelGeometry<?> baseModel, List<Vector3i> parts, boolean dynamic, Vector3i size)
	{
		this.baseModel = baseModel;
		this.parts = new HashSet<>(parts);
		this.dynamic = dynamic;
		this.size = size;
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
							IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation)
	{
		IBakedModel bakedBase = baseModel.bake(owner, bakery, spriteGetter, ModelRotation.X0_Y0, overrides, modelLocation);
		if(dynamic)
			return new BakedDynamicSplitModel<>(
					(ICacheKeyProvider<?>)bakedBase, parts, modelTransform, size
			);
		else
			return new BakedBasicSplitModel(bakedBase, parts, modelTransform, size);
	}

	@Override
	public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter,
												  Set<Pair<String, String>> missingTextureErrors)
	{
		return baseModel.getTextures(owner, modelGetter, missingTextureErrors);
	}
}
