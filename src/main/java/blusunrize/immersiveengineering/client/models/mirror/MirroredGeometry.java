/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.mirror;

import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.*;
import java.util.function.Function;

import static blusunrize.immersiveengineering.client.utils.ModelUtils.copyTypes;

public record MirroredGeometry(UnbakedModel inner) implements IUnbakedGeometry<MirroredGeometry>
{
	@Override
	public BakedModel bake(
			IGeometryBakingContext owner, ModelBaker bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
			ItemOverrides overrides, ResourceLocation modelLoc
	)
	{
		BakedModel baseResult = inner.bake(bakery, spriteGetter, new MirroredModelState(modelState), modelLoc);
		if(baseResult instanceof SimpleBakedModel simpleModel)
		{
			List<BakedQuad> unculledQuads = MirroredModelLoader.getReversedQuads(simpleModel, null);
			Map<Direction, List<BakedQuad>> culledQuads = new EnumMap<>(Direction.class);
			for(Direction d : DirectionUtils.VALUES)
				culledQuads.put(d, MirroredModelLoader.getReversedQuads(simpleModel, d));
			return new SimpleBakedModel(
					unculledQuads, culledQuads,
					baseResult.useAmbientOcclusion(), baseResult.usesBlockLight(), baseResult.isGui3d(),
					baseResult.getParticleIcon(ModelData.EMPTY), baseResult.getTransforms(), baseResult.getOverrides(),
					copyTypes(simpleModel)
			);
		}
		else if(baseResult instanceof ICacheKeyProvider<?> cachedModel)
			return new CachedMirroredModel<>(cachedModel);
		else
			throw new RuntimeException("Tried to mirror model "+inner+" which is neither simple nor cacheable");
	}
}
