/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallback;
import com.mojang.datafixers.util.Pair;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import malte0811.modelsplitter.model.OBJModel;
import malte0811.modelsplitter.model.Polygon;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public record IEOBJModel(OBJModel<OBJMaterial> base, boolean dynamic, IEOBJCallback<?> callback)
		implements IUnbakedGeometry<IEOBJModel>
{

	@Override
	public BakedModel bake(
			IGeometryBakingContext context,
			ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter,
			ModelState modelState,
			ItemOverrides overrides,
			ResourceLocation modelLocation
	)
	{
		return new GeneralIEOBJModel<>(callback, base, context, spriteGetter, modelState, dynamic);
	}

	@Override
	public Collection<Material> getMaterials(
			IGeometryBakingContext context,
			Function<ResourceLocation, UnbakedModel> modelGetter,
			Set<Pair<String, String>> missingTextureErrors
	)
	{
		return base.getFaces().stream()
				.map(Polygon::getTexture)
				.distinct()
				.map(mat -> UnbakedGeometryHelper.resolveDirtyMaterial(mat.map_Kd(), context))
				.collect(Collectors.toList());
	}
}