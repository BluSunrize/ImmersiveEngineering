package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.QuadTransformer;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
		this.recolorTransformer = new QuadTransformer(new Transformation(null), $ -> color);
	}

	@Override
	public BakedModel bake(
			IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
			ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation
	)
	{
		BakedModel baseModel = baseGeometry.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
		ImmutableMap<TransformType, Transformation> transformMap = PerspectiveMapWrapper.getTransforms(
				new CompositeModelState(owner.getCombinedTransform(), modelTransform)
		);
		ItemMultiLayerBakedModel.Builder builder = ItemMultiLayerBakedModel.builder(
				owner, baseModel.getParticleIcon(EmptyModelData.INSTANCE), new OverrideHandler(overrides, bakery, owner), transformMap
		);
		ResourceLocation fluidMaskLocation = IEFluids.fluidPotion.get().getAttributes().getStillTexture();
		for(Pair<BakedModel, RenderType> layer : baseModel.getLayerModels(ItemStack.EMPTY, false))
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
	public Collection<Material> getTextures(
			IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors
	)
	{
		return baseGeometry.getTextures(owner, modelGetter, missingTextureErrors);
	}

	public static class Loader implements IModelLoader<PotionBucketModel>
	{
		public static final ResourceLocation LOADER_NAME = ImmersiveEngineering.rl("potion_bucket");

		@Override
		public void onResourceManagerReload(@Nonnull ResourceManager resourceManager)
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

	private static class OverrideHandler extends ItemOverrides
	{
		private final Int2ObjectMap<BakedModel> coloredModels = new Int2ObjectOpenHashMap<>();
		private final ItemOverrides nested;
		private final ModelBakery bakery;
		private final IModelConfiguration owner;

		private OverrideHandler(ItemOverrides nested, ModelBakery bakery, IModelConfiguration owner)
		{
			this.nested = nested;
			this.bakery = bakery;
			this.owner = owner;
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
			final int color = fluid.getFluid().getAttributes().getColor(fluid);
			return coloredModels.computeIfAbsent(color, i -> new PotionBucketModel(i).bake(
					owner, bakery, ModelLoader.defaultTextureGetter(), BlockModelRotation.X0_Y0, this, ImmersiveEngineering.rl("potion_bucket_override")
			));
		}
	}
}
