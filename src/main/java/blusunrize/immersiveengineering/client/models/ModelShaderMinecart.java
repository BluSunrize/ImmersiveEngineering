/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderCaseMinecart;
import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class ModelShaderMinecart extends ModelMinecart
{
	public static HashMap<Integer, ItemStack> shadedCarts = new HashMap<Integer, ItemStack>();
	public static boolean rendersReplaced = false;

	public ModelRenderer[] sideModelsMirrored = new ModelRenderer[7];
	//	public HashMap<IIcon,ModelShaderMinecart> remappedModels = new HashMap();

	public ModelShaderMinecart(ModelMinecart model)
	{
		super();
		this.sideModels = ClientUtils.copyModelRenderers(model, model.sideModels);
		this.sideModelsMirrored = ClientUtils.copyModelRenderers(model, model.sideModels);
		sideModelsMirrored[4].mirror = true;
		ArrayList<ModelBox> newCubes = new ArrayList<ModelBox>();
		for(ModelBox cube : sideModelsMirrored[4].cubeList)
			newCubes.add(new ModelBox(sideModelsMirrored[4], 0, 0, cube.posX1, cube.posY1, cube.posZ1, (int)(cube.posX2-cube.posX1), (int)(cube.posY2-cube.posY1), (int)(cube.posZ2-cube.posZ1), 0));
		sideModelsMirrored[4].cubeList = newCubes;
	}

	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3, float f4, float f5)
	{
		ShaderCase sCase = null;
		ItemStack shader = ItemStack.EMPTY;
		if(shadedCarts.containsKey(entity.getEntityId()))
		{
			shader = shadedCarts.get(entity.getEntityId());
			if(shader!=null&&!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
				sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, null, "immersiveengineering:minecart");
		}
		if(sCase!=null)
		{
			GlStateManager.enableBlend();
			OpenGlHelper.glBlendFunc(770, 771, 0, 1);

			sideModels[5].rotationPointY = 4.0F-f2;
			sideModelsMirrored[5].rotationPointY = 4.0F-f2;
			for(int part = 0; part < sideModels.length-1; part++)
				if(sideModels[part]!=null)
				{
					float scale = 1;
					ShaderLayer[] layers = sCase.getLayers();

					//identify part 1+2, they shouldn'T render with additional?!

					for(int pass = 0; pass < layers.length; pass++)
						if(sCase.renderModelPartForPass(shader, null, ""+part, pass))
						{
							int col = sCase.getARGBColourModifier(shader, null, ""+part, pass);
							boolean upScale = pass!=layers.length-2;
							GlStateManager.scale(scale, scale, scale);
							GlStateManager.color((col >> 16&255)/255f, (col >> 8&255)/255f, (col&255)/255f, (col >> 24&255)/255f);

//							if(pass==maxPasses-1)
//								ClientUtils.bindTexture("immersiveengineering:textures/models/shaders/minecart_uncoloured.png");
//							else if(pass==maxPasses-2 && ((ShaderCaseMinecart)sCase).additionalTexture!=null)
//								ClientUtils.bindTexture(sCase.getBaseTexturePath()+((ShaderCaseMinecart)sCase).additionalTexture+".png");
//							else if(pass==0)
//								ClientUtils.bindTexture("immersiveengineering:textures/models/shaders/minecart_0.png");
//							else
//							{
//								ClientUtils.bindTexture(sCase.getBaseTexturePath()+"1_"+sCase.getOverlayType()+".png");
//								upScale = false;
//							}

							ClientUtils.mc().getTextureManager().bindTexture(sCase.getReplacementSprite(shader, null, ""+part, pass));

							if(layers[pass].isDynamicLayer())
								layers[pass].modifyRender(true, part);
							if(((ShaderCaseMinecart)sCase).mirrorSideForPass[pass])
								sideModelsMirrored[part].render(f5);
							else
								sideModels[part].render(f5);
							if(layers[pass].isDynamicLayer())
								layers[pass].modifyRender(false, part);

							GlStateManager.color(1, 1, 1, 1);
							GlStateManager.scale(1/scale, 1/scale, 1/scale);
//							if(upScale)
//								scale += .001f;
						}
				}

			GlStateManager.disableBlend();
		}
		else
			super.render(entity, f0, f1, f2, f3, f4, f5);
	}

//	ModelShaderMinecart getRemappedModel(IIcon icon)
//	{
//		if(remappedModels.containsKey(icon))
//			return remappedModels.get(icon);
//
//		ModelShaderMinecart modelRemapped = new ModelShaderMinecart(this);
//		int ox = MathHelper.floor_float(icon.getMinU() * ClientEventHandler.itemSheetWidth);
//		int oy = MathHelper.floor_float(icon.getMinV() * ClientEventHandler.itemSheetHeight);
//		modelRemapped.textureWidth = ClientEventHandler.itemSheetWidth;
//		modelRemapped.textureHeight = ClientEventHandler.itemSheetHeight;
//		for(ModelRenderer mr : modelRemapped.sideModels)
//			if(mr!=null)
//				mr.setTextureOffset(mr.textureOffsetX+ox, mr.textureOffsetX+oy);
//		for(ModelRenderer mr : modelRemapped.sideModelsMirrored)
//			if(mr!=null)
//				mr.setTextureOffset(mr.textureOffsetX+ox, mr.textureOffsetX+oy);
//		modelRemapped.sideModels = ClientUtils.copyModelRenderers(modelRemapped, modelRemapped.sideModels);
//		modelRemapped.sideModelsMirrored = ClientUtils.copyModelRenderers(modelRemapped, modelRemapped.sideModelsMirrored);
//		remappedModels.put(icon, modelRemapped);
//		return modelRemapped;
//	}
}