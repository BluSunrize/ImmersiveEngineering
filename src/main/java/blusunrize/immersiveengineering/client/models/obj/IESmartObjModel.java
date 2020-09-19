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
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.models.connection.RenderCacheKey;
import blusunrize.immersiveengineering.client.models.obj.OBJHelper.MeshWrapper;
import blusunrize.immersiveengineering.client.utils.CombinedModelData;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IModelDataBlock;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.IBlockDisplayReader;
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

@SuppressWarnings("deprecation")
public class IESmartObjModel implements ICacheKeyProvider<RenderCacheKey>
{
	public static Cache<ComparableItemStack, IBakedModel> cachedBakedItemModels = CacheBuilder.newBuilder()
			.maximumSize(100).expireAfterAccess(60, TimeUnit.SECONDS).build();
	public static Cache<RenderCacheKey, List<BakedQuad>> modelCache = CacheBuilder.newBuilder()
			.maximumSize(100).expireAfterAccess(60, TimeUnit.SECONDS).build();

	//TODO check which ones are still needed
	ImmutableList<BakedQuad> bakedQuads;
	ItemStack tempStack = ItemStack.EMPTY;
	BlockState tempState;
	public LivingEntity tempEntity;
	public static LivingEntity tempEntityStatic;
	public boolean isDynamic;

	public final OBJModel baseModel;
	private final IBakedModel baseBaked;
	private final IModelConfiguration owner;
	private final ModelBakery bakery;
	private final Function<RenderMaterial, TextureAtlasSprite> spriteGetter;
	private final IModelTransform sprite;

	private final IEObjState state;

	public IESmartObjModel(OBJModel baseModel, IBakedModel baseBaked, IModelConfiguration owner, ModelBakery bakery,
						   Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform sprite,
						   IEObjState state, boolean dynamic)
	{

		this.baseModel = baseModel;
		this.baseBaked = baseBaked;
		this.owner = owner;
		this.bakery = bakery;
		this.spriteGetter = spriteGetter;
		this.sprite = sprite;
		this.state = state;
		this.isDynamic = dynamic;
	}

	@Override
	public boolean doesHandlePerspectives()
	{
		return true;
	}

	@Override
	public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat)
	{
		TransformationMatrix matrix =
				PerspectiveMapWrapper.getTransforms(owner.getCombinedTransform()).getOrDefault(cameraTransformType, TransformationMatrix.identity());

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
	public boolean isSideLit()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return isDynamic;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return baseBaked.getParticleTexture();
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return overrideList;
	}

	ItemOverrideList overrideList = new ItemOverrideList()
	{
		@Override
		public IBakedModel getOverrideModel(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack,
												 @Nullable ClientWorld world, @Nullable LivingEntity entity)
		{
			tempEntityStatic = entity;
			ComparableItemStack comp = ComparableItemStack.create(stack, false, true);
			if(comp==null)
				return originalModel;
			IBakedModel model = cachedBakedItemModels.getIfPresent(comp);
			if(model==null)
			{
					/*TODO
				if(originalModel instanceof IESmartObjModel)
				{
					IESmartObjModel newModel = (IESmartObjModel)originalModel;

					ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
					builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
					TextureAtlasSprite missing = Minecraft.getInstance().getTextureMap()
							.getAtlasSprite(new ResourceLocation("missingno").toString());

					for(String s : newModel.baseOld.getModel().getMatLib().getMaterialNames())
					{
						TextureAtlasSprite sprite;
						{
							LazyOptional<TextureAtlasSprite> tempSprite = stack.getCapability(CapabilityShader.SHADER_CAPABILITY).map(wrapper ->
							{
								ItemStack shader = wrapper.getShaderItem();
								if(!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
								{
									ShaderCase sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, stack, wrapper.getShaderType());
									if(sCase!=null)
									{
										ResourceLocation rl = sCase.getReplacementSprite(shader, stack, s, 0);
										return ClientUtils.getSprite(rl);
									}
								}
								return missing;
							})
									.filter(t -> t!=missing);
							if(tempSprite.isPresent())
								sprite = tempSprite.orElse(missing);
							else
								sprite = null;
						}
						if(sprite!=null&&stack.getItem() instanceof IOBJModelCallback)
							sprite = ((IOBJModelCallback)stack.getItem()).getTextureReplacement(stack, s);
						if(sprite==null)
							sprite = Minecraft.getInstance().getTextureMap().getAtlasSprite(
									newModel.baseOld.getModel().getMatLib().getMaterial(s).getTexture().getTextureLocation().toString());
						builder.put(s, sprite);
					}
					builder.put("missingno", missing);
					IESmartObjModel bakedModel = new IESmartObjModel(newModel.baseModel, baseBaked, newModel.baseOld.getModel(),
							newModel.state, newModel.getFormat(), builder.build(), transformationMap, isDynamic);
					bakedModel.tempStack = stack;
					bakedModel.tempEntity = entity;
					model = bakedModel;
				}
				else
					 */
				if(originalModel instanceof IESmartObjModel)
				{
					IESmartObjModel smrtModel = (IESmartObjModel)originalModel;

					model = new IESmartObjModel(smrtModel.baseModel, smrtModel.baseBaked, smrtModel.owner, smrtModel.bakery,
							smrtModel.spriteGetter, smrtModel.sprite,
							smrtModel.state, smrtModel.isDynamic);
					((IESmartObjModel)model).tempStack = stack;
					((IESmartObjModel)model).tempEntity = entity;
				}
				else
					model = originalModel;
				comp.copy();
				cachedBakedItemModels.put(comp, model);
			}
			if(model instanceof IESmartObjModel)
			{
				((IESmartObjModel)model).tempStack = stack;
				((IESmartObjModel)model).tempEntity = entity;
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
	public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
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
			TileEntity te = world.getTileEntity(pos);
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
		return new CombinedModelData(customData.toArray(new IModelData[0]));
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
			return new RenderCacheKey(blockState, MinecraftForgeClient.getRenderLayer(), visibility, tex, cacheKey);
		else
			return new RenderCacheKey(blockState, MinecraftForgeClient.getRenderLayer(), cacheKey);
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
					model = new IESmartObjModel(baseModel, baseBaked, owner, bakery, spriteGetter, sprite, visibility, isDynamic);
				else
					model = new IESmartObjModel(baseModel, baseBaked, owner, bakery, spriteGetter, sprite, this.state, isDynamic);

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
			if(!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
				sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, tempStack, wrapper.getShaderType());
		}
		else if(data.hasProperty(CapabilityShader.MODEL_PROPERTY))
		{
			ShaderWrapper wrapper = data.getData(CapabilityShader.MODEL_PROPERTY);
			if(wrapper!=null)
			{
				shader = wrapper.getShaderItem();
				if(!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
					sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, null, wrapper.getShaderType());
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

	private Cache<Pair<String, String>, List<ShadedQuads>> groupCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build();

	public <T> List<ShadedQuads> addQuadsForGroup(IOBJModelCallback<T> callback, T callbackObject, String groupName,
												  ShaderCase sCase, boolean allowCaching)
	{
		String objCacheKey = callback!=null?callback.getCacheKey(callbackObject): "<none>";
		if(sCase!=null)
			objCacheKey += ";"+sCase.getShaderType().toString();
		Pair<String, String> cacheKey = Pair.of(groupName, objCacheKey);
		if(allowCaching&&false)
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
		TransformationMatrix transform = state.transform;
		TransformationMatrix optionalTransform = sprite.getRotation();
		if(callback!=null)
			optionalTransform = callback.applyTransformations(callbackObject, groupName, optionalTransform);

		final MaterialSpriteGetter<T> spriteGetter = new MaterialSpriteGetter<>(this.spriteGetter, groupName, callback, callbackObject, sCase);
		final MaterialColorGetter<T> colorGetter = new MaterialColorGetter<>(groupName, callback, callbackObject, sCase);
		final TextureCoordinateRemapper coordinateRemapper = new TextureCoordinateRemapper(this.baseModel, sCase);

		if(state.visibility.isVisible(groupName)&&(callback==null||callback.shouldRenderGroup(callbackObject, groupName)))
			for(int pass = 0; pass < numPasses; ++pass)
				if(sCase==null||sCase.shouldRenderGroupForPass(groupName, pass))
				{
					List<BakedQuad> quads = new ArrayList<>();
					spriteGetter.setRenderPass(pass);
					colorGetter.setRenderPass(pass);
					coordinateRemapper.setRenderPass(pass);
					//g.addQuads(owner, new QuadListAdder(quads::add, transform), bakery, spriteGetter, sprite, format);
					IModelBuilder modelBuilder = new QuadListAdder(quads::add, transform);
					addModelObjectQuads(g, owner, modelBuilder, spriteGetter, colorGetter, coordinateRemapper, optionalTransform);
					final TransformationMatrix finalTransform = optionalTransform;
					g.getParts().stream().filter(part -> owner.getPartVisibility(part)&&part instanceof ModelObject)
							.forEach(part -> addModelObjectQuads((ModelObject)part, owner, modelBuilder, spriteGetter,
									colorGetter, coordinateRemapper, finalTransform));
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
									 TransformationMatrix transform)
	{
		List<MeshWrapper> meshes = OBJHelper.getMeshes(modelObject);
		for(MeshWrapper mesh : meshes)
		{
			MaterialLibrary.Material mat = mesh.getMaterial();
			if(mat==null)
				continue;
			TextureAtlasSprite texture = spriteGetter.apply(
					mat.name,
					ModelLoaderRegistry.resolveTexture(mat.diffuseColorMap, owner)
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

	public static class ShadedQuads
	{
		public final ShaderLayer layer;
		public final List<BakedQuad> quadsInLayer;

		public ShadedQuads(ShaderLayer layer, List<BakedQuad> quadsInLayer)
		{
			this.layer = layer;
			this.quadsInLayer = quadsInLayer;
		}
	}
}