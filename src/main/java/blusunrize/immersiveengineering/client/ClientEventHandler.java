package blusunrize.immersiveengineering.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.obj.Face;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.TextureCoordinate;
import net.minecraftforge.client.model.obj.WavefrontObject;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.client.gui.GuiBlastFurnace;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.gui.ContainerRevolver;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.items.ItemSkyhook;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

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
			WireType.iconDefaultWire = event.map.registerIcon("immersiveengineering:wire");
		}
		if(event.map.getTextureType()==Config.getInt("revolverSheetID"))
		{
			IELogger.info("Stitching Revolver Textures!");
			((ItemRevolver)IEContent.itemRevolver).stichRevolverTextures(event.map);
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

	public static Set<Connection> skyhookGrabableConnections = new HashSet();
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side.isClient() && event.phase==TickEvent.Phase.START && event.player!=null && event.player==ClientUtils.mc().renderViewEntity)
		{
			skyhookGrabableConnections.clear();
			EntityPlayer player = event.player;
			ItemStack stack = player.getCurrentEquippedItem();
			if(stack!=null && stack.getItem() instanceof ItemSkyhook)
			{
				TileEntity connector = null;
				double lastDist = 0;
				Connection line = null;
				double py = player.posY+player.getEyeHeight();
				for(int xx=-2; xx<=2; xx++)
					for(int zz=-2; zz<=2; zz++)
						for(int yy=0; yy<=3; yy++)
						{
							TileEntity tile = player.worldObj.getTileEntity((int)player.posX+xx, (int)py+yy, (int)player.posZ+zz);
							if(tile!=null)
							{
								Connection con = SkylineHelper.getTargetConnection(player.worldObj, tile.xCoord,tile.yCoord,tile.zCoord, player, null);
								if(con!=null)
								{
									double d = tile.getDistanceFrom(player.posX,py,player.posZ);
									if(connector==null || d<lastDist)
									{
										connector=tile;
										lastDist=d;
										line=con;
									}
								}
							}
						}
				if(line!=null&&connector!=null)
					skyhookGrabableConnections.add(line);
			}
		}
		if(event.side.isClient() && event.phase == TickEvent.Phase.END && event.player!=null)
		{
			EntityPlayer player = event.player;
			ItemStack stack = player.getCurrentEquippedItem();
			if(stack!=null && stack.getItem() instanceof ItemDrill && (player!=ClientUtils.mc().renderViewEntity||ClientUtils.mc().gameSettings.thirdPersonView!=0))
			{
				if (player.getItemInUseCount() <= 0)
				{
					player.clearItemInUse();
					player.setItemInUse(stack, 2147483647);
				}
			}

		}
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT
				&& ClientUtils.mc().currentScreen != null
				&& ClientUtils.mc().currentScreen instanceof GuiBlastFurnace
				&& BlastFurnaceRecipe.isValidBlastFuel(event.itemStack))
			event.toolTip.add(EnumChatFormatting.GRAY+StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.blastFuelTime", BlastFurnaceRecipe.getBlastFuelTime(event.itemStack)));
	}

	@SubscribeEvent()
	public void lastWorldRender(RenderWorldLastEvent event)
	{
		connectionsRendered = false;
	}
	static boolean connectionsRendered = false;
	public static void renderAllIEConnections(float partial)
	{
		if(connectionsRendered)
			return;
		GL11.glPushMatrix();

		GL11.glDisable(GL11.GL_CULL_FACE);
		//		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		RenderHelper.enableStandardItemLighting();

		Tessellator.instance.startDrawing(GL11.GL_QUADS);

		EntityLivingBase viewer = ClientUtils.mc().renderViewEntity;
		double dx = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partial;//(double)event.partialTicks;
		double dy = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partial;//(double)event.partialTicks;
		double dz = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partial;//(double)event.partialTicks;

		for(Object o : ClientUtils.mc().renderGlobal.tileEntities)
			if(o instanceof IImmersiveConnectable)
			{
				TileEntity tile = (TileEntity)o;
				//				int lb = tile.getWorldObj().getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord, tile.zCoord, 0);
				//				int lb_j = lb % 65536;
				//				int lb_k = lb / 65536;
				//				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lb_j / 1.0F, (float)lb_k / 1.0F);


				Tessellator.instance.setTranslation(tile.xCoord-dx, tile.yCoord-dy, tile.zCoord-dz);
				//				GL11.glTranslated((tile.xCoord+.5-dx), (tile.yCoord+.5-dy), (tile.zCoord+.5-dz));
				ClientUtils.renderAttachedConnections((TileEntity)tile);
				//				GL11.glTranslated(-(tile.xCoord+.5-dx), -(tile.yCoord+.5-dy), -(tile.zCoord+.5-dz));

			}

		Iterator<ImmersiveNetHandler.Connection> it = skyhookGrabableConnections.iterator();
		World world = viewer.worldObj;
		while(it.hasNext())
		{
			ImmersiveNetHandler.Connection con = it.next();
			Tessellator.instance.setTranslation(con.start.posX-dx, con.start.posY-dy, con.start.posZ-dz);
			double r = con.cableType.getRenderDiameter()/2;
			ClientUtils.drawConnection(con, Utils.toIIC(con.start, world), Utils.toIIC(con.end, world),   0x00ff99,128,r*1.75, con.cableType.getIcon(con));
		}

		Tessellator.instance.setTranslation(0,0,0);
		Tessellator.instance.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPopMatrix();
		connectionsRendered = true;
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
						ClientUtils.font().drawString(s, event.resolution.getScaledWidth()/2 - ClientUtils.font().getStringWidth(s)/2, event.resolution.getScaledHeight()-GuiIngameForge.left_height-10, WireType.ELECTRUM.getColour(null), true);
					}
				}

			}
			else if( Utils.isHammer(ClientUtils.mc().thePlayer.getCurrentEquippedItem()))
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
				int bulletAmount = bullets.length;
				float dx = event.resolution.getScaledWidth()-32-48;
				float dy = event.resolution.getScaledHeight()-64;
				GL11.glPushMatrix();
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glTranslated(dx, dy, 0);
				GL11.glScalef(.5f, .5f, 1);

				ClientUtils.drawTexturedRect(0,1,74,74, 0/256f,74/256f, 51/256f,125/256f);
				if(bulletAmount>=18)
					ClientUtils.drawTexturedRect(47,1,103,74, 74/256f,177/256f, 51/256f,125/256f);
				else if(bulletAmount>8)
					ClientUtils.drawTexturedRect(57,1,79,39, 57/256f,136/256f, 12/256f,51/256f);

				RenderItem ir = RenderItem.getInstance();
				int[][] slots = ContainerRevolver.slotPositions[bulletAmount>=18?2: bulletAmount>8?1: 0];
				for(int i=0; i<bulletAmount; i++)
				{
					if(bullets[i]!=null)
					{
						int x = 0; 
						int y = 0;
						if(i==0)
						{
							x = 29;
							y = 3;
						}
						else if(i-1<slots.length)
						{
							x = slots[i-1][0];
							y = slots[i-1][1];
						}
						else
						{
							int ii = i-(slots.length+1);
							x = ii==0?48: ii==1?29: ii==3?2: 10;
							y = ii==1?57: ii==3?30: ii==4?11: 49;
						}

						ir.renderItemIntoGUI(ClientUtils.mc().fontRenderer, ClientUtils.mc().renderEngine, bullets[i], x,y);
					}
				}
				RenderHelper.disableStandardItemLighting();
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glPopMatrix();
			}
			else if(ClientUtils.mc().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemDrill && ClientUtils.mc().thePlayer.getCurrentEquippedItem().getItemDamage()==0)
			{
				ItemStack drill = ClientUtils.mc().thePlayer.getCurrentEquippedItem(); 
				ClientUtils.bindTexture("immersiveengineering:textures/gui/fuelGauge.png");
				GL11.glColor4f(1, 1, 1, 1);
				float dx = event.resolution.getScaledWidth()-16;
				float dy = event.resolution.getScaledHeight()-12;
				GL11.glPushMatrix();
				GL11.glTranslated(dx, dy, 0);
				ClientUtils.drawTexturedRect(-24,-68, 33,81, 179/256f,210/256f, 0/96f,80/96f);

				GL11.glTranslated(-23,-28,0);
				FluidStack fuel = ((ItemDrill)drill.getItem()).getFluid(drill);
				//				for(float angle=80; angle>=-80; angle-=20)
				//				{
				float angle = 80-(160* (fuel!=null? fuel.amount/(float)((ItemDrill)drill.getItem()).getCapacity(drill): 0));
				GL11.glRotatef(angle, 0, 0, 1);
				ClientUtils.drawTexturedRect(6,-2, 24,4, 91/256f,123/256f, 80/96f,87/96f);
				GL11.glRotatef(-angle, 0, 0, 1);
				//				}
				GL11.glTranslated(23,28,0);

				ClientUtils.drawTexturedRect(-33-17,-40-26, 33+30,40+37, 108/256f,174/256f, 0/96f,80/96f);

				RenderItem ir = RenderItem.getInstance();
				float scale = 1;//.75f;
				GL11.glScalef(scale,scale,scale);
				ItemStack head = ((ItemDrill)drill.getItem()).getHead(drill);
				if(head!=null)
				{
					ir.renderItemIntoGUI(ClientUtils.mc().fontRenderer, ClientUtils.mc().renderEngine, head, (int)(-48/scale),(int)(-36/scale));
					ir.renderItemOverlayIntoGUI(ClientUtils.font(), ClientUtils.mc().renderEngine, head, (int)(-48/scale),(int)(-36/scale));
					RenderHelper.disableStandardItemLighting();
				}
				GL11.glPopMatrix();
			}
		}
	}

	//	public static ArrayListMultimap<ChunkCoordinates, AxisAlignedBB> additionalBlockBounds = ArrayListMultimap.create();
	//	public static void addAdditionalBlockBounds(ChunkCoordinates cc, AxisAlignedBB aabb)
	//	{
	//		for(AxisAlignedBB aabb1 : additionalBlockBounds.get(cc))
	//			if(aabb1.toString().equals(aabb.toString()))
	//				return;
	//		additionalBlockBounds.put(cc, aabb);
	//	}
	@SubscribeEvent()
	public void renderAdditionalBlockBounds(DrawBlockHighlightEvent event)
	{
		if(event.subID==0 && event.target.typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK)
		{	
			float f1 = 0.002F;
			double d0 = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * (double)event.partialTicks;
			double d1 = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * (double)event.partialTicks;
			double d2 = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * (double)event.partialTicks;
			//			if(additionalBlockBounds.containsKey(new ChunkCoordinates(event.target.blockX,event.target.blockY,event.target.blockZ)))
			if(event.player.worldObj.getBlock(event.target.blockX,event.target.blockY,event.target.blockZ) instanceof IEBlockInterfaces.ICustomBoundingboxes)
			{
				ChunkCoordinates cc = new ChunkCoordinates(event.target.blockX,event.target.blockY,event.target.blockZ);
				IEBlockInterfaces.ICustomBoundingboxes block = (IEBlockInterfaces.ICustomBoundingboxes) event.player.worldObj.getBlock(event.target.blockX,event.target.blockY,event.target.blockZ);
				Set<AxisAlignedBB> set = block.addCustomSelectionBoxesToList(event.player.worldObj, cc.posX,cc.posY,cc.posZ, event.player);
				if(!set.isEmpty())
				{
					//				if(!(event.player.worldObj.getTileEntity(event.target.blockX,event.target.blockY,event.target.blockZ) instanceof IEBlockInterfaces.ICustomBoundingboxes))
					//				{
					//					additionalBlockBounds.removeAll(cc);
					//					return;
					//				}
					GL11.glEnable(GL11.GL_BLEND);
					OpenGlHelper.glBlendFunc(770, 771, 1, 0);
					GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
					GL11.glLineWidth(2.0F);
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					GL11.glDepthMask(false);
					for(AxisAlignedBB aabb : set)
						//				for(AxisAlignedBB aabb : additionalBlockBounds.get(cc))
						if(aabb!=null)
							RenderGlobal.drawOutlinedBoundingBox(aabb.getOffsetBoundingBox(cc.posX,cc.posY,cc.posZ).expand((double)f1, (double)f1, (double)f1).getOffsetBoundingBox(-d0, -d1, -d2), -1);

					GL11.glDepthMask(true);
					GL11.glEnable(GL11.GL_TEXTURE_2D);
					GL11.glDisable(GL11.GL_BLEND);
					event.setCanceled(true);
				}
			}


			ItemStack stack = event.player.getCurrentEquippedItem();
			World world = event.player.worldObj;
			if(stack!=null && stack.getItem() instanceof ItemDrill && ((ItemDrill)stack.getItem()).isEffective(world.getBlock(event.target.blockX,event.target.blockY,event.target.blockZ).getMaterial()))
			{
				ItemStack head = ((ItemDrill)stack.getItem()).getHead(stack);
				if(head!=null)
				{
					int side = event.target.sideHit;
					int diameter = ((IDrillHead)head.getItem()).getMiningSize(head)+((ItemDrill)stack.getItem()).getUpgrades(stack).getInteger("size");
					int depth = ((IDrillHead)head.getItem()).getMiningDepth(head)+((ItemDrill)stack.getItem()).getUpgrades(stack).getInteger("depth");

					int startX=event.target.blockX;
					int startY=event.target.blockY;
					int startZ=event.target.blockZ;
					if(diameter%2==0)//even numbers
					{
						float hx = (float)event.target.hitVec.xCoord-event.target.blockX;
						float hy = (float)event.target.hitVec.yCoord-event.target.blockY;
						float hz = (float)event.target.hitVec.zCoord-event.target.blockZ;
						if((side<2&&hx<.5)||(side<4&&hx<.5))
							startX-= diameter/2;
						if(side>1&&hy<.5)
							startY-= diameter/2;
						if((side<2&&hz<.5)||(side>3&&hz<.5))
							startZ-= diameter/2;
					}
					else//odd numbers
					{
						startX-=(side==4||side==5?0: diameter/2);
						startY-=(side==0||side==1?0: diameter/2);
						startZ-=(side==2||side==3?0: diameter/2);
					}

					GL11.glColor4f(0.1F, 0.1F, 0.1F, 0.4F);
					GL11.glLineWidth(1F);
					GL11.glDisable(GL11.GL_TEXTURE_2D);

					//					AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(startX,startY,startZ, startX+(side==4||side==5?1:diameter),startY+(side==0||side==1?1:diameter),startZ+(side==2||side==3?1: diameter));
					//					RenderGlobal.drawOutlinedBoundingBox(aabb.expand((double)f1, (double)f1, (double)f1).getOffsetBoundingBox(-d0, -d1, -d2), -1);
					for(int dd=0; dd<depth; dd++)
						for(int dw=0; dw<diameter; dw++)
							for(int dh=0; dh<diameter; dh++)
							{
								int x = startX+ (side==4||side==5?dd: dw);
								int y = startY+ (side==0||side==1?dd: dh);
								int z = startZ+ (side==0||side==1?dh: side==4||side==5?dw: dd);
								Block block = event.player.worldObj.getBlock(x,y,z);
								if(block!=null && !block.isAir(world, x, y, z) && block.getPlayerRelativeBlockHardness(event.player, world, x, y, z) != 0)
								{
									if(!((ItemDrill)stack.getItem()).canBreakExtraBlock(world, block, x, y, z, world.getBlockMetadata(x,y,z), event.player, stack, head, false))
										continue;
									AxisAlignedBB aabb = block.getSelectedBoundingBoxFromPool(event.player.worldObj, x,y,z);
									if(aabb!=null)
									{
										RenderGlobal.drawOutlinedBoundingBox(aabb.expand((double)f1, (double)f1, (double)f1).getOffsetBoundingBox(-d0, -d1, -d2), -1);
									}
								}
							}
					GL11.glDepthMask(true);
					GL11.glEnable(GL11.GL_TEXTURE_2D);
					GL11.glDisable(GL11.GL_BLEND);
				}
			}

		}
	}
}
