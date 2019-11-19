/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.resource.IResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.api.wires.WireApi.INFOS;
import static net.minecraftforge.resource.VanillaResourceType.MODELS;
import static net.minecraftforge.resource.VanillaResourceType.TEXTURES;


public class FeedthroughLoader implements ICustomModelLoader
{
	public static final String RESOURCE_LOCATION = "models/block/smartmodel/feedthrough";

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if(resourcePredicate.test(TEXTURES)||resourcePredicate.test(MODELS))
			onResourceManagerReload(resourceManager);
	}

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
	public IUnbakedModel loadModel(@Nonnull ResourceLocation modelLocation)
	{
		return new FeedthroughModelRaw();
	}

	private class FeedthroughModelRaw implements IUnbakedModel
	{
		@Nonnull
		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			return INFOS.values().stream().map((i) -> i.modelLoc).collect(Collectors.toCollection(ArrayList::new));
		}

		@Override
		public Collection<ResourceLocation> getTextures(@Nonnull Function<ResourceLocation, IUnbakedModel> modelGetter,
														@Nonnull Set<String> missingTextureErrors)
		{
			return ImmutableList.of();
		}

		@Nullable
		@Override
		public IBakedModel bake(@Nonnull ModelBakery bakery, @Nonnull Function<ResourceLocation, TextureAtlasSprite> spriteGetter,
								@Nonnull ISprite sprite, @Nonnull VertexFormat format)
		{
			return new FeedthroughModel();
		}

	}
}
