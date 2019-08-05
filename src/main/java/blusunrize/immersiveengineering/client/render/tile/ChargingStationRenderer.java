/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ChargingStationTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class ChargingStationRenderer extends TileEntityRenderer<ChargingStationTileEntity>
{
	@Override
	public void render(ChargingStationTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(te.getWorld()!=null&&te.getWorld().isBlockLoaded(te.getPos()))
		{
			GlStateManager.pushMatrix();
			GlStateManager.translated(x+.5, y+.3125, z+.5);
			GlStateManager.scalef(.75f, .75f, .75f);
			ClientUtils.bindAtlas();
			switch(te.facing)
			{
				case NORTH:
					GlStateManager.rotatef(180, 0, 1, 0);
					break;
				case SOUTH:
					break;
				case WEST:
					GlStateManager.rotatef(-90, 0, 1, 0);
					break;
				case EAST:
					GlStateManager.rotatef(90, 0, 1, 0);
					break;
			}
			if(!te.inventory.get(0).isEmpty())
			{
				GlStateManager.pushMatrix();
				float scale = .625f;
				GlStateManager.scalef(scale, scale, 1);
				ClientUtils.mc().getItemRenderer().renderItem(te.inventory.get(0), TransformType.FIXED);
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