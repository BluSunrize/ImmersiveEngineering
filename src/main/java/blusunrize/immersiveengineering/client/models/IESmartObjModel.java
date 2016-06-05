package blusunrize.immersiveengineering.client.models;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.shader.IShaderEquipableItem;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.models.smart.ConnModelReal.ExtBlockstateAdapter;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModelPart;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.Face;
import net.minecraftforge.client.model.obj.OBJModel.Group;
import net.minecraftforge.client.model.obj.OBJModel.Normal;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.client.model.obj.OBJModel.TextureCoordinate;
import net.minecraftforge.client.model.obj.OBJModel.Vertex;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;

@SuppressWarnings("deprecation")
public class IESmartObjModel extends OBJBakedModel
{
	Map<ComparableItemStack, IBakedModel> cachedBakedItemModels = new ConcurrentHashMap<ComparableItemStack, IBakedModel>();
	Map<TileEntity, IBakedModel> cachedBakedTileModels = new ConcurrentHashMap<TileEntity, IBakedModel>();
	IBakedModel baseModel;
	HashMap<TransformType, Matrix4> transformationMap = new HashMap<TransformType, Matrix4>();
	Set<BakedQuad> bakedQuads;
	TextureAtlasSprite tempSprite;
	ItemStack tempStack;
	IBlockState tempState;

	public IESmartObjModel(IBakedModel baseModel, OBJModel model, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures, HashMap<TransformType, Matrix4> transformationMap)
	{
		model.super(model, state, format, textures);
		this.baseModel = baseModel;
		this.transformationMap = transformationMap;
	}

	@Override
	public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
	{
		if(transformationMap==null)
			return super.handlePerspective(cameraTransformType);
		//		Matrix4 matrix = new Matrix4(); //Assign Matrixes here manually in debug mode, then move them to the actual registration method
		Matrix4 matrix = transformationMap.containsKey(cameraTransformType)?transformationMap.get(cameraTransformType).copy():new Matrix4();
		if(this.tempStack!=null && this.tempStack.getItem() instanceof IOBJModelCallback)
			matrix = ((IOBJModelCallback)this.tempStack.getItem()).handlePerspective(this.tempStack, cameraTransformType, matrix);
		//		matrix.translate(.125, -.0625, -.45);
		//		matrix.scale(.625, .625, .625);
		//		matrix.rotate(Math.toRadians(190), 1,0,0);
		//		matrix.translate(0, .5, .125).scale(.875, .875, .875).rotate(Math.toRadians(40), 0,1,0);
		return Pair.of(this, matrix.toMatrix4f());
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack)
	{
		ComparableItemStack comp = ApiUtils.createComparableItemStack(stack);
		if(comp==null)
			return this;
		if(this.cachedBakedItemModels.containsKey(comp))
			return this.cachedBakedItemModels.get(comp);

		ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
		builder.put(ModelLoader.White.loc.toString(), ModelLoader.White.instance);
		TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation("missingno").toString());

		for(String s : this.getModel().getMatLib().getMaterialNames())
		{
			TextureAtlasSprite sprite = null;
			if(stack.getItem() instanceof IShaderEquipableItem)
			{
				ItemStack shader = ((IShaderEquipableItem)stack.getItem()).getShaderItem(stack);
				if(shader!=null && shader.getItem() instanceof IShaderItem)
				{
					ShaderCase sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, stack, ((IShaderEquipableItem)stack.getItem()).getShaderType());
					if(sCase!=null)
						sprite = sCase.getReplacementSprite(shader, stack, s, 0);
				}
			}
			if(sprite==null && stack.getItem() instanceof IOBJModelCallback)
				sprite = ((IOBJModelCallback)stack.getItem()).getTextureReplacement(stack, s);
			if(sprite==null)
				sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(this.getModel().getMatLib().getMaterial(s).getTexture().getTextureLocation().toString());
			if(sprite==null)
				sprite = missing;
			builder.put(s, sprite);
		}
		builder.put("missingno", missing);
		IESmartObjModel bakedModel = new IESmartObjModel(this.baseModel, this.getModel(), this.getState(), this.getFormat(), builder.build(), transformationMap);
		bakedModel.tempStack = stack;
		this.cachedBakedItemModels.put(comp, bakedModel);
		return bakedModel;
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		//		if(bakedQuads==null)
		{
			bakedQuads = Collections.synchronizedSet(new LinkedHashSet<BakedQuad>());
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
			//System.out.println("Has callback: "+ (this.tempState instanceof IExtendedBlockState && ((IExtendedBlockState)this.tempState).getUnlistedNames().contains(IEProperties.OBJ_MODEL_CALLBACK)) );			
			if(this.tempStack!=null && tempStack.getItem() instanceof IOBJModelCallback)
			{
				callback = (IOBJModelCallback)tempStack.getItem();
				callbackObject = this.tempStack;
			}
			else if(this.tempState!=null && this.tempState instanceof IExtendedBlockState && ((IExtendedBlockState)this.tempState).getUnlistedNames().contains(IEProperties.OBJ_MODEL_CALLBACK))
			{
				callback = ((IExtendedBlockState)this.tempState).getValue(IEProperties.OBJ_MODEL_CALLBACK); 
				callbackObject = this.tempState;
			}

			for(Group g : getModel().getMatLib().getGroups().values())
			{
				if(callback!=null)
					if(!callback.shouldRenderGroup(callbackObject, g.getName()))
						continue;
				int maxPasses = 1;
				if(sCase!=null)
					maxPasses = sCase.getPasses(shader, tempStack, g.getName());
				Set<Face> faces = Collections.synchronizedSet(new LinkedHashSet<Face>());
				Optional<TRSRTransformation> transform = Optional.absent();
				if(this.getState() instanceof OBJState)
				{
					OBJState state = (OBJState)this.getState();
					if(state.parent != null)
						transform = state.parent.apply(Optional.<IModelPart>absent());
					if(callback!=null)
						transform = callback.applyTransformations(callbackObject, g.getName(), transform);
					if(state.getGroupsWithVisibility(true).contains(g.getName()))
						faces.addAll(g.applyTransform(transform));
				}
				else
				{
					transform = getState().apply(Optional.<IModelPart>absent());
					if(callback!=null)
						transform = callback.applyTransformations(callbackObject, g.getName(), transform);
					faces.addAll(g.applyTransform(transform));
				}

				for(int pass=0; pass<maxPasses; pass++)
				{
					float[] colour = {1,1,1,1};
					if(sCase!=null)
					{
						int[] iCol = sCase.getRGBAColourModifier(shader, tempStack, g.getName(), pass);
						for(int i=0; i<iCol.length; i++)
							colour[i] = iCol[i]/255f;
					}
					else if(tempState!=null)
					{
						//						int iCol = tempState.getBlock().colorMultiplier(ClientUtils.mc().theWorld, tempState, MinecraftForgeClient.getRenderPass());
						//						colour[0] = (iCol>>16&255)/255f;
						//						colour[1] = (iCol>>8&255)/255f;
						//						colour[2] = (iCol&255)/255f;
						//						colour[3] = (iCol>>24&255)/255f;
					}


					for(Face f : faces)
					{
						tempSprite = null;
						if(this.getModel().getMatLib().getMaterial(f.getMaterialName()).isWhite())
						{
							for(Vertex v : f.getVertices())
								if(!v.getMaterial().equals(this.getModel().getMatLib().getMaterial(v.getMaterial().getName())))
									v.setMaterial(this.getModel().getMatLib().getMaterial(v.getMaterial().getName()));
							tempSprite = ModelLoader.White.instance;
						}
						else
						{
							if(sCase!=null)
								tempSprite = sCase.getReplacementSprite(shader, tempStack, g.getName(), pass);
							if(tempSprite==null && callback!=null)
								tempSprite = callback.getTextureReplacement(callbackObject, f.getMaterialName());
							if(tempSprite==null && this.tempState!=null && this.tempState instanceof IExtendedBlockState && ((IExtendedBlockState)this.tempState).getUnlistedNames().contains(IEProperties.OBJ_TEXTURE_REMAP))
							{
								HashMap<String,String> map = ((IExtendedBlockState)this.tempState).getValue(IEProperties.OBJ_TEXTURE_REMAP); 
								String s = map!=null?map.get(g.getName()):null;
								if(s!=null)			
									tempSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(s);
							}
							if(tempSprite==null)
								tempSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(this.getModel().getMatLib().getMaterial(f.getMaterialName()).getTexture().getTextureLocation().toString());
						}
						if(tempSprite!=null)
						{
							UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(getFormat());
							builder.setQuadOrientation(EnumFacing.getFacingFromVector(f.getNormal().x, f.getNormal().y, f.getNormal().z));
							builder.setQuadColored();
							builder.setQuadTint(pass);
							Normal faceNormal = f.getNormal();
							putVertexData(builder, f.getVertices()[0], faceNormal, TextureCoordinate.getDefaultUVs()[0], tempSprite, colour);
							putVertexData(builder, f.getVertices()[1], faceNormal, TextureCoordinate.getDefaultUVs()[1], tempSprite, colour);
							putVertexData(builder, f.getVertices()[2], faceNormal, TextureCoordinate.getDefaultUVs()[2], tempSprite, colour);
							putVertexData(builder, f.getVertices()[3], faceNormal, TextureCoordinate.getDefaultUVs()[3], tempSprite, colour);
							bakedQuads.add(builder.build());
						}
					}
				}
			}
		}
		List<BakedQuad> quadList = Collections.synchronizedList(Lists.newArrayList(bakedQuads));
		return quadList;
	}

	protected final void putVertexData(UnpackedBakedQuad.Builder builder, Vertex v, Normal faceNormal, TextureCoordinate defUV, TextureAtlasSprite sprite, float[] colour)
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
				if(sprite!=null)//Double Safety. I have no idea how it even happens, but it somehow did .-.
					if(!v.hasTextureCoordinate())
						builder.put(e,
								sprite.getInterpolatedU(defUV.u * 16),
								sprite.getInterpolatedV((1-defUV.v) * 16),//Can't access v-flip in customdata. Might change in future Forge versions
								0, 1);
					else
					{
						builder.put(e,
								sprite.getInterpolatedU(v.getTextureCoordinate().u * 16),
								sprite.getInterpolatedV((1-v.getTextureCoordinate().v) * 16),
								0, 1);
					}
				else
					builder.put(e);
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

	@Override
	public OBJBakedModel handleBlockState(IBlockState state)
	{
		this.tempState = state;
		if(state instanceof IExtendedBlockState)
		{
			modelCache.clear();
			IExtendedBlockState exState = (IExtendedBlockState) state;
			ExtBlockstateAdapter adapter = new ExtBlockstateAdapter(exState);
			if(!modelCache.containsKey(adapter))
			{
				IESmartObjModel model = null;
				if(exState.getUnlistedNames().contains(OBJProperty.instance))
				{
					OBJState s = exState.getValue(OBJProperty.instance);
					if(s!=null)
					{
						if(s.parent==null || s.parent==TRSRTransformation.identity())
							s.parent = this.getState();
						if(s.getVisibilityMap().containsKey(Group.ALL) || s.getVisibilityMap().containsKey(Group.ALL_EXCEPT))
							this.updateStateVisibilityMap(s);
						model = new IESmartObjModel(baseModel, getModel(), s, getFormat(), getTextures(), transformationMap);
					}
				}
				if(model==null)
					model = new IESmartObjModel(baseModel, getModel(), this.getState(), getFormat(), getTextures(), transformationMap);
				model.tempState = state;
				modelCache.put(adapter, model);
			}
			return modelCache.get(adapter);
		}
		return this;
	}
	static int getExtendedStateHash(IExtendedBlockState state)
	{
		return state.hashCode()*31 + state.getUnlistedProperties().hashCode();
	}

	static HashMap<ExtBlockstateAdapter, IESmartObjModel> modelCache = new HashMap<>();
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