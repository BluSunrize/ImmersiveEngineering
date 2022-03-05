/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.LightUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class ModelCoresample extends BakedIEModel
{
	private static final Cache<String, ModelCoresample> modelCache = CacheBuilder.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build();
	@Nullable
	private final MineralMix[] minerals;
	private List<BakedQuad> bakedQuads;

	public ModelCoresample(@Nullable MineralMix[] minerals)
	{
		this.minerals = minerals;
	}

	public static void clearCache()
	{
		modelCache.invalidateAll();
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState coreState, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		MineralMix[] minerals;
		if(extraData.hasProperty(Model.MINERAL))
			minerals = extraData.getData(Model.MINERAL);
		else
			minerals = this.minerals;
		if(bakedQuads==null||minerals==null)
		{
			bakedQuads = new ArrayList<>();
			Exception cause = null;
			try
			{
				float width = .25f;
				float depth = .25f;
				float wOff = (1-width)/2;
				float dOff = (1-depth)/2;
				int pixelLength = 0;

				List<Pair<TextureAtlasSprite, Integer>> textureOre = new ArrayList<>();
				TextureAtlasSprite textureStone = null;
				if(minerals!=null&&minerals.length > 0)
				{
					int allocatedPx = 16/minerals.length;
					for(MineralMix mineral : minerals)
					{
						for(StackWithChance o : mineral.outputs)
							if(!o.stack().get().isEmpty())
							{
								int weight = Math.round(allocatedPx*o.chance());
								Block b = Block.byItem(o.stack().get().getItem());
								if(b==Blocks.AIR)
									b = mineral.background;
								BlockState state = b.defaultBlockState();
								BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
								textureOre.add(Pair.of(model.getParticleIcon(), weight));
								pixelLength += weight;
							}
						BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(mineral.background.defaultBlockState());
						textureStone = model.getParticleIcon();
					}
				}
				else
				{
					pixelLength = 16;
					textureStone = ClientUtils.getSprite(new ResourceLocation("block/stone"));
				}

				double[] stoneUVs = {
						16*wOff, 16*dOff,
						16*(wOff+width), 16*(dOff+depth),
				};

				putVertexData(new Vec3(0, -1, 0), new Vec3[]{new Vec3(wOff, 0, dOff),
						new Vec3(wOff+width, 0, dOff), new Vec3(wOff+width, 0, dOff+depth),
						new Vec3(wOff, 0, dOff+depth)}, stoneUVs, textureStone, bakedQuads);
				putVertexData(new Vec3(0, 1, 0), new Vec3[]{new Vec3(wOff, 1, dOff),
						new Vec3(wOff, 1, dOff+depth), new Vec3(wOff+width, 1, dOff+depth),
						new Vec3(wOff+width, 1, dOff)}, stoneUVs, textureStone, bakedQuads);
				if(textureOre.isEmpty())
				{
					double[][] uvs = new double[4][];
					for(int j = 0; j < 4; j++)
						uvs[j] = new double[]{
								j*4, 0,
								(j+1)*4, 16,
						};

					putVertexData(new Vec3(0, 0, -1), new Vec3[]{
							new Vec3(wOff, 0, dOff),
							new Vec3(wOff, 1, dOff),
							new Vec3(wOff+width, 1, dOff),
							new Vec3(wOff+width, 0, dOff)
					}, uvs[0], textureStone, bakedQuads);
					putVertexData(new Vec3(0, 0, 1), new Vec3[]{
							new Vec3(wOff+width, 0, dOff+depth),
							new Vec3(wOff+width, 1, dOff+depth),
							new Vec3(wOff, 1, dOff+depth),
							new Vec3(wOff, 0, dOff+depth)
					}, uvs[2], textureStone, bakedQuads);
					putVertexData(new Vec3(-1, 0, 0), new Vec3[]{
									new Vec3(wOff, 0, dOff+depth),
									new Vec3(wOff, 1, dOff+depth),
									new Vec3(wOff, 1, dOff),
									new Vec3(wOff, 0, dOff)
							},
							uvs[3], textureStone, bakedQuads);
					putVertexData(new Vec3(1, 0, 0), new Vec3[]{
							new Vec3(wOff+width, 0, dOff),
							new Vec3(wOff+width, 1, dOff),
							new Vec3(wOff+width, 1, dOff+depth),
							new Vec3(wOff+width, 0, dOff+depth)
					}, uvs[1], textureStone, bakedQuads);
				}
				else
				{
					float h = 0;
					for(Pair<TextureAtlasSprite, Integer> pair : textureOre)
					{
						TextureAtlasSprite sprite = pair.getFirst();
						int weight = pair.getSecond();
						int v = weight > 8?16-weight: 8;
						double[][] uvs = new double[4][];
						for(int j = 0; j < 4; j++)
							uvs[j] = new double[]{
									j*4, v,
									(j+1)*4, v+weight,
							};

						float h1 = weight/(float)pixelLength;
						putVertexData(new Vec3(0, 0, -1), new Vec3[]{
								new Vec3(wOff, h, dOff),
								new Vec3(wOff, h+h1, dOff),
								new Vec3(wOff+width, h+h1, dOff),
								new Vec3(wOff+width, h, dOff)
						}, uvs[0], sprite, bakedQuads);
						putVertexData(new Vec3(0, 0, 1), new Vec3[]{
								new Vec3(wOff+width, h, dOff+depth),
								new Vec3(wOff+width, h+h1, dOff+depth),
								new Vec3(wOff, h+h1, dOff+depth),
								new Vec3(wOff, h, dOff+depth)
						}, uvs[2], sprite, bakedQuads);
						putVertexData(new Vec3(-1, 0, 0), new Vec3[]{
								new Vec3(wOff, h, dOff+depth),
								new Vec3(wOff, h+h1, dOff+depth),
								new Vec3(wOff, h+h1, dOff),
								new Vec3(wOff, h, dOff)
						}, uvs[3], sprite, bakedQuads);
						putVertexData(new Vec3(1, 0, 0), new Vec3[]{
								new Vec3(wOff+width, h, dOff),
								new Vec3(wOff+width, h+h1, dOff),
								new Vec3(wOff+width, h+h1, dOff+depth),
								new Vec3(wOff+width, h, dOff+depth)
						}, uvs[1], sprite, bakedQuads);
						h += h1;
					}
				}
			} catch(Exception e)
			{
				e.printStackTrace();
				cause = e;
			}
			if(bakedQuads.isEmpty())
			{
				if(cause!=null)
					throw new RuntimeException("Empty quad list!", cause);
				else
					throw new RuntimeException("Empty quad list!");
			}
			return bakedQuads;
		}
		return bakedQuads;
	}

	protected final void putVertexData(Vec3 normal, Vec3[] vertices, double[] uvs, TextureAtlasSprite sprite, List<BakedQuad> out)
	{
		float d = LightUtil.diffuseLight((float)normal.x, (float)normal.y, (float)normal.z);
		BakedQuad quad = ModelUtils.createBakedQuad(vertices, Direction.getNearest(normal.x, normal.y, normal.z),
				sprite,
				uvs,
				new float[]{d, d, d, 1},
				false
		);
		out.add(quad);
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return null;
	}

	@Override
	public ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

	@Override
	public ItemOverrides getOverrides()
	{
		return overrideList;
	}


	private final ItemOverrides overrideList = new ItemOverrides()
	{
		@Nullable
		@Override
		public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel worldIn, @Nullable LivingEntity entityIn, int unused)
		{
			MineralMix[] minerals = CoresampleItem.getMineralMixes(worldIn, stack);
			if(minerals.length > 0)
			{
				try
				{
					String cacheKey = "";
					for(int i = 0; i < minerals.length; i++)
						cacheKey += (i > 0?"_": "")+minerals[i].getId().toString();
					return modelCache.get(cacheKey, () -> new ModelCoresample(minerals));
				} catch(ExecutionException e)
				{
					throw new RuntimeException(e);
				}
			}
			return originalModel;
		}
	};

	static HashMap<TransformType, Matrix4> transformationMap = new HashMap<>();

	static
	{
		transformationMap.put(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().translate(0, .28, 0).rotate(Math.toRadians(180), 1, 0, 0).rotate(Math.toRadians(-90), 0, 1, 0));
		transformationMap.put(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().translate(0, .28, 0).rotate(Math.toRadians(180), 1, 0, 0).rotate(Math.toRadians(-90), 0, 1, 0));
		transformationMap.put(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(0, .0625, -.125).scale(.625, .625, .625).rotate(Math.toRadians(30), 1, 0, 0).rotate(Math.toRadians(130), 0, 1, 0));
		transformationMap.put(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(0, .0625, -.125).scale(.625, .625, .625).rotate(Math.toRadians(30), 1, 0, 0).rotate(Math.toRadians(130), 0, 1, 0));
		transformationMap.put(TransformType.GUI, new Matrix4().scale(1.25, 1.25, 1.25).rotate(Math.toRadians(180), 1, 0, 0).rotate(Math.toRadians(20), 0, 1, 0).rotate(Math.toRadians(-30), 0, 0, 1));
		transformationMap.put(TransformType.FIXED, new Matrix4().scale(1.5, 1.5, 1.5).rotate(Math.toRadians(180), 1, 0, 0));
		transformationMap.put(TransformType.GROUND, new Matrix4().scale(1.5, 1.5, 1.5).rotate(Math.toRadians(180), 1, 0, 0));
	}

	@Override
	public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack mat)
	{
		return this;
	}

	@Override
	public boolean usesBlockLight()
	{
		return false;
	}

	public static class RawCoresampleModel implements IModelGeometry<RawCoresampleModel>
	{
		@Override
		public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
		{
			return new ModelCoresample(null);
		}

		@Override
		public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<com.mojang.datafixers.util.Pair<String, String>> missingTextureErrors)
		{
			return ImmutableList.of();
		}
	}

	public static class CoresampleLoader implements IModelLoader<RawCoresampleModel>
	{
		public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "models/coresample");

		@Override
		public void onResourceManagerReload(ResourceManager resourceManager)
		{
		}

		@Override
		public RawCoresampleModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
		{
			return new RawCoresampleModel();
		}
	}
}