package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.client.models.IESmartObjModel;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.client.models.ModelCoresample;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemIEBase;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class ImmersiveModelRegistry
{
	public static ImmersiveModelRegistry instance = new ImmersiveModelRegistry();
	private static final ImmutableMap<String, String> flipData = ImmutableMap.of("flip-v", String.valueOf(true));
	private HashMap<ModelResourceLocation, ItemModelReplacement> itemModelReplacements = new HashMap<ModelResourceLocation, ItemModelReplacement>();

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event)
	{
		for(Map.Entry<ModelResourceLocation, ItemModelReplacement> entry : itemModelReplacements.entrySet())
		{
			Object object = event.getModelRegistry().getObject(entry.getKey());
			if(object instanceof IBakedModel)
			{
				try
				{
					IBakedModel existingModel = (IBakedModel) object;
					event.getModelRegistry().putObject(entry.getKey(), entry.getValue().createBakedModel(existingModel));
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		ModelResourceLocation mLoc = new ModelResourceLocation(new ResourceLocation("immersiveengineering", IEContent.itemCoresample.itemName), "inventory");
		event.getModelRegistry().putObject(mLoc, new ModelCoresample());
		ModelConveyor modelConveyor = new ModelConveyor();
		mLoc = new ModelResourceLocation(new ResourceLocation("immersiveengineering", "conveyor"), "normal");
		event.getModelRegistry().putObject(mLoc, modelConveyor);
		mLoc = new ModelResourceLocation(new ResourceLocation("immersiveengineering", "conveyor"), "inventory");
		event.getModelRegistry().putObject(mLoc, modelConveyor);
	}

	public void registerCustomItemModel(ItemStack stack, ItemModelReplacement replacement)
	{
		if(stack.getItem() instanceof ItemIEBase)
		{
			ResourceLocation loc;
			if(((ItemIEBase) stack.getItem()).getSubNames() != null && ((ItemIEBase) stack.getItem()).getSubNames().length > 0)
				loc = new ResourceLocation("immersiveengineering", ((ItemIEBase) stack.getItem()).itemName + "/" + ((ItemIEBase) stack.getItem()).getSubNames()[stack.getItemDamage()]);
			else
				loc = new ResourceLocation("immersiveengineering", ((ItemIEBase) stack.getItem()).itemName);
			itemModelReplacements.put(new ModelResourceLocation(loc, "inventory"), replacement);
		}
	}


	public abstract static class ItemModelReplacement
	{
		public abstract IBakedModel createBakedModel(IBakedModel existingModel);
	}

	public static class ItemModelReplacement_OBJ extends ItemModelReplacement
	{
		String objPath;
		HashMap<TransformType, Matrix4> transformationMap = new HashMap<TransformType, Matrix4>();

		public ItemModelReplacement_OBJ(String path)
		{
			this.objPath = path;
			for(TransformType t : TransformType.values())
				transformationMap.put(t, new Matrix4());
		}

		public ItemModelReplacement_OBJ setTransformations(TransformType type, Matrix4 matrix)
		{
			this.transformationMap.put(type, matrix);
			return this;
		}

		@Override
		public IBakedModel createBakedModel(IBakedModel existingModel)
		{
			try
			{
				Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
				{
					@Override
					public TextureAtlasSprite apply(ResourceLocation location)
					{
						return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
					}
				};
				ResourceLocation modelLocation = new ResourceLocation(objPath);
				OBJModel objModel = (OBJModel) OBJLoader.INSTANCE.loadModel(modelLocation);
				objModel = (OBJModel) objModel.process(flipData);
				ImmutableMap.Builder<String, TextureAtlasSprite> builder = ImmutableMap.builder();
				builder.put(ModelLoader.White.LOCATION.toString(), ModelLoader.White.INSTANCE);
				TextureAtlasSprite missing = textureGetter.apply(new ResourceLocation("missingno"));
				for(String s : objModel.getMatLib().getMaterialNames())
					if(objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation().getResourcePath().startsWith("#"))
					{
						FMLLog.severe("OBJLoader: Unresolved texture '%s' for obj model '%s'", objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation().getResourcePath(), modelLocation);
						builder.put(s, missing);
					} else
						builder.put(s, textureGetter.apply(objModel.getMatLib().getMaterial(s).getTexture().getTextureLocation()));

				return new IESmartObjModel(existingModel, objModel, new OBJModel.OBJState(Lists.newArrayList(OBJModel.Group.ALL), true), DefaultVertexFormats.ITEM, builder.build(), transformationMap);
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}
}