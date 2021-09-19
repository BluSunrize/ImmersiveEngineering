/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.obj.OBJModel;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class IEOBJModel implements IModelGeometry<IEOBJModel>
{
	private final boolean dynamic;
	private final OBJModel base;
	private final IEObjState state;

	public IEOBJModel(OBJModel base, boolean dynamic, IEObjState state)
	{
		this.dynamic = dynamic;
		this.base = base;
		this.state = state;
	}

	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
							ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
	{
		BakedModel baseBaked = base.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
		return new IESmartObjModel(base, baseBaked, owner, bakery, spriteGetter, modelTransform, state, dynamic,
				ImmutableMap.of());
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
	{
		return base.getTextures(owner, modelGetter, missingTextureErrors);
	}
}