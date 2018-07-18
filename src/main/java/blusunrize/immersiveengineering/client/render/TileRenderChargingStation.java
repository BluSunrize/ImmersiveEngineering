/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityChargingStation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileRenderChargingStation extends TileEntitySpecialRenderer<TileEntityChargingStation>
{
	@Override
	public void render(TileEntityChargingStation te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(te.getWorld()!=null&&te.getWorld().isBlockLoaded(te.getPos(), false))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x+.5, y+.3125, z+.5);
			GlStateManager.scale(.75f, .75f, .75f);
			ClientUtils.bindAtlas();
			switch(te.facing)
			{
				case NORTH:
					GlStateManager.rotate(180, 0, 1, 0);
					break;
				case SOUTH:
					break;
				case WEST:
					GlStateManager.rotate(-90, 0, 1, 0);
					break;
				case EAST:
					GlStateManager.rotate(90, 0, 1, 0);
					break;
			}
			if(!te.inventory.get(0).isEmpty())
			{
				GlStateManager.pushMatrix();
				float scale = .625f;
				GlStateManager.scale(scale, scale, 1);
				ClientUtils.mc().getRenderItem().renderItem(te.inventory.get(0), TransformType.FIXED);
				GlStateManager.popMatrix();

//				if(!RenderManager.instance.options.fancyGraphics && MinecraftForgeClient.getItemRenderer(te.inventory, ItemRenderType.ENTITY)==null)
//				{
//					float rot = te.facing==3?180: te.facing==4?-90: te.facing==5?90: 0;
//					GL11.glRotatef(rot - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
//				}
//				EntityItem entityitem = new EntityItem(te.getWorld(), 0.0D, 0.0D, 0.0D, te.inventory);
//				entityitem.hoverStart = 0.0F;
//				RenderItem.renderInFrame = true;
//				RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
//				RenderItem.renderInFrame = false;
			}
			GlStateManager.popMatrix();
		}
	}
}