/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import malte0811.modelsplitter.model.OBJModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record IEOBJModel(
		OBJModel<OBJMaterial> base, boolean dynamic, IEOBJCallback<?> callback, @Nullable List<ResourceLocation> layers
) implements IUnbakedGeometry<IEOBJModel>
{

	@Override
	public BakedModel bake(
			IGeometryBakingContext context,
			ModelBaker bakery,
			Function<Material, TextureAtlasSprite> spriteGetter,
			ModelState modelState,
			ItemOverrides overrides
	)
	{
		List<RenderType> blockTypes = new ArrayList<>();
		List<RenderType> itemTypes = new ArrayList<>();
		List<RenderType> fabulousItemTypes = new ArrayList<>();
		if(layers!=null)
			for(final ResourceLocation name : layers)
			{
				final RenderTypeGroup types = context.getRenderType(name);
				blockTypes.add(types.block());
				itemTypes.add(types.entity());
				fabulousItemTypes.add(types.entityFabulous());
			}
		else
		{
			blockTypes = List.of(RenderType.solid());
			itemTypes = fabulousItemTypes = List.of(NeoForgeRenderTypes.ITEM_LAYERED_SOLID.get());
		}
		return new GeneralIEOBJModel<>(
				callback, base, context, spriteGetter, modelState, dynamic,
				ChunkRenderTypeSet.of(blockTypes), itemTypes, fabulousItemTypes
		);
	}
}