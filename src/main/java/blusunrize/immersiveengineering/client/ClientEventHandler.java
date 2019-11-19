/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.RenderData;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.client.gui.BlastFurnaceScreen;
import blusunrize.immersiveengineering.client.gui.RevolverScreen;
import blusunrize.immersiveengineering.client.render.tile.AutoWorkbenchRenderer;
import blusunrize.immersiveengineering.client.render.tile.AutoWorkbenchRenderer.BlueprintLines;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.TurntableTileEntity;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.network.MessageChemthrowerSwitch;
import blusunrize.immersiveengineering.common.network.MessageMagnetEquip;
import blusunrize.immersiveengineering.common.network.MessageRequestBlockUpdate;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledSound;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledTickableSound;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraft.block.Block;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeIngameGui;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class ClientEventHandler implements ISelectiveResourceReloadListener
{
	private boolean shieldToggleButton = false;
	private int shieldToggleTimer = 0;
	private static final String[] BULLET_TOOLTIP = {"\u00A0\u00A0IE\u00A0", "\u00A0\u00A0AMMO\u00A0", "\u00A0\u00A0HERE\u00A0", "\u00A0\u00A0--\u00A0"};

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if(resourcePredicate.test(VanillaResourceType.TEXTURES))
			ImmersiveEngineering.proxy.clearRenderCaches();
	}

	public static final Map<Connection, Pair<BlockPos, AtomicInteger>> FAILED_CONNECTIONS = new HashMap<>();

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side==LogicalSide.CLIENT&&event.player!=null&&event.player==ClientUtils.mc().getRenderViewEntity())
		{
			if(event.phase==Phase.END)
			{
				if(this.shieldToggleTimer > 0)
					this.shieldToggleTimer--;
				if(ClientProxy.keybind_magnetEquip.isKeyDown()&&!this.shieldToggleButton)
					if(this.shieldToggleTimer <= 0)
						this.shieldToggleTimer = 7;
					else
					{
						PlayerEntity player = event.player;
						ItemStack held = player.getHeldItem(Hand.OFF_HAND);
						if(!held.isEmpty()&&held.getItem() instanceof IEShieldItem)
						{
							if(((IEShieldItem)held.getItem()).getUpgrades(held).getBoolean("magnet")&&
									((IEShieldItem)held.getItem()).getUpgrades(held).contains("prevSlot"))
								ImmersiveEngineering.packetHandler.sendToServer(new MessageMagnetEquip(-1));
						}
						else
						{
							for(int i = 0; i < player.inventory.mainInventory.size(); i++)
							{
								ItemStack s = player.inventory.mainInventory.get(i);
								if(!s.isEmpty()&&s.getItem() instanceof IEShieldItem&&((IEShieldItem)s.getItem()).getUpgrades(s).getBoolean("magnet"))
									ImmersiveEngineering.packetHandler.sendToServer(new MessageMagnetEquip(i));
							}
						}
					}
				if(this.shieldToggleButton!=ClientUtils.mc().gameSettings.keyBindBack.isKeyDown())
					this.shieldToggleButton = ClientUtils.mc().gameSettings.keyBindBack.isKeyDown();


				if(ClientProxy.keybind_chemthrowerSwitch.isPressed())
				{
					ItemStack held = event.player.getHeldItem(Hand.MAIN_HAND);
					if(held.getItem() instanceof ChemthrowerItem&&((ChemthrowerItem)held.getItem()).getUpgrades(held).getBoolean("multitank"))
						ImmersiveEngineering.packetHandler.sendToServer(new MessageChemthrowerSwitch(true));
				}
			}
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		FAILED_CONNECTIONS.entrySet().removeIf(entry -> entry.getValue().getValue().decrementAndGet() <= 0);
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		event.getItemStack();
		if(event.getItemStack().isEmpty())
			return;
		event.getItemStack().getCapability(CapabilityShader.SHADER_CAPABILITY).ifPresent(wrapper ->
		{
			ItemStack shader = wrapper.getShaderItem();
			if(!shader.isEmpty())
				event.getToolTip().add(shader.getDisplayName().setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
		});
		Style gray = new Style().setColor(TextFormatting.GRAY);
		if(ItemNBTHelper.hasKey(event.getItemStack(), Lib.NBT_Earmuffs))
		{
			ItemStack earmuffs = ItemNBTHelper.getItemStack(event.getItemStack(), Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty())
				event.getToolTip().add(earmuffs.getDisplayName().setStyle(gray));
		}
		if(ItemNBTHelper.hasKey(event.getItemStack(), Lib.NBT_Powerpack))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(event.getItemStack(), Lib.NBT_Powerpack);
			if(!powerpack.isEmpty())
			{
				event.getToolTip().add(powerpack.getDisplayName().setStyle(gray));
				event.getToolTip().add(new StringTextComponent(EnergyHelper.getEnergyStored(powerpack)+"/"+EnergyHelper.getMaxEnergyStored(powerpack)+" IF")
						.setStyle(gray));
			}
		}
		if(ClientUtils.mc().currentScreen!=null
				&&ClientUtils.mc().currentScreen instanceof BlastFurnaceScreen
				&&BlastFurnaceRecipe.isValidBlastFuel(event.getItemStack()))
			event.getToolTip().add(new TranslationTextComponent("desc.immersiveengineering.info.blastFuelTime", BlastFurnaceRecipe.getBlastFuelTime(event.getItemStack()))
					.setStyle(gray));
		if(IEConfig.GENERAL.oreTooltips.get()&&event.getFlags().isAdvanced())
		{
			for(ResourceLocation oid : ItemTags.getCollection().getOwningTags(event.getItemStack().getItem()))
				event.getToolTip().add(new StringTextComponent(oid.toString()).setStyle(gray));
		}

		if(event.getItemStack().getItem() instanceof IBulletContainer)
			for(String s : BULLET_TOOLTIP)
				event.getToolTip().add(new StringTextComponent(s));
	}

	@SubscribeEvent
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
				GlStateManager.translatef(currentX, currentY, 700);
				GlStateManager.scalef(.5f, .5f, 1);

				RevolverScreen.drawExternalGUI(bullets, bulletAmount);

				GlStateManager.disableRescaleNormal();
				GlStateManager.popMatrix();
			}
		}
	}

	@SubscribeEvent
	public void onPlaySound(PlaySoundEvent event)
	{
		if(event.getSound()==null)
		{
			return;
		}
		else
		{
			event.getSound().getCategory();
		}
		if(!ItemEarmuffs.affectedSoundCategories.contains(event.getSound().getCategory().getName()))
			return;
		if(ClientUtils.mc().player!=null&&!ClientUtils.mc().player.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty())
		{
			ItemStack earmuffs = ClientUtils.mc().player.getItemStackFromSlot(EquipmentSlotType.HEAD);
			if(ItemNBTHelper.hasKey(earmuffs, Lib.NBT_Earmuffs))
				earmuffs = ItemNBTHelper.getItemStack(earmuffs, Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty()&&Misc.earmuffs.equals(earmuffs.getItem())&&!ItemNBTHelper.getBoolean(earmuffs, "IE:Earmuffs:Cat_"+event.getSound().getCategory().getName()))
			{
				for(String blacklist : IEConfig.TOOLS.earDefenders_SoundBlacklist.get())
					if(blacklist!=null&&blacklist.equalsIgnoreCase(event.getSound().getSoundLocation().toString()))
						return;
				if(event.getSound() instanceof ITickableSound)
					event.setResultSound(new IEMuffledTickableSound((ITickableSound)event.getSound(), ItemEarmuffs.getVolumeMod(earmuffs)));
				else
					event.setResultSound(new IEMuffledSound(event.getSound(), ItemEarmuffs.getVolumeMod(earmuffs)));

				if(event.getSound().getCategory()==SoundCategory.RECORDS)
				{
					BlockPos pos = new BlockPos(event.getSound().getX(), event.getSound().getY(), event.getSound().getZ());
					if(ClientUtils.mc().worldRenderer.mapSoundPositions.containsKey(pos))
						ClientUtils.mc().worldRenderer.mapSoundPositions.put(pos, event.getResultSound());
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

	@SubscribeEvent
	public void onRenderItemFrame(RenderItemInFrameEvent event)
	{
		if(!event.getItem().isEmpty()&&event.getItem().getItem() instanceof EngineersBlueprintItem)
		{
			double playerDistanceSq = ClientUtils.mc().player.getDistanceSq(event.getEntityItemFrame());

			if(playerDistanceSq < 1000)
			{
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(event.getItem(), "blueprint"));
				if(recipes.length > 0)
				{
					int i = event.getEntityItemFrame().getRotation();
					BlueprintCraftingRecipe recipe = recipes[i%recipes.length];
					BlueprintLines blueprint = recipe==null?null: AutoWorkbenchRenderer.getBlueprintDrawable(recipe, event.getEntityItemFrame().getEntityWorld());
					if(blueprint!=null)
					{
						GlStateManager.rotatef(-i*45.0F, 0.0F, 0.0F, 1.0F);
						ClientUtils.bindTexture("immersiveengineering:textures/models/blueprint_frame.png");
						GlStateManager.translated(-.5, .5, -.001);
						ClientUtils.drawTexturedRect(.125f, -.875f, .75f, .75f, 1d, 0d, 1d, 0d);
						//Width depends on distance
						float lineWidth = playerDistanceSq < 3?3: playerDistanceSq < 25?2: playerDistanceSq < 40?1: .5f;
						GlStateManager.translated(.75, -.25, -.002);
						GlStateManager.disableCull();
						GlStateManager.disableTexture();
						GlStateManager.enableBlend();
						float scale = .0375f/(blueprint.getTextureScale()/16f);
						GlStateManager.scalef(-scale, -scale, scale);
						GlStateManager.color3f(1, 1, 1);

						blueprint.draw(lineWidth);

						GlStateManager.scalef(1/scale, -1/scale, 1/scale);
						GlStateManager.enableAlphaTest();
						GlStateManager.enableTexture();
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
				int width = ClientUtils.mc().mainWindow.getScaledWidth();
				int height = ClientUtils.mc().mainWindow.getScaledHeight();
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
				GlStateManager.blendFuncSeparate(770, 771, 1, 0);

				GlStateManager.translated(offsetX, offsetY, 0);
				ClientUtils.drawTexturedRect(0, 0, resMin, resMin, 0f, 1f, 0f, 1f);

				ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
				ClientUtils.drawTexturedRect(218/256f*resMin, 64/256f*resMin, 24/256f*resMin, 128/256f*resMin, 64/256f, 88/256f, 96/256f, 224/256f);
				ItemStack equipped = ClientUtils.mc().player.getHeldItem(Hand.MAIN_HAND);
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
						GlStateManager.translated(223/256f*resMin, 64/256f*resMin, 0);
						GlStateManager.translated(0, (5+stepOffset)/256*resMin, 0);
						for(int i = 0; i < steps.length; i++)
						{
							ClientUtils.drawTexturedRect(0, 0, 8/256f*resMin, 7/256f*resMin, 88/256f, 96/256f, 96/256f, 103/256f);
							GlStateManager.translated(0, stepLength/256*resMin, 0);
							totalOffset += stepLength;

							if(curStep==-1||Math.abs(steps[i]-ZoomHandler.fovZoom) < dist)
							{
								curStep = i;
								dist = Math.abs(steps[i]-ZoomHandler.fovZoom);
							}
						}
						GlStateManager.translated(0, -totalOffset/256*resMin, 0);

						if(curStep < steps.length)
						{
							GlStateManager.translated(6/256f*resMin, curStep*stepLength/256*resMin, 0);
							ClientUtils.drawTexturedRect(0, 0, 8/256f*resMin, 7/256f*resMin, 88/256f, 98/256f, 103/256f, 110/256f);
							ClientUtils.font().drawString((1/steps[curStep])+"x", (int)(16/256f*resMin), 0, 0xffffff);
							GlStateManager.translated(-6/256f*resMin, -curStep*stepLength/256*resMin, 0);
						}
						GlStateManager.translated(0, -((5+stepOffset)/256*resMin), 0);
						GlStateManager.translated(-223/256f*resMin, -64/256f*resMin, 0);
					}
				}

				GlStateManager.translated(-offsetX, -offsetY, 0);
			}
		}
	}

	@SubscribeEvent()
	public void onRenderOverlayPost(RenderGameOverlayEvent.Post event)
	{
		int scaledWidth = ClientUtils.mc().mainWindow.getScaledWidth();
		int scaledHeight = ClientUtils.mc().mainWindow.getScaledHeight();
		if(ClientUtils.mc().player!=null&&event.getType()==RenderGameOverlayEvent.ElementType.TEXT)
		{
			PlayerEntity player = ClientUtils.mc().player;

			for(Hand hand : Hand.values())
				if(!player.getHeldItem(hand).isEmpty())
				{
					ItemStack equipped = player.getHeldItem(hand);
					if(ItemStack.areItemsEqual(new ItemStack(Tools.voltmeter), equipped)||equipped.getItem() instanceof IWireCoil)
					{
						if(equipped.hasTag()&&equipped.getOrCreateTag().contains("linkingPos", NBT.TAG_COMPOUND))
						{
							CompoundNBT link = equipped.getOrCreateTag().getCompound("linkingPos");
							DimensionBlockPos pos = new DimensionBlockPos(link.getCompound("master"));
							String s = I18n.format(Lib.DESC_INFO+"attachedTo", pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
							int col = WireType.ELECTRUM.getColour(null);
							if(equipped.getItem() instanceof IWireCoil)
							{
								//TODO use actual connection offset rather than pos
								RayTraceResult rtr = ClientUtils.mc().objectMouseOver;
								double d;
								if(rtr instanceof BlockRayTraceResult)
									d = ((BlockRayTraceResult)rtr).getPos().distanceSq(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ(), true);
								else
									d = player.getDistanceSq(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
								int max = ((IWireCoil)equipped.getItem()).getWireType(equipped).getMaxLength();
								if(d > max*max)
									col = 0xdd3333;
							}
							ClientUtils.font().drawStringWithShadow(s, scaledWidth/2-ClientUtils.font().getStringWidth(s)/2, scaledHeight-ForgeIngameGui.left_height-20, col);
						}
					}
					else if(equipped.getItem()==Misc.fluorescentTube)
					{
						String s = I18n.format("desc.immersiveengineering.info.colour", "#"+FluorescentTubeItem.hexColorString(equipped));
						ClientUtils.font().drawStringWithShadow(s, scaledWidth/2-ClientUtils.font().getStringWidth(s)/2,
								scaledHeight-ForgeIngameGui.left_height-20, FluorescentTubeItem.getRGBInt(equipped, 1));
					}
					else if(equipped.getItem() instanceof RevolverItem||equipped.getItem() instanceof SpeedloaderItem)
					{
						NonNullList<ItemStack> bullets = ((IBulletContainer)equipped.getItem()).getBullets(equipped, true);
						if(bullets!=null)
						{
							int bulletAmount = ((IBulletContainer)equipped.getItem()).getBulletCount(equipped);
							HandSide side = hand==Hand.MAIN_HAND?player.getPrimaryHand(): player.getPrimaryHand().opposite();
							boolean right = side==HandSide.RIGHT;
							float dx = right?scaledWidth-32-48: 48;
							float dy = scaledHeight-64;
							GlStateManager.pushMatrix();
							GlStateManager.enableRescaleNormal();
							GlStateManager.enableBlend();
							GlStateManager.translated(dx, dy, 0);
							GlStateManager.scalef(.5f, .5f, 1);

							RevolverScreen.drawExternalGUI(bullets, bulletAmount);

							if(equipped.getItem() instanceof RevolverItem)
							{
								int cd = ((RevolverItem)equipped.getItem()).getShootCooldown(equipped);
								float cdMax = ((RevolverItem)equipped.getItem()).getMaxShootCooldown(equipped);
								float cooldown = 1-cd/cdMax;
								if(cooldown > 0)
								{
									GlStateManager.scalef(2, 2, 1);
									GlStateManager.translated(-dx, -dy, 0);
									GlStateManager.translated(scaledWidth/2+(right?1: -6), scaledHeight/2-7, 0);

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
					else if(equipped.getItem() instanceof RailgunItem)
					{
						int duration = 72000-(player.isHandActive()&&player.getActiveHand()==hand?player.getItemInUseCount(): 0);
						int chargeTime = ((RailgunItem)equipped.getItem()).getChargeTime(equipped);
						int chargeLevel = duration < 72000?Math.min(99, (int)(duration/(float)chargeTime*100)): 0;
						float scale = 2f;
						GlStateManager.pushMatrix();
						GlStateManager.translated(scaledWidth-80, scaledHeight-30, 0);
						GlStateManager.scalef(scale, scale, 1);
						ClientProxy.nixieFont.drawString((chargeLevel < 10?"0": "")+chargeLevel, 0, 0, Lib.colour_nixieTubeText);
						GlStateManager.scalef(1/scale, 1/scale, 1);
						GlStateManager.popMatrix();
					}
					else if(equipped.getItem() instanceof DrillItem||equipped.getItem() instanceof ChemthrowerItem)
					{
						boolean drill = equipped.getItem() instanceof DrillItem;
						ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
						GlStateManager.color3f(1, 1, 1);
						float dx = scaledWidth-16;
						float dy = scaledHeight;
						GlStateManager.pushMatrix();
						GlStateManager.translated(dx, dy, 0);
						int w = 31;
						int h = 62;
						double uMin = 179/256f;
						double uMax = 210/256f;
						double vMin = 9/256f;
						double vMax = 71/256f;
						ClientUtils.drawTexturedRect(-24, -68, w, h, uMin, uMax, vMin, vMax);

						GlStateManager.translated(-23, -37, 0);
						LazyOptional<IFluidHandlerItem> handlerOpt = FluidUtil.getFluidHandler(equipped);
						int capacity = -1;
						if(handlerOpt.isPresent())
						{
							IFluidHandlerItem handler = handlerOpt.orElseThrow(RuntimeException::new);
							if(handler.getTanks() > 0)
								capacity = handler.getTankCapacity(0);
						}
						if(capacity >= 0)
						{
							FluidStack fuel = FluidUtil.getFluidContained(equipped).orElse(FluidStack.EMPTY);
							int amount = fuel.getAmount();
							if(!drill&&player.isHandActive()&&player.getActiveHand()==hand)
							{
								int use = player.getItemInUseMaxCount();
								amount -= use*IEConfig.TOOLS.chemthrower_consumption.get();
							}
							float cap = (float)capacity;
							float angle = 83-(166*amount/cap);
							GlStateManager.rotatef(angle, 0, 0, 1);
							ClientUtils.drawTexturedRect(6, -2, 24, 4, 91/256f, 123/256f, 80/256f, 87/256f);
							GlStateManager.rotatef(-angle, 0, 0, 1);
							GlStateManager.translated(23, 37, 0);
							if(drill)
							{
								ClientUtils.drawTexturedRect(-54, -73, 66, 72, 108/256f, 174/256f, 4/256f, 76/256f);
								ItemRenderer ir = ClientUtils.mc().getItemRenderer();
								ItemStack head = ((DrillItem)equipped.getItem()).getHead(equipped);
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
								if(!fuel.isEmpty())
								{
									String name = ClientUtils.font().trimStringToWidth(fuel.getDisplayName().getFormattedText(), 50).trim();
									ClientUtils.font().drawString(name, -68-ClientUtils.font().getStringWidth(name)/2, -15, 0);
								}
							}
						}
						GlStateManager.popMatrix();
					}
					else if(equipped.getItem() instanceof IEShieldItem)
					{
						CompoundNBT upgrades = ((IEShieldItem)equipped.getItem()).getUpgrades(equipped);
						if(!upgrades.isEmpty())
						{
							ClientUtils.bindTexture("immersiveengineering:textures/gui/hud_elements.png");
							GlStateManager.color3f(1, 1, 1);
							boolean boundLeft = (player.getPrimaryHand()==HandSide.RIGHT)==(hand==Hand.OFF_HAND);
							float dx = boundLeft?16: (scaledWidth-16-64);
							float dy = scaledHeight;
							GlStateManager.pushMatrix();
							GlStateManager.enableBlend();
							GlStateManager.translated(dx, dy, 0);
							ClientUtils.drawTexturedRect(0, -22, 64, 22, 0, 64/256f, 176/256f, 198/256f);

							if(upgrades.getBoolean("flash"))
							{
								ClientUtils.drawTexturedRect(11, -38, 16, 16, 11/256f, 27/256f, 160/256f, 176/256f);
								if(upgrades.contains("flash_cooldown"))
								{
									float h = upgrades.getInt("flash_cooldown")/40f*16;
									ClientUtils.drawTexturedRect(11, -22-h, 16, h, 11/256f, 27/256f, (214-h)/256f, 214/256f);
								}
							}
							if(upgrades.getBoolean("shock"))
							{
								ClientUtils.drawTexturedRect(40, -38, 12, 16, 40/256f, 52/256f, 160/256f, 176/256f);
								if(upgrades.contains("shock_cooldown"))
								{
									float h = upgrades.getInt("shock_cooldown")/40f*16;
									ClientUtils.drawTexturedRect(40, -22-h, 12, h, 40/256f, 52/256f, (214-h)/256f, 214/256f);
								}
							}
							GlStateManager.disableBlend();
							GlStateManager.popMatrix();
						}
					}
					if(equipped.getItem()==Tools.voltmeter)
					{

						RayTraceResult rrt = ClientUtils.mc().objectMouseOver;
						IFluxReceiver receiver = null;
						Direction side = null;
						if(rrt instanceof BlockRayTraceResult)
						{
							BlockRayTraceResult mop = (BlockRayTraceResult)rrt;
							TileEntity tileEntity = player.world.getTileEntity(mop.getPos());
							if(tileEntity instanceof IFluxReceiver)
								receiver = (IFluxReceiver)tileEntity;
							side = mop.getFace();
							if(player.world.getGameTime()%20==0)
								ImmersiveEngineering.packetHandler.sendToServer(new MessageRequestBlockUpdate(mop.getPos()));
						}
						else if(rrt instanceof EntityRayTraceResult&&((EntityRayTraceResult)rrt).getEntity() instanceof IFluxReceiver)
							receiver = (IFluxReceiver)((EntityRayTraceResult)rrt).getEntity();
						if(receiver!=null)
						{
							String[] text = new String[0];
							int maxStorage = receiver.getMaxEnergyStored(side);
							int storage = receiver.getEnergyStored(side);
							if(maxStorage > 0)
								text = I18n.format(Lib.DESC_INFO+"energyStored", "<br>"+Utils.toScientificNotation(storage, "0##", 100000)+" / "+Utils.toScientificNotation(maxStorage, "0##", 100000)).split("<br>");
							int col = IEConfig.GENERAL.nixietubeFont.get()?Lib.colour_nixieTubeText: 0xffffff;
							int i = 0;
							for(String s : text)
								if(s!=null)
								{
									int w = ClientProxy.nixieFontOptional.getStringWidth(s);
									ClientProxy.nixieFontOptional.drawStringWithShadow(s, scaledWidth/2-w/2,
											scaledHeight/2-4-text.length*(ClientProxy.nixieFontOptional.FONT_HEIGHT+2)+(i++)*(ClientProxy.nixieFontOptional.FONT_HEIGHT+2), col);
								}
						}
					}
				}
			if(ClientUtils.mc().objectMouseOver!=null)
			{
				boolean hammer = !player.getHeldItem(Hand.MAIN_HAND).isEmpty()&&Utils.isHammer(player.getHeldItem(Hand.MAIN_HAND));
				RayTraceResult mop = ClientUtils.mc().objectMouseOver;
				if(mop instanceof BlockRayTraceResult)
				{
					TileEntity tileEntity = player.world.getTileEntity(((BlockRayTraceResult)mop).getPos());
					if(tileEntity instanceof IBlockOverlayText)
					{
						IBlockOverlayText overlayBlock = (IBlockOverlayText)tileEntity;
						String[] text = overlayBlock.getOverlayText(ClientUtils.mc().player, mop, hammer);
						boolean useNixie = overlayBlock.useNixieFont(ClientUtils.mc().player, mop);
						if(text!=null&&text.length > 0)
						{
							FontRenderer font = useNixie?ClientProxy.nixieFontOptional: ClientUtils.font();
							int col = (useNixie&&IEConfig.GENERAL.nixietubeFont.get())?Lib.colour_nixieTubeText: 0xffffff;
							int i = 0;
							for(String s : text)
								if(s!=null)
									font.drawStringWithShadow(s, scaledWidth/2+8, scaledHeight/2+8+(i++)*font.FONT_HEIGHT, col);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent()
	public void onFogUpdate(EntityViewRenderEvent.RenderFogEvent event)
	{
		Entity e = event.getInfo().getRenderViewEntity();
		if(e instanceof LivingEntity&&((LivingEntity)e).isPotionActive(IEPotions.flashed))
		{
			EffectInstance effect = ((LivingEntity)e).getActivePotionEffect(IEPotions.flashed);
			int timeLeft = effect.getDuration();
			float saturation = 1-timeLeft/(float)(80+40*effect.getAmplifier());//Total Time =  4s + 2s per amplifier

			float f1 = -2.5f+15.0F*saturation;
			if(timeLeft < 20)
				f1 += (event.getFarPlaneDistance()/4)*(1-timeLeft/20f);

			GlStateManager.fogMode(GlStateManager.FogMode.LINEAR);
			GlStateManager.fogStart(f1*0.25F);
			GlStateManager.fogEnd(f1);
			GlStateManager.fogDensity(.125f);

			if(GL.getCapabilities().GL_NV_fog_distance)
				GlStateManager.fogi(34138, 34139);
		}
	}

	@SubscribeEvent()
	public void onFogColourUpdate(EntityViewRenderEvent.FogColors event)
	{
		Entity e = event.getInfo().getRenderViewEntity();
		if(e instanceof LivingEntity&&((LivingEntity)e).isPotionActive(IEPotions.flashed))
		{
			event.setRed(1);
			event.setGreen(1);
			event.setBlue(1);
		}
	}

	@SubscribeEvent()
	public void onFOVUpdate(FOVUpdateEvent event)
	{
		PlayerEntity player = ClientUtils.mc().player;
		if(!player.getHeldItem(Hand.MAIN_HAND).isEmpty()&&player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof IZoomTool)
		{
			if(player.isSneaking()&&player.onGround)
			{
				ItemStack equipped = player.getHeldItem(Hand.MAIN_HAND);
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
							ZoomHandler.fovZoom = steps[curStep];
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
	public void onMouseEvent(InputEvent.MouseScrollEvent event)
	{
		if(event.getScrollDelta()!=0&&ClientUtils.mc().currentScreen==null)
		{
			PlayerEntity player = ClientUtils.mc().player;
			if(player!=null&&!player.getHeldItem(Hand.MAIN_HAND).isEmpty()&&player.isSneaking())
			{
				ItemStack equipped = player.getHeldItem(Hand.MAIN_HAND);

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
							int newStep = curStep+(event.getScrollDelta() > 0?-1: 1);
							if(newStep >= 0&&newStep < steps.length)
								ZoomHandler.fovZoom = steps[newStep];
							event.setCanceled(true);
						}
					}
				}
				if(IEConfig.TOOLS.chemthrower_scroll.get()&&equipped.getItem() instanceof ChemthrowerItem&&((ChemthrowerItem)equipped.getItem()).getUpgrades(equipped).getBoolean("multitank"))
				{
					ImmersiveEngineering.packetHandler.sendToServer(new MessageChemthrowerSwitch(event.getScrollDelta() < 0));
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent()
	public void renderAdditionalBlockBounds(DrawBlockHighlightEvent event)
	{
		if(event.getSubID()==0&&event.getTarget().getType()==Type.BLOCK)
		{
			BlockRayTraceResult rtr = (BlockRayTraceResult)event.getTarget();
			Entity player = event.getInfo().getRenderViewEntity();
			float f1 = 0.002F;
			double px = -TileEntityRendererDispatcher.staticPlayerX;
			double py = -TileEntityRendererDispatcher.staticPlayerY;
			double pz = -TileEntityRendererDispatcher.staticPlayerZ;
			TileEntity tile = player.world.getTileEntity(rtr.getPos());
			ItemStack stack = player instanceof LivingEntity?((LivingEntity)player).getHeldItem(Hand.MAIN_HAND): ItemStack.EMPTY;

			if(Utils.isHammer(stack)&&tile instanceof TurntableTileEntity)
			{
				BlockPos pos = rtr.getPos();

				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
				GlStateManager.lineWidth(2.0F);
				GlStateManager.disableTexture();
				GlStateManager.depthMask(false);

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder BufferBuilder = tessellator.getBuffer();

				Direction f = ((TurntableTileEntity)tile).getFacing();
				double tx = pos.getX()+.5;
				double ty = pos.getY()+.5;
				double tz = pos.getZ()+.5;
				if(!player.world.isAirBlock(pos.offset(f)))
				{
					tx += f.getXOffset();
					ty += f.getYOffset();
					tz += f.getZOffset();
				}
				BufferBuilder.setTranslation(tx+px, ty+py, tz+pz);

				double angle = -player.ticksExisted%80/40d*Math.PI;
				drawRotationArrows(tessellator, BufferBuilder, f, angle, ((TurntableTileEntity)tile).invert);

				BufferBuilder.setTranslation(0, 0, 0);

				GlStateManager.depthMask(true);
				GlStateManager.enableTexture();
				GlStateManager.disableBlend();
			}

			World world = player.world;
			if(!stack.isEmpty()&&ConveyorHandler.conveyorBlocks.containsValue(Block.getBlockFromItem(stack.getItem()))&&rtr.getFace().getAxis()==Axis.Y)
			{
				Direction side = rtr.getFace();
				BlockPos pos = rtr.getPos();
				VoxelShape shape = world.getBlockState(pos).getRenderShape(world, pos);
				AxisAlignedBB targetedBB = null;
				if(!shape.isEmpty())
					targetedBB = shape.getBoundingBox();
				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
				GlStateManager.lineWidth(2.0F);
				GlStateManager.disableTexture();
				GlStateManager.depthMask(false);

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder BufferBuilder = tessellator.getBuffer();
				BufferBuilder.setTranslation(pos.getX()+px, pos.getY()+py, pos.getZ()+pz);
				double[][] points = new double[4][];


				if(side.getAxis()==Axis.Y)
				{
					double y = targetedBB==null?0: side==Direction.DOWN?targetedBB.minY-f1: targetedBB.maxY+f1;
					points[0] = new double[]{0-f1, y, 0-f1};
					points[1] = new double[]{1+f1, y, 1+f1};
					points[2] = new double[]{0-f1, y, 1+f1};
					points[3] = new double[]{1+f1, y, 0-f1};
				}
				else if(side.getAxis()==Axis.Z)
				{
					double z = targetedBB==null?0: side==Direction.NORTH?targetedBB.minZ-f1: targetedBB.maxZ+f1;
					points[0] = new double[]{1+f1, 1+f1, z};
					points[1] = new double[]{0-f1, 0-f1, z};
					points[2] = new double[]{0-f1, 1+f1, z};
					points[3] = new double[]{1+f1, 0-f1, z};
				}
				else
				{
					double x = targetedBB==null?0: side==Direction.WEST?targetedBB.minX-f1: targetedBB.maxX+f1;
					points[0] = new double[]{x, 1+f1, 1+f1};
					points[1] = new double[]{x, 0-f1, 0-f1};
					points[2] = new double[]{x, 1+f1, 0-f1};
					points[3] = new double[]{x, 0-f1, 1+f1};
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

				float xFromMid = side.getAxis()==Axis.X?0: (float)rtr.getHitVec().x-pos.getX()-.5f;
				float yFromMid = side.getAxis()==Axis.Y?0: (float)rtr.getHitVec().y-pos.getY()-.5f;
				float zFromMid = side.getAxis()==Axis.Z?0: (float)rtr.getHitVec().z-pos.getZ()-.5f;
				float max = Math.max(Math.abs(yFromMid), Math.max(Math.abs(xFromMid), Math.abs(zFromMid)));
				Vec3d dir = new Vec3d(max==Math.abs(xFromMid)?Math.signum(xFromMid): 0, max==Math.abs(yFromMid)?Math.signum(yFromMid): 0, max==Math.abs(zFromMid)?Math.signum(zFromMid): 0);
				drawBlockOverlayArrow(tessellator, BufferBuilder, dir, side, targetedBB);
				BufferBuilder.setTranslation(0, 0, 0);

				GlStateManager.depthMask(true);
				GlStateManager.enableTexture();
				GlStateManager.disableBlend();
			}

			if(!stack.isEmpty()&&stack.getItem() instanceof DrillItem&&
					((DrillItem)stack.getItem()).isEffective(world.getBlockState(rtr.getPos()).getMaterial()))
			{
				ItemStack head = ((DrillItem)stack.getItem()).getHead(stack);
				if(!head.isEmpty()&&player instanceof PlayerEntity)
				{
					ImmutableList<BlockPos> blocks = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world,
							(PlayerEntity)player, event.getTarget());
					drawAdditionalBlockbreak(event.getContext(), (PlayerEntity)player, event.getPartialTicks(), blocks);
				}
			}
		}
	}

	private static double[][] rotationArrowCoords = {{.375, 0}, {.5, -.125}, {.4375, -.125}, {.4375, -.25}, {.25, -.4375}, {-.25, -.4375}, {-.4375, -.25}, {-.4375, -.0625}, {-.3125, -.0625}, {-.3125, -.1875}, {-.1875, -.3125}, {.1875, -.3125}, {.3125, -.1875}, {.3125, -.125}, {.25, -.125}};
	private static double[][] rotationArrowQuads = {rotationArrowCoords[7], rotationArrowCoords[8], rotationArrowCoords[6], rotationArrowCoords[9], rotationArrowCoords[5], rotationArrowCoords[10], rotationArrowCoords[4], rotationArrowCoords[11], rotationArrowCoords[3], rotationArrowCoords[12], rotationArrowCoords[2], rotationArrowCoords[13], rotationArrowCoords[1], rotationArrowCoords[14], rotationArrowCoords[0], rotationArrowCoords[0]};

	public static void drawRotationArrows(Tessellator tessellator, BufferBuilder BufferBuilder, Direction facing, double rotation, boolean flip)
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

	public static void drawBlockOverlayArrow(Tessellator tessellator, BufferBuilder BufferBuilder, Vec3d directionVec, Direction side, AxisAlignedBB targetedBB)
	{
		Vec3d[] translatedPositions = new Vec3d[arrowCoords.length];
		Matrix4 mat = new Matrix4();
		Vec3d defaultDir = side.getAxis()==Axis.Y?new Vec3d(0, 0, 1): new Vec3d(0, 1, 0);
		directionVec = directionVec.normalize();
		double angle = Math.acos(defaultDir.dotProduct(directionVec));
		Vec3d axis = defaultDir.crossProduct(directionVec);
		mat.rotate(angle, axis.x, axis.y, axis.z);
		if(side.getAxis()==Axis.Z)
			mat.rotate(Math.PI/2, 1, 0, 0).rotate(Math.PI, 0, 1, 0);
		else if(side.getAxis()==Axis.X)
			mat.rotate(Math.PI/2, 0, 0, 1).rotate(Math.PI/2, 0, 1, 0);
		for(int i = 0; i < translatedPositions.length; i++)
		{
			Vec3d vec = mat.apply(new Vec3d(arrowCoords[i][0], 0, arrowCoords[i][1])).add(.5, .5, .5);
			if(targetedBB!=null)
				vec = new Vec3d(side==Direction.WEST?targetedBB.minX-.002: side==Direction.EAST?targetedBB.maxX+.002: vec.x, side==Direction.DOWN?targetedBB.minY-.002: side==Direction.UP?targetedBB.maxY+.002: vec.y, side==Direction.NORTH?targetedBB.minZ-.002: side==Direction.SOUTH?targetedBB.maxZ+.002: vec.z);
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

	public static void drawAdditionalBlockbreak(WorldRenderer context, PlayerEntity player, float partialTicks, Collection<BlockPos> blocks)
	{
		for(BlockPos pos : blocks)
			context.drawSelectionBox(ClientUtils.mc().gameRenderer.getActiveRenderInfo(), new BlockRayTraceResult(new Vec3d(0, 0, 0), Direction.DOWN, pos, false), 0);

		PlayerController controllerMP = ClientUtils.mc().playerController;
		if(controllerMP.isHittingBlock)
			ClientUtils.drawBlockDamageTexture(ClientUtils.tes(), ClientUtils.tes().getBuffer(), player, partialTicks, player.world, blocks);
	}

	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent event)
	{
		//Overlay renderer for the sample drill
		boolean chunkBorders = false;
		for(Hand hand : Hand.values())
			if(ClientUtils.mc().player.getHeldItem(hand).getItem()==GameData.getBlockItemMap().get(MetalDevices.sampleDrill))
			{
				chunkBorders = true;
				break;
			}
		if(!chunkBorders&&ClientUtils.mc().objectMouseOver instanceof BlockRayTraceResult&&
				ClientUtils.mc().world.getTileEntity(((BlockRayTraceResult)ClientUtils.mc().objectMouseOver).getPos()) instanceof SampleDrillTileEntity)
			chunkBorders = true;

		float partial = event.getPartialTicks();
		if(!FractalParticle.PARTICLE_FRACTAL_DEQUE.isEmpty())
		{
			double px = TileEntityRendererDispatcher.staticPlayerX;
			double py = TileEntityRendererDispatcher.staticPlayerY;
			double pz = TileEntityRendererDispatcher.staticPlayerZ;

			Tessellator tessellator = Tessellator.getInstance();

			GlStateManager.disableTexture();
			GlStateManager.enableBlend();
			GlStateManager.disableCull();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);

			tessellator.getBuffer().setTranslation(-px, -py, -pz);
			FractalParticle part;
			while((part = FractalParticle.PARTICLE_FRACTAL_DEQUE.pollFirst())!=null)
				part.render(tessellator, tessellator.getBuffer(), partial);
			tessellator.getBuffer().setTranslation(0, 0, 0);

			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.enableTexture();
		}

		if(chunkBorders)
		{
			PlayerEntity player = ClientUtils.mc().player;
			double px = TileEntityRendererDispatcher.staticPlayerX;
			double py = TileEntityRendererDispatcher.staticPlayerY;
			double pz = TileEntityRendererDispatcher.staticPlayerZ;
			int chunkX = (int)player.posX >> 4<<4;
			int chunkZ = (int)player.posZ >> 4<<4;
			int y = Math.min((int)player.posY-2, 0);//TODO player.getEntityWorld().getChunk(new BlockPos(player.posX, 0, player.posZ)).getLowestHeight());
			float h = (float)Math.max(32, player.posY-y+4);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder BufferBuilder = tessellator.getBuffer();

			GlStateManager.disableTexture();
			GlStateManager.enableBlend();
			GlStateManager.disableCull();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			float r = Lib.COLOUR_F_ImmersiveOrange[0];
			float g = Lib.COLOUR_F_ImmersiveOrange[1];
			float b = Lib.COLOUR_F_ImmersiveOrange[2];
			BufferBuilder.setTranslation(chunkX-px, y+2-py, chunkZ-pz);
			GlStateManager.lineWidth(5f);
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
			GlStateManager.enableTexture();
		}

		if(!FAILED_CONNECTIONS.isEmpty())
		{
			Entity viewer = ClientUtils.mc().getRenderViewEntity();
			if(viewer==null)
				viewer = ClientUtils.mc().player;
			double dx = viewer.lastTickPosX+(viewer.posX-viewer.lastTickPosX)*partial;
			double dy = viewer.lastTickPosY+(viewer.posY-viewer.lastTickPosY)*partial;
			double dz = viewer.lastTickPosZ+(viewer.posZ-viewer.lastTickPosZ)*partial;
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder bb = tes.getBuffer();
			float oldLineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
			GlStateManager.lineWidth(5);
			GlStateManager.disableTexture();
			GlStateManager.enableBlend();
			bb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			for(Entry<Connection, Pair<BlockPos, AtomicInteger>> entry : FAILED_CONNECTIONS.entrySet())
			{
				Connection conn = entry.getKey();
				bb.setTranslation(conn.getEndA().getX()-dx,
						conn.getEndA().getY()-dy,
						conn.getEndA().getZ()-dz);
				int time = entry.getValue().getValue().get();
				float alpha = (float)Math.min((2+Math.sin(time*Math.PI/40))/3, time/20F);
				Vec3d prev = conn.getPoint(0, conn.getEndA());
				for(int i = 0; i < RenderData.POINTS_PER_WIRE; i++)
				{
					bb.pos(prev.x, prev.y, prev.z)
							.color(1, 0, 0, alpha).endVertex();
					alpha = (float)Math.min((2+Math.sin((time+(i+1)*8)*Math.PI/40))/3, time/20F);
					Vec3d next = conn.getPoint((i+1)/(double)RenderData.POINTS_PER_WIRE, conn.getEndA());
					bb.pos(next.x, next.y, next.z)
							.color(1, 0, 0, alpha).endVertex();
					prev = next;
				}
			}
			bb.setTranslation(0, 0, 0);
			tes.draw();
			GlStateManager.lineWidth(oldLineWidth);
			GlStateManager.enableBlend();
			GlStateManager.color4f(1, 0, 0, .5F);
			renderObstructingBlocks(bb, tes, dx, dy, dz);

			GlStateManager.disableBlend();
			GlStateManager.enableTexture();
		}
	}

	@SubscribeEvent()
	public void onClientDeath(LivingDeathEvent event)
	{
	}

	@SubscribeEvent()
	public void onRenderLivingPre(RenderLivingEvent.Pre event)
	{
		if(event.getEntity().getPersistentData().contains("headshot"))
			enableHead(event.getRenderer(), false);
	}

	@SubscribeEvent()
	public void onRenderLivingPost(RenderLivingEvent.Post event)
	{
		if(event.getEntity().getPersistentData().contains("headshot"))
			enableHead(event.getRenderer(), true);
	}

	private static void enableHead(LivingRenderer renderer, boolean shouldEnable)
	{
		EntityModel m = renderer.getEntityModel();
		if(m instanceof IHasHead)
			((IHasHead)m).func_205072_a().showModel = shouldEnable;
	}
}
