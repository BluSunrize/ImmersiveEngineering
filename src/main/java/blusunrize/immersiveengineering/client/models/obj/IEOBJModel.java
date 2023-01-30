/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import com.mojang.datafixers.util.Pair;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import malte0811.modelsplitter.model.OBJModel;
import malte0811.modelsplitter.model.Polygon;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public record IEOBJModel(OBJModel<OBJMaterial> base, boolean dynamic, IEOBJCallback<?> callback)
		implements IModelGeometry<IEOBJModel>
{

	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
						   ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
	{
		return new GeneralIEOBJModel<>(callback, base, owner, spriteGetter, modelTransform, dynamic);
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
	{
		return base.getFaces().stream()
				.map(Polygon::getTexture)
				.distinct()
				.map(mat -> ModelLoaderRegistry.resolveTexture(mat.map_Kd(), owner))
				.collect(Collectors.toList());
	}
}