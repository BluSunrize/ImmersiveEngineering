/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.models.connection.RenderCacheKey;
import blusunrize.immersiveengineering.client.models.obj.OBJHelper.MeshWrapper;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IModelDataBlock;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.obj.OBJModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class IESmartObjModel implements ICacheKeyProvider<RenderCacheKey>
{
	public static Cache<ComparableItemStack, BakedModel> cachedBakedItemModels = CacheBuilder.newBuilder()
			.maximumSize(100).expireAfterAccess(60, TimeUnit.SECONDS).build();
	public static Cache<RenderCacheKey, List<BakedQuad>> modelCache = CacheBuilder.newBuilder()
			.maximumSize(100).expireAfterAccess(60, TimeUnit.SECONDS).build();

	//TODO check which ones are still needed
	private ImmutableList<BakedQuad> bakedQuads;
	private ItemStack tempStack = ItemStack.EMPTY;
	private BlockState tempState;
	private LivingEntity tempEntity;
	public static LivingEntity tempEntityStatic;

	private final Map<String, String> texReplacements;
	public final OBJModel baseModel;
	private final BakedModel baseBaked;
	private final IModelConfiguration owner;
	private final ModelBakery bakery;
	private final Function<Material, TextureAtlasSprite> spriteGetter;
	private final ModelState sprite;
	private final boolean isDynamic;

	private final IEObjState state;

	public IESmartObjModel(OBJModel baseModel, BakedModel baseBaked, IModelConfiguration owner, ModelBakery bakery,
						   Function<Material, TextureAtlasSprite> spriteGetter, ModelState sprite,
						   IEObjState state, boolean dynamic, Map<String, String> texReplacements)
	{

		this.baseModel = baseModel;
		this.baseBaked = baseBaked;
		this.owner = owner;
		this.bakery = bakery;
		this.spriteGetter = spriteGetter;
		this.sprite = sprite;
		this.state = state;
		this.isDynamic = dynamic;
		this.texReplacements = texReplacements;
		// Default tint index should be -1 (see VertexLighterFlat), OBJ materials set it to 0 by default
		OBJHelper.getGroups(baseModel).values().stream()
				.flatMap(g -> Stream.concat(Stream.of(g), OBJHelper.getParts(g).values().stream()))
				.flatMap(o -> OBJHelper.getMeshes(o).stream())
				.map(MeshWrapper::getMaterial)
				.filter(m -> m.diffuseTintIndex==0)
				.forEach(m -> m.diffuseTintIndex = -1);
	}

	@Override
	public boolean doesHandlePerspectives()
	{
		return true;
	}

	@Override
	public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack mat)
	{
		Transformation matrix =
				PerspectiveMapWrapper.getTransforms(owner.getCombinedTransform()).getOrDefault(cameraTransformType, Transformation.identity());

		matrix.push(mat);
		if(!this.tempStack.isEmpty()&&this.tempStack.getItem() instanceof IOBJModelCallback)
			((IOBJModelCallback)this.tempStack.getItem()).handlePerspective(this.tempStack, cameraTransformType, mat, tempEntity);

		//matrix = new Matrix4(); //Assign Matrixes here manually in debug mode, then move them to the actual registration method
		//Dynamic stuff to use when figurign out positioning for new items!
		//if(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND)//FP_R
		//	matrix =  new Matrix4().scale(.375, .4375, .375).translate(-.25, 1, .5).rotate(Math.PI*.5, 0, 1, 0);
		//else if(cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND)//FP_L
		//	matrix = new Matrix4().scale(-.375, .4375, .375).translate(.25, 1, .5).rotate(-Math.PI*.5, 0, 1, 0);
		//else if(cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND) //TP_R
		//	matrix = new Matrix4().translate(0, .5, .1);
		//else if(cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND) //TP_L
		//	matrix = new Matrix4().translate(0, .5, .1);
		//else if(cameraTransformType==TransformType.FIXED) //FIXED
		//	matrix = new Matrix4();
		//else if(cameraTransformType==TransformType.GUI) //INV
		//	matrix = new Matrix4().rotate(-Math.PI/4, 0, 0, 1).rotate(Math.PI/8, 0, 1, 0);
		//if (cameraTransformType==TransformType.GROUND)//GROUND
		//	matrix = new Matrix4().scale(.5, .5, .5).translate(0, .5, 0);

		return this;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand)
	{
		return getQuads(state, side, rand, EmptyModelData.INSTANCE);
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
	public boolean usesBlockLight()
	{
		return true;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return isDynamic;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return baseBaked.getParticleIcon();
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides()
	{
		return overrideList;
	}

	ItemOverrides overrideList = new ItemOverrides()
	{
		@Override
		public BakedModel resolve(@Nonnull BakedModel originalModel, @Nonnull ItemStack stack,
												 @Nullable ClientLevel world, @Nullable LivingEntity entity, int unused)
		{
			// TODO get cache key here and return a "fixed key" model
			tempEntityStatic = entity;
			ComparableItemStack comp = ComparableItemStack.create(stack, false, true);
			if(comp==null)
				return originalModel;
			BakedModel model = cachedBakedItemModels.getIfPresent(comp);
			if(model==null)
			{
				if(originalModel instanceof IESmartObjModel smrtModel)
				{

					model = new IESmartObjModel(smrtModel.baseModel, smrtModel.baseBaked, smrtModel.owner, smrtModel.bakery,
							smrtModel.spriteGetter, smrtModel.sprite,
							smrtModel.state, smrtModel.isDynamic, ImmutableMap.of());
					((IESmartObjModel)model).tempStack = stack;
					((IESmartObjModel)model).tempEntity = entity;
				}
				else
					model = originalModel;
				comp.copy();
				cachedBakedItemModels.put(comp, model);
			}
			if(model instanceof IESmartObjModel smartModel)
			{
				smartModel.tempStack = stack;
				smartModel.tempEntity = entity;
			}
			return model;
		}
	};

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(BlockState blockState, Direction side, @Nonnull Random rand, @Nonnull IModelData modelData)
	{
		if(side!=null)
			return ImmutableList.of();
		IEObjState objState = null;
		Map<String, String> tex = ImmutableMap.of();
		if(modelData.hasProperty(Model.IE_OBJ_STATE))
			objState = modelData.getData(Model.IE_OBJ_STATE);
		if(modelData.hasProperty(Model.TEXTURE_REMAP))
			tex = modelData.getData(Model.TEXTURE_REMAP);
		return getQuads(blockState, objState, tex, true, modelData);
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		List<IModelData> customData = new ArrayList<>();
		if(state.getBlock() instanceof IModelDataBlock)
			customData.add(((IModelDataBlock)state.getBlock()).getModelData(world, pos, state, tileData));
		else
		{
			BlockEntity te = world.getBlockEntity(pos);
			if(te instanceof IOBJModelCallback)
				customData.add(new SinglePropertyModelData<>((IOBJModelCallback<?>)te, IOBJModelCallback.PROPERTY));
		}
		customData.add(tileData);
		return CombinedModelData.combine(customData.toArray(new IModelData[0]));
	}

	@Override
	@Nullable
	public RenderCacheKey getKey(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		if(side!=null)
			return null;
		IEObjState objState = null;
		Map<String, String> tex = ImmutableMap.of();
		if(extraData.hasProperty(Model.IE_OBJ_STATE))
			objState = extraData.getData(Model.IE_OBJ_STATE);
		if(extraData.hasProperty(Model.TEXTURE_REMAP))
			tex = extraData.getData(Model.TEXTURE_REMAP);
		return getKey(state, extraData, true, objState, tex);
	}

	public RenderCacheKey getKey(@Nullable BlockState blockState, @Nonnull IModelData modelData,
								 boolean addAnimationAndTex, IEObjState visibility, Map<String, String> tex)
	{
		String cacheKey = "";
		if(blockState!=null&&modelData.hasProperty(IOBJModelCallback.PROPERTY))
			cacheKey = modelData.getData(IOBJModelCallback.PROPERTY).getCacheKey(blockState);
		if(addAnimationAndTex)
			return new RenderCacheKey(blockState, sprite, MinecraftForgeClient.getRenderLayer(), visibility, tex, cacheKey);
		else
			return new RenderCacheKey(blockState, sprite, MinecraftForgeClient.getRenderLayer(), cacheKey);
	}

	public List<BakedQuad> getQuads(BlockState blockState, IEObjState visibility, Map<String, String> tex,
									boolean addAnimationAndTex, IModelData modelData)
	{
		if(blockState==null)
		{
			if(bakedQuads==null)
				bakedQuads = ImmutableList.copyOf(buildQuads(modelData));
			return bakedQuads;
		}
		this.tempState = blockState;
		RenderCacheKey key = getKey(blockState, modelData, addAnimationAndTex, visibility, tex);
		try
		{
			List<BakedQuad> quads = modelCache.get(key, () ->
			{
				IESmartObjModel model;
				if(visibility!=null)
					model = new IESmartObjModel(baseModel, baseBaked, owner, bakery, spriteGetter, sprite, visibility, isDynamic, tex);
				else
					model = new IESmartObjModel(baseModel, baseBaked, owner, bakery, spriteGetter, sprite, this.state, isDynamic, tex);

				model.tempState = blockState;
				return model.buildQuads(modelData);
			});
			return Collections.synchronizedList(Lists.newArrayList(quads));
		} catch(ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}

	private ImmutableList<BakedQuad> buildQuads(IModelData data)
	{
		return ImmutableList.of();
	}
}