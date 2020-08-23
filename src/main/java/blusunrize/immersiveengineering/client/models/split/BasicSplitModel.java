/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class BasicSplitModel implements IModelGeometry<BasicSplitModel>
{
	private final IModelGeometry<?> baseModel;
	private final List<Vec3i> parts;

	public BasicSplitModel(IModelGeometry<?> baseModel, List<Vec3i> parts)
	{
		this.baseModel = baseModel;
		this.parts = parts;
	}

	@Override
	public IBakedModel bake(
			IModelConfiguration owner,
			ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter,
			IModelTransform modelTransform,
			ItemOverrideList overrides,
			ResourceLocation modelLocation
	)
	{
		return new BakedSplitModel(
				baseModel.bake(owner, bakery, spriteGetter, ModelRotation.X0_Y0, overrides, modelLocation),
				parts,
				modelTransform
		);
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
	{
		return baseModel.getTextures(owner, modelGetter, missingTextureErrors);
	}
}
