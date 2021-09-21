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
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.models.connection.RenderCacheKey;
import blusunrize.immersiveengineering.client.models.obj.OBJHelper.MeshWrapper;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IModelDataBlock;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.math.Vector4f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.obj.MaterialLibrary;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.ModelGroup;
import net.minecraftforge.client.model.obj.OBJModel.ModelObject;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
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
	public boolean isDynamic;
	private final Map<String, String> texReplacements;

	public final OBJModel baseModel;
	private final BakedModel baseBaked;
	private final IModelConfiguration owner;
	private final ModelBakery bakery;
	private final Function<Material, TextureAtlasSprite> spriteGetter;
	private final ModelState sprite;

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
		if(state.getBlock() instanceof IAdvancedHasObjProperty)
			customData.add(new SinglePropertyModelData<>(
					((IAdvancedHasObjProperty)state.getBlock()).getIEObjState(state),
					Model.IE_OBJ_STATE
			));
		if(state.getBlock() instanceof IModelDataBlock)
			customData.add(((IModelDataBlock)state.getBlock()).getModelData(world, pos, state, tileData));
		else
		{
			BlockEntity te = world.getBlockEntity(pos);
			if(te instanceof IOBJModelCallback)
				customData.add(new SinglePropertyModelData<>((IOBJModelCallback<?>)te, IOBJModelCallback.PROPERTY));
			if(te instanceof IAdvancedHasObjProperty)
				customData.add(new SinglePropertyModelData<>(((IAdvancedHasObjProperty)te).getIEObjState(state),
						Model.IE_OBJ_STATE));
			if(te!=null)
			{
				LazyOptional<ShaderWrapper> shaderCap = te.getCapability(CapabilityShader.SHADER_CAPABILITY);
				if(shaderCap.isPresent())
					customData.add(new SinglePropertyModelData<>(shaderCap.orElseThrow(RuntimeException::new),
							CapabilityShader.MODEL_PROPERTY));
			}
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
		List<BakedQuad> quads = Lists.newArrayList();
		ItemStack shader = ItemStack.EMPTY;
		ShaderCase sCase = null;
		IOBJModelCallback callback = null;
		Object callbackObject = null;
		LazyOptional<ShaderWrapper> shaderOpt = tempStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
		if(shaderOpt.isPresent())
		{
			ShaderWrapper wrapper = shaderOpt.orElseThrow(NullPointerException::new);
			shader = wrapper.getShaderItem();
			if(shader.getItem() instanceof IShaderItem shaderItem)
				sCase = shaderItem.getShaderCase(shader, tempStack, wrapper.getShaderType());
		}
		else if(data.hasProperty(CapabilityShader.MODEL_PROPERTY))
		{
			ShaderWrapper wrapper = data.getData(CapabilityShader.MODEL_PROPERTY);
			if(wrapper!=null)
			{
				shader = wrapper.getShaderItem();
				if(shader.getItem() instanceof IShaderItem shaderItem)
					sCase = shaderItem.getShaderCase(shader, null, wrapper.getShaderType());
			}
		}

		if(!this.tempStack.isEmpty()&&tempStack.getItem() instanceof IOBJModelCallback)
		{
			callback = (IOBJModelCallback)tempStack.getItem();
			callbackObject = this.tempStack;
		}
		else if(data.hasProperty(IOBJModelCallback.PROPERTY))
		{
			callback = data.getData(IOBJModelCallback.PROPERTY);
			callbackObject = this.tempState;
		}
		for(String groupName : OBJHelper.getGroups(baseModel).keySet())
		{
			List<ShadedQuads> temp = addQuadsForGroup(callback, callbackObject, groupName, sCase, true);
			quads.addAll(
					temp.stream()
							.map(s -> s.quadsInLayer)
							.flatMap(List::stream)
							.filter(Objects::nonNull)
							.collect(Collectors.toList())
			);
		}

		if(callback!=null)
			quads = callback.modifyQuads(callbackObject, quads);
		return ImmutableList.copyOf(quads);
	}

	private final Cache<Pair<String, String>, List<ShadedQuads>> groupCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build();

	public <T> List<ShadedQuads> addQuadsForGroup(IOBJModelCallback<T> callback, T callbackObject, String groupName,
												  ShaderCase sCase, boolean allowCaching)
	{
		String objCacheKey = callback!=null?callback.getCacheKey(callbackObject): "<none>";
		if(sCase!=null)
			objCacheKey += ";"+sCase.getShaderType().toString();
		Pair<String, String> cacheKey = Pair.of(groupName, objCacheKey);
		if(allowCaching)
		{
			List<ShadedQuads> cached = groupCache.getIfPresent(cacheKey);
			if(cached!=null)
				return cached;
		}
		final int numPasses;
		if(sCase!=null)
			numPasses = sCase.getLayers().length;
		else
			numPasses = 1;
		ModelGroup g = OBJHelper.getGroups(baseModel).get(groupName);
		List<ShadedQuads> ret = new ArrayList<>();
		Transformation transform = state.transform();
		Transformation optionalTransform = sprite.getRotation();
		if(callback!=null)
			optionalTransform = callback.applyTransformations(callbackObject, groupName, optionalTransform);

		final MaterialSpriteGetter<T> spriteGetter = new MaterialSpriteGetter<>(this.spriteGetter, groupName, callback, callbackObject, sCase);
		final MaterialColorGetter<T> colorGetter = new MaterialColorGetter<>(groupName, callback, callbackObject, sCase);
		final TextureCoordinateRemapper coordinateRemapper = new TextureCoordinateRemapper(this.baseModel, sCase);

		if(state.visibility().isVisible(groupName)&&(callback==null||callback.shouldRenderGroup(callbackObject, groupName)))
			for(int pass = 0; pass < numPasses; ++pass)
				if(sCase==null||sCase.shouldRenderGroupForPass(groupName, pass))
				{
					List<BakedQuad> quads = new ArrayList<>();
					spriteGetter.setRenderPass(pass);
					colorGetter.setRenderPass(pass);
					coordinateRemapper.setRenderPass(pass);
					//g.addQuads(owner, new QuadListAdder(quads::add, transform), bakery, spriteGetter, sprite, format);
					IModelBuilder<?> modelBuilder = new QuadListAdder(quads::add, transform);
					Optional<String> texOverride = Optional.ofNullable(texReplacements.get(groupName));
					addModelObjectQuads(g, owner, modelBuilder, spriteGetter, colorGetter, coordinateRemapper, optionalTransform, texOverride);
					final Transformation finalTransform = optionalTransform;
					g.getParts().stream().filter(part -> owner.getPartVisibility(part)&&part instanceof ModelObject)
							.forEach(part -> addModelObjectQuads((ModelObject)part, owner, modelBuilder, spriteGetter,
									colorGetter, coordinateRemapper, finalTransform, texOverride));
					ShaderLayer layer = sCase!=null?sCase.getLayers()[pass]: new ShaderLayer(new ResourceLocation("missing/no"), -1)
					{
						@Override
						public RenderType getRenderType(RenderType baseType)
						{
							return baseType;
						}
					};
					ret.add(new ShadedQuads(layer, quads));
				}
		if(allowCaching)
			groupCache.put(cacheKey, ret);
		return ret;
	}

	/**
	 * Yep, this is 90% a copy of ModelObject.addQuads. We need custom hooks in there, so we copy the rest around it.
	 */
	private void addModelObjectQuads(ModelObject modelObject, IModelConfiguration owner, IModelBuilder<?> modelBuilder,
									 MaterialSpriteGetter<?> spriteGetter, MaterialColorGetter<?> colorGetter,
									 TextureCoordinateRemapper coordinateRemapper,
									 Transformation transform, Optional<String> textureOverride)
	{
		List<MeshWrapper> meshes = OBJHelper.getMeshes(modelObject);
		for(MeshWrapper mesh : meshes)
		{
			MaterialLibrary.Material mat = mesh.getMaterial();
			if(mat==null)
				continue;
			TextureAtlasSprite texture = spriteGetter.apply(
					mat.name,
					ModelLoaderRegistry.resolveTexture(textureOverride.orElse(mat.diffuseColorMap), owner)
			);
			int tintIndex = mat.diffuseTintIndex;
			Vector4f colorTint = colorGetter.apply(mat.name, mat.diffuseColor);

			for(int[][] face : mesh.getFaces())
			{
				boolean drawFace = coordinateRemapper.remapCoord(face);
				if(drawFace)
				{
					Pair<BakedQuad, Direction> quad = OBJHelper.makeQuad(baseModel, face, tintIndex, colorTint,
							mat.ambientColor, texture, transform);
					if(quad.getRight()==null)
						modelBuilder.addGeneralQuad(quad.getLeft());
					else
						modelBuilder.addFaceQuad(quad.getRight(), quad.getLeft());
				}
				coordinateRemapper.resetCoords();
			}
		}
	}

	public record ShadedQuads(ShaderLayer layer, List<BakedQuad> quadsInLayer)
	{
	}
}