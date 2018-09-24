/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.smart;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.api.energy.wires.WireApi.INFOS;


public class FeedthroughLoader implements ICustomModelLoader
{
	public static final String RESOURCE_LOCATION = "models/block/smartmodel/feedthrough";

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
		FeedthroughModel.CACHE.invalidateAll();
	}

	@Override
	public boolean accepts(@Nonnull ResourceLocation modelLocation)
	{
		return modelLocation.getPath().equals(RESOURCE_LOCATION);
	}

	@Nonnull
	@Override
	public IModel loadModel(@Nonnull ResourceLocation modelLocation)
	{
		return new FeedthroughModelRaw();
	}

	private class FeedthroughModelRaw implements IModel
	{
		@Nonnull
		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			return INFOS.values().stream().map((i) -> i.modelLoc).collect(Collectors.toCollection(ArrayList::new));
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getTextures()
		{
			return ImmutableList.of();
		}

		@Nonnull
		@Override
		public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format,
								@Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
		{
			return new FeedthroughModel();
		}

	}
}
