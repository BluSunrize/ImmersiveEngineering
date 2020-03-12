/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.obj.OBJModel2;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class IEOBJModel implements IModelGeometry<IEOBJModel>
{
	private final boolean dynamic;
	private final OBJModel2 base;
	private final IEObjState state;

	public IEOBJModel(OBJModel2 base, boolean dynamic, IEObjState state)
	{
		this.dynamic = dynamic;
		this.base = base;
		this.state = state;
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
							Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite,
							VertexFormat format, ItemOverrideList overrides)
	{
		IBakedModel baseBaked = base.bake(owner, bakery, spriteGetter, sprite, format, overrides);
		return new IESmartObjModel(base, baseBaked, owner, bakery, spriteGetter, sprite, format, state, dynamic);
	}

	@Override
	public Collection<ResourceLocation> getTextureDependencies(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors)
	{
		return base.getTextureDependencies(owner, modelGetter, missingTextureErrors);
	}
}