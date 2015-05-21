package blusunrize.immersiveengineering.client;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.obj.Face;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.TextureCoordinate;
import net.minecraftforge.client.model.obj.WavefrontObject;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.client.render.TileRenderIE;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;

import com.google.common.collect.ArrayListMultimap;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientEventHandler
{
	@SubscribeEvent()
	public void textureStich(TextureStitchEvent.Pre event)
	{
		if(event.map.getTextureType()==0)
		{
			if(IEContent.IECreosote)
				IEContent.fluidCreosote.setIcons(event.map.registerIcon("immersiveengineering:fluid/creosote_still"), event.map.registerIcon("immersiveengineering:fluid/creosote_flow"));
			if(IEContent.IEPlantoil)
				IEContent.fluidPlantoil.setIcons(event.map.registerIcon("immersiveengineering:fluid/plantoil_still"), event.map.registerIcon("immersiveengineering:fluid/plantoil_flow"));
			if(IEContent.IEEthanol)
				IEContent.fluidEthanol.setIcons(event.map.registerIcon("immersiveengineering:fluid/ethanol_still"), event.map.registerIcon("immersiveengineering:fluid/ethanol_flow"));
			if(IEContent.IEBiodiesel)
				IEContent.fluidBiodiesel.setIcons(event.map.registerIcon("immersiveengineering:fluid/biodiesel_still"), event.map.registerIcon("immersiveengineering:fluid/biodiesel_flow"));
		}
	}
	@SubscribeEvent()
	public void textureStich(TextureStitchEvent.Post event)
	{
		for(ModelIEObj modelIE : ModelIEObj.existingStaticRenders)
		{
			WavefrontObject model = modelIE.rebindModel();
			rebindUVsToIcon(model, modelIE.getBlockIcon());
		}
	}

	void rebindUVsToIcon(WavefrontObject model, IIcon icon)
	{
		float minU = icon.getInterpolatedU(0);
		float sizeU = icon.getInterpolatedU(16) - minU;
		float minV = icon.getInterpolatedV(0);
		float sizeV = icon.getInterpolatedV(16) - minV;

		for(GroupObject groupObject : model.groupObjects)
			for(Face face : groupObject.faces)
				for (int i = 0; i < face.vertices.length; ++i)
				{
					TextureCoordinate textureCoordinate = face.textureCoordinates[i];
					face.textureCoordinates[i] = new TextureCoordinate(
							minU + sizeU * textureCoordinate.u,
							minV + sizeV * textureCoordinate.v
							);
				}
	}

	@SubscribeEvent()
	public void onChatMessage(ClientChatReceivedEvent event)
	{
		//		I should probably try to catch that thing here sometime...meh
		//		String loc = StatCollector.translateToLocal("death.attack.ieCrushed").substring(5);
		//		if(event.message.getUnformattedTextForChat().contains(loc))
	}

	@SubscribeEvent()
	public void postWorldRender(RenderWorldLastEvent event)
	{
		GL11.glPushMatrix();
		Tessellator.instance.startDrawing(GL11.GL_QUADS);
		for(Object o : event.context.tileEntities)
			if(o instanceof IImmersiveConnectable)
			{
				TileEntity tile = (TileEntity)o;
				int lb = tile.getWorldObj().getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord, tile.zCoord, 0);
				int lb_j = lb % 65536;
				int lb_k = lb / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lb_j / 1.0F, (float)lb_k / 1.0F);

				
				EntityLivingBase viewer = ClientUtils.mc().renderViewEntity;
				double dx = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * (double)event.partialTicks;
				double dy = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * (double)event.partialTicks;
				double dz = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * (double)event.partialTicks;

				Tessellator.instance.setTranslation(tile.xCoord-dx, tile.yCoord-dy, tile.zCoord-dz);
//				GL11.glTranslated((tile.xCoord+.5-dx), (tile.yCoord+.5-dy), (tile.zCoord+.5-dz));
				ClientUtils.renderAttachedConnections((TileEntity)tile);
//				GL11.glTranslated(-(tile.xCoord+.5-dx), -(tile.yCoord+.5-dy), -(tile.zCoord+.5-dz));
				Tessellator.instance.setTranslation(0,0,0);
			}
		Tessellator.instance.draw();
		GL11.glPopMatrix();
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
			else if( Config.getBoolean("colourblindSupport") && Utils.isHammer(ClientUtils.mc().thePlayer.getCurrentEquippedItem()))
			{
				MovingObjectPosition mop = ClientUtils.mc().objectMouseOver;
				if(mop!=null && ClientUtils.mc().thePlayer.worldObj.getTileEntity(mop.blockX, mop.blockY, mop.blockZ) instanceof IEBlockInterfaces.IBlockOverlayText)
				{
					IEBlockInterfaces.IBlockOverlayText overlayBlock = (IBlockOverlayText) ClientUtils.mc().thePlayer.worldObj.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
					String[] s = overlayBlock.getOverlayText(mop);
					if(s!=null && s.length>0)
						for(int is=0; is<s.length; is++)
							ClientUtils.font().drawString(s[is], event.resolution.getScaledWidth()/2+8, event.resolution.getScaledHeight()/2+8+is*ClientUtils.font().FONT_HEIGHT, 0xcccccc, true);
				}

			}
			else if(ClientUtils.mc().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemRevolver && ClientUtils.mc().thePlayer.getCurrentEquippedItem().getItemDamage()!=2)
			{
				ClientUtils.bindTexture("immersiveengineering:textures/gui/revolver.png");
				ItemStack[] bullets = ((ItemRevolver)ClientUtils.mc().thePlayer.getCurrentEquippedItem().getItem()).getBullets(ClientUtils.mc().thePlayer.getCurrentEquippedItem());
				float dx = event.resolution.getScaledWidth()-32-(bullets.length>8?32:0);
				float dy = event.resolution.getScaledHeight()-32;
				GL11.glPushMatrix();
				GL11.glTranslated(dx, dy, 0);
				ClientUtils.drawTexturedRect(-20,-20,40,40, 50/256f,126/256f, 28/256f,104/256f);
				boolean b = ((ItemRevolver)IEContent.itemRevolver).getBulletSlotAmount(ClientUtils.mc().thePlayer.getCurrentEquippedItem())>8;
				if(b)
				{
					GL11.glTranslated(40*(56/76f), 0, 0);
					ClientUtils.drawTexturedRect(-20,-20,40,40, 176/256f,256/256f, 28/256f,104/256f);
					GL11.glTranslated(-40*(56/76f), 0, 0);
				}
				//				Tessellator tes = ClientUtils.tes();
				//				tes.startDrawingQuads();
				//				tes.addVertexWithUV( 20, 20, 0,126/256f, 104/256f);
				//				tes.addVertexWithUV( 20,-20, 0,126/256f, 28/256f);
				//				tes.addVertexWithUV(-20,-20, 0, 50/256f, 28/256f);
				//				tes.addVertexWithUV(-20, 20, 0, 50/256f, 104/256f);
				//				tes.draw();

				RenderItem ir = RenderItem.getInstance();
				GL11.glScalef(.5f, .5f, .5f);
				for(int i=0; i<bullets.length; i++)
				{
					if(bullets[i]!=null)
					{
						int x = 0; 
						int y = 0;
						if(!b)
						{
							x = i==0||i==4?0 : i==1||i==3?22: i==2?28: i==5||i==7?-23: -29;
							y = i==0? -29: i==1||i==7?-23: i==2||i==6?0 : i==3||i==5?22: 28;
						}
						else
						{
							x = i==0||i==10?0 : i==1||i==9?22: i==2||i==8?41: i==3||i==7?62: i==4||i==6?83: i==5?89: i==11||i==13?-23: -29;
							y = i==0||i==3?-29: i==1||i==2||i==4||i==13?-23: i==5||i==12?0 : i==6||i==8||i==9||i==11?22: 28;
						}
						ir.renderItemIntoGUI(ClientUtils.mc().fontRenderer, ClientUtils.mc().renderEngine, bullets[i], x-8,y-8);
						//						ir.renderItemIntoGUI(ClientUtils.mc().fontRenderer, ClientUtils.mc().renderEngine, new ItemStack(Blocks.stained_glass_pane,1,3), x-8,y-8);
					}
				}
				RenderHelper.disableStandardItemLighting();
				GL11.glPopMatrix();
			}
		}
	}

	public static ArrayListMultimap<ChunkCoordinates, AxisAlignedBB> additionalBlockBounds = ArrayListMultimap.create();
	public static void addAdditionalBlockBounds(ChunkCoordinates cc, AxisAlignedBB aabb)
	{
		for(AxisAlignedBB aabb1 : additionalBlockBounds.get(cc))
			if(aabb1.toString().equals(aabb.toString()))
				return;
		additionalBlockBounds.put(cc, aabb);
	}
	@SubscribeEvent()
	public void renderAdditionalBlockBounds(DrawBlockHighlightEvent event)
	{
		if(event.subID==0 && event.target.typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK && additionalBlockBounds.containsKey(new ChunkCoordinates(event.target.blockX,event.target.blockY,event.target.blockZ)))
		{
			ChunkCoordinates cc = new ChunkCoordinates(event.target.blockX,event.target.blockY,event.target.blockZ);
			if(!(event.player.worldObj.getTileEntity(event.target.blockX,event.target.blockY,event.target.blockZ) instanceof IEBlockInterfaces.ICustomBoundingboxes))
			{
				additionalBlockBounds.removeAll(cc);
				return;
			}

			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
			GL11.glLineWidth(2.0F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(false);
			float f1 = 0.002F;
			double d0 = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * (double)event.partialTicks;
			double d1 = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * (double)event.partialTicks;
			double d2 = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * (double)event.partialTicks;
			for(AxisAlignedBB aabb : additionalBlockBounds.get(cc))
				RenderGlobal.drawOutlinedBoundingBox(aabb.getOffsetBoundingBox(cc.posX,cc.posY,cc.posZ).expand((double)f1, (double)f1, (double)f1).getOffsetBoundingBox(-d0, -d1, -d2), -1);

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			event.setCanceled(true);
		}
	}
}
