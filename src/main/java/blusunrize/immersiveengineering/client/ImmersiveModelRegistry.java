package blusunrize.immersiveengineering.client;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.client.models.IESmartObjModel;
import blusunrize.immersiveengineering.client.models.ModelCoresample;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemIEBase;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("deprecation")
public class ImmersiveModelRegistry 
{
	public static ImmersiveModelRegistry instance = new ImmersiveModelRegistry();
	private static final ImmutableMap<String, String> flipData = ImmutableMap.of("flip-v", String.valueOf(true));
	private HashMap<ModelResourceLocation, ItemModelReplacement> itemModelReplacements = new HashMap<ModelResourceLocation, ItemModelReplacement>();

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event)
	{
		//		if(true)
		//			return;
		//		ResourceLocation loc = new ResourceLocation("immersiveengineering","tool/voltmeter");
		//		ModelResourceLocation mrl = new ModelResourceLocation(loc, "inventory");
		for(Map.Entry<ModelResourceLocation, ItemModelReplacement> entry : itemModelReplacements.entrySet())
		{
			Object object = event.modelRegistry.getObject(entry.getKey());
			if(object instanceof IBakedModel)
			{
				try {
					IBakedModel existingModel = (IBakedModel)object;
					event.modelRegistry.putObject(entry.getKey(), createBakedObjItemModel(existingModel, entry.getValue(), new OBJModel.OBJState(Lists.newArrayList(OBJModel.Group.ALL), true), DefaultVertexFormats.ITEM));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		ModelResourceLocation mLoc = new ModelResourceLocation(new ResourceLocation("immersiveengineering",IEContent.itemCoresample.itemName), "inventory");
		event.modelRegistry.putObject(mLoc, new ModelCoresample());
	}

	public void registerCustomItemModel(ItemStack stack, ItemModelReplacement replacement)
	{
		if(stack.getItem() instanceof ItemIEBase)
		{
			ResourceLocation loc; 
			if(((ItemIEBase)stack.getItem()).getSubNames()!=null && ((ItemIEBase)stack.getItem()).getSubNames().length>0)
				loc = new ResourceLocation("immersiveengineering",((ItemIEBase)stack.getItem()).itemName+"/"+((ItemIEBase)stack.getItem()).getSubNames()[stack.getItemDamage()]);
			else
				loc = new ResourceLocation("immersiveengineering",((ItemIEBase)stack.getItem()).itemName);
			itemModelReplacements.put(new ModelResourceLocation(loc, "inventory"), replacement);
		}
	}


	public static class ItemModelReplacement
	{
		String objPath;
		HashMap<TransformType, Matrix4> transformationMap = new HashMap<TransformType, Matrix4>();
		public ItemModelReplacement(String path)
		{
			this.objPath = path;
			for(TransformType t : TransformType.values())
				transformationMap.put(t, new Matrix4());
		}

		public ItemModelReplacement setTransformations(TransformType type, Matrix4 matrix)
		{
			this.transformationMap.put(type, matrix);
			return this;
		}


		public OBJBakedModel createBakedModel(IBakedModel existingModel, OBJModel objModel, IModelState state, VertexFormat format, ImmutableMap<String, TextureAtlasSprite> textures)
		{
			return new IESmartObjModel(existingModel, objModel, state, format, textures, transformationMap);
		}
	}

	public OBJBakedModel createBakedObjItemModel(IBakedModel existingModel, ItemModelReplacement replacement, IModelState state, VertexFormat format)
	{
		try {
			Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
			{
				public TextureAtlasSprite apply(ResourceLocation location)
				{
					return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
				}
			};
			ResourceLocation modelLocation = new ResourceLocation(replacement.objPath);
			OBJModel objModel = (OBJModel)OBJLoader.instance.loadModel(modelLocation);
			objModel = (OBJModel)objModel.process(flipData);
			ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
			builder.put(ModelLoader.White.loc.toString(), ModelLoader.White.instance);
			TextureAtlasSprite missing = textureGetter.apply(new ResourceLocation("missingno"));
			for(String s : objModel.getMatLib().getMaterialNames())
				if(objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation().getResourcePath().startsWith("#"))
				{
					FMLLog.severe("OBJLoader: Unresolved texture '%s' for obj model '%s'", objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation().getResourcePath(), modelLocation);
					builder.put(s, missing);
				}
				else
					builder.put(s, textureGetter.apply(objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation()));

			builder.put("missingno", missing);
			return replacement.createBakedModel(existingModel, objModel, state, format, builder.build());
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
}