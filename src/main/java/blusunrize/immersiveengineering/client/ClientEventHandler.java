/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.energy.wires.IWireCoil;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.client.fx.ParticleFractal;
import blusunrize.immersiveengineering.client.gui.GuiBlastFurnace;
import blusunrize.immersiveengineering.client.gui.GuiRevolver;
import blusunrize.immersiveengineering.client.gui.GuiToolbox;
import blusunrize.immersiveengineering.client.render.TileRenderAutoWorkbench;
import blusunrize.immersiveengineering.client.render.TileRenderAutoWorkbench.BlueprintLines;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityTurntable;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.network.MessageChemthrowerSwitch;
import blusunrize.immersiveengineering.common.util.network.MessageMagnetEquip;
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
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientEventHandler implements IResourceManagerReloadListener
{
	private boolean shieldToggleButton = false;
	private int shieldToggleTimer = 0;
	private static final String[] BULLET_TOOLTIP = {"\u00A0\u00A0IE\u00A0", "\u00A0\u00A0AMMO\u00A0", "\u00A0\u00A0HERE\u00A0", "\u00A0\u00A0--\u00A0"};

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{
		TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
		for(int i = 0; i < ClientUtils.destroyBlockIcons.length; i++)
			ClientUtils.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_"+i);

		ImmersiveEngineering.proxy.clearRenderCaches();
	}

	public static Set<Connection> skyhookGrabableConnections = new HashSet<>();
	public static final Map<Connection, Pair<BlockPos, AtomicInteger>> FAILED_CONNECTIONS = new HashMap<>();

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side.isClient()&&event.player!=null&&event.player==ClientUtils.mc().getRenderViewEntity())
		{
			//if(event.phase==Phase.START)
			//{
			//	skyhookGrabableConnections.clear();
			//	EntityPlayer player = event.player;
			//	ItemStack stack = player.getActiveItemStack();
			//	if(!stack.isEmpty()&&stack.getItem() instanceof ItemSkyhook)
			//	{
			//		Connection line = ApiUtils.getTargetConnection(player.getEntityWorld(), player, null, 0);
			//		if(line!=null)
			//			skyhookGrabableConnections.add(line);
			//	}
			//}

			if(event.phase==Phase.END)
			{
				if(this.shieldToggleTimer > 0)
					this.shieldToggleTimer--;
				if(ClientProxy.keybind_magnetEquip.isKeyDown()&&!this.shieldToggleButton)
					if(this.shieldToggleTimer <= 0)
						this.shieldToggleTimer = 7;
					else
					{
						EntityPlayer player = event.player;
						ItemStack held = player.getHeldItem(EnumHand.OFF_HAND);
						if(!held.isEmpty()&&held.getItem() instanceof ItemIEShield)
						{
							if(((ItemIEShield)held.getItem()).getUpgrades(held).getBoolean("magnet")&&((ItemIEShield)held.getItem()).getUpgrades(held).hasKey("prevSlot"))
								ImmersiveEngineering.packetHandler.sendToServer(new MessageMagnetEquip(-1));
						}
						else
						{
							for(int i = 0; i < player.inventory.mainInventory.size(); i++)
							{
								ItemStack s = player.inventory.mainInventory.get(i);
								if(!s.isEmpty()&&s.getItem() instanceof ItemIEShield&&((ItemIEShield)s.getItem()).getUpgrades(s).getBoolean("magnet"))
									ImmersiveEngineering.packetHandler.sendToServer(new MessageMagnetEquip(i));
							}
						}
					}
				if(this.shieldToggleButton!=ClientUtils.mc().gameSettings.keyBindBack.isKeyDown())
					this.shieldToggleButton = ClientUtils.mc().gameSettings.keyBindBack.isKeyDown();


				if(ClientProxy.keybind_chemthrowerSwitch.isPressed())
				{
					ItemStack held = event.player.getHeldItem(EnumHand.MAIN_HAND);
					if(held.getItem() instanceof ItemChemthrower&&((ItemChemthrower)held.getItem()).getUpgrades(held).getBoolean("multitank"))
						ImmersiveEngineering.packetHandler.sendToServer(new MessageChemthrowerSwitch(true));
				}
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
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		FAILED_CONNECTIONS.entrySet().removeIf(entry -> entry.getValue().getValue().decrementAndGet() <= 0);
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if(event.getItemStack()==null||event.getItemStack().isEmpty())
			return;
		if(event.getItemStack().hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
		{
			ShaderWrapper wrapper = event.getItemStack().getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			ItemStack shader = wrapper!=null?wrapper.getShaderItem(): null;
			if(!shader.isEmpty())
				event.getToolTip().add(TextFormatting.DARK_GRAY+shader.getDisplayName());
		}
		if(ItemNBTHelper.hasKey(event.getItemStack(), Lib.NBT_Earmuffs))
		{
			ItemStack earmuffs = ItemNBTHelper.getItemStack(event.getItemStack(), Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty())
				event.getToolTip().add(TextFormatting.GRAY+earmuffs.getDisplayName());
		}
		if(ItemNBTHelper.hasKey(event.getItemStack(), Lib.NBT_Powerpack))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(event.getItemStack(), Lib.NBT_Powerpack);
			if(!powerpack.isEmpty())
			{
				event.getToolTip().add(TextFormatting.GRAY+powerpack.getDisplayName());
				event.getToolTip().add(TextFormatting.GRAY.toString()+EnergyHelper.getEnergyStored(powerpack)+"/"+EnergyHelper.getMaxEnergyStored(powerpack)+" IF");
			}
		}
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT
				&&ClientUtils.mc().currentScreen!=null
				&&ClientUtils.mc().currentScreen instanceof GuiBlastFurnace
				&&BlastFurnaceRecipe.isValidBlastFuel(event.getItemStack()))
			event.getToolTip().add(TextFormatting.GRAY+I18n.format("desc.immersiveengineering.info.blastFuelTime", BlastFurnaceRecipe.getBlastFuelTime(event.getItemStack())));
		if(IEConfig.oreTooltips&&event.getFlags().isAdvanced())
		{
			for(int oid : OreDictionary.getOreIDs(event.getItemStack()))
				event.getToolTip().add(TextFormatting.GRAY+OreDictionary.getOreName(oid));
//			FluidStack fs = FluidUtil.getFluidContained(event.getItemStack());
//			if(fs!=null && fs.getFluid()!=null)
//				event.getToolTip().add("Fluid: "+ FluidRegistry.getFluidName(fs));
		}

		if(event.getItemStack().getItem() instanceof IBulletContainer)
			for(String s : BULLET_TOOLTIP)
				event.getToolTip().add(s);
	}

	@SubscribeEvent()
	public void onRenderTooltip(RenderTooltipEvent.PostText event)
	{
		ItemStack stack = event.getStack();
		if(stack.getItem() instanceof IBulletContainer)
		{
			NonNullList<ItemStack> bullets = ((IBulletContainer)stack.getItem()).getBullets(stack, true);
			if(bullets!=null)
			{
				int bulletAmount = ((IBulletContainer)stack.getItem()).getBulletCount(stack);
				int line = event.getLines().size()-Utils.findSequenceInList(event.getLines(), BULLET_TOOLTIP, (s, s2) -> s.equals(s2.substring(2)));

				int currentX = event.getX();
				int currentY = line > 0?event.getY()+(event.getHeight()+1-line*10): event.getY()-42;

				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.enableRescaleNormal();
				GlStateManager.translate(currentX, currentY, 700);
				GlStateManager.scale(.5f, .5f, 1);

				GuiRevolver.drawExternalGUI(bullets, bulletAmount);

				GlStateManager.disableRescaleNormal();
				GlStateManager.popMatrix();
			}
		}
	}

	@SubscribeEvent
	public void onPlaySound(PlaySoundEvent event)
	{
		if(event.getSound()==null||event.getSound().getCategory()==null)
			return;
		if(!ItemEarmuffs.affectedSoundCategories.contains(event.getSound().getCategory().getName()))
			return;
		if(ClientUtils.mc().player!=null&&!ClientUtils.mc().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
		{
			ItemStack earmuffs = ClientUtils.mc().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			if(ItemNBTHelper.hasKey(earmuffs, Lib.NBT_Earmuffs))
				earmuffs = ItemNBTHelper.getItemStack(earmuffs, Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty()&&IEContent.itemEarmuffs.equals(earmuffs.getItem())&&!ItemNBTHelper.getBoolean(earmuffs, "IE:Earmuffs:Cat_"+event.getSound().getCategory().getName()))
			{
				for(String blacklist : IEConfig.Tools.earDefenders_SoundBlacklist)
					if(blacklist!=null&&blacklist.equalsIgnoreCase(event.getSound().getSoundLocation().toString()))
						return;
				if(event.getSound() instanceof ITickableSound)
					event.setResultSound(new IEMuffledTickableSound((ITickableSound)event.getSound(), ItemEarmuffs.getVolumeMod(earmuffs)));
				else
					event.setResultSound(new IEMuffledSound(event.getSound(), ItemEarmuffs.getVolumeMod(earmuffs)));

				if(event.getSound() instanceof PositionedSoundRecord)
				{
					BlockPos pos = new BlockPos(event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF());
					if(ClientUtils.mc().renderGlobal.mapSoundPositions.containsKey(pos))
						ClientUtils.mc().renderGlobal.mapSoundPositions.put(pos, event.getResultSound());
				}
			}
		}
	}

	private void renderObstructingBlocks(BufferBuilder bb, Tessellator tes, double dx, double dy, double dz)
	{
		bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		for(Map.Entry<Connection, Pair<BlockPos, AtomicInteger>> entry : FAILED_CONNECTIONS.entrySet())
		{
			BlockPos obstruction = entry.getValue().getKey();
			bb.setTranslation(obstruction.getX()-dx,
					obstruction.getY()-dy,
					obstruction.getZ()-dz);
			final double eps = 1e-3;
			ClientUtils.renderBox(bb, -eps, -eps, -eps, 1+eps, 1+eps, 1+eps);
		}
		bb.setTranslation(0, 0, 0);
		tes.draw();
	}

	/*
	static boolean connectionsRendered = false;
	public static void renderAllIEConnections(float partial)
	{
		if(connectionsRendered)
			return;
		GlStateManager.pushMatrix();

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
				//				int lb = tile.getworld().getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord, tile.zCoord, 0);
				//				int lb_j = lb % 65536;
				//				int lb_k = lb / 65536;
				//				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lb_j / 1.0F, (float)lb_k / 1.0F);


				Tessellator.instance.setTranslation(tile.xCoord-dx, tile.yCoord-dy, tile.zCoord-dz);
				//				GlStateManager.translate((tile.xCoord+.5-dx), (tile.yCoord+.5-dy), (tile.zCoord+.5-dz));
				ClientUtils.renderAttachedConnections((TileEntity)tile);
				//				GlStateManager.translate(-(tile.xCoord+.5-dx), -(tile.yCoord+.5-dy), -(tile.zCoord+.5-dz));

			}

		Iterator<ImmersiveNetHandler.Connection> it = skyhookGrabableConnections.iterator();
		World world = viewer.world;
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

		GlStateManager.popMatrix();
		connectionsRendered = true;
	}
	 */

	@SubscribeEvent
	public void onRenderItemFrame(RenderItemInFrameEvent event)
	{
		if(!event.getItem().isEmpty()&&event.getItem().getItem() instanceof ItemEngineersBlueprint)
		{
			double playerDistanceSq = ClientUtils.mc().player.getDistanceSq(event.getEntityItemFrame().getPosition());

			if(playerDistanceSq < 1000)
			{
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(event.getItem(), "blueprint"));
				if(recipes!=null&&recipes.length > 0)
				{
					int i = event.getEntityItemFrame().getRotation();
					BlueprintCraftingRecipe recipe = recipes[i%recipes.length];
					BlueprintLines blueprint = recipe==null?null: TileRenderAutoWorkbench.getBlueprintDrawable(recipe, event.getEntityItemFrame().getEntityWorld());
					if(blueprint!=null)
					{
						GlStateManager.rotate(-i*45.0F, 0.0F, 0.0F, 1.0F);
						ClientUtils.bindTexture("immersiveengineering:textures/models/blueprint_frame.png");
						GlStateManager.translate(-.5, .5, -.001);
						ClientUtils.drawTexturedRect(.125f, -.875f, .75f, .75f, 1d, 0d, 1d, 0d);
						//Width depends on distance
						float lineWidth = playerDistanceSq < 3?3: playerDistanceSq < 25?2: playerDistanceSq < 40?1: .5f;
						GlStateManager.translate(.75, -.25, -.002);
						GlStateManager.disableCull();
						GlStateManager.disableTexture2D();
						GlStateManager.enableBlend();
						float scale = .0375f/(blueprint.getTextureScale()/16f);
						GlStateManager.scale(-scale, -scale, scale);
						GlStateManager.color(1, 1, 1, 1);

						blueprint.draw(lineWidth);

						GlStateManager.scale(1/scale, -1/scale, 1/scale);
						GlStateManager.enableAlpha();
						GlStateManager.enableTexture2D();
						GlStateManager.enableCull();

						event.setCanceled(true);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event)
	{
		if(ZoomHandler.isZooming&&event.getType()==RenderGameOverlayEvent.ElementType.CROSSHAIRS)
		{
			event.setCanceled(true);
			if(ZoomHandler.isZooming)
			{
				ClientUtils.bindTexture("immersiveengineering:textures/gui/scope.png");
				int width = event.getResolution().getScaledWidth();
				int height = event.getResolution().getScaledHeight();
				int resMin = Math.min(width, height);
				float offsetX = (width-resMin)/2f;
				float offsetY = (height-resMin)/2f;

				if(resMin==width)
				{
					ClientUtils.drawColouredRect(0, 0, width, (int)offsetY+1, 0xff000000);
					ClientUtils.drawColouredRect(0, (int)offsetY+resMin, width, (int)offsetY+1, 0xff000000);
				}
				else
				{
					ClientUtils.drawColouredRect(0, 0, (int)offsetX+1, height, 0xff000000);
					ClientUtils.drawColouredRect((int)offsetX+resMin, 0, (int)offsetX+1, height, 0xff000000);
				}
				GlStateManager.enableBlend();
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);

				GlStateManager.translate(offsetX, offsetY, 0);
				ClientUtils.drawTexturedRect(0, 0, resMin, resMin, 0f, 1f, 0f, 1f);

				ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
				ClientUtils.drawTexturedRect(218/256f*resMin, 64/256f*resMin, 24/256f*resMin, 128/256f*resMin, 64/256f, 88/256f, 96/256f, 224/256f);
				ItemStack equipped = ClientUtils.mc().player.getHeldItem(EnumHand.MAIN_HAND);
				if(!equipped.isEmpty()&&equipped.getItem() instanceof IZoomTool)
				{
					IZoomTool tool = (IZoomTool)equipped.getItem();
					float[] steps = tool.getZoomSteps(equipped, ClientUtils.mc().player);
					if(steps!=null&&steps.length > 1)
					{
						int curStep = -1;
						float dist = 0;

						float totalOffset = 0;
						float stepLength = 118/(float)steps.length;
						float stepOffset = (stepLength-7)/2f;
						GlStateManager.translate(223/256f*resMin, 64/256f*resMin, 0);
						GlStateManager.translate(0, (5+stepOffset)/256*resMin, 0);
						for(int i = 0; i < steps.length; i++)
						{
							ClientUtils.drawTexturedRect(0, 0, 8/256f*resMin, 7/256f*resMin, 88/256f, 96/256f, 96/256f, 103/256f);
							GlStateManager.translate(0, stepLength/256*resMin, 0);
							totalOffset += stepLength;

							if(curStep==-1||Math.abs(steps[i]-ZoomHandler.fovZoom) < dist)
							{
								curStep = i;
								dist = Math.abs(steps[i]-ZoomHandler.fovZoom);
							}
						}
						GlStateManager.translate(0, -totalOffset/256*resMin, 0);

						if(curStep >= 0&&curStep < steps.length)
						{
							GlStateManager.translate(6/256f*resMin, curStep*stepLength/256*resMin, 0);
							ClientUtils.drawTexturedRect(0, 0, 8/256f*resMin, 7/256f*resMin, 88/256f, 98/256f, 103/256f, 110/256f);
							ClientUtils.font().drawString((1/steps[curStep])+"x", (int)(16/256f*resMin), 0, 0xffffff);
							GlStateManager.translate(-6/256f*resMin, -curStep*stepLength/256*resMin, 0);
						}
						GlStateManager.translate(0, -((5+stepOffset)/256*resMin), 0);
						GlStateManager.translate(-223/256f*resMin, -64/256f*resMin, 0);
					}
				}

				GlStateManager.translate(-offsetX, -offsetY, 0);
			}
		}
	}

	@SubscribeEvent()
	public void onRenderOverlayPost(RenderGameOverlayEvent.Post event)
	{
		if(ClientUtils.mc().player!=null&&event.getType()==RenderGameOverlayEvent.ElementType.TEXT)
		{
			EntityPlayer player = ClientUtils.mc().player;

			for(EnumHand hand : EnumHand.values())
				if(!player.getHeldItem(hand).isEmpty())
				{
					ItemStack equipped = player.getHeldItem(hand);
					if(OreDictionary.itemMatches(new ItemStack(IEContent.itemTool, 1, 2), equipped, false)||equipped.getItem() instanceof IWireCoil)
					{
						if(ItemNBTHelper.hasKey(equipped, "linkingPos"))
						{
							int[] link = ItemNBTHelper.getIntArray(equipped, "linkingPos");
							if(link!=null&&link.length > 3)
							{
								String s = I18n.format(Lib.DESC_INFO+"attachedTo", link[1], link[2], link[3]);
								int col = WireType.ELECTRUM.getColour(null);
								if(equipped.getItem() instanceof IWireCoil)
								{
									RayTraceResult rtr = ClientUtils.mc().objectMouseOver;
									double d = rtr!=null&&rtr.getBlockPos()!=null?rtr.getBlockPos().distanceSq(link[1], link[2], link[3]): player.getDistanceSq(link[1], link[2], link[3]);
									int max = ((IWireCoil)equipped.getItem()).getWireType(equipped).getMaxLength();
									if(d > max*max)
										col = 0xdd3333;
								}
								ClientUtils.font().drawString(s, event.getResolution().getScaledWidth()/2-ClientUtils.font().getStringWidth(s)/2, event.getResolution().getScaledHeight()-GuiIngameForge.left_height-20, col, true);
							}
						}
					}
					else if(OreDictionary.itemMatches(equipped, new ItemStack(IEContent.itemFluorescentTube), false))
					{
						String s = I18n.format("desc.immersiveengineering.info.colour", "#"+ItemFluorescentTube.hexColorString(equipped));
						ClientUtils.font().drawString(s, event.getResolution().getScaledWidth()/2-ClientUtils.font().getStringWidth(s)/2,
								event.getResolution().getScaledHeight()-GuiIngameForge.left_height-20, ItemFluorescentTube.getRGBInt(equipped, 1), true);
					}
					else if(equipped.getItem() instanceof ItemRevolver||equipped.getItem() instanceof ItemSpeedloader)
					{
						NonNullList<ItemStack> bullets = ((IBulletContainer)equipped.getItem()).getBullets(equipped, true);
						if(bullets!=null)
						{
							int bulletAmount = ((IBulletContainer)equipped.getItem()).getBulletCount(equipped);
							EnumHandSide side = hand==EnumHand.MAIN_HAND?player.getPrimaryHand(): player.getPrimaryHand().opposite();
							boolean right = side==EnumHandSide.RIGHT;
							float dx = right?event.getResolution().getScaledWidth()-32-48: 48;
							float dy = event.getResolution().getScaledHeight()-64;
							GlStateManager.pushMatrix();
							GlStateManager.enableRescaleNormal();
							GlStateManager.enableBlend();
							GlStateManager.translate(dx, dy, 0);
							GlStateManager.scale(.5f, .5f, 1);

							GuiRevolver.drawExternalGUI(bullets, bulletAmount);

							if(equipped.getItem() instanceof ItemRevolver)
							{
								int cd = ((ItemRevolver)equipped.getItem()).getShootCooldown(equipped);
								float cdMax = ((ItemRevolver)equipped.getItem()).getMaxShootCooldown(equipped);
								float cooldown = 1-cd/cdMax;
								if(cooldown > 0)
								{
									GlStateManager.scale(2, 2, 1);
									GlStateManager.translate(-dx, -dy, 0);
									GlStateManager.translate(event.getResolution().getScaledWidth()/2+(right?1: -6), event.getResolution().getScaledHeight()/2-7, 0);

									float h1 = cooldown > .33?.5f: cooldown*1.5f;
									float h2 = cooldown;
									float x2 = cooldown < .75?1: 4*(1-cooldown);

									float uMin = (88+(right?0: 7*x2))/256f;
									float uMax = (88+(right?7*x2: 0))/256f;
									float vMin1 = (112+(right?h1: h2)*15)/256f;
									float vMin2 = (112+(right?h2: h1)*15)/256f;

									ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
									Tessellator tessellator = Tessellator.getInstance();
									BufferBuilder worldrenderer = tessellator.getBuffer();
									worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
									worldrenderer.pos((right?0: 1-x2)*7, 15, 0).tex(uMin, 127/256f).endVertex();
									worldrenderer.pos((right?x2: 1)*7, 15, 0).tex(uMax, 127/256f).endVertex();
									worldrenderer.pos((right?x2: 1)*7, (right?h2: h1)*15, 0).tex(uMax, vMin2).endVertex();
									worldrenderer.pos((right?0: 1-x2)*7, (right?h1: h2)*15, 0).tex(uMin, vMin1).endVertex();
									tessellator.draw();
								}
							}
							RenderHelper.disableStandardItemLighting();
							GlStateManager.disableBlend();
							GlStateManager.popMatrix();
						}

					}
					else if(equipped.getItem() instanceof ItemRailgun)
					{
						int duration = 72000-(player.isHandActive()&&player.getActiveHand()==hand?player.getItemInUseCount(): 0);
						int chargeTime = ((ItemRailgun)equipped.getItem()).getChargeTime(equipped);
						int chargeLevel = duration < 72000?Math.min(99, (int)(duration/(float)chargeTime*100)): 0;
						float scale = 2f;
						GlStateManager.pushMatrix();
						GlStateManager.translate(event.getResolution().getScaledWidth()-80, event.getResolution().getScaledHeight()-30, 0);
						GlStateManager.scale(scale, scale, 1);
						ClientProxy.nixieFont.drawString((chargeLevel < 10?"0": "")+chargeLevel, 0, 0, Lib.colour_nixieTubeText, false);
						GlStateManager.scale(1/scale, 1/scale, 1);
						GlStateManager.popMatrix();
					}
					else if((equipped.getItem() instanceof ItemDrill&&equipped.getItemDamage()==0)
							||equipped.getItem() instanceof ItemChemthrower)
					{
						boolean drill = equipped.getItem() instanceof ItemDrill;
						ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
						GlStateManager.color(1, 1, 1, 1);
						float dx = event.getResolution().getScaledWidth()-16;
						float dy = event.getResolution().getScaledHeight();
						GlStateManager.pushMatrix();
						GlStateManager.translate(dx, dy, 0);
						int w = 31;
						int h = 62;
						double uMin = 179/256f;
						double uMax = 210/256f;
						double vMin = 9/256f;
						double vMax = 71/256f;
						ClientUtils.drawTexturedRect(-24, -68, w, h, uMin, uMax, vMin, vMax);

						GlStateManager.translate(-23, -37, 0);
						IFluidHandler handler = FluidUtil.getFluidHandler(equipped);
						int capacity = -1;
						if(handler!=null)
						{
							IFluidTankProperties[] props = handler.getTankProperties();
							if(props!=null&&props.length > 0)
								capacity = props[0].getCapacity();
						}
						if(capacity > -1)
						{
							FluidStack fuel = FluidUtil.getFluidContained(equipped);
							int amount = fuel!=null?fuel.amount: 0;
							if(!drill&&player.isHandActive()&&player.getActiveHand()==hand)
							{
								int use = player.getItemInUseMaxCount();
								amount -= use*IEConfig.Tools.chemthrower_consumption;
							}
							float cap = (float)capacity;
							float angle = 83-(166*amount/cap);
							GlStateManager.rotate(angle, 0, 0, 1);
							ClientUtils.drawTexturedRect(6, -2, 24, 4, 91/256f, 123/256f, 80/256f, 87/256f);
							GlStateManager.rotate(-angle, 0, 0, 1);
							//					for(int i=0; i<=8; i++)
							//					{
							//						float angle = 83-(166/8f)*i;
							//						GL11.glRotatef(angle, 0, 0, 1);
							//						ClientUtils.drawTexturedRect(6,-2, 24,4, 91/256f,123/256f, 80/96f,87/96f);
							//						GL11.glRotatef(-angle, 0, 0, 1);
							//					}
							GlStateManager.translate(23, 37, 0);
							if(drill)
							{
								ClientUtils.drawTexturedRect(-54, -73, 66, 72, 108/256f, 174/256f, 4/256f, 76/256f);
								RenderItem ir = ClientUtils.mc().getRenderItem();
								ItemStack head = ((ItemDrill)equipped.getItem()).getHead(equipped);
								if(!head.isEmpty())
								{
									ir.renderItemIntoGUI(head, -51, -45);
									ir.renderItemOverlayIntoGUI(head.getItem().getFontRenderer(head), head, -51, -45, null);
									RenderHelper.disableStandardItemLighting();
								}
							}
							else
							{
								ClientUtils.drawTexturedRect(-41, -73, 53, 72, 8/256f, 61/256f, 4/256f, 76/256f);
								boolean ignite = ItemNBTHelper.getBoolean(equipped, "ignite");
								ClientUtils.drawTexturedRect(-32, -43, 12, 12, 66/256f, 78/256f, (ignite?21: 9)/256f, (ignite?33: 21)/256f);

								ClientUtils.drawTexturedRect(-100, -20, 64, 16, 0/256f, 64/256f, 76/256f, 92/256f);
								if(fuel!=null)
								{
									String name = ClientUtils.font().trimStringToWidth(fuel.getLocalizedName(), 50).trim();
									ClientUtils.font().drawString(name, -68-ClientUtils.font().getStringWidth(name)/2, -15, 0);
								}
							}
						}
						GlStateManager.popMatrix();
					}
					else if(equipped.getItem() instanceof ItemIEShield)
					{
						NBTTagCompound upgrades = ((ItemIEShield)equipped.getItem()).getUpgrades(equipped);
						if(!upgrades.isEmpty())
						{
							ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
							GlStateManager.color(1, 1, 1, 1);
							boolean boundLeft = (player.getPrimaryHand()==EnumHandSide.RIGHT)==(hand==EnumHand.OFF_HAND);
							float dx = boundLeft?16: (event.getResolution().getScaledWidth()-16-64);
							float dy = event.getResolution().getScaledHeight();
							GlStateManager.pushMatrix();
							GlStateManager.enableBlend();
							GlStateManager.translate(dx, dy, 0);
							ClientUtils.drawTexturedRect(0, -22, 64, 22, 0, 64/256f, 176/256f, 198/256f);

							if(upgrades.getBoolean("flash"))
							{
								ClientUtils.drawTexturedRect(11, -38, 16, 16, 11/256f, 27/256f, 160/256f, 176/256f);
								if(upgrades.hasKey("flash_cooldown"))
								{
									float h = upgrades.getInteger("flash_cooldown")/40f*16;
									ClientUtils.drawTexturedRect(11, -22-h, 16, h, 11/256f, 27/256f, (214-h)/256f, 214/256f);
								}
							}
							if(upgrades.getBoolean("shock"))
							{
								ClientUtils.drawTexturedRect(40, -38, 12, 16, 40/256f, 52/256f, 160/256f, 176/256f);
								if(upgrades.hasKey("shock_cooldown"))
								{
									float h = upgrades.getInteger("shock_cooldown")/40f*16;
									ClientUtils.drawTexturedRect(40, -22-h, 12, h, 40/256f, 52/256f, (214-h)/256f, 214/256f);
								}
							}
							GlStateManager.disableBlend();
							GlStateManager.popMatrix();
						}
					}
					//				else if(equipped.getItem() instanceof ItemRailgun)
					//				{
					//					float dx = event.getResolution().getScaledWidth()-32-48;
					//					float dy = event.getResolution().getScaledHeight()-40;
					//					ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
					//					GlStateManager.color(1, 1, 1, 1);
					//					GlStateManager.pushMatrix();
					//					GL11.glEnable(GL11.GL_BLEND);
					//					GlStateManager.translate(dx, dy, 0);
					//
					//					int duration = player.getItemInUseDuration();
					//					int chargeTime = ((ItemRailgun)equipped.getItem()).getChargeTime(equipped);
					//					int chargeLevel = Math.min(99, (int)(duration/(float)chargeTime*100));
					//					//					ClientUtils.drawTexturedRect(0,0, 64,32, 0/256f,64/256f, 96/256f,128/256f);
					//
					//					GlStateManager.scale(1.5f,1.5f,1.5f);
					//					int col = Config.getBoolean("nixietubeFont")?Lib.colour_nixieTubeText:0xffffff;
					//					ClientProxy.nixieFont.setDrawTubeFlag(false);
					//					//					ClientProxy.nixieFont.drawString((chargeLevel<10?"0"+chargeLevel:""+chargeLevel), 19,3, col);
					//					ClientProxy.nixieFont.setDrawTubeFlag(true);
					//
					//					GlStateManager.popMatrix();
					//				}

					RayTraceResult mop = ClientUtils.mc().objectMouseOver;
					if(mop!=null&&mop.getBlockPos()!=null)
					{
						TileEntity tileEntity = player.world.getTileEntity(mop.getBlockPos());
						if(OreDictionary.itemMatches(new ItemStack(IEContent.itemTool, 1, 2), equipped, true))
						{
							int col = IEConfig.nixietubeFont?Lib.colour_nixieTubeText: 0xffffff;
							String[] text = null;
							if(tileEntity instanceof IFluxReceiver)
							{
								int maxStorage = ((IFluxReceiver)tileEntity).getMaxEnergyStored(mop.sideHit);
								int storage = ((IFluxReceiver)tileEntity).getEnergyStored(mop.sideHit);
								if(maxStorage > 0)
									text = I18n.format(Lib.DESC_INFO+"energyStored", "<br>"+Utils.toScientificNotation(storage, "0##", 100000)+" / "+Utils.toScientificNotation(maxStorage, "0##", 100000)).split("<br>");
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
								if(maxStorage > 0)
									text = I18n.format(Lib.DESC_INFO+"energyStored", "<br>"+Utils.toScientificNotation(storage, "0##", 100000)+" / "+Utils.toScientificNotation(maxStorage, "0##", 100000)).split("<br>");
							}
							if(text!=null)
							{
								if(player.world.getTotalWorldTime()%20==0)
								{
									ImmersiveEngineering.packetHandler.sendToServer(new MessageRequestBlockUpdate(mop.getBlockPos()));
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
				boolean hammer = !player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()&&Utils.isHammer(player.getHeldItem(EnumHand.MAIN_HAND));
				RayTraceResult mop = ClientUtils.mc().objectMouseOver;
				if(mop!=null&&mop.getBlockPos()!=null)
				{
					TileEntity tileEntity = player.world.getTileEntity(mop.getBlockPos());
					if(tileEntity instanceof IBlockOverlayText)
					{
						IBlockOverlayText overlayBlock = (IBlockOverlayText)tileEntity;
						String[] text = overlayBlock.getOverlayText(ClientUtils.mc().player, mop, hammer);
						boolean useNixie = overlayBlock.useNixieFont(ClientUtils.mc().player, mop);
						if(text!=null&&text.length > 0)
						{
							FontRenderer font = useNixie?ClientProxy.nixieFontOptional: ClientUtils.font();
							int col = (useNixie&&IEConfig.nixietubeFont)?Lib.colour_nixieTubeText: 0xffffff;
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
	public void onFogUpdate(EntityViewRenderEvent.RenderFogEvent event)
	{
		if(event.getEntity() instanceof EntityLivingBase&&((EntityLivingBase)event.getEntity()).getActivePotionEffect(IEPotions.flashed)!=null)
		{
			PotionEffect effect = ((EntityLivingBase)event.getEntity()).getActivePotionEffect(IEPotions.flashed);
			int timeLeft = effect.getDuration();
			float saturation = 1-timeLeft/(float)(80+40*effect.getAmplifier());//Total Time =  4s + 2s per amplifier

			float f1 = -2.5f+15.0F*saturation;
			if(timeLeft < 20)
				f1 += (event.getFarPlaneDistance()/4)*(1-timeLeft/20f);

			GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
			GlStateManager.setFogStart(f1*0.25F);
			GlStateManager.setFogEnd(f1);
			GlStateManager.setFogDensity(.125f);

			if(GLContext.getCapabilities().GL_NV_fog_distance)
				GlStateManager.glFogi(34138, 34139);
		}
	}

	@SubscribeEvent()
	public void onFogColourUpdate(EntityViewRenderEvent.FogColors event)
	{
		if(event.getEntity() instanceof EntityLivingBase&&((EntityLivingBase)event.getEntity()).getActivePotionEffect(IEPotions.flashed)!=null)
		{
			event.setRed(1);
			event.setGreen(1);
			event.setBlue(1);
		}
	}

	@SubscribeEvent()
	public void onFOVUpdate(FOVUpdateEvent event)
	{
		EntityPlayer player = ClientUtils.mc().player;
		if(!player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()&&player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IZoomTool)
		{
			if(player.isSneaking()&&player.onGround)
			{
				ItemStack equipped = player.getHeldItem(EnumHand.MAIN_HAND);
				IZoomTool tool = (IZoomTool)equipped.getItem();
				if(tool.canZoom(equipped, player))
				{
					if(!ZoomHandler.isZooming)
					{
						float[] steps = tool.getZoomSteps(equipped, player);
						if(steps!=null&&steps.length > 0)
						{
							int curStep = -1;
							float dist = 0;
							for(int i = 0; i < steps.length; i++)
								if(curStep==-1||Math.abs(steps[i]-ZoomHandler.fovZoom) < dist)
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
		if(player.getActivePotionEffect(IEPotions.concreteFeet)!=null)
			event.setNewfov(1);

	}

	@SubscribeEvent
	public void onMouseEvent(MouseEvent event)
	{
		if(event.getDwheel()!=0)
		{
			EntityPlayer player = ClientUtils.mc().player;
			if(!player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()&&player.isSneaking())
			{
				ItemStack equipped = player.getHeldItem(EnumHand.MAIN_HAND);

				if(equipped.getItem() instanceof IZoomTool)
				{
					IZoomTool tool = (IZoomTool)equipped.getItem();
					if(tool.canZoom(equipped, player))
					{
						float[] steps = tool.getZoomSteps(equipped, player);
						if(steps!=null&&steps.length > 0)
						{
							int curStep = -1;
							float dist = 0;
							for(int i = 0; i < steps.length; i++)
								if(curStep==-1||Math.abs(steps[i]-ZoomHandler.fovZoom) < dist)
								{
									curStep = i;
									dist = Math.abs(steps[i]-ZoomHandler.fovZoom);
								}
							if(curStep!=-1)
							{
								int newStep = curStep+(event.getDwheel() > 0?-1: 1);
								if(newStep >= 0&&newStep < steps.length)
									ZoomHandler.fovZoom = steps[newStep];
								event.setCanceled(true);
							}
						}
					}
				}
				if(Config.IEConfig.Tools.chemthrower_scroll&&equipped.getItem() instanceof ItemChemthrower&&((ItemChemthrower)equipped.getItem()).getUpgrades(equipped).getBoolean("multitank"))
				{
					ImmersiveEngineering.packetHandler.sendToServer(new MessageChemthrowerSwitch(event.getDwheel() < 0));
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onKey(GuiScreenEvent.MouseInputEvent.Pre event)
	{
		//Stopping cpw's inventory sorter till I can get him to make it better
		if(event.getGui() instanceof GuiToolbox&&Mouse.getEventButton()==2)
			event.setCanceled(true);
	}

	@SubscribeEvent()
	public void renderAdditionalBlockBounds(DrawBlockHighlightEvent event)
	{
		if(event.getSubID()==0&&event.getTarget().typeOfHit==Type.BLOCK)
		{
			float f1 = 0.002F;
			double px = -TileEntityRendererDispatcher.staticPlayerX;
			double py = -TileEntityRendererDispatcher.staticPlayerY;
			double pz = -TileEntityRendererDispatcher.staticPlayerZ;
			TileEntity tile = event.getPlayer().world.getTileEntity(event.getTarget().getBlockPos());
			ItemStack stack = event.getPlayer().getHeldItem(EnumHand.MAIN_HAND);
			//			if(event.getPlayer().world.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof IEBlockInterfaces.ICustomBoundingboxes)
			if(tile instanceof IAdvancedSelectionBounds)
			{
				//				IEBlockInterfaces.ICustomBoundingboxes block = (IEBlockInterfaces.ICustomBoundingboxes) event.getPlayer().world.getBlockState(event.getTarget().getBlockPos()).getBlock();
				IAdvancedSelectionBounds iasb = (IAdvancedSelectionBounds)tile;
				List<AxisAlignedBB> boxes = iasb.getAdvancedSelectionBounds();
				if(boxes!=null&&!boxes.isEmpty())
				{
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
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
						RenderGlobal.drawSelectionBoundingBox(overrideBox.grow(f1).offset(px, py, pz), 0, 0, 0, 0.4f);
					else
						for(AxisAlignedBB aabb : additionalBoxes.isEmpty()?boxes: additionalBoxes)
							RenderGlobal.drawSelectionBoundingBox(aabb.grow(f1).offset(px, py, pz), 0, 0, 0, 0.4f);
					GlStateManager.depthMask(true);
					GlStateManager.enableTexture2D();
					GlStateManager.disableBlend();
					event.setCanceled(true);
				}
			}

			if(Utils.isHammer(stack)&&tile instanceof TileEntityTurntable)
			{
				BlockPos pos = event.getTarget().getBlockPos();

				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
				GlStateManager.glLineWidth(2.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder BufferBuilder = tessellator.getBuffer();

				EnumFacing f = ((TileEntityTurntable)tile).getFacing();
				double tx = pos.getX()+.5;
				double ty = pos.getY()+.5;
				double tz = pos.getZ()+.5;
				if(!event.getPlayer().world.isAirBlock(pos.offset(f)))
				{
					tx += f.getXOffset();
					ty += f.getYOffset();
					tz += f.getZOffset();
				}
				BufferBuilder.setTranslation(tx+px, ty+py, tz+pz);

				double angle = -event.getPlayer().ticksExisted%80/40d*Math.PI;
				drawRotationArrows(tessellator, BufferBuilder, f, angle, ((TileEntityTurntable)tile).invert);

				BufferBuilder.setTranslation(0, 0, 0);

				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
			}

			World world = event.getPlayer().world;
			if(!stack.isEmpty()&&IEContent.blockConveyor.equals(Block.getBlockFromItem(stack.getItem()))&&event.getTarget().sideHit.getAxis()==Axis.Y)
			{
				EnumFacing side = event.getTarget().sideHit;
				BlockPos pos = event.getTarget().getBlockPos();
				AxisAlignedBB targetedBB = world.getBlockState(pos).getSelectedBoundingBox(world, pos);
				if(targetedBB!=null)
					targetedBB = targetedBB.offset(-pos.getX(), -pos.getY(), -pos.getZ());
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
				GlStateManager.glLineWidth(2.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder BufferBuilder = tessellator.getBuffer();
				BufferBuilder.setTranslation(pos.getX()+px, pos.getY()+py, pos.getZ()+pz);
				double[][] points = new double[4][];


				if(side.getAxis()==Axis.Y)
				{
					points[0] = new double[]{0-f1, side==EnumFacing.DOWN?((targetedBB!=null?targetedBB.minY: 0)-f1): ((targetedBB!=null?targetedBB.maxY: 1)+f1), 0-f1};
					points[1] = new double[]{1+f1, side==EnumFacing.DOWN?((targetedBB!=null?targetedBB.minY: 0)-f1): ((targetedBB!=null?targetedBB.maxY: 1)+f1), 1+f1};
					points[2] = new double[]{0-f1, side==EnumFacing.DOWN?((targetedBB!=null?targetedBB.minY: 0)-f1): ((targetedBB!=null?targetedBB.maxY: 1)+f1), 1+f1};
					points[3] = new double[]{1+f1, side==EnumFacing.DOWN?((targetedBB!=null?targetedBB.minY: 0)-f1): ((targetedBB!=null?targetedBB.maxY: 1)+f1), 0-f1};
				}
				else if(side.getAxis()==Axis.Z)
				{
					points[0] = new double[]{1+f1, 1+f1, side==EnumFacing.NORTH?((targetedBB!=null?targetedBB.minZ: 0)-f1): ((targetedBB!=null?targetedBB.maxZ: 1)+f1)};
					points[1] = new double[]{0-f1, 0-f1, side==EnumFacing.NORTH?((targetedBB!=null?targetedBB.minZ: 0)-f1): ((targetedBB!=null?targetedBB.maxZ: 1)+f1)};
					points[2] = new double[]{0-f1, 1+f1, side==EnumFacing.NORTH?((targetedBB!=null?targetedBB.minZ: 0)-f1): ((targetedBB!=null?targetedBB.maxZ: 1)+f1)};
					points[3] = new double[]{1+f1, 0-f1, side==EnumFacing.NORTH?((targetedBB!=null?targetedBB.minZ: 0)-f1): ((targetedBB!=null?targetedBB.maxZ: 1)+f1)};
				}
				else
				{
					points[0] = new double[]{side==EnumFacing.WEST?((targetedBB!=null?targetedBB.minX: 0)-f1): ((targetedBB!=null?targetedBB.maxX: 1)+f1), 1+f1, 1+f1};
					points[1] = new double[]{side==EnumFacing.WEST?((targetedBB!=null?targetedBB.minX: 0)-f1): ((targetedBB!=null?targetedBB.maxX: 1)+f1), 0-f1, 0-f1};
					points[2] = new double[]{side==EnumFacing.WEST?((targetedBB!=null?targetedBB.minX: 0)-f1): ((targetedBB!=null?targetedBB.maxX: 1)+f1), 1+f1, 0-f1};
					points[3] = new double[]{side==EnumFacing.WEST?((targetedBB!=null?targetedBB.minX: 0)-f1): ((targetedBB!=null?targetedBB.maxX: 1)+f1), 0-f1, 1+f1};
				}
				BufferBuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
				for(double[] point : points)
					BufferBuilder.pos(point[0], point[1], point[2]).color(0, 0, 0, 0.4F).endVertex();
				tessellator.draw();

				BufferBuilder.begin(2, DefaultVertexFormats.POSITION_COLOR);
				BufferBuilder.pos(points[0][0], points[0][1], points[0][2]).color(0, 0, 0, 0.4F).endVertex();
				BufferBuilder.pos(points[2][0], points[2][1], points[2][2]).color(0, 0, 0, 0.4F).endVertex();
				BufferBuilder.pos(points[1][0], points[1][1], points[1][2]).color(0, 0, 0, 0.4F).endVertex();
				BufferBuilder.pos(points[3][0], points[3][1], points[3][2]).color(0, 0, 0, 0.4F).endVertex();
				tessellator.draw();

				float xFromMid = side.getAxis()==Axis.X?0: (float)event.getTarget().hitVec.x-pos.getX()-.5f;
				float yFromMid = side.getAxis()==Axis.Y?0: (float)event.getTarget().hitVec.y-pos.getY()-.5f;
				float zFromMid = side.getAxis()==Axis.Z?0: (float)event.getTarget().hitVec.z-pos.getZ()-.5f;
				float max = Math.max(Math.abs(yFromMid), Math.max(Math.abs(xFromMid), Math.abs(zFromMid)));
				Vec3d dir = new Vec3d(max==Math.abs(xFromMid)?Math.signum(xFromMid): 0, max==Math.abs(yFromMid)?Math.signum(yFromMid): 0, max==Math.abs(zFromMid)?Math.signum(zFromMid): 0);
				if(dir!=null)
					drawBlockOverlayArrow(tessellator, BufferBuilder, dir, side, targetedBB);
				BufferBuilder.setTranslation(0, 0, 0);

				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
			}

			if(!stack.isEmpty()&&stack.getItem() instanceof ItemDrill&&((ItemDrill)stack.getItem()).isEffective(world.getBlockState(event.getTarget().getBlockPos()).getMaterial()))
			{
				ItemStack head = ((ItemDrill)stack.getItem()).getHead(stack);
				if(!head.isEmpty())
				{
					ImmutableList<BlockPos> blocks = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world, event.getPlayer(), event.getTarget());
					drawAdditionalBlockbreak(event.getContext(), event.getPlayer(), event.getPartialTicks(), blocks);
				}
			}
		}
	}

	private static double[][] rotationArrowCoords = {{.375, 0}, {.5, -.125}, {.4375, -.125}, {.4375, -.25}, {.25, -.4375}, {-.25, -.4375}, {-.4375, -.25}, {-.4375, -.0625}, {-.3125, -.0625}, {-.3125, -.1875}, {-.1875, -.3125}, {.1875, -.3125}, {.3125, -.1875}, {.3125, -.125}, {.25, -.125}};
	private static double[][] rotationArrowQuads = {rotationArrowCoords[7], rotationArrowCoords[8], rotationArrowCoords[6], rotationArrowCoords[9], rotationArrowCoords[5], rotationArrowCoords[10], rotationArrowCoords[4], rotationArrowCoords[11], rotationArrowCoords[3], rotationArrowCoords[12], rotationArrowCoords[2], rotationArrowCoords[13], rotationArrowCoords[1], rotationArrowCoords[14], rotationArrowCoords[0], rotationArrowCoords[0]};

	public static void drawRotationArrows(Tessellator tessellator, BufferBuilder BufferBuilder, EnumFacing facing, double rotation, boolean flip)
	{
		double cos = Math.cos(rotation);
		double sin = Math.sin(rotation);
		BufferBuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
		for(double[] p : rotationArrowCoords)
		{
			double w = (cos*p[0]+sin*p[1]);
			double h = (-sin*p[0]+cos*p[1]);
			double xx = facing.getXOffset() < 0?-(.5+.002): facing.getXOffset() > 0?(.5+.002): (facing.getAxis()==Axis.Y^flip?-1: 1)*facing.getAxisDirection().getOffset()*h;
			double yy = facing.getYOffset() < 0?-(.5+.002): facing.getYOffset() > 0?(.5+.002): w;
			double zz = facing.getZOffset() < 0?-(.5+.002): facing.getZOffset() > 0?(.5+.002): facing.getAxis()==Axis.X?(flip?1: -1)*facing.getAxisDirection().getOffset()*h: w;
			BufferBuilder.pos(xx, yy, zz).color(0, 0, 0, 0.4F).endVertex();
		}
		tessellator.draw();
		BufferBuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
		for(double[] p : rotationArrowCoords)
		{
			double w = (cos*p[0]+sin*p[1]);
			double h = (-sin*p[0]+cos*p[1]);
			double xx = facing.getXOffset() < 0?-(.5+.002): facing.getXOffset() > 0?(.5+.002): (facing.getAxis()==Axis.Y^flip?1: -1)*facing.getAxisDirection().getOffset()*h;
			double yy = facing.getYOffset() < 0?-(.5+.002): facing.getYOffset() > 0?(.5+.002): -w;
			double zz = facing.getZOffset() < 0?-(.5+.002): facing.getZOffset() > 0?(.5+.002): facing.getAxis()==Axis.X?(flip?-1: 1)*facing.getAxisDirection().getOffset()*h: -w;
			BufferBuilder.pos(xx, yy, zz).color(0, 0, 0, 0.4F).endVertex();
		}
		tessellator.draw();

		BufferBuilder.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
		for(double[] p : rotationArrowQuads)
		{
			double w = (cos*p[0]+sin*p[1]);
			double h = (-sin*p[0]+cos*p[1]);
			double xx = facing.getXOffset() < 0?-(.5+.002): facing.getXOffset() > 0?(.5+.002): (facing.getAxis()==Axis.Y^flip?-1: 1)*facing.getAxisDirection().getOffset()*h;
			double yy = facing.getYOffset() < 0?-(.5+.002): facing.getYOffset() > 0?(.5+.002): w;
			double zz = facing.getZOffset() < 0?-(.5+.002): facing.getZOffset() > 0?(.5+.002): facing.getAxis()==Axis.X?(flip?1: -1)*facing.getAxisDirection().getOffset()*h: w;
			BufferBuilder.pos(xx, yy, zz).color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F).endVertex();
		}
		tessellator.draw();
		BufferBuilder.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
		for(double[] p : rotationArrowQuads)
		{
			double w = (cos*p[0]+sin*p[1]);
			double h = (-sin*p[0]+cos*p[1]);
			double xx = facing.getXOffset() < 0?-(.5+.002): facing.getXOffset() > 0?(.5+.002): (facing.getAxis()==Axis.Y^flip?1: -1)*facing.getAxisDirection().getOffset()*h;
			double yy = facing.getYOffset() < 0?-(.5+.002): facing.getYOffset() > 0?(.5+.002): -w;
			double zz = facing.getZOffset() < 0?-(.5+.002): facing.getZOffset() > 0?(.5+.002): facing.getAxis()==Axis.X?(flip?-1: 1)*facing.getAxisDirection().getOffset()*h: -w;
			BufferBuilder.pos(xx, yy, zz).color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F).endVertex();
		}
		tessellator.draw();
	}

	private static float[][] arrowCoords = {{0, .375f}, {.3125f, .0625f}, {.125f, .0625f}, {.125f, -.375f}, {-.125f, -.375f}, {-.125f, .0625f}, {-.3125f, .0625f}};

	public static void drawBlockOverlayArrow(Tessellator tessellator, BufferBuilder BufferBuilder, Vec3d directionVec, EnumFacing side, AxisAlignedBB targetedBB)
	{
		Vec3d[] translatedPositions = new Vec3d[arrowCoords.length];
		Matrix4 mat = new Matrix4();
		Vec3d defaultDir = side.getAxis()==Axis.Y?new Vec3d(0, 0, 1): new Vec3d(0, 1, 0);
		directionVec = directionVec.normalize();
		double angle = Math.acos(defaultDir.dotProduct(directionVec));
		Vec3d axis = defaultDir.crossProduct(directionVec);
		mat.rotate(angle, axis.x, axis.y, axis.z);
		if(side!=null)
		{
			if(side.getAxis()==Axis.Z)
				mat.rotate(Math.PI/2, 1, 0, 0).rotate(Math.PI, 0, 1, 0);
			else if(side.getAxis()==Axis.X)
				mat.rotate(Math.PI/2, 0, 0, 1).rotate(Math.PI/2, 0, 1, 0);
		}
		for(int i = 0; i < translatedPositions.length; i++)
		{
			Vec3d vec = mat.apply(new Vec3d(arrowCoords[i][0], 0, arrowCoords[i][1])).add(.5, .5, .5);
			if(side!=null&&targetedBB!=null)
				vec = new Vec3d(side==EnumFacing.WEST?targetedBB.minX-.002: side==EnumFacing.EAST?targetedBB.maxX+.002: vec.x, side==EnumFacing.DOWN?targetedBB.minY-.002: side==EnumFacing.UP?targetedBB.maxY+.002: vec.y, side==EnumFacing.NORTH?targetedBB.minZ-.002: side==EnumFacing.SOUTH?targetedBB.maxZ+.002: vec.z);
			translatedPositions[i] = vec;
		}

		BufferBuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
		for(Vec3d point : translatedPositions)
			BufferBuilder.pos(point.x, point.y, point.z).color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F).endVertex();
		tessellator.draw();
		BufferBuilder.begin(2, DefaultVertexFormats.POSITION_COLOR);
		for(Vec3d point : translatedPositions)
			BufferBuilder.pos(point.x, point.y, point.z).color(0, 0, 0, 0.4F).endVertex();
		tessellator.draw();
	}

	public static void drawAdditionalBlockbreak(RenderGlobal context, EntityPlayer player, float partialTicks, Collection<BlockPos> blocks)
	{
		for(BlockPos pos : blocks)
			context.drawSelectionBox(player, new RayTraceResult(new Vec3d(0, 0, 0), null, pos), 0, partialTicks);

		PlayerControllerMP controllerMP = ClientUtils.mc().playerController;
		if(controllerMP.isHittingBlock)
			ClientUtils.drawBlockDamageTexture(ClientUtils.tes(), ClientUtils.tes().getBuffer(), player, partialTicks, player.world, blocks);
	}

	private static ItemStack sampleDrill = ItemStack.EMPTY;
	private static ItemStack coreSample = ItemStack.EMPTY;

	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent event)
	{
		EntityPlayer player = ClientUtils.mc().player;
		//Overlay renderer for the sample drill
		int[] chunkBorders = null;
		if(sampleDrill.isEmpty())
			sampleDrill = new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta());
		if(coreSample.isEmpty())
			coreSample = new ItemStack(IEContent.itemCoresample);
		for(EnumHand hand : EnumHand.values())
			if(OreDictionary.itemMatches(sampleDrill, player.getHeldItem(hand), true))
			{
				chunkBorders = new int[]{(int)player.posX >> 4<<4, (int)player.posZ >> 4<<4};
				break;
			}
			else if(OreDictionary.itemMatches(coreSample, player.getHeldItem(hand), true))
			{
				int[] coords = ItemNBTHelper.getIntArray(player.getHeldItem(hand), "coords");
				if(coords[0]==player.getEntityWorld().provider.getDimension())
				{
					chunkBorders = new int[]{coords[1]<<4, coords[2]<<4};
					break;
				}
			}
		if(chunkBorders==null&&ClientUtils.mc().objectMouseOver!=null&&ClientUtils.mc().objectMouseOver.typeOfHit==Type.BLOCK&&ClientUtils.mc().world.getTileEntity(ClientUtils.mc().objectMouseOver.getBlockPos()) instanceof TileEntitySampleDrill)
			chunkBorders = new int[]{(int)player.posX >> 4<<4, (int)player.posZ >> 4<<4};

		float partial = event.getPartialTicks();
		if(!ParticleFractal.PARTICLE_FRACTAL_DEQUE.isEmpty())
		{
			double px = TileEntityRendererDispatcher.staticPlayerX;
			double py = TileEntityRendererDispatcher.staticPlayerY;
			double pz = TileEntityRendererDispatcher.staticPlayerZ;

			Tessellator tessellator = Tessellator.getInstance();

			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.disableCull();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);

			tessellator.getBuffer().setTranslation(-px, -py, -pz);
			ParticleFractal part;
			while((part = ParticleFractal.PARTICLE_FRACTAL_DEQUE.pollFirst())!=null)
				part.render(tessellator, tessellator.getBuffer(), partial);
			tessellator.getBuffer().setTranslation(0, 0, 0);

			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
		}

		if(chunkBorders!=null)
		{
			renderChunkBorders(player, chunkBorders[0], chunkBorders[1]);
		}

		if(!FAILED_CONNECTIONS.isEmpty())
		{
			Entity viewer = ClientUtils.mc().getRenderViewEntity();
			if(viewer==null)
				viewer = player;
			double dx = viewer.lastTickPosX+(viewer.posX-viewer.lastTickPosX)*partial;
			double dy = viewer.lastTickPosY+(viewer.posY-viewer.lastTickPosY)*partial;
			double dz = viewer.lastTickPosZ+(viewer.posZ-viewer.lastTickPosZ)*partial;
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder bb = tes.getBuffer();
			float oldLineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
			GlStateManager.glLineWidth(5);
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			bb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			for(Entry<Connection, Pair<BlockPos, AtomicInteger>> entry : FAILED_CONNECTIONS.entrySet())
			{
				Connection conn = entry.getKey();
				bb.setTranslation(conn.start.getX()-dx,
						conn.start.getY()-dy,
						conn.start.getZ()-dz);
				Vec3d[] points = conn.getSubVertices(ClientUtils.mc().world);
				int time = entry.getValue().getValue().get();
				float alpha = (float)Math.min((2+Math.sin(time*Math.PI/40))/3, time/20F);
				for(int i = 0; i < points.length-1; i++)
				{
					bb.pos(points[i].x, points[i].y, points[i].z)
							.color(1, 0, 0, alpha).endVertex();
					alpha = (float)Math.min((2+Math.sin((time+(i+1)*8)*Math.PI/40))/3, time/20F);
					bb.pos(points[i+1].x, points[i+1].y, points[i+1].z)
							.color(1, 0, 0, alpha).endVertex();
				}
			}
			bb.setTranslation(0, 0, 0);
			tes.draw();
			GlStateManager.glLineWidth(oldLineWidth);
			GlStateManager.enableBlend();
			GlStateManager.color(1, 0, 0, .5F);
			renderObstructingBlocks(bb, tes, dx, dy, dz);

			//Code to render the obstructing block through other blocks
			//GlStateManager.color(1, 0, 0, .25F);
			//GlStateManager.depthFunc(GL11.GL_GREATER);
			//renderObstructingBlocks(bb, tes, dx, dy, dz);
			//GlStateManager.depthFunc(GL11.GL_LEQUAL);

			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
		}
	}

	private static void renderChunkBorders(EntityPlayer player, int chunkX, int chunkZ)
	{
		double px = TileEntityRendererDispatcher.staticPlayerX;
		double py = TileEntityRendererDispatcher.staticPlayerY;
		double pz = TileEntityRendererDispatcher.staticPlayerZ;

		int y = Math.min((int)player.posY, player.getEntityWorld().getChunk(new BlockPos(player.posX, 0, player.posZ)).getLowestHeight())-2;
		float h = (float)Math.max(32, player.posY-y+4);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder BufferBuilder = tessellator.getBuffer();

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		float r = Lib.COLOUR_F_ImmersiveOrange[0];
		float g = Lib.COLOUR_F_ImmersiveOrange[1];
		float b = Lib.COLOUR_F_ImmersiveOrange[2];
		BufferBuilder.setTranslation(chunkX-px, y+2-py, chunkZ-pz);
		GlStateManager.glLineWidth(5f);
		BufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		BufferBuilder.pos(0, 0, 0).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(0, h, 0).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(16, 0, 0).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(16, h, 0).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(16, 0, 16).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(16, h, 16).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(0, 0, 16).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(0, h, 16).color(r, g, b, .375f).endVertex();

		BufferBuilder.pos(0, 2, 0).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(16, 2, 0).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(0, 2, 0).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(0, 2, 16).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(0, 2, 16).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(16, 2, 16).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(16, 2, 0).color(r, g, b, .375f).endVertex();
		BufferBuilder.pos(16, 2, 16).color(r, g, b, .375f).endVertex();
		tessellator.draw();
		BufferBuilder.setTranslation(0, 0, 0);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();

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
				((ModelBiped)model).bipedHead.showModel = false;
			else if(model instanceof ModelVillager)
				((ModelVillager)model).villagerHead.showModel = false;
		}
	}

	@SubscribeEvent()
	public void onRenderLivingPost(RenderLivingEvent.Post event)
	{
		if(event.getEntity().getEntityData().hasKey("headshot"))
		{
			ModelBase model = event.getRenderer().mainModel;
			if(model instanceof ModelBiped)
				((ModelBiped)model).bipedHead.showModel = true;
			else if(model instanceof ModelVillager)
				((ModelVillager)model).villagerHead.showModel = true;
		}
	}


	private boolean justLoggedIn = false;

	@SubscribeEvent
	public void onLoginClientPre(ClientConnectedToServerEvent ev)
	{
		justLoggedIn = true;
	}

	@SubscribeEvent
	public void onLoginClient(EntityJoinWorldEvent ev)
	{
		if(ev.getEntity()==Minecraft.getMinecraft().player&&justLoggedIn)
		{
			String javaV = System.getProperty("java.version");
			if(javaV.equals("1.8.0_25"))
				Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation(Lib.CHAT_INFO+"old_java", javaV));
			justLoggedIn = false;
		}
	}
}
