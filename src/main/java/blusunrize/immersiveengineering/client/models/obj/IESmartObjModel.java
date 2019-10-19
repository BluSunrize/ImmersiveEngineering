/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.models.connection.RenderCacheKey;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.*;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad.Builder;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class IESmartObjModel extends OBJBakedModel
{
	public static Cache<ComparableItemStack, IBakedModel> cachedBakedItemModels = CacheBuilder.newBuilder()
			.maximumSize(100).expireAfterAccess(60, TimeUnit.SECONDS).build();
	public static HashMap<RenderCacheKey, List<BakedQuad>> modelCache = new HashMap<>();
	IBakedModel baseModel;
	HashMap<TransformType, Matrix4> transformationMap;
	ImmutableList<BakedQuad> bakedQuads;
	ItemStack tempStack = ItemStack.EMPTY;
	BlockState tempState;
	public LivingEntity tempEntity;
	public static LivingEntity tempEntityStatic;
	VertexFormat format;
	Map<String, String> texReplace = null;
	public TransformType lastCameraTransform = TransformType.FIXED;
	boolean isDynamic;

	public IESmartObjModel(IBakedModel baseModel, OBJModel model, IModelState state, VertexFormat format,
						   ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4> transformationMap,
						   boolean isDynamic)
	{
		model.super(model, state, format, textures);
		this.baseModel = baseModel;
		this.transformationMap = transformationMap;
		this.format = format;
		this.isDynamic = isDynamic;
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
	{
		this.lastCameraTransform = cameraTransformType;
		if(transformationMap==null||transformationMap.isEmpty())
			return super.handlePerspective(cameraTransformType);
		Matrix4 matrix = transformationMap.containsKey(cameraTransformType)?transformationMap.get(cameraTransformType).copy(): new Matrix4();

		if(!this.tempStack.isEmpty()&&this.tempStack.getItem() instanceof IOBJModelCallback)
			matrix = ((IOBJModelCallback)this.tempStack.getItem()).handlePerspective(this.tempStack, cameraTransformType, matrix, tempEntity);

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

		return Pair.of(this, matrix.toMatrix4f());
	}

	VertexFormat getFormat()
	{
		return this.format;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return isDynamic;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return overrideList;
	}

	ItemOverrideList overrideList = new ItemOverrideList()
	{
		@Override
		public IBakedModel getModelWithOverrides(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack,
												 @Nullable World world, @Nullable LivingEntity entity)
		{
			tempEntityStatic = entity;
			ComparableItemStack comp = ApiUtils.createComparableItemStack(stack, false, true);
			if(comp==null)
				return originalModel;
			IBakedModel model = cachedBakedItemModels.getIfPresent(comp);
			if(model==null)
			{
				if(originalModel instanceof IESmartObjModel)
				{
					IESmartObjModel newModel = (IESmartObjModel)originalModel;

					ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
					builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
					TextureAtlasSprite missing = Minecraft.getInstance().getTextureMap()
							.getAtlasSprite(new ResourceLocation("missingno").toString());

					for(String s : newModel.getModel().getMatLib().getMaterialNames())
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
									newModel.getModel().getMatLib().getMaterial(s).getTexture().getTextureLocation().toString());
						builder.put(s, sprite);
					}
					builder.put("missingno", missing);
					IESmartObjModel bakedModel = new IESmartObjModel(newModel.baseModel, newModel.getModel(), newModel.getState(),
							newModel.getFormat(), builder.build(), transformationMap, isDynamic);
					bakedModel.tempStack = stack;
					bakedModel.tempEntity = entity;
					model = bakedModel;
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

	@Override
	public List<BakedQuad> getQuads(BlockState blockState, Direction side, Random rand, IModelData modelData)
	{
		if(side!=null)
			return ImmutableList.of();
		OBJState objState = null;
		Map<String, String> tex = ImmutableMap.of();
		if(modelData.hasProperty(Model.OBJ_STATE))
			objState = modelData.getData(Model.OBJ_STATE);
		if(modelData.hasProperty(Model.TEXTURE_REMAP))
			tex = modelData.getData(Model.TEXTURE_REMAP);
		//TODO addAnimationAndtex?
		return getQuads(blockState, side, rand.nextLong(), objState, tex, true, modelData);
	}

	public List<BakedQuad> getQuads(BlockState blockState, Direction side, long rand, OBJState objstate, Map<String, String> tex,
									boolean addAnimationAndTex, IModelData modelData)
	{
		texReplace = tex;
		this.tempState = blockState;
		RenderCacheKey adapter;
		if(objstate!=null)
		{
			if(objstate.parent==null||objstate.parent==TRSRTransformation.identity())
				objstate.parent = this.getState();
			if(objstate.getVisibilityMap().containsKey(Group.ALL)||objstate.getVisibilityMap().containsKey(Group.ALL_EXCEPT))
				this.updateStateVisibilityMap(objstate);
		}
		if(addAnimationAndTex)
			adapter = new RenderCacheKey(blockState, MinecraftForgeClient.getRenderLayer(), objstate, tex);
		else
			adapter = new RenderCacheKey(blockState, MinecraftForgeClient.getRenderLayer());
		List<BakedQuad> quads = modelCache.get(adapter);
		if(quads==null)
		{
			IESmartObjModel model = null;
			if(objstate!=null)
				model = new IESmartObjModel(baseModel, getModel(), objstate, getFormat(), getTextures(),
						transformationMap, isDynamic);
			if(model==null)
				model = new IESmartObjModel(baseModel, getModel(), this.getState(), getFormat(), getTextures(),
						transformationMap, isDynamic);
			model.tempState = blockState;
			model.texReplace = tex;
			quads = model.buildQuads(modelData);
			modelCache.put(adapter, quads);
		}
		return Collections.synchronizedList(Lists.newArrayList(quads));
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
		else if(data.hasProperty(CapabilityShader.BLOCKSTATE_PROPERTY))
		{
			ShaderWrapper wrapper = data.getData(CapabilityShader.BLOCKSTATE_PROPERTY);
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
		for(String groupName : getModel().getMatLib().getGroups().keySet())
		{
			List<Pair<BakedQuad, ShaderLayer>> temp = addQuadsForGroup(callback, callbackObject, groupName, sCase, shader);
			quads.addAll(temp.stream().filter(Objects::nonNull).map(Pair::getKey).collect(Collectors.toList()));
		}

		if(callback!=null)
			quads = callback.modifyQuads(callbackObject, quads);
		return ImmutableList.copyOf(quads);
	}

	public <T> List<Pair<BakedQuad, ShaderLayer>> addQuadsForGroup(IOBJModelCallback<T> callback, T callbackObject,
																   String groupName, ShaderCase sCase,
																   ItemStack shader)
	{
		int maxPasses = 1;
		if(sCase!=null)
			maxPasses = sCase.getLayers().length;
		Group g = getModel().getMatLib().getGroups().get(groupName);
		List<Face> faces = new ArrayList<>();
		Optional<TRSRTransformation> transform = Optional.empty();
		if(this.getState() instanceof OBJState)
		{
			OBJState state = (OBJState)this.getState();
			if(state.parent!=null)
				transform = state.parent.apply(Optional.empty());
			if(callback!=null)
				transform = callback.applyTransformations(callbackObject, groupName, transform);
			if(state.getGroupsWithVisibility(true).contains(groupName))
				faces.addAll(g.applyTransform(transform));
		}
		else
		{
			transform = getState().apply(Optional.empty());
			if(callback!=null)
				transform = callback.applyTransformations(callbackObject, groupName, transform);
			faces.addAll(g.applyTransform(transform));
		}
		List<Pair<BakedQuad, ShaderLayer>> quads = new ArrayList<>(faces.size());
		for(int pass = 0; pass < maxPasses; pass++)
		{
			ShaderLayer shaderLayer = sCase!=null?sCase.getLayers()[pass]: null;
			if(callback!=null)
				if(!callback.shouldRenderGroup(callbackObject, groupName))
					continue;
			if(sCase!=null)
				if(!sCase.renderModelPartForPass(shader, tempStack, groupName, pass))
					continue;

			int argb = 0xffffffff;
			if(sCase!=null)
				argb = sCase.getARGBColourModifier(shader, tempStack, groupName, pass);
			else if(callback!=null)
				argb = callback.getRenderColour(callbackObject, groupName);

			boolean dynQuad = false;

			float[] colour = {(argb >> 16&255)/255f, (argb >> 8&255)/255f, (argb&255)/255f, (argb >> 24&255)/255f};

			for(int faceId = 0; faceId < faces.size(); faceId++)
			{
				Face f = faces.get(faceId);
				TextureAtlasSprite tempSprite = null;
				if(this.getModel().getMatLib().getMaterial(f.getMaterialName()).isWhite()&&!"null".equals(f.getMaterialName()))
				{
					for(Vertex v : f.getVertices())
						if(!v.getMaterial().equals(this.getModel().getMatLib().getMaterial(v.getMaterial().getName())))
							v.setMaterial(this.getModel().getMatLib().getMaterial(v.getMaterial().getName()));
					tempSprite = ModelLoader.White.INSTANCE;
				}
				else
				{
					if(sCase!=null)
					{
						ResourceLocation rl = sCase.getReplacementSprite(shader, tempStack, groupName, pass);
						if(rl!=null)
							tempSprite = ClientUtils.getSprite(rl);
					}
					if(tempSprite==null&&callback!=null)
						tempSprite = callback.getTextureReplacement(callbackObject, f.getMaterialName());
					if(tempSprite==null&&tempState!=null&&texReplace!=null)
					{
						String s = texReplace.get(groupName);
						if(s!=null)
							tempSprite = Minecraft.getInstance().getTextureMap().getAtlasSprite(s);
					}
					if(tempSprite==null&&!"null".equals(f.getMaterialName()))
						tempSprite = Minecraft.getInstance().getTextureMap().getAtlasSprite(this.getModel().getMatLib().getMaterial(f.getMaterialName()).getTexture().getTextureLocation().toString());
				}
				if(tempSprite!=null)
				{
					Builder builder = new Builder(getFormat());
					builder.setQuadOrientation(Direction.getFacingFromVector(f.getNormal().x, f.getNormal().y, f.getNormal().z));
					builder.setTexture(tempSprite);
					builder.setQuadTint(pass);
					Normal faceNormal = f.getNormal();
					TextureCoordinate[] uvs = new TextureCoordinate[4];
					boolean renderFace = true;
					for(int i = 0; i < 4; i++)
					{
						Vertex vertex = f.getVertices()[i];
						//V-Flip is processed here already, rather than in the later method, since it's needed for easy UV comparissons on the Shader Layers
						uvs[i] = vertex.hasTextureCoordinate()?new TextureCoordinate(vertex.getTextureCoordinate().u, 1-vertex.getTextureCoordinate().v, vertex.getTextureCoordinate().w): TextureCoordinate.getDefaultUVs()[i];

						if(shaderLayer!=null)
						{
							double[] texBounds = shaderLayer.getTextureBounds();
							if(texBounds!=null)
							{
								if(texBounds[0] > uvs[i].u||uvs[i].u > texBounds[2]||texBounds[1] > uvs[i].v||uvs[i].v > texBounds[3])//if any uvs are outside the layers bounds
								{
									renderFace = false;
									break;
								}
								double dU = texBounds[2]-texBounds[0];
								double dV = texBounds[3]-texBounds[1];
								//Rescaling to the partial bounds that the texture represents
								uvs[i].u = (float)((uvs[i].u-texBounds[0])/dU);
								uvs[i].v = (float)((uvs[i].v-texBounds[1])/dV);
							}
							//Rescaling to the selective area of the texture that is used
							double[] cutBounds = shaderLayer.getCutoutBounds();
							if(cutBounds!=null)
							{
								double dU = cutBounds[2]-cutBounds[0];
								double dV = cutBounds[3]-cutBounds[1];
								uvs[i].u = (float)(cutBounds[0]+dU*uvs[i].u);
								uvs[i].v = (float)(cutBounds[1]+dV*uvs[i].v);
							}
						}
					}
					if(renderFace)
					{
						for(int i = 0; i < 4; i++)
							putVertexData(builder, f.getVertices()[i], faceNormal, uvs[i], tempSprite, colour);
						quads.add(new ImmutablePair<>(builder.build(),
								shaderLayer!=null&&shaderLayer.isDynamicLayer()?shaderLayer: null));
					}
				}
			}
		}
		return quads;
	}

	protected final void putVertexData(IVertexConsumer builder, Vertex v, Normal faceNormal, TextureCoordinate texCoord, TextureAtlasSprite sprite, float[] colour)
	{
		for(int e = 0; e < getFormat().getElementCount(); e++)
		{
			switch(getFormat().getElement(e).getUsage())
			{
				case POSITION:
					builder.put(e, v.getPos().x, v.getPos().y, v.getPos().z, v.getPos().w);
					break;
				case COLOR:
					float d;
					if(v.hasNormal())
						d = LightUtil.diffuseLight(v.getNormal().x, v.getNormal().y, v.getNormal().z);
					else
						d = LightUtil.diffuseLight(faceNormal.x, faceNormal.y, faceNormal.z);
					if(v.getMaterial()!=null)
						builder.put(e,
								d*v.getMaterial().getColor().x*colour[0],
								d*v.getMaterial().getColor().y*colour[1],
								d*v.getMaterial().getColor().z*colour[2],
								v.getMaterial().getColor().w*colour[3]);
					else
						builder.put(e, d*colour[0], d*colour[1], d*colour[2], 1*colour[3]);
					break;
				case UV:
					builder.put(e,
							sprite.getInterpolatedU(texCoord.u*16),
							sprite.getInterpolatedV((texCoord.v)*16),//v-flip used to be processed here but was moved because of shader layers
							0, 1);
					break;
				case NORMAL:
					if(!v.hasNormal())
						builder.put(e, faceNormal.x, faceNormal.y, faceNormal.z, 0);
					else
						builder.put(e, v.getNormal().x, v.getNormal().y, v.getNormal().z, 0);
					break;
				default:
					builder.put(e);
			}
		}
	}

	protected void updateStateVisibilityMap(OBJState state)
	{
		if(state.getVisibilityMap().containsKey(Group.ALL))
		{
			boolean operation = state.getVisibilityMap().get(Group.ALL);
			state.getVisibilityMap().clear();
			for(String s : this.getModel().getMatLib().getGroups().keySet())
			{
				state.getVisibilityMap().put(s, OBJState.Operation.SET_TRUE.performOperation(operation));
			}
		}
		else if(state.getVisibilityMap().containsKey(Group.ALL_EXCEPT))
		{
			List<String> exceptList = state.getGroupNamesFromMap().subList(1, state.getGroupNamesFromMap().size());
			state.getVisibilityMap().remove(Group.ALL_EXCEPT);
			for(String s : this.getModel().getMatLib().getGroups().keySet())
			{
				if(!exceptList.contains(s))
				{
					state.getVisibilityMap().put(s, OBJState.Operation.SET_TRUE.performOperation(state.getVisibilityMap().get(s)));
				}
			}
		}
		else
		{
			for(String s : state.getVisibilityMap().keySet())
			{
				state.getVisibilityMap().put(s, OBJState.Operation.SET_TRUE.performOperation(state.getVisibilityMap().get(s)));
			}
		}
	}

	static Field f_textures;

	public static ImmutableMap<String, TextureAtlasSprite> getTexturesForOBJModel(IBakedModel model)
	{
		try
		{
			if(f_textures==null)
			{
				f_textures = OBJBakedModel.class.getDeclaredField("textures");
				f_textures.setAccessible(true);
			}
			return (ImmutableMap<String, TextureAtlasSprite>)f_textures.get(model);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public ImmutableMap<String, TextureAtlasSprite> getTextures()
	{
		try
		{
			if(f_textures==null)
			{
				f_textures = OBJBakedModel.class.getDeclaredField("textures");
				f_textures.setAccessible(true);
			}
			return (ImmutableMap<String, TextureAtlasSprite>)f_textures.get(this);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}