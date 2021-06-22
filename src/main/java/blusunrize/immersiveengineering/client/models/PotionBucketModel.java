package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.QuadTransformer;
import blusunrize.immersiveengineering.common.fluids.IEFluids;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class PotionBucketModel implements IModelGeometry<PotionBucketModel>
{
	private final QuadTransformer recolorTransformer;
	private final IModelGeometry<?> baseGeometry = new DynamicBucketModel(
			IEFluids.fluidPotion.get(), false, true, true, true
	);

	public PotionBucketModel(int color)
	{
		this.recolorTransformer = new QuadTransformer(new TransformationMatrix(null), $ -> color);
	}

	@Override
	public IBakedModel bake(
			IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
			IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation
	)
	{
		IBakedModel baseModel = baseGeometry.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
		ImmutableMap<TransformType, TransformationMatrix> transformMap = PerspectiveMapWrapper.getTransforms(
				new ModelTransformComposition(owner.getCombinedTransform(), modelTransform)
		);
		ItemMultiLayerBakedModel.Builder builder = ItemMultiLayerBakedModel.builder(
				owner, baseModel.getParticleTexture(EmptyModelData.INSTANCE), new OverrideHandler(overrides, bakery, owner), transformMap
		);
		ResourceLocation fluidMaskLocation = IEFluids.fluidPotion.get().getAttributes().getStillTexture();
		for(Pair<IBakedModel, RenderType> layer : baseModel.getLayerModels(ItemStack.EMPTY, false))
		{
			List<BakedQuad> baseQuads = layer.getFirst().getQuads(null, null, Utils.RAND, EmptyModelData.INSTANCE);
			List<BakedQuad> newQuads = new ArrayList<>(baseQuads.size());
			for(BakedQuad baseQuad : baseQuads)
			{
				if(baseQuad.getSprite().getName().equals(fluidMaskLocation))
					newQuads.add(recolorTransformer.apply(baseQuad));
				else
					newQuads.add(baseQuad);
			}
			builder.addQuads(layer.getSecond(), newQuads);
		}
		return builder.build();
	}

	@Override
	public Collection<RenderMaterial> getTextures(
			IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors
	)
	{
		return baseGeometry.getTextures(owner, modelGetter, missingTextureErrors);
	}

	public static class Loader implements IModelLoader<PotionBucketModel>
	{
		public static final ResourceLocation LOADER_NAME = ImmersiveEngineering.rl("potion_bucket");

		@Override
		public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
		{
		}

		@Nonnull
		@Override
		public PotionBucketModel read(
				@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents
		)
		{
			return new PotionBucketModel(-1);
		}
	}

	private static class OverrideHandler extends ItemOverrideList
	{
		private final Int2ObjectMap<IBakedModel> coloredModels = new Int2ObjectOpenHashMap<>();
		private final ItemOverrideList nested;
		private final ModelBakery bakery;
		private final IModelConfiguration owner;

		private OverrideHandler(ItemOverrideList nested, ModelBakery bakery, IModelConfiguration owner)
		{
			this.nested = nested;
			this.bakery = bakery;
			this.owner = owner;
		}

		@Nullable
		@Override
		public IBakedModel getOverrideModel(
				@Nonnull IBakedModel model, @Nonnull ItemStack stack, @Nullable ClientWorld world,
				@Nullable LivingEntity livingEntity
		)
		{
			final FluidStack fluid = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
			if(fluid.isEmpty())
				return nested.getOverrideModel(model, stack, world, livingEntity);
			final int color = fluid.getFluid().getAttributes().getColor(fluid);
			return coloredModels.computeIfAbsent(color, i -> new PotionBucketModel(i).bake(
					owner, bakery, ModelLoader.defaultTextureGetter(), ModelRotation.X0_Y0, this, ImmersiveEngineering.rl("potion_bucket_override")
			));
		}
	}
}
