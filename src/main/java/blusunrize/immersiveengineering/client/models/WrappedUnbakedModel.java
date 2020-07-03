/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
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
	public Collection<RenderMaterial> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
	{
		return base.getTextures(modelGetter, missingTextureErrors);
	}

	@Nullable
	@Override
	public IBakedModel bakeModel(ModelBakery modelBakeryIn, Function<RenderMaterial, TextureAtlasSprite> spriteGetterIn, IModelTransform transformIn, ResourceLocation locationIn)
	{
		return base.bakeModel(modelBakeryIn, spriteGetterIn, transformIn, locationIn);
	}

	@Override
	public Optional<? extends IClip> getClip(String name)
	{
		return base.getClip(name);
	}

	protected abstract WrappedUnbakedModel newInstance(IUnbakedModel base);
}
