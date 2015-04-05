package blusunrize.immersiveengineering.client;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.ItemNBTHelper;
import blusunrize.immersiveengineering.common.Lib;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientEventHandler
{
	@SubscribeEvent()
	public void textureStich(TextureStitchEvent.Pre event)
	{
		if(event.map.getTextureType()==0 && IEContent.IECreosote)
			IEContent.fluidCreosote.setIcons(event.map.registerIcon("immersiveengineering:creosote_still"), event.map.registerIcon("immersiveengineering:creosote_flow"));
	}

	@SubscribeEvent()
	public void renderOverlay(RenderGameOverlayEvent.Post event)
	{
		if(ClientUtils.mc().thePlayer!=null && ClientUtils.mc().thePlayer.getCurrentEquippedItem()!=null && event.type == RenderGameOverlayEvent.ElementType.TEXT)
		{
			if( OreDictionary.itemMatches(new ItemStack(IEContent.itemTool,1,2), ClientUtils.mc().thePlayer.getCurrentEquippedItem(), false) || OreDictionary.itemMatches(new ItemStack(IEContent.itemWireCoil,1,OreDictionary.WILDCARD_VALUE), ClientUtils.mc().thePlayer.getCurrentEquippedItem(), false) )
			{
				if(ItemNBTHelper.hasKey(ClientUtils.mc().thePlayer.getCurrentEquippedItem(), "linkingPos"))
				{
					int[] link = ItemNBTHelper.getIntArray(ClientUtils.mc().thePlayer.getCurrentEquippedItem(), "linkingPos");
					if(link!=null&&link.length>3)
					{
						String s = StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"attachedTo", link[1],link[2],link[3]);
						ClientUtils.font().drawString(s, event.resolution.getScaledWidth()/2 - ClientUtils.font().getStringWidth(s)/2, event.resolution.getScaledHeight()-GuiIngameForge.left_height-10, WireType.ELECTRUM.getColour(), true);
					}
				}

			}
			else if(ClientUtils.mc().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemRevolver)
			{
				ClientUtils.bindTexture("immersiveengineering:textures/gui/revolver.png");
				float dx = event.resolution.getScaledWidth()-32;
				float dy = event.resolution.getScaledHeight()-32;
				GL11.glPushMatrix();
				GL11.glTranslated(dx, dy, 0);
				Tessellator tes = ClientUtils.tes();
				tes.startDrawingQuads();
				tes.addVertexWithUV( 20, 20, 0,126/256f, 104/256f);
				tes.addVertexWithUV( 20,-20, 0,126/256f, 28/256f);
				tes.addVertexWithUV(-20,-20, 0, 50/256f, 28/256f);
				tes.addVertexWithUV(-20, 20, 0, 50/256f, 104/256f);
				tes.draw();

				ItemStack[] bullets = ((ItemRevolver)ClientUtils.mc().thePlayer.getCurrentEquippedItem().getItem()).getBullets(ClientUtils.mc().thePlayer.getCurrentEquippedItem());
				RenderItem ir = RenderItem.getInstance();
				GL11.glScalef(.5f, .5f, .5f);
				for(int i=0; i<bullets.length; i++)
				{
					if(bullets[i]!=null)
					{
						int x = i==0||i==4?0 : i==1||i==3?21: i==2?27: i==5||i==7?-21: -27;
						int y = i==0? -28: i==1||i==7?-22: i==2||i==6?0 : i==3||i==5?22: 28;
						ir.renderItemIntoGUI(ClientUtils.mc().fontRenderer, ClientUtils.mc().renderEngine, bullets[i], x-8,y-8);
					}
				}
				RenderHelper.disableStandardItemLighting();
				GL11.glPopMatrix();
			}
		}
	}
}
