package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.client.gui.GuiBlastFurnace;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.gui.ContainerRevolver;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.network.MessageRequestBlockUpdate;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledSound;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledTickableSound;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class ClientEventHandler implements IResourceManagerReloadListener
{
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{
		TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
		for(int i=0; i<ClientUtils.destroyBlockIcons.length; i++)
			ClientUtils.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);

		ImmersiveEngineering.proxy.clearRenderCaches();
	}

	public static Set<Connection> skyhookGrabableConnections = new HashSet();
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side.isClient() && event.phase== Phase.START && event.player!=null && event.player==ClientUtils.mc().getRenderViewEntity())
		{
			skyhookGrabableConnections.clear();
			EntityPlayer player = event.player;
			ItemStack stack = player.getActiveItemStack();
			if(stack!=null && stack.getItem() instanceof ItemSkyhook)
			{
				TileEntity connector = null;
				double lastDist = 0;
				Connection line = null;
				double py = player.posY+player.getEyeHeight();
				BlockPos pos = new BlockPos(player.posX,player.posY,player.posZ);
				for(int xx=-2; xx<=2; xx++)
					for(int zz=-2; zz<=2; zz++)
						for(int yy=0; yy<=3; yy++)
						{
							TileEntity tile = player.worldObj.getTileEntity(pos.add(xx, yy, zz));
							if(tile!=null)
							{
								Connection con = SkylineHelper.getTargetConnection(player.worldObj, pos.add(xx, yy, zz), player, null);
								if(con!=null)
								{
									double d = tile.getDistanceSq(player.posX,py,player.posZ);
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
//		if(event.side.isClient() && event.phase == Phase.END && event.player!=null)
//		{
//			EntityPlayer player = event.player;
//			ItemStack stack = player.getActiveItemStack();
//			boolean twohanded = stack!=null && (stack.getItem() instanceof ItemDrill);
//			if(twohanded && (player!=ClientUtils.mc().getRenderViewEntity()||ClientUtils.mc().gameSettings.thirdPersonView!=0))
//			{
//				if(player.getItemInUseCount() <= 0)
//				{
//					player.stopActiveHand();
//					player.setActiveHand(EnumHand.MAIN_HAND);
//				}
//			}
//
//		}
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if(event.getItemStack().hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
		{
			ShaderWrapper wrapper = event.getItemStack().getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			ItemStack shader = wrapper != null ? wrapper.getShaderItem() : null;
			if(shader != null)
				event.getToolTip().add(TextFormatting.DARK_GRAY + shader.getDisplayName());
		}
		if(ItemNBTHelper.hasKey(event.getItemStack(),"IE:Earmuffs"))
		{
			ItemStack earmuffs = ItemNBTHelper.getItemStack(event.getItemStack(), "IE:Earmuffs");
			if(earmuffs!=null)
				event.getToolTip().add(TextFormatting.GRAY+earmuffs.getDisplayName());
		}
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT
				&& ClientUtils.mc().currentScreen != null
				&& ClientUtils.mc().currentScreen instanceof GuiBlastFurnace
				&& BlastFurnaceRecipe.isValidBlastFuel(event.getItemStack()))
			event.getToolTip().add(TextFormatting.GRAY+ I18n.format("desc.ImmersiveEngineering.info.blastFuelTime", BlastFurnaceRecipe.getBlastFuelTime(event.getItemStack())));
		if(IEConfig.oreTooltips && event.isShowAdvancedItemTooltips())
		{
			for(int oid : OreDictionary.getOreIDs(event.getItemStack()))
				event.getToolTip().add(TextFormatting.GRAY + OreDictionary.getOreName(oid));
//			FluidStack fs = FluidUtil.getFluidContained(event.getItemStack());
//			if(fs!=null && fs.getFluid()!=null)
//				event.getToolTip().add("Fluid: "+ FluidRegistry.getFluidName(fs));
		}
	}

	@SubscribeEvent
	public void onPlaySound(PlaySoundEvent event)
	{
		if(!ItemEarmuffs.affectedSoundCategories.contains(event.getSound().getCategory().getName()))
			return;
		if(ClientUtils.mc().thePlayer!=null && ClientUtils.mc().thePlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD)!=null)
		{
			ItemStack earmuffs = ClientUtils.mc().thePlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			if(ItemNBTHelper.hasKey(earmuffs, "IE:Earmuffs"))
				earmuffs = ItemNBTHelper.getItemStack(earmuffs, "IE:Earmuffs");
			if(earmuffs!=null && IEContent.itemEarmuffs.equals(earmuffs.getItem()) && !ItemNBTHelper.getBoolean(earmuffs,"IE:Earmuffs:Cat_"+event.getSound().getCategory().getName()))
			{
				for(String blacklist : IEConfig.Tools.earDefenders_SoundBlacklist)
					if(blacklist!=null && blacklist.equalsIgnoreCase(event.getSound().getSoundLocation().toString()))
						return;
				if(event.getSound() instanceof ITickableSound)
					event.setResultSound(new IEMuffledTickableSound((ITickableSound)event.getSound(), ItemEarmuffs.getVolumeMod(earmuffs)));
				else
					event.setResultSound(new IEMuffledSound(event.getSound(), ItemEarmuffs.getVolumeMod(earmuffs)));

				if(event.getSound() instanceof PositionedSoundRecord)
				{
					BlockPos pos = new BlockPos(event.getSound().getXPosF(),event.getSound().getYPosF(),event.getSound().getZPosF());
					if(ClientUtils.mc().renderGlobal.mapSoundPositions.containsKey(pos))
						ClientUtils.mc().renderGlobal.mapSoundPositions.put(pos, event.getResultSound());
				}
			}
		}
	}
	/*
	@SubscribeEvent()
	public void lastWorldRender(RenderWorldLastEvent event)
	{
		connectionsRendered = false;
		ParticleRenderer.dispatch();
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
	 */
	@SubscribeEvent()
	public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event)
	{
		if(ZoomHandler.isZooming && event.getType()==RenderGameOverlayEvent.ElementType.CROSSHAIRS)
		{
			event.setCanceled(true);
			if(ZoomHandler.isZooming)
			{
				ClientUtils.bindTexture("immersiveengineering:textures/gui/scope.png");
				int width = event.getResolution().getScaledWidth();
				int height = event.getResolution().getScaledHeight();
				int resMin = Math.min(width,height);
				float offsetX = (width-resMin)/2f;
				float offsetY = (height-resMin)/2f;

				if(resMin==width)
				{
					ClientUtils.drawColouredRect(0,0, width,(int)offsetY+1, 0xff000000);
					ClientUtils.drawColouredRect(0,(int)offsetY+resMin, width,(int)offsetY+1, 0xff000000);
				}
				else
				{
					ClientUtils.drawColouredRect(0,0, (int)offsetX+1,height, 0xff000000);
					ClientUtils.drawColouredRect((int)offsetX+resMin,0, (int)offsetX+1,height, 0xff000000);
				}
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);

				GL11.glTranslatef(offsetX,offsetY,0);
				ClientUtils.drawTexturedRect(0,0,resMin,resMin, 0f,1f,0f,1f);

				ClientUtils.bindTexture("immersiveengineering:textures/gui/hudElements.png");
				ClientUtils.drawTexturedRect(218/256f*resMin,64/256f*resMin, 24/256f*resMin,128/256f*resMin, 64/256f,88/256f,96/256f,224/256f);
				ItemStack equipped = ClientUtils.mc().thePlayer.getHeldItem(EnumHand.MAIN_HAND);
				if(equipped!=null && equipped.getItem() instanceof IZoomTool)
				{
					IZoomTool tool = (IZoomTool)equipped.getItem();
					float[] steps = tool.getZoomSteps(equipped, ClientUtils.mc().thePlayer);
					if(steps!=null && steps.length>1)
					{
						int curStep = -1;
						float dist=0;

						float totalOffset = 0;
						float stepLength = 118/(float)steps.length;
						float stepOffset = (stepLength-7)/2f;
						GL11.glTranslatef(223/256f*resMin,64/256f*resMin, 0);
						GL11.glTranslatef(0,(5+stepOffset)/256*resMin,0);
						for(int i=0; i<steps.length; i++)
						{
							ClientUtils.drawTexturedRect(0,0, 8/256f*resMin,7/256f*resMin, 88/256f,96/256f,96/256f,103/256f);
							GL11.glTranslatef(0,stepLength/256*resMin,0);
							totalOffset += stepLength;

							if(curStep==-1 || Math.abs(steps[i]-ZoomHandler.fovZoom)<dist)
							{
								curStep = i;
								dist = Math.abs(steps[i]-ZoomHandler.fovZoom);
							}
						}
						GL11.glTranslatef(0,-totalOffset/256*resMin,0);

						if(curStep>=0 && curStep<steps.length)
						{
							GL11.glTranslatef(6/256f*resMin,curStep*stepLength/256*resMin,0);
							ClientUtils.drawTexturedRect(0,0, 8/256f*resMin,7/256f*resMin, 88/256f,98/256f,103/256f,110/256f);
							ClientUtils.font().drawString((1/steps[curStep])+"x", (int)(16/256f*resMin),0, 0xffffff);
							GL11.glTranslatef(-6/256f*resMin,-curStep*stepLength/256*resMin,0);
						}
						GL11.glTranslatef(0,-((5+stepOffset)/256*resMin),0);
						GL11.glTranslatef(-223/256f*resMin,-64/256f*resMin, 0);
					}
				}

				GL11.glTranslatef(-offsetX,-offsetY,0);
			}
		}
	}

	@SubscribeEvent()
	public void onRenderOverlayPost(RenderGameOverlayEvent.Post event)
	{
		if(ClientUtils.mc().thePlayer!=null && event.getType() == RenderGameOverlayEvent.ElementType.TEXT)
		{
			EntityPlayer player = ClientUtils.mc().thePlayer;

			for(EnumHand hand : EnumHand.values())
				if(player.getHeldItem(hand)!=null)
				{
					ItemStack equipped = player.getHeldItem(hand);
					if(OreDictionary.itemMatches(new ItemStack(IEContent.itemTool,1,2), equipped, false) || OreDictionary.itemMatches(new ItemStack(IEContent.itemWireCoil,1,OreDictionary.WILDCARD_VALUE), equipped, false) )
					{
						if(ItemNBTHelper.hasKey(equipped, "linkingPos"))
						{
							int[] link = ItemNBTHelper.getIntArray(equipped, "linkingPos");
							if(link!=null&&link.length>3)
							{
								String s = I18n.format(Lib.DESC_INFO+"attachedTo", link[1],link[2],link[3]);
								ClientUtils.font().drawString(s, event.getResolution().getScaledWidth()/2 - ClientUtils.font().getStringWidth(s)/2, event.getResolution().getScaledHeight()-GuiIngameForge.left_height-20, WireType.ELECTRUM.getColour(null), true);
							}
						}
					}
					else if (OreDictionary.itemMatches(equipped, new ItemStack(IEContent.itemFluorescentTube), false))
					{
						String s = I18n.format("desc.ImmersiveEngineering.info.colour", "#"+ItemFluorescentTube.hexColorString(equipped));
						ClientUtils.font().drawString(s, event.getResolution().getScaledWidth()/2 - ClientUtils.font().getStringWidth(s)/2, event.getResolution().getScaledHeight()-GuiIngameForge.left_height-20, ItemFluorescentTube.getRGBInt(equipped), true);
					}
					else if(equipped.getItem() instanceof ItemRevolver && equipped.getItemDamage()!=2)
					{
						ClientUtils.bindTexture("immersiveengineering:textures/gui/revolver.png");
						ItemStack[] bullets = ((ItemRevolver)equipped.getItem()).getBullets(equipped);
						int bulletAmount = bullets.length;
						EnumHandSide side = hand==EnumHand.MAIN_HAND?player.getPrimaryHand():player.getPrimaryHand().opposite();
						float dx = side==EnumHandSide.RIGHT?event.getResolution().getScaledWidth()-32-48:48;
						float dy = event.getResolution().getScaledHeight()-64;
						GlStateManager.pushMatrix();
						GlStateManager.enableBlend();
						GlStateManager.translate(dx, dy, 0);
						GlStateManager.scale(.5f, .5f, 1);
						GlStateManager.color(1, 1, 1, 1);

						ClientUtils.drawTexturedRect(0,1,74,74, 0/256f,74/256f, 51/256f,125/256f);
						if(bulletAmount>=18)
							ClientUtils.drawTexturedRect(47,1,103,74, 74/256f,177/256f, 51/256f,125/256f);
						else if(bulletAmount>8)
							ClientUtils.drawTexturedRect(57,1,79,39, 57/256f,136/256f, 12/256f,51/256f);

						RenderItem ir = ClientUtils.mc().getRenderItem();
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

								ir.renderItemIntoGUI(bullets[i], x,y);
							}
						}
						RenderHelper.disableStandardItemLighting();
						GlStateManager.disableBlend();
						GlStateManager.popMatrix();
					} else if(equipped.getItem() instanceof ItemRailgun)
					{
						int duration = 72000 - player.getItemInUseCount();
						int chargeTime = ((ItemRailgun)equipped.getItem()).getChargeTime(equipped);
						int chargeLevel = duration < 72000 ? Math.min(99, (int)(duration / (float)chargeTime * 100)) : 0;
						float scale = 2f;
						GlStateManager.pushMatrix();
						GlStateManager.translate(event.getResolution().getScaledWidth() - 80, event.getResolution().getScaledHeight() - 30, 0);
						GlStateManager.scale(scale, scale, 1);
						ClientProxy.nixieFont.drawString((chargeLevel < 10 ? "0" : "") + chargeLevel, 0, 0, Lib.colour_nixieTubeText, false);
						GlStateManager.scale(1 / scale, 1 / scale, 1);
						GlStateManager.popMatrix();
					}
					else if((equipped.getItem() instanceof ItemDrill && equipped.getItemDamage()==0)
							||equipped.getItem() instanceof ItemChemthrower)
					{
						boolean drill = equipped.getItem() instanceof ItemDrill;
						ClientUtils.bindTexture("immersiveengineering:textures/gui/hudElements.png");
						GL11.glColor4f(1, 1, 1, 1);
						float dx = event.getResolution().getScaledWidth()-16;
						float dy = event.getResolution().getScaledHeight();
						GL11.glPushMatrix();
						GL11.glTranslated(dx, dy, 0);
						int w = 31;
						int h = 62;
						double uMin = 179/256f;
						double uMax = 210/256f;
						double vMin = 9/256f;
						double vMax = 71/256f;
						ClientUtils.drawTexturedRect(-24,-68, w,h, uMin,uMax,vMin,vMax);

						GL11.glTranslated(-23,-37,0);
						IFluidHandler handler = FluidUtil.getFluidHandler(equipped);
						int capacity = -1;
						if(handler!=null)
						{
							IFluidTankProperties[] props = handler.getTankProperties();
							if(props!=null&&props.length>0)
								capacity = props[0].getCapacity();
						}
						if(capacity>-1)
						{
							FluidStack fuel = FluidUtil.getFluidContained(equipped);
							int amount = fuel != null ? fuel.amount : 0;
							if(!drill && player.isHandActive())
							{
								int use = player.getItemInUseMaxCount();
								amount -= use * IEConfig.Tools.chemthrower_consumption;
							}
							float cap = (float) capacity;
							float angle = 83 - (166 * amount / cap);
							GL11.glRotatef(angle, 0, 0, 1);
							ClientUtils.drawTexturedRect(6, -2, 24, 4, 91 / 256f, 123 / 256f, 80 / 256f, 87 / 256f);
							GL11.glRotatef(-angle, 0, 0, 1);
							//					for(int i=0; i<=8; i++)
							//					{
							//						float angle = 83-(166/8f)*i;
							//						GL11.glRotatef(angle, 0, 0, 1);
							//						ClientUtils.drawTexturedRect(6,-2, 24,4, 91/256f,123/256f, 80/96f,87/96f);
							//						GL11.glRotatef(-angle, 0, 0, 1);
							//					}
							GL11.glTranslated(23, 37, 0);
							if(drill)
							{
								ClientUtils.drawTexturedRect(-54, -73, 66, 72, 108 / 256f, 174 / 256f, 4 / 256f, 76 / 256f);
								RenderItem ir = ClientUtils.mc().getRenderItem();
								ItemStack head = ((ItemDrill) equipped.getItem()).getHead(equipped);
								if(head != null)
								{
									ir.renderItemIntoGUI(head, -51, -45);
									ir.renderItemOverlayIntoGUI(head.getItem().getFontRenderer(head), head, -51, -45, null);
									RenderHelper.disableStandardItemLighting();
								}
							} else
							{
								ClientUtils.drawTexturedRect(-41, -73, 53, 72, 8 / 256f, 61 / 256f, 4 / 256f, 76 / 256f);
								boolean ignite = ItemNBTHelper.getBoolean(equipped, "ignite");
								ClientUtils.drawTexturedRect(-32, -43, 12, 12, 66 / 256f, 78 / 256f, (ignite ? 21 : 9) / 256f, (ignite ? 33 : 21) / 256f);

							}
							GL11.glPopMatrix();
						}
					}
					//				else if(equipped.getItem() instanceof ItemRailgun)
					//				{
					//					float dx = event.getResolution().getScaledWidth()-32-48;
					//					float dy = event.getResolution().getScaledHeight()-40;
					//					ClientUtils.bindTexture("immersiveengineering:textures/gui/hudElements.png");
					//					GL11.glColor4f(1, 1, 1, 1);
					//					GL11.glPushMatrix();
					//					GL11.glEnable(GL11.GL_BLEND);
					//					GL11.glTranslated(dx, dy, 0);
					//
					//					int duration = player.getItemInUseDuration();
					//					int chargeTime = ((ItemRailgun)equipped.getItem()).getChargeTime(equipped);
					//					int chargeLevel = Math.min(99, (int)(duration/(float)chargeTime*100));
					//					//					ClientUtils.drawTexturedRect(0,0, 64,32, 0/256f,64/256f, 96/256f,128/256f);
					//
					//					GL11.glScalef(1.5f,1.5f,1.5f);
					//					int col = Config.getBoolean("nixietubeFont")?Lib.colour_nixieTubeText:0xffffff;
					//					ClientProxy.nixieFont.setDrawTubeFlag(false);
					//					//					ClientProxy.nixieFont.drawString((chargeLevel<10?"0"+chargeLevel:""+chargeLevel), 19,3, col);
					//					ClientProxy.nixieFont.setDrawTubeFlag(true);
					//
					//					GL11.glPopMatrix();
					//				}

					RayTraceResult mop = ClientUtils.mc().objectMouseOver;
					if(mop!=null && mop.getBlockPos()!=null)
					{
						TileEntity tileEntity = player.worldObj.getTileEntity(mop.getBlockPos());
						if(OreDictionary.itemMatches(new ItemStack(IEContent.itemTool,1,2), equipped, true))
						{
							int col = IEConfig.nixietubeFont?Lib.colour_nixieTubeText:0xffffff;
							String[] text = null;
							if(tileEntity instanceof IFluxReceiver)
							{
								int maxStorage = ((IFluxReceiver)tileEntity).getMaxEnergyStored(mop.sideHit);
								int storage = ((IFluxReceiver)tileEntity).getEnergyStored(mop.sideHit);
								if(maxStorage>0)
									text = I18n.format(Lib.DESC_INFO+"energyStored","<br>"+Utils.toScientificNotation(storage,"0##",100000)+" / "+Utils.toScientificNotation(maxStorage,"0##",100000)).split("<br>");
							}
							//						else if(Lib.GREG && GregTechHelper.gregtech_isValidEnergyOutput(tileEntity))
							//						{
							//							String gregStored = GregTechHelper.gregtech_getEnergyStored(tileEntity);
							//							if(gregStored!=null)
							//								text = StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"energyStored","<br>"+gregStored).split("<br>");
							//						}
							else if(mop.entityHit instanceof IFluxReceiver)
							{
								int maxStorage = ((IFluxReceiver)mop.entityHit).getMaxEnergyStored(null);
								int storage = ((IFluxReceiver)mop.entityHit).getEnergyStored(null);
								if(maxStorage>0)
									text = I18n.format(Lib.DESC_INFO+"energyStored","<br>"+Utils.toScientificNotation(storage,"0##",100000)+" / "+Utils.toScientificNotation(maxStorage,"0##",100000)).split("<br>");
							}
							if(text!=null)
							{
								if (player.worldObj.getTotalWorldTime()%20==0)
								{
									ImmersiveEngineering.packetHandler.sendToServer(new MessageRequestBlockUpdate(player.dimension, mop.getBlockPos()));
								}
								int i = 0;
								for(String s : text)
									if(s!=null)
									{
										int w = ClientProxy.nixieFontOptional.getStringWidth(s);
										ClientProxy.nixieFontOptional.drawString(s, event.getResolution().getScaledWidth()/2-w/2, event.getResolution().getScaledHeight()/2-4-text.length*(ClientProxy.nixieFontOptional.FONT_HEIGHT+2)+(i++)*(ClientProxy.nixieFontOptional.FONT_HEIGHT+2), col, true);
									}
							}
						}
					}
				}
			if(ClientUtils.mc().objectMouseOver!=null)
			{
				boolean hammer = player.getHeldItem(EnumHand.MAIN_HAND) != null && Utils.isHammer(player.getHeldItem(EnumHand.MAIN_HAND));
				RayTraceResult mop = ClientUtils.mc().objectMouseOver;
				if(mop!=null && mop.getBlockPos()!=null)
				{
					TileEntity tileEntity = player.worldObj.getTileEntity(mop.getBlockPos());
					if(tileEntity instanceof IBlockOverlayText)
					{
						IBlockOverlayText overlayBlock = (IBlockOverlayText) tileEntity;
						String[] text = overlayBlock.getOverlayText(ClientUtils.mc().thePlayer, mop, hammer);
						boolean useNixie = overlayBlock.useNixieFont(ClientUtils.mc().thePlayer, mop);
						if(text!=null && text.length>0)
						{
							FontRenderer font = useNixie?ClientProxy.nixieFontOptional:ClientUtils.font();
							int col = (useNixie&& IEConfig.nixietubeFont)?Lib.colour_nixieTubeText:0xffffff;
							int i = 0;
							for(String s : text)
								if(s!=null)
									font.drawString(s, event.getResolution().getScaledWidth()/2+8, event.getResolution().getScaledHeight()/2+8+(i++)*font.FONT_HEIGHT, col, true);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent()
	public void onFOVUpdate(FOVUpdateEvent event)
	{
		EntityPlayer player = ClientUtils.mc().thePlayer;
		if(player.getHeldItem(EnumHand.MAIN_HAND)!=null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IZoomTool)
		{
			if(player.isSneaking() && player.onGround)
			{
				ItemStack equipped = player.getHeldItem(EnumHand.MAIN_HAND);
				IZoomTool tool = (IZoomTool)equipped.getItem();
				if(tool.canZoom(equipped, player))
				{
					if(!ZoomHandler.isZooming)
					{
						float[] steps = tool.getZoomSteps(equipped, player);
						if(steps!=null && steps.length>0)
						{
							int curStep = -1;
							float dist=0;
							for(int i=0; i<steps.length; i++)
								if(curStep==-1 || Math.abs(steps[i]-ZoomHandler.fovZoom)<dist)
								{
									curStep = i;
									dist = Math.abs(steps[i]-ZoomHandler.fovZoom);
								}
							if(curStep!=-1)
								ZoomHandler.fovZoom = steps[curStep];
							else
								ZoomHandler.fovZoom = event.getFov();
						}
						ZoomHandler.isZooming = true;
					}
					event.setNewfov(ZoomHandler.fovZoom);
				}
				else if(ZoomHandler.isZooming)
					ZoomHandler.isZooming = false;
			}
			else if(ZoomHandler.isZooming)
				ZoomHandler.isZooming = false;
		}
		else if(ZoomHandler.isZooming)
			ZoomHandler.isZooming = false;
	}
	@SubscribeEvent
	public void onMouseEvent(MouseEvent event)
	{
		if(event.getDwheel() != 0)
		{
			EntityPlayer player = ClientUtils.mc().thePlayer;
			if(player.getHeldItem(EnumHand.MAIN_HAND)!=null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IZoomTool && player.isSneaking())
			{
				ItemStack equipped = player.getHeldItem(EnumHand.MAIN_HAND);
				IZoomTool tool = (IZoomTool)equipped.getItem();
				if(tool.canZoom(equipped, player))
				{
					float[] steps = tool.getZoomSteps(equipped, player);
					if(steps!=null && steps.length>0)
					{
						int curStep = -1;
						float dist=0;
						for(int i=0; i<steps.length; i++)
							if(curStep==-1 || Math.abs(steps[i]-ZoomHandler.fovZoom)<dist)
							{
								curStep = i;
								dist = Math.abs(steps[i]-ZoomHandler.fovZoom);
							}
						if(curStep!=-1)
						{
							int newStep = curStep+(event.getDwheel()>0?-1:1);
							if(newStep>=0 && newStep<steps.length)
								ZoomHandler.fovZoom = steps[newStep];
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent()
	public void renderAdditionalBlockBounds(DrawBlockHighlightEvent event)
	{
		if(event.getSubID()==0 && event.getTarget().typeOfHit== RayTraceResult.Type.BLOCK)
		{
			float f1 = 0.002F;
			double px = -TileEntityRendererDispatcher.staticPlayerX;
			double py = -TileEntityRendererDispatcher.staticPlayerY;
			double pz = -TileEntityRendererDispatcher.staticPlayerZ;
			TileEntity tile = event.getPlayer().worldObj.getTileEntity(event.getTarget().getBlockPos());
			//			if(event.getPlayer().worldObj.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof IEBlockInterfaces.ICustomBoundingboxes)
			if(tile instanceof IAdvancedSelectionBounds)
			{
				//				IEBlockInterfaces.ICustomBoundingboxes block = (IEBlockInterfaces.ICustomBoundingboxes) event.getPlayer().worldObj.getBlockState(event.getTarget().getBlockPos()).getBlock();
				IAdvancedSelectionBounds iasb = (IAdvancedSelectionBounds)tile;
				List<AxisAlignedBB> boxes = iasb.getAdvancedSelectionBounds();
				if(boxes!=null && !boxes.isEmpty())
				{
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.glLineWidth(2.0F);
					GlStateManager.disableTexture2D();
					GlStateManager.depthMask(false);
					ArrayList<AxisAlignedBB> additionalBoxes = new ArrayList<AxisAlignedBB>();
					AxisAlignedBB overrideBox = null;
					for(AxisAlignedBB aabb : boxes)
						if(aabb!=null)
						{
							if(iasb.isOverrideBox(aabb, event.getPlayer(), event.getTarget(), additionalBoxes))
								overrideBox = aabb;
						}

					if(overrideBox!=null)
						RenderGlobal.func_189697_a(overrideBox.expand(f1, f1, f1).offset(px, py, pz), 0, 0, 0, 0.4f);
					else
						for(AxisAlignedBB aabb : additionalBoxes.isEmpty()?boxes:additionalBoxes)
							RenderGlobal.func_189697_a(aabb.expand(f1, f1, f1).offset(px, py, pz), 0, 0, 0, 0.4f);
					GlStateManager.depthMask(true);
					GlStateManager.enableTexture2D();
					GlStateManager.disableBlend();
					event.setCanceled(true);
				}
			}


			World world = event.getPlayer().worldObj;
			ItemStack stack = event.getPlayer().getHeldItem(EnumHand.MAIN_HAND);
			if(stack != null && IEContent.blockConveyor.equals(Block.getBlockFromItem(stack.getItem())) && event.getTarget().sideHit.getAxis() == Axis.Y)
			{
				EnumFacing side = event.getTarget().sideHit;
				BlockPos pos = event.getTarget().getBlockPos();
				AxisAlignedBB targetedBB = world.getBlockState(pos).getSelectedBoundingBox(world, pos);
				if(targetedBB != null)
					targetedBB = targetedBB.offset(-pos.getX(), -pos.getY(), -pos.getZ());
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(2.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);

				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer vertexbuffer = tessellator.getBuffer();
				vertexbuffer.setTranslation(pos.getX() + px, pos.getY() + py, pos.getZ() + pz);
				double[][] points = new double[4][];


				if(side.getAxis() == Axis.Y)
				{
					points[0] = new double[]{0 - f1, side == EnumFacing.DOWN ? ((targetedBB != null ? targetedBB.minY : 0) - f1) : ((targetedBB != null ? targetedBB.maxY : 1) + f1), 0 - f1};
					points[1] = new double[]{1 + f1, side == EnumFacing.DOWN ? ((targetedBB != null ? targetedBB.minY : 0) - f1) : ((targetedBB != null ? targetedBB.maxY : 1) + f1), 1 + f1};
					points[2] = new double[]{0 - f1, side == EnumFacing.DOWN ? ((targetedBB != null ? targetedBB.minY : 0) - f1) : ((targetedBB != null ? targetedBB.maxY : 1) + f1), 1 + f1};
					points[3] = new double[]{1 + f1, side == EnumFacing.DOWN ? ((targetedBB != null ? targetedBB.minY : 0) - f1) : ((targetedBB != null ? targetedBB.maxY : 1) + f1), 0 - f1};
				} else if(side.getAxis() == Axis.Z)
				{
					points[0] = new double[]{1 + f1, 1 + f1, side == EnumFacing.NORTH ? ((targetedBB != null ? targetedBB.minZ : 0) - f1) : ((targetedBB != null ? targetedBB.maxZ : 1) + f1)};
					points[1] = new double[]{0 - f1, 0 - f1, side == EnumFacing.NORTH ? ((targetedBB != null ? targetedBB.minZ : 0) - f1) : ((targetedBB != null ? targetedBB.maxZ : 1) + f1)};
					points[2] = new double[]{0 - f1, 1 + f1, side == EnumFacing.NORTH ? ((targetedBB != null ? targetedBB.minZ : 0) - f1) : ((targetedBB != null ? targetedBB.maxZ : 1) + f1)};
					points[3] = new double[]{1 + f1, 0 - f1, side == EnumFacing.NORTH ? ((targetedBB != null ? targetedBB.minZ : 0) - f1) : ((targetedBB != null ? targetedBB.maxZ : 1) + f1)};
				} else
				{
					points[0] = new double[]{side == EnumFacing.WEST ? ((targetedBB != null ? targetedBB.minX : 0) - f1) : ((targetedBB != null ? targetedBB.maxX : 1) + f1), 1 + f1, 1 + f1};
					points[1] = new double[]{side == EnumFacing.WEST ? ((targetedBB != null ? targetedBB.minX : 0) - f1) : ((targetedBB != null ? targetedBB.maxX : 1) + f1), 0 - f1, 0 - f1};
					points[2] = new double[]{side == EnumFacing.WEST ? ((targetedBB != null ? targetedBB.minX : 0) - f1) : ((targetedBB != null ? targetedBB.maxX : 1) + f1), 1 + f1, 0 - f1};
					points[3] = new double[]{side == EnumFacing.WEST ? ((targetedBB != null ? targetedBB.minX : 0) - f1) : ((targetedBB != null ? targetedBB.maxX : 1) + f1), 0 - f1, 1 + f1};
				}
				vertexbuffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
				for(double[] point : points)
					vertexbuffer.pos(point[0], point[1], point[2]).color(0, 0, 0, 0.4F).endVertex();
				tessellator.draw();

				vertexbuffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
				vertexbuffer.pos(points[0][0], points[0][1], points[0][2]).color(0, 0, 0, 0.4F).endVertex();
				vertexbuffer.pos(points[2][0], points[2][1], points[2][2]).color(0, 0, 0, 0.4F).endVertex();
				vertexbuffer.pos(points[1][0], points[1][1], points[1][2]).color(0, 0, 0, 0.4F).endVertex();
				vertexbuffer.pos(points[3][0], points[3][1], points[3][2]).color(0, 0, 0, 0.4F).endVertex();
				tessellator.draw();

				float xFromMid = side.getAxis() == Axis.X ? 0 : (float) event.getTarget().hitVec.xCoord - pos.getX() - .5f;
				float yFromMid = side.getAxis() == Axis.Y ? 0 : (float) event.getTarget().hitVec.yCoord - pos.getY() - .5f;
				float zFromMid = side.getAxis() == Axis.Z ? 0 : (float) event.getTarget().hitVec.zCoord - pos.getZ() - .5f;
				float max = Math.max(Math.abs(yFromMid), Math.max(Math.abs(xFromMid), Math.abs(zFromMid)));
				Vec3d dir = new Vec3d(max == Math.abs(xFromMid) ? Math.signum(xFromMid) : 0, max == Math.abs(yFromMid) ? Math.signum(yFromMid) : 0, max == Math.abs(zFromMid) ? Math.signum(zFromMid) : 0);
				if(dir != null)
					drawBlockOverlayArrow(tessellator, vertexbuffer, dir, side, targetedBB);
				vertexbuffer.setTranslation(0, 0, 0);

				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
			}

			if(stack!=null && stack.getItem() instanceof ItemDrill && ((ItemDrill)stack.getItem()).isEffective(world.getBlockState(event.getTarget().getBlockPos()).getMaterial()))
			{
				ItemStack head = ((ItemDrill)stack.getItem()).getHead(stack);
				if(head!=null)
				{
					ImmutableList<BlockPos> blocks = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world, event.getPlayer(), event.getTarget());
					drawAdditionalBlockbreak(event.getContext(), event.getPlayer(), event.getPartialTicks(), blocks);
				}
			}

		}
	}

	private static float[][] arrowCoords = {{0, .375f}, {.3125f, .0625f}, {.125f, .0625f}, {.125f, -.375f}, {-.125f, -.375f}, {-.125f, .0625f}, {-.3125f, .0625f}};

	public static void drawBlockOverlayArrow(Tessellator tessellator, VertexBuffer vertexbuffer, Vec3d directionVec, EnumFacing side, AxisAlignedBB targetedBB)
	{
		Vec3d[] translatedPositions = new Vec3d[arrowCoords.length];
		Matrix4 mat = new Matrix4();
		Vec3d defaultDir = side.getAxis() == Axis.Y ? new Vec3d(0, 0, 1) : new Vec3d(0, 1, 0);
		directionVec = directionVec.normalize();
		double angle = Math.acos(defaultDir.dotProduct(directionVec));
		Vec3d axis = defaultDir.crossProduct(directionVec);
		mat.rotate(angle, axis.xCoord, axis.yCoord, axis.zCoord);
		if(side != null)
		{
			if(side.getAxis() == Axis.Z)
				mat.rotate(Math.PI / 2, 1, 0, 0).rotate(Math.PI, 0, 1, 0);
			else if(side.getAxis() == Axis.X)
				mat.rotate(Math.PI / 2, 0, 0, 1).rotate(Math.PI / 2, 0, 1, 0);
		}
		for(int i = 0; i < translatedPositions.length; i++)
		{
			Vec3d vec = mat.apply(new Vec3d(arrowCoords[i][0], 0, arrowCoords[i][1])).addVector(.5, .5, .5);
			if(side != null && targetedBB != null)
				vec = new Vec3d(side == EnumFacing.WEST ? targetedBB.minX - .002 : side == EnumFacing.EAST ? targetedBB.maxX + .002 : vec.xCoord, side == EnumFacing.DOWN ? targetedBB.minY - .002 : side == EnumFacing.UP ? targetedBB.maxY + .002 : vec.yCoord, side == EnumFacing.NORTH ? targetedBB.minZ - .002 : side == EnumFacing.SOUTH ? targetedBB.maxZ + .002 : vec.zCoord);
			translatedPositions[i] = vec;
		}

		vertexbuffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
		for(Vec3d point : translatedPositions)
			vertexbuffer.pos(point.xCoord, point.yCoord, point.zCoord).color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F).endVertex();
		tessellator.draw();
		vertexbuffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
		for(Vec3d point : translatedPositions)
			vertexbuffer.pos(point.xCoord, point.yCoord, point.zCoord).color(0, 0, 0, 0.4F).endVertex();
		tessellator.draw();
	}

	public static void drawAdditionalBlockbreak(RenderGlobal context, EntityPlayer player, float partialTicks, Collection<BlockPos> blocks)
	{
		for(BlockPos pos : blocks)
			context.drawSelectionBox(player, new RayTraceResult(new Vec3d(0, 0, 0), null, pos), 0, partialTicks);

		PlayerControllerMP controllerMP = ClientUtils.mc().playerController;
		if(controllerMP.isHittingBlock)
			ClientUtils.drawBlockDamageTexture(ClientUtils.tes(), ClientUtils.tes().getBuffer(), player, partialTicks, player.worldObj, blocks);
	}

	//	static void renderBoundingBox(AxisAlignedBB aabb, double offsetX, double offsetY, double offsetZ, float expand)
	//	{
	//		if(aabb instanceof AdvancedAABB && ((AdvancedAABB)aabb).drawOverride!=null && ((AdvancedAABB)aabb).drawOverride.length>0)
	//		{
	//			double midX = aabb.minX+(aabb.maxX-aabb.minX)/2;
	//			double midY = aabb.minY+(aabb.maxY-aabb.minY)/2;
	//			double midZ = aabb.minZ+(aabb.maxZ-aabb.minZ)/2;
	//			ClientUtils.tes().addTranslation((float)offsetX, (float)offsetY, (float)offsetZ);
	//			for(Vec3[] face : ((AdvancedAABB)aabb).drawOverride)
	//			{
	//				ClientUtils.tes().startDrawing(GL11.GL_LINE_LOOP);
	//				for(Vec3 v : face)
	//					ClientUtils.tes().addVertex(v.xCoord+(v.xCoord<midX?-expand:expand),v.yCoord+(v.yCoord<midY?-expand:expand),v.zCoord+(v.zCoord<midZ?-expand:expand));
	//				ClientUtils.tes().draw();
	//			}
	//			ClientUtils.tes().addTranslation((float)-offsetX, (float)-offsetY, (float)-offsetZ);
	//		}
	//		else
	//			RenderGlobal.drawOutlinedBoundingBox(aabb.getOffsetBoundingBox(offsetX, offsetY, offsetZ).expand((double)expand, (double)expand, (double)expand), -1);
	//	}

	@SubscribeEvent()
	public void onClientDeath(LivingDeathEvent event)
	{
	}
	@SubscribeEvent()
	public void onRenderLivingPre(RenderLivingEvent.Pre event)
	{
		if(event.getEntity().getEntityData().hasKey("headshot"))
		{
			ModelBase model = event.getRenderer().mainModel;
			if(model instanceof ModelBiped)
				((ModelBiped)model).bipedHead.showModel=false;
			else if(model instanceof ModelVillager)
				((ModelVillager)model).villagerHead.showModel=false;
		}
		for(EnumHand hand : EnumHand.values())
		{
			ItemStack heldItem = event.getEntity().getHeldItem(hand);
			if(heldItem != null)
			{
				ArmPose twohanded = null;
				if(OreDictionary.itemMatches(new ItemStack(IEContent.itemChemthrower), heldItem, true) || OreDictionary.itemMatches(new ItemStack(IEContent.itemDrill), heldItem, true) || OreDictionary.itemMatches(new ItemStack(IEContent.itemRailgun), heldItem, true))
					twohanded = ArmPose.BLOCK;
				if(twohanded!=null && event.getEntity().getHeldItem(hand==EnumHand.MAIN_HAND?EnumHand.OFF_HAND:EnumHand.MAIN_HAND)==null)
				{
					ModelBase model = event.getRenderer().getMainModel();
					if(model instanceof ModelBiped)
					{
						if(hand==EnumHand.MAIN_HAND)
							((ModelBiped) model).leftArmPose = twohanded;
						else
							((ModelBiped) model).rightArmPose = twohanded;
					}
				}
			}
		}
	}
	@SubscribeEvent()
	public void onRenderLivingPost(RenderLivingEvent.Post event)
	{
		if(event.getEntity().getEntityData().hasKey("headshot"))
		{
			ModelBase model = event.getRenderer().mainModel;
			if(model instanceof ModelBiped)
				((ModelBiped)model).bipedHead.showModel=true;
			else if(model instanceof ModelVillager)
				((ModelVillager)model).villagerHead.showModel=true;
		}
	}

	//====================================================================
	//This stuff is necessary to work around a rendering issue with WAILA.
	//====================================================================

	boolean blendOn;
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onRenderTickLowest(TickEvent.RenderTickEvent ev)
	{
		if (ev.phase!=Phase.START)
			return;
		if (blendOn)
			GlStateManager.enableBlend();
		else
			GlStateManager.disableBlend();
	}
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onRenderTickHighest(TickEvent.RenderTickEvent ev)
	{
		if (ev.phase!=Phase.START)
			return;
		blendOn = GL11.glGetBoolean(GL11.GL_BLEND);
	}
}
