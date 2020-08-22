/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class BasicSplitModel implements IModelGeometry<BasicSplitModel>
{
	private final IModelGeometry<?> baseModel;

	public BasicSplitModel(IModelGeometry<?> baseModel)
	{
		this.baseModel = baseModel;
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
				baseModel.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation),
				//TODO
				IEMultiblocks.CRUSHER
		);
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
	{
		return baseModel.getTextures(owner, modelGetter, missingTextureErrors);
	}
}
