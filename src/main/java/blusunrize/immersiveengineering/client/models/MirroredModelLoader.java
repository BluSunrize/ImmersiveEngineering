/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.MirroredModelLoader.Geometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.ExpandedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class MirroredModelLoader implements IModelLoader<Geometry>
{
	public static final String INNER_MODEL = "inner_model";
	public static final ResourceLocation ID = ImmersiveEngineering.rl("mirror");

	@Nonnull
	@Override
	public Geometry read(@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents)
	{
		JsonElement innerJson = modelContents.get(INNER_MODEL);
		BlockModel baseModel = ExpandedBlockModelDeserializer.INSTANCE.fromJson(innerJson, BlockModel.class);
		return new Geometry(baseModel);
	}

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager pResourceManager)
	{
	}

	public record Geometry(UnbakedModel inner) implements IModelGeometry<Geometry>
	{

		@Override
		public BakedModel bake(
				IModelConfiguration owner, ModelBakery bakery,
				Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
				ItemOverrides overrides, ResourceLocation modelLoc
		)
		{
			return inner.bake(bakery, spriteGetter, new MirroredModelState(modelState), modelLoc);
		}

		@Override
		public Collection<Material> getTextures(
				IModelConfiguration owner,
				Function<ResourceLocation, UnbakedModel> modelGetter,
				Set<Pair<String, String>> missingTextureErrors
		)
		{
			return inner.getMaterials(modelGetter, missingTextureErrors);
		}
	}

	public static class MirroredModelState implements ModelState
	{
		private static final Transformation MIRRORED_IDENTITY = new Transformation(
				null, null, new Vector3f(1, 1, -1), null
		);
		private final ModelState inner;
		private final Transformation mirroredMainRotation;

		public MirroredModelState(ModelState inner)
		{
			this.inner = inner;
			this.mirroredMainRotation = mirror(inner.getRotation());
		}

		@Nonnull
		public Transformation getRotation()
		{
			return mirroredMainRotation;
		}

		public boolean isUvLocked()
		{
			return inner.isUvLocked();
		}

		public Transformation getPartTransformation(Object part)
		{
			return mirror(inner.getPartTransformation(part));
		}

		private static Transformation mirror(Transformation in)
		{
			return in.compose(MIRRORED_IDENTITY);
		}
	}
}
