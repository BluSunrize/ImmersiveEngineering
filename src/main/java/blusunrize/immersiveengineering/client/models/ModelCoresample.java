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
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
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
	private static final Cache<MineralMix, ModelCoresample> modelCache = CacheBuilder.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build();
	@Nullable
	private MineralMix mineral;
	private final VertexFormat format;
	private List<BakedQuad> bakedQuads;

	public ModelCoresample(@Nullable MineralMix mineral, VertexFormat format)
	{
		this.mineral = mineral;
		this.format = format;
	}

	public static void clearCache()
	{
		modelCache.invalidateAll();
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState coreState, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		MineralMix mineral;
		if(extraData.hasProperty(Model.MINERAL))
			mineral = extraData.getData(Model.MINERAL);
		else
			mineral = this.mineral;
		if(bakedQuads==null||this.mineral==null)
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

				Map<TextureAtlasSprite, Integer> textureOre = new HashMap<>();
				TextureAtlasSprite textureStone;
				if(mineral!=null)
				{
					for(StackWithChance o : mineral.outputs)
						if(!o.getStack().isEmpty())
						{
							int weight = Math.max(2, Math.round(16*o.getChance()));
							Block b = Block.getBlockFromItem(o.getStack().getItem());
							if(b==Blocks.AIR)
								b = mineral.background;
							BlockState state = b.getDefaultState();
							IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
							textureOre.put(model.getParticleTexture(), weight);
							pixelLength += weight;
						}
					IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(mineral.background.getDefaultState());
					textureStone = model.getParticleTexture();
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

				putVertexData(new Vector3d(0, -1, 0), new Vector3d[]{new Vector3d(wOff, 0, dOff),
						new Vector3d(wOff+width, 0, dOff), new Vector3d(wOff+width, 0, dOff+depth),
						new Vector3d(wOff, 0, dOff+depth)}, stoneUVs, textureStone, bakedQuads);
				putVertexData(new Vector3d(0, 1, 0), new Vector3d[]{new Vector3d(wOff, 1, dOff),
						new Vector3d(wOff, 1, dOff+depth), new Vector3d(wOff+width, 1, dOff+depth),
						new Vector3d(wOff+width, 1, dOff)}, stoneUVs, textureStone, bakedQuads);
				if(textureOre.isEmpty())
				{
					double[][] uvs = new double[4][];
					for(int j = 0; j < 4; j++)
						uvs[j] = new double[]{
								j*4, 0,
								(j+1)*4, 16,
						};

					putVertexData(new Vector3d(0, 0, -1), new Vector3d[]{
							new Vector3d(wOff, 0, dOff),
							new Vector3d(wOff, 1, dOff),
							new Vector3d(wOff+width, 1, dOff),
							new Vector3d(wOff+width, 0, dOff)
					}, uvs[0], textureStone, bakedQuads);
					putVertexData(new Vector3d(0, 0, 1), new Vector3d[]{
							new Vector3d(wOff+width, 0, dOff+depth),
							new Vector3d(wOff+width, 1, dOff+depth),
							new Vector3d(wOff, 1, dOff+depth),
							new Vector3d(wOff, 0, dOff+depth)
					}, uvs[2], textureStone, bakedQuads);
					putVertexData(new Vector3d(-1, 0, 0), new Vector3d[]{
									new Vector3d(wOff, 0, dOff+depth),
									new Vector3d(wOff, 1, dOff+depth),
									new Vector3d(wOff, 1, dOff),
									new Vector3d(wOff, 0, dOff)
							},
							uvs[3], textureStone, bakedQuads);
					putVertexData(new Vector3d(1, 0, 0), new Vector3d[]{
							new Vector3d(wOff+width, 0, dOff),
							new Vector3d(wOff+width, 1, dOff),
							new Vector3d(wOff+width, 1, dOff+depth),
							new Vector3d(wOff+width, 0, dOff+depth)
					}, uvs[1], textureStone, bakedQuads);
				}
				else
				{
					float h = 0;
					for(TextureAtlasSprite sprite : textureOre.keySet())
					{
						int weight = textureOre.get(sprite);
						int v = weight > 8?16-weight: 8;
						double[][] uvs = new double[4][];
						for(int j = 0; j < 4; j++)
							uvs[j] = new double[]{
									j*4, v,
									(j+1)*4, v+weight,
							};

						float h1 = weight/(float)pixelLength;
						putVertexData(new Vector3d(0, 0, -1), new Vector3d[]{
								new Vector3d(wOff, h, dOff),
								new Vector3d(wOff, h+h1, dOff),
								new Vector3d(wOff+width, h+h1, dOff),
								new Vector3d(wOff+width, h, dOff)
						}, uvs[0], sprite, bakedQuads);
						putVertexData(new Vector3d(0, 0, 1), new Vector3d[]{
								new Vector3d(wOff+width, h, dOff+depth),
								new Vector3d(wOff+width, h+h1, dOff+depth),
								new Vector3d(wOff, h+h1, dOff+depth),
								new Vector3d(wOff, h, dOff+depth)
						}, uvs[2], sprite, bakedQuads);
						putVertexData(new Vector3d(-1, 0, 0), new Vector3d[]{
								new Vector3d(wOff, h, dOff+depth),
								new Vector3d(wOff, h+h1, dOff+depth),
								new Vector3d(wOff, h+h1, dOff),
								new Vector3d(wOff, h, dOff)
						}, uvs[3], sprite, bakedQuads);
						putVertexData(new Vector3d(1, 0, 0), new Vector3d[]{
								new Vector3d(wOff+width, h, dOff),
								new Vector3d(wOff+width, h+h1, dOff),
								new Vector3d(wOff+width, h+h1, dOff+depth),
								new Vector3d(wOff+width, h, dOff+depth)
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

	protected final void putVertexData(Vector3d normal, Vector3d[] vertices, double[] uvs, TextureAtlasSprite sprite, List<BakedQuad> out)
	{
		float d = LightUtil.diffuseLight((float)normal.x, (float)normal.y, (float)normal.z);
		BakedQuad quad = ClientUtils.createBakedQuad(format, vertices, Direction.getFacingFromVector(normal.x, normal.y, normal.z),
				sprite,
				uvs,
				new float[]{d, d, d, 1},
				false
		);
		out.add(quad);
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return null;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return overrideList;
	}


	ItemOverrideList overrideList = new ItemOverrideList()
	{

		@Nullable
		@Override
		public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn)
		{
			MineralMix mineral = CoresampleItem.getMix(stack);
			if(mineral!=null)
			{
				try
				{
					return modelCache.get(mineral, () -> {
						VertexFormat format;
						if(originalModel instanceof ModelCoresample)
							format = ((ModelCoresample)originalModel).format;
						else
							format = DefaultVertexFormats.BLOCK;
						return new ModelCoresample(mineral, format);
					});
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
	public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat)
	{
		return this;
	}

	@Override
	public boolean func_230044_c_()
	{
		return false;
	}

	public static class RawCoresampleModel implements IModelGeometry<RawCoresampleModel>
	{
		@Override
		public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation)
		{
			return new ModelCoresample(null, DefaultVertexFormats.BLOCK);
		}

		@Override
		public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<com.mojang.datafixers.util.Pair<String, String>> missingTextureErrors)
		{
			return ImmutableList.of();
		}
	}

	public static class CoresampleLoader implements IModelLoader<RawCoresampleModel>
	{
		public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "models/coresample");

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager)
		{
		}

		@Override
		public RawCoresampleModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
		{
			return new RawCoresampleModel();
		}
	}
}