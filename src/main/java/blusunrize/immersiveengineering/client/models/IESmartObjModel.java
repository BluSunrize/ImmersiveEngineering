package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.shader.IShaderEquipableItem;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.models.smart.ConnModelReal.ExtBlockstateAdapter;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.*;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class IESmartObjModel extends OBJBakedModel
{
	public static Map<ComparableItemStack, IBakedModel> cachedBakedItemModels = new ConcurrentHashMap<ComparableItemStack, IBakedModel>();
	public static HashMap<ExtBlockstateAdapter, List<BakedQuad>> modelCache = new HashMap<>();
	IBakedModel baseModel;
	HashMap<TransformType, Matrix4> transformationMap = new HashMap<TransformType, Matrix4>();
	ImmutableList<BakedQuad> bakedQuads;
	TextureAtlasSprite tempSprite;
	ItemStack tempStack;
	IBlockState tempState;
	VertexFormat format;

	public IESmartObjModel(IBakedModel baseModel, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4> transformationMap)
	{
		model.super(model, state, format, textures);
		this.baseModel = baseModel;
		this.transformationMap = transformationMap;
		this.format = format;
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
	{
		if(transformationMap==null || transformationMap.isEmpty())
			return super.handlePerspective(cameraTransformType);
		//		Matrix4 matrix = new Matrix4(); //Assign Matrixes here manually in debug mode, then move them to the actual registration method
		Matrix4 matrix = transformationMap.containsKey(cameraTransformType)?transformationMap.get(cameraTransformType).copy():new Matrix4();
		if(this.tempStack!=null && this.tempStack.getItem() instanceof IOBJModelCallback)
			matrix = ((IOBJModelCallback)this.tempStack.getItem()).handlePerspective(this.tempStack, cameraTransformType, matrix);

		//Dynamic stuff to use when figurign out positioning for new items!
		//FP_R
//		if(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND)
//			matrix = new Matrix4().rotate(Math.toRadians(-90), 0,1,0).scale(.1875, .25, .25).translate(-.5, .4375, .5);
//		else if(cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND)//FP_L
//			matrix = new Matrix4().rotate(Math.toRadians(90), 0,1,0).scale(.1875, .25, .25).translate(.45, .4375, .5);
//		else if(cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND) //TP_R
//			matrix = new Matrix4().translate(-.125, .125,-.125).scale(.125, .125, .125).rotate(Math.toRadians(-90), 0,1,0).rotate(Math.toRadians(-10), 0,0,1);
//		else if(cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND) //TP_L
//			matrix = new Matrix4().translate(.0, .0625,-.125).scale(.125, .125, .125).rotate(Math.toRadians(90), 0,1,0).rotate(Math.toRadians(0), 0,0,1);
//		else if(cameraTransformType==TransformType.FIXED) //FIXED
//			matrix = new Matrix4().translate(.1875, -.0781225, -.15625).scale(.2, .2, .2).rotate(Math.toRadians(-40), 0,1,0).rotate(Math.toRadians(-35), 0,0,1);
//		else if(cameraTransformType==TransformType.GUI) //INV
//			matrix = new Matrix4().translate(-.25, 0,-.0625).scale(.1875, .1875, .1875).rotate(Math.PI, 0, 1, 0).rotate(Math.toRadians(-40), 0, 0, 1);
//		else //GROUND
//			matrix = new Matrix4().translate(.125, 0, .0625).scale(.125, .125, .125);
		return Pair.of(this, matrix.toMatrix4f());
	}

	VertexFormat getFormat()
	{
		return this.format;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return overrideList;
	}
	ItemOverrideList overrideList = new ItemOverrideList(new ArrayList())
	{
		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
		{
			ComparableItemStack comp = ApiUtils.createComparableItemStack(stack);
			if(comp == null)
				return originalModel;
			if(cachedBakedItemModels.containsKey(comp))
				return cachedBakedItemModels.get(comp);
			if(!(originalModel instanceof IESmartObjModel))
				return originalModel;
			IESmartObjModel model = (IESmartObjModel)originalModel;

			ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
			builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
			TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation("missingno").toString());

			for (String s : model.getModel().getMatLib().getMaterialNames())
			{
				TextureAtlasSprite sprite = null;
				if (stack.getItem() instanceof IShaderEquipableItem)
				{
					ItemStack shader = ((IShaderEquipableItem) stack.getItem()).getShaderItem(stack);
					if (shader != null && shader.getItem() instanceof IShaderItem)
					{
						ShaderCase sCase = ((IShaderItem) shader.getItem()).getShaderCase(shader, stack, ((IShaderEquipableItem) stack.getItem()).getShaderType());
						if (sCase != null)
							sprite = sCase.getReplacementSprite(shader, stack, s, 0);
					}
				}
				if (sprite == null && stack.getItem() instanceof IOBJModelCallback)
					sprite = ((IOBJModelCallback) stack.getItem()).getTextureReplacement(stack, s);
				if (sprite == null)
					sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(model.getModel().getMatLib().getMaterial(s).getTexture().getTextureLocation().toString());
				if (sprite == null)
					sprite = missing;
				builder.put(s, sprite);
			}
			builder.put("missingno", missing);
			IESmartObjModel bakedModel = new IESmartObjModel(model.baseModel, model.getModel(), model.getState(), model.getFormat(), builder.build(), transformationMap);
			bakedModel.tempStack = stack;
			cachedBakedItemModels.put(comp, bakedModel);
			return bakedModel;
		}
	};

	@Override
	public List<BakedQuad> getQuads(IBlockState blockState, EnumFacing side, long rand)
	{
		this.tempState = blockState;
		if(blockState instanceof IExtendedBlockState)
		{
			IExtendedBlockState exState = (IExtendedBlockState) blockState;
			ExtBlockstateAdapter adapter = new ExtBlockstateAdapter(exState, MinecraftForgeClient.getRenderLayer(), ExtBlockstateAdapter.CONNS_OBJ_CALLBACK);
			if(!modelCache.containsKey(adapter))
			{
				IESmartObjModel model = null;
				if(exState.getUnlistedNames().contains(Properties.AnimationProperty))
				{
					IModelState s = exState.getValue(Properties.AnimationProperty);
					if(s instanceof OBJState)
					{
						OBJState objstate = (OBJState)s;
						if(objstate.parent==null || objstate.parent==TRSRTransformation.identity())
							objstate.parent = this.getState();
						if(objstate.getVisibilityMap().containsKey(Group.ALL) || objstate.getVisibilityMap().containsKey(Group.ALL_EXCEPT))
							this.updateStateVisibilityMap(objstate);
						model = new IESmartObjModel(baseModel, getModel(), objstate, getFormat(), getTextures(), transformationMap);
					}
				}
				if(model==null)
					model = new IESmartObjModel(baseModel, getModel(), this.getState(), getFormat(), getTextures(), transformationMap);
				model.tempState = blockState;
				modelCache.put(adapter, model.buildQuads());
			}
			return Collections.synchronizedList(Lists.newArrayList(modelCache.get(adapter)));
		}
		if(bakedQuads==null)
			bakedQuads = buildQuads();
		List<BakedQuad> quadList = Collections.synchronizedList(Lists.newArrayList(bakedQuads));
		return quadList;
	}


	private ImmutableList<BakedQuad> buildQuads()
	{
		List<BakedQuad> quads = Lists.newArrayList();
		ItemStack shader = null;
		ShaderCase sCase = null;
		IOBJModelCallback callback = null;
		Object callbackObject = null;
		if(this.tempStack!=null && tempStack.getItem() instanceof IShaderEquipableItem)
		{
			shader = ((IShaderEquipableItem)tempStack.getItem()).getShaderItem(tempStack);
			if(shader!=null && shader.getItem() instanceof IShaderItem)
				sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, tempStack, ((IShaderEquipableItem)tempStack.getItem()).getShaderType());
		}
		if(this.tempStack!=null && tempStack.getItem() instanceof IOBJModelCallback)
		{
			callback = (IOBJModelCallback)tempStack.getItem();
			callbackObject = this.tempStack;
		} else if(this.tempState != null && this.tempState instanceof IExtendedBlockState && ((IExtendedBlockState)this.tempState).getUnlistedNames().contains(IOBJModelCallback.PROPERTY))
		{
			callback = ((IExtendedBlockState)this.tempState).getValue(IOBJModelCallback.PROPERTY);
			callbackObject = this.tempState;
		}

		for(Group g : getModel().getMatLib().getGroups().values())
		{
			if (callback != null)
				if (!callback.shouldRenderGroup(callbackObject, g.getName()))
					continue;
			int maxPasses = 1;
			if (sCase != null)
				maxPasses = sCase.getPasses(shader, tempStack, g.getName());
			Set<Face> faces = Collections.synchronizedSet(new LinkedHashSet<Face>());
			Optional<TRSRTransformation> transform = Optional.absent();
			if (this.getState() instanceof OBJState)
			{
				OBJState state = (OBJState) this.getState();
				if (state.parent != null)
					transform = state.parent.apply(Optional.absent());
				if (callback != null)
					transform = callback.applyTransformations(callbackObject, g.getName(), transform);
				if (state.getGroupsWithVisibility(true).contains(g.getName()))
					faces.addAll(g.applyTransform(transform));
			} else
			{
				transform = getState().apply(Optional.absent());
				if(callback != null)
					transform = callback.applyTransformations(callbackObject, g.getName(), transform);
				faces.addAll(g.applyTransform(transform));
			}

			for (int pass = 0; pass < maxPasses; pass++)
			{
				float[] colour = {1, 1, 1, 1};
				if (sCase != null)
				{
					int[] iCol = sCase.getRGBAColourModifier(shader, tempStack, g.getName(), pass);
					for (int i = 0; i < iCol.length; i++)
						colour[i] = iCol[i] / 255f;
				} else if(callback != null)
				{
					int iCol = callback.getRenderColour(callbackObject, g.getName());
					//						int iCol = tempState.getBlock().colorMultiplier(ClientUtils.mc().theWorld, tempState, MinecraftForgeClient.getRenderPass());
					colour[0] = (iCol >> 16 & 255) / 255f;
					colour[1] = (iCol >> 8 & 255) / 255f;
					colour[2] = (iCol & 255) / 255f;
					colour[3] = (iCol >> 24 & 255) / 255f;
				}


				for (Face f : faces)
				{
					tempSprite = null;
					if(this.getModel().getMatLib().getMaterial(f.getMaterialName()).isWhite() && !"null".equals(f.getMaterialName()))
					{
						for(Vertex v : f.getVertices())
							if(!v.getMaterial().equals(this.getModel().getMatLib().getMaterial(v.getMaterial().getName())))
								v.setMaterial(this.getModel().getMatLib().getMaterial(v.getMaterial().getName()));
						tempSprite = ModelLoader.White.INSTANCE;
					} else
					{
						if(sCase != null)
							tempSprite = sCase.getReplacementSprite(shader, tempStack, g.getName(), pass);
						if(tempSprite == null && callback != null)
							tempSprite = callback.getTextureReplacement(callbackObject, f.getMaterialName());
						if(tempSprite == null && this.tempState != null && this.tempState instanceof IExtendedBlockState && ((IExtendedBlockState)this.tempState).getUnlistedNames().contains(IEProperties.OBJ_TEXTURE_REMAP))
						{
							HashMap<String, String> map = ((IExtendedBlockState) this.tempState).getValue(IEProperties.OBJ_TEXTURE_REMAP);
							String s = map != null ? map.get(g.getName()) : null;
							if(s != null)
								tempSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(s);
						}
						if(tempSprite == null && !"null".equals(f.getMaterialName()))
							tempSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(this.getModel().getMatLib().getMaterial(f.getMaterialName()).getTexture().getTextureLocation().toString());
					}
					if (tempSprite != null)
					{
						UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(getFormat());
						builder.setQuadOrientation(EnumFacing.getFacingFromVector(f.getNormal().x, f.getNormal().y, f.getNormal().z));
						builder.setTexture(tempSprite);
						builder.setQuadTint(pass);
						Normal faceNormal = f.getNormal();
						TextureCoordinate[] uvs = new TextureCoordinate[4];

						for(int i=0; i<4; i++)
						{
							Vertex v = f.getVertices()[i];
							uvs[i] = v.hasTextureCoordinate()?v.getTextureCoordinate():TextureCoordinate.getDefaultUVs()[i];
						}

						putVertexData(builder, f.getVertices()[0], faceNormal, TextureCoordinate.getDefaultUVs()[0], tempSprite, colour);
						putVertexData(builder, f.getVertices()[1], faceNormal, TextureCoordinate.getDefaultUVs()[1], tempSprite, colour);
						putVertexData(builder, f.getVertices()[2], faceNormal, TextureCoordinate.getDefaultUVs()[2], tempSprite, colour);
						putVertexData(builder, f.getVertices()[3], faceNormal, TextureCoordinate.getDefaultUVs()[3], tempSprite, colour);
						quads.add(builder.build());
					}
				}
			}
		}
		if(callback != null)
			quads = callback.modifyQuads(callbackObject, quads);
		return ImmutableList.copyOf(quads);
	}


	protected final void putVertexData(UnpackedBakedQuad.Builder builder, Vertex v, Normal faceNormal, TextureCoordinate texCoord, TextureAtlasSprite sprite, float[] colour)
	{
		for(int e = 0; e < getFormat().getElementCount(); e++)
		{
			switch (getFormat().getElement(e).getUsage())
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

					if(v.getMaterial() != null)
						builder.put(e,
								d * v.getMaterial().getColor().x*colour[0],
								d * v.getMaterial().getColor().y*colour[1],
								d * v.getMaterial().getColor().z*colour[2],
								v.getMaterial().getColor().w*colour[3]);
					else
						builder.put(e, d*colour[0], d*colour[1], d*colour[2], 1*colour[3]);
					break;
				case UV:
					if(sprite==null)//Double Safety. I have no idea how it even happens, but it somehow did .-.
						sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
					builder.put(e,
							sprite.getInterpolatedU(texCoord.u * 16),
							sprite.getInterpolatedV((1-texCoord.v) * 16),//Can't access v-flip in customdata. Might change in future Forge versions
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

	static int getExtendedStateHash(IExtendedBlockState state)
	{
		return state.hashCode()*31 + state.getUnlistedProperties().hashCode();
	}

	//	private final LoadingCache<Integer, IESmartObjModel> ieobjcache = CacheBuilder.newBuilder().maximumSize(20).build(new CacheLoader<Integer, IESmartObjModel>()
	//	{
	//		public IESmartObjModel load(IModelState state) throws Exception
	//		{
	//			return new IESmartObjModel(baseModel, getModel(), state, getFormat(), getTextures(), transformationMap);
	//		}
	//	});

	protected void updateStateVisibilityMap(OBJState state)
	{
		if (state.getVisibilityMap().containsKey(Group.ALL))
		{
			boolean operation = state.getVisibilityMap().get(Group.ALL);
			state.getVisibilityMap().clear();
			for (String s : this.getModel().getMatLib().getGroups().keySet())
			{
				state.getVisibilityMap().put(s,  OBJState.Operation.SET_TRUE.performOperation(operation));
			}
		}
		else if (state.getVisibilityMap().containsKey(Group.ALL_EXCEPT))
		{
			List<String> exceptList = state.getGroupNamesFromMap().subList(1, state.getGroupNamesFromMap().size());
			state.getVisibilityMap().remove(Group.ALL_EXCEPT);
			for (String s : this.getModel().getMatLib().getGroups().keySet())
			{
				if (!exceptList.contains(s))
				{
					state.getVisibilityMap().put(s, OBJState.Operation.SET_TRUE.performOperation(state.getVisibilityMap().get(s)));
				}
			}
		}
		else
		{
			for (String s : state.getVisibilityMap().keySet())
			{
				state.getVisibilityMap().put(s, OBJState.Operation.SET_TRUE.performOperation(state.getVisibilityMap().get(s)));
			}
		}
	}

	static Field f_textures;
	public static ImmutableMap<String, TextureAtlasSprite> getTexturesForOBJModel(IBakedModel model)
	{
		try{
			if(f_textures==null)
			{
				f_textures = OBJBakedModel.class.getDeclaredField("textures");
				f_textures.setAccessible(true);
			}
			return (ImmutableMap<String, TextureAtlasSprite>)f_textures.get(model);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public ImmutableMap<String, TextureAtlasSprite> getTextures()
	{
		try{
			if(f_textures==null)
			{
				f_textures = OBJBakedModel.class.getDeclaredField("textures");
				f_textures.setAccessible(true);
			}
			return (ImmutableMap<String, TextureAtlasSprite>)f_textures.get(this);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}