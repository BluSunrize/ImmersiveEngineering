/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.utils.QuadTransformer;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.register.IEFluids;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.CompositeModel;
import net.minecraftforge.client.model.CompositeModel.Baked.Builder;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public final class PotionBucketModel implements IUnbakedGeometry<PotionBucketModel>
{
	private final IQuadTransformer recolorTransformer;
	private final IUnbakedGeometry<?> baseGeometry;

	public PotionBucketModel(int color)
	{
		this.recolorTransformer = QuadTransformer.color($ -> color);
		JsonObject baseModelJSON = new JsonObject();
		baseModelJSON.addProperty("fluid", IEFluids.POTION.getId().toString());
		this.baseGeometry = DynamicFluidContainerModel.Loader.INSTANCE.read(baseModelJSON, null);
	}

	@Override
	public BakedModel bake(
			IGeometryBakingContext context, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter,
			ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation
	)
	{
		BakedModel baseModel = baseGeometry.bake(context, bakery, spriteGetter, modelTransform, overrides, modelLocation);
		StandaloneGeometryBakingContext itemContext = StandaloneGeometryBakingContext.builder(context)
				.withGui3d(false)
				.withUseBlockLight(false)
				.build(modelLocation);
		Builder builder = CompositeModel.Baked.builder(
				itemContext,
				baseModel.getParticleIcon(ModelData.EMPTY),
				new OverrideHandler(baseModel.getOverrides(), bakery, context, spriteGetter),
				context.getTransforms()
		);
		ResourceLocation fluidMaskLocation = IClientFluidTypeExtensions.of(IEFluids.POTION.get()).getStillTexture();
		for(var layerModel : baseModel.getRenderPasses(ItemStack.EMPTY, false))
		{
			var layerGroup = layerModel instanceof SimpleBakedModel simple?ModelUtils.copyTypes(simple): RenderTypeGroup.EMPTY;
			for(var layer : layerModel.getRenderTypes(ItemStack.EMPTY, false))
			{
				List<BakedQuad> baseQuads = layerModel.getQuads(
						null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, layer
				);
				for(BakedQuad baseQuad : baseQuads)
				{
					BakedQuad newQuad;
					if(baseQuad.getSprite().contents().name().equals(fluidMaskLocation))
						newQuad = recolorTransformer.process(baseQuad);
					else
						newQuad = baseQuad;
					builder.addQuads(layerGroup, newQuad);
				}
			}
		}
		return builder.build();
	}

	public static class Loader implements IGeometryLoader<PotionBucketModel>
	{
		public static final ResourceLocation LOADER_NAME = ImmersiveEngineering.rl("potion_bucket");

		@Nonnull
		@Override
		public PotionBucketModel read(
				@Nonnull JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext
		)
		{
			return new PotionBucketModel(-1);
		}
	}

	private static class OverrideHandler extends ItemOverrides
	{
		private final Int2ObjectMap<BakedModel> coloredModels = new Int2ObjectOpenHashMap<>();
		private final ItemOverrides nested;
		private final ModelBaker bakery;
		private final IGeometryBakingContext owner;
		private final Function<Material, TextureAtlasSprite> textureGetter;

		private OverrideHandler(
				ItemOverrides nested,
				ModelBaker bakery,
				IGeometryBakingContext owner,
				Function<Material, TextureAtlasSprite> textureGetter
		)
		{
			this.nested = nested;
			this.bakery = bakery;
			this.owner = owner;
			this.textureGetter = textureGetter;
		}

		@Nullable
		@Override
		public BakedModel resolve(
				@Nonnull BakedModel model, @Nonnull ItemStack stack, @Nullable ClientLevel world,
				@Nullable LivingEntity livingEntity, int unused
		)
		{
			final FluidStack fluid = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
			if(fluid.isEmpty())
				return nested.resolve(model, stack, world, livingEntity, unused);
			final int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
			return coloredModels.computeIfAbsent(color, i -> new PotionBucketModel(i).bake(
					owner, bakery, textureGetter,
					BlockModelRotation.X0_Y0, this, ImmersiveEngineering.rl("potion_bucket_override")
			));
		}
	}
}
