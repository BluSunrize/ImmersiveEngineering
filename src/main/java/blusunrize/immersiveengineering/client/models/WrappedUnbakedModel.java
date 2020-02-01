/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.IClip;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public abstract class WrappedUnbakedModel implements IUnbakedModel
{
	protected final IUnbakedModel base;

	public WrappedUnbakedModel(IUnbakedModel base)
	{
		this.base = base;
	}

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return base.getDependencies();
	}

	@Override
	public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors)
	{
		return base.getTextures(modelGetter, missingTextureErrors);
	}

	@Nullable
	@Override
	public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format)
	{
		return base.bake(bakery, spriteGetter, sprite, format);
	}

	@Override
	public IModelState getDefaultState()
	{
		return base.getDefaultState();
	}

	@Override
	public Optional<? extends IClip> getClip(String name)
	{
		return base.getClip(name);
	}

	@Override
	public IUnbakedModel process(ImmutableMap<String, String> customData)
	{
		return newInstance(base.process(customData));
	}

	@Override
	public IUnbakedModel smoothLighting(boolean value)
	{
		return newInstance(base.smoothLighting(value));
	}

	@Override
	public IUnbakedModel gui3d(boolean value)
	{
		return newInstance(base.gui3d(value));
	}

	@Override
	public IUnbakedModel retexture(ImmutableMap<String, String> textures)
	{
		return newInstance(base.retexture(textures));
	}

	protected abstract WrappedUnbakedModel newInstance(IUnbakedModel base);
}
