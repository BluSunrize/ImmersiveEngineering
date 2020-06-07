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
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.RenderData;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.client.gui.BlastFurnaceScreen;
import blusunrize.immersiveengineering.client.gui.RevolverScreen;
import blusunrize.immersiveengineering.client.render.tile.AutoWorkbenchRenderer;
import blusunrize.immersiveengineering.client.render.tile.AutoWorkbenchRenderer.BlueprintLines;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.TurntableTileEntity;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.network.MessageMagnetEquip;
import blusunrize.immersiveengineering.common.network.MessageRequestBlockUpdate;
import blusunrize.immersiveengineering.common.network.MessageRevolverRotate;
import blusunrize.immersiveengineering.common.network.MessageScrollwheelItem;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledSound;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledTickableSound;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasHead;
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
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
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

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class ClientEventHandler implements ISelectiveResourceReloadListener
{
	private boolean shieldToggleButton = false;
	private int shieldToggleTimer = 0;
	private static final String[] BULLET_TOOLTIP = {"  IE ", "  AMMO ", "  HERE ", "  -- "};

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if(resourcePredicate.test(VanillaResourceType.TEXTURES))
			ImmersiveEngineering.proxy.clearRenderCaches();
	}

	public static final Map<Connection, Pair<Collection<BlockPos>, AtomicInteger>> FAILED_CONNECTIONS = new HashMap<>();

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


				if(!ClientProxy.keybind_chemthrowerSwitch.isInvalid()&&ClientProxy.keybind_chemthrowerSwitch.isPressed())
				{
					ItemStack held = event.player.getHeldItem(Hand.MAIN_HAND);
					if(held.getItem() instanceof IScrollwheel)
						ImmersiveEngineering.packetHandler.sendToServer(new MessageScrollwheelItem(true));
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
				&&BlastFurnaceFuel.isValidBlastFuel(event.getItemStack()))
			event.getToolTip().add(new TranslationTextComponent("desc.immersiveengineering.info.blastFuelTime", BlastFurnaceFuel.getBlastFuelTime(event.getItemStack()))
					.setStyle(gray));

		if(IEConfig.GENERAL.tagTooltips.get()&&event.getFlags().isAdvanced())
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
				int line = event.getLines().size()-Utils.findSequenceInList(event.getLines(), BULLET_TOOLTIP, (a, b) -> b.endsWith(a));

				int currentX = event.getX();
				int currentY = line > 0?event.getY()+(event.getHeight()+1-line*10): event.getY()-42;

				MatrixStack transform = new MatrixStack();
				transform.push();
				transform.translate(currentX, currentY, 700);
				transform.scale(.5f, .5f, 1);

				RevolverScreen.drawExternalGUI(bullets, bulletAmount, transform);
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
		if(!EarmuffsItem.affectedSoundCategories.contains(event.getSound().getCategory().getName()))
			return;
		if(ClientUtils.mc().player!=null&&!ClientUtils.mc().player.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty())
		{
			ItemStack earmuffs = ClientUtils.mc().player.getItemStackFromSlot(EquipmentSlotType.HEAD);
			if(ItemNBTHelper.hasKey(earmuffs, Lib.NBT_Earmuffs))
				earmuffs = ItemNBTHelper.getItemStack(earmuffs, Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty()&&
					Misc.earmuffs==earmuffs.getItem()&&
					!ItemNBTHelper.getBoolean(earmuffs, "IE:Earmuffs:Cat_"+event.getSound().getCategory().getName()))
			{
				for(String blacklist : IEConfig.TOOLS.earDefenders_SoundBlacklist.get())
					if(blacklist!=null&&blacklist.equalsIgnoreCase(event.getSound().getSoundLocation().toString()))
						return;
				if(event.getSound() instanceof ITickableSound)
					event.setResultSound(new IEMuffledTickableSound((ITickableSound)event.getSound(), EarmuffsItem.getVolumeMod(earmuffs)));
				else
					event.setResultSound(new IEMuffledSound(event.getSound(), EarmuffsItem.getVolumeMod(earmuffs)));

				if(event.getSound().getCategory()==SoundCategory.RECORDS)
				{
					BlockPos pos = new BlockPos(event.getSound().getX(), event.getSound().getY(), event.getSound().getZ());
					if(ClientUtils.mc().worldRenderer.mapSoundPositions.containsKey(pos))
						ClientUtils.mc().worldRenderer.mapSoundPositions.put(pos, event.getResultSound());
				}
			}
		}
	}

	private void renderObstructingBlocks(MatrixStack transform, IRenderTypeBuffer buffers)
	{
		IVertexBuilder baseBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
		TransformingVertexBuilder builder = new TransformingVertexBuilder(baseBuilder);
		builder.setColor(1, 0, 0, 0.5F);
		for(Entry<Connection, Pair<Collection<BlockPos>, AtomicInteger>> entry : FAILED_CONNECTIONS.entrySet())
		{
			for(BlockPos obstruction : entry.getValue().getKey())
			{
				transform.push();
				transform.translate(obstruction.getX(), obstruction.getY(), obstruction.getZ());
				final float eps = 1e-3f;
				ClientUtils.renderBox(builder, transform, -eps, -eps, -eps, 1+eps, 1+eps, 1+eps);
				transform.pop();
			}
		}
	}

	@SubscribeEvent
	public void onRenderItemFrame(RenderItemInFrameEvent event)
	{
		if(event.getItem().getItem() instanceof EngineersBlueprintItem)
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
						MatrixStack transform = event.getMatrix();
						transform.push();
						IRenderTypeBuffer buffer = event.getBuffers();
						transform.rotate(new Quaternion(0, 0, -i*45, true));
						transform.translate(-.5, .5, -.001);
						IVertexBuilder builder = buffer.getBuffer(IERenderTypes.getGui(rl("textures/models/blueprint_frame.png")));
						ClientUtils.drawTexturedRect(builder, transform, .125f, -.875f, .75f, .75f, 1, 1, 1, 1, 1, 0, 1, 0);
						//Width depends on distance
						float lineWidth = playerDistanceSq < 3?3: playerDistanceSq < 25?2: playerDistanceSq < 40?1: .5f;
						transform.translate(.75, -.25, -.002);
						float scale = .0375f/(blueprint.getTextureScale()/16f);
						transform.scale(-scale, -scale, scale);

						blueprint.draw(lineWidth, transform, buffer);

						transform.pop();
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
				IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
				MatrixStack transform = new MatrixStack();
				transform.push();
				int width = ClientUtils.mc().getMainWindow().getScaledWidth();
				int height = ClientUtils.mc().getMainWindow().getScaledHeight();
				int resMin = Math.min(width, height);
				float offsetX = (width-resMin)/2f;
				float offsetY = (height-resMin)/2f;

				if(resMin==width)
				{
					ClientUtils.drawColouredRect(0, 0, width, (int)offsetY+1, 0xff000000, buffers, transform);
					ClientUtils.drawColouredRect(0, (int)offsetY+resMin, width, (int)offsetY+1, 0xff000000, buffers, transform);
				}
				else
				{
					ClientUtils.drawColouredRect(0, 0, (int)offsetX+1, height, 0xff000000, buffers, transform);
					ClientUtils.drawColouredRect((int)offsetX+resMin, 0, (int)offsetX+1, height, 0xff000000, buffers, transform);
				}
				transform.translate(offsetX, offsetY, 0);
				IVertexBuilder builder = buffers.getBuffer(IERenderTypes.getGui(rl("textures/gui/scope.png")));
				ClientUtils.drawTexturedRect(builder, transform, 0, 0, resMin, resMin, 1, 1, 1, 1, 0f, 1f, 0f, 1f);

				builder = buffers.getBuffer(IERenderTypes.getGui(rl("textures/gui/hud_elements.png")));
				ClientUtils.drawTexturedRect(builder, transform, 218/256f*resMin, 64/256f*resMin, 24/256f*resMin, 128/256f*resMin, 1, 1, 1, 1, 64/256f, 88/256f, 96/256f, 224/256f);
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
						transform.translate(223/256f*resMin, 64/256f*resMin, 0);
						transform.translate(0, (5+stepOffset)/256*resMin, 0);
						for(int i = 0; i < steps.length; i++)
						{
							ClientUtils.drawTexturedRect(builder, transform, 0, 0, 8/256f*resMin, 7/256f*resMin, 1, 1, 1, 1, 88/256f, 96/256f, 96/256f, 103/256f);
							transform.translate(0, stepLength/256*resMin, 0);
							totalOffset += stepLength;

							if(curStep==-1||Math.abs(steps[i]-ZoomHandler.fovZoom) < dist)
							{
								curStep = i;
								dist = Math.abs(steps[i]-ZoomHandler.fovZoom);
							}
						}
						transform.translate(0, -totalOffset/256*resMin, 0);

						if(curStep < steps.length)
						{
							transform.translate(6/256f*resMin, curStep*stepLength/256*resMin, 0);
							ClientUtils.drawTexturedRect(builder, transform, 0, 0, 8/256f*resMin, 7/256f*resMin, 1, 1, 1, 1, 88/256f, 98/256f, 103/256f, 110/256f);
							ClientUtils.font().renderString((1/steps[curStep])+"x", (int)(16/256f*resMin), 0, 0xffffff, true,
									transform.getLast().getMatrix(), buffers, false, 0, 0xf000f0);
							transform.translate(-6/256f*resMin, -curStep*stepLength/256*resMin, 0);
						}
						transform.translate(0, -((5+stepOffset)/256*resMin), 0);
						transform.translate(-223/256f*resMin, -64/256f*resMin, 0);
					}
				}

				transform.translate(-offsetX, -offsetY, 0);
				buffers.finish();
			}
		}
	}

	@SubscribeEvent()
	public void onRenderOverlayPost(RenderGameOverlayEvent.Post event)
	{
		int scaledWidth = ClientUtils.mc().getMainWindow().getScaledWidth();
		int scaledHeight = ClientUtils.mc().getMainWindow().getScaledHeight();
		if(ClientUtils.mc().player!=null&&event.getType()==RenderGameOverlayEvent.ElementType.TEXT)
		{
			PlayerEntity player = ClientUtils.mc().player;
			MatrixStack transform = new MatrixStack();
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

			for(Hand hand : Hand.values())
				if(!player.getHeldItem(hand).isEmpty())
				{
					ItemStack equipped = player.getHeldItem(hand);
					if(ItemStack.areItemsEqual(new ItemStack(Tools.voltmeter), equipped)||equipped.getItem() instanceof IWireCoil)
					{
						if(equipped.hasTag()&&equipped.getOrCreateTag().contains("linkingPos", NBT.TAG_COMPOUND))
						{
							CompoundNBT link = equipped.getOrCreateTag().getCompound("linkingPos");
							ConnectionPoint cp = new ConnectionPoint(link);
							BlockPos pos = cp.getPosition();
							String s = I18n.format(Lib.DESC_INFO+"attachedTo", pos.getX(), pos.getY(), pos.getZ());
							int col = WireType.ELECTRUM.getColour(null);
							if(equipped.getItem() instanceof IWireCoil)
							{
								//TODO use actual connection offset rather than pos
								RayTraceResult rtr = ClientUtils.mc().objectMouseOver;
								double d;
								if(rtr instanceof BlockRayTraceResult)
									d = ((BlockRayTraceResult)rtr).getPos().distanceSq(pos.getX(), pos.getY(), pos.getZ(), true);
								else
									d = player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
								int max = ((IWireCoil)equipped.getItem()).getWireType(equipped).getMaxLength();
								if(d > max*max)
									col = 0xdd3333;
							}
							ClientUtils.font().renderString(
									s, scaledWidth/2-ClientUtils.font().getStringWidth(s)/2, scaledHeight-ForgeIngameGui.left_height-20, col,
									true, transform.getLast().getMatrix(), buffer, false, 0, 0xf000f0);
						}
					}
					else if(equipped.getItem()==Misc.fluorescentTube)
					{
						String s = I18n.format("desc.immersiveengineering.info.colour", "#"+FluorescentTubeItem.hexColorString(equipped));
						ClientUtils.font().renderString(s, scaledWidth/2-ClientUtils.font().getStringWidth(s)/2,
								scaledHeight-ForgeIngameGui.left_height-20, FluorescentTubeItem.getRGBInt(equipped, 1),
								true, transform.getLast().getMatrix(), buffer, false, 0, 0xf000f0
						);
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
							transform.push();
							transform.push();
							transform.translate(dx, dy, 0);
							transform.scale(.5f, .5f, 1);
							RevolverScreen.drawExternalGUI(bullets, bulletAmount, transform);
							transform.pop();

							if(equipped.getItem() instanceof RevolverItem)
							{
								int cd = ((RevolverItem)equipped.getItem()).getShootCooldown(equipped);
								float cdMax = ((RevolverItem)equipped.getItem()).getMaxShootCooldown(equipped);
								float cooldown = 1-cd/cdMax;
								if(cooldown > 0)
								{
									transform.translate(scaledWidth/2+(right?1: -6), scaledHeight/2-7, 0);

									float h1 = cooldown > .33?.5f: cooldown*1.5f;
									float h2 = cooldown;
									float x2 = cooldown < .75?1: 4*(1-cooldown);

									float uMin = (88+(right?0: 7*x2))/256f;
									float uMax = (88+(right?7*x2: 0))/256f;
									float vMin1 = (112+(right?h1: h2)*15)/256f;
									float vMin2 = (112+(right?h2: h1)*15)/256f;

									IVertexBuilder builder = buffer.getBuffer(IERenderTypes.getGui(rl("textures/gui/hud_elements.png")));
									Matrix4f mat = transform.getLast().getMatrix();
									builder.pos(mat, (right?0: 1-x2)*7, 15, 0)
											.color(1F, 1F, 1F, 1F)
											.tex(uMin, 127/256f)
											.endVertex();
									builder.pos(mat, (right?x2: 1)*7, 15, 0)
											.color(1F, 1F, 1F, 1F)
											.tex(uMax, 127/256f)
											.endVertex();
									builder.pos(mat, (right?x2: 1)*7, (right?h2: h1)*15, 0)
											.color(1F, 1F, 1F, 1F)
											.tex(uMax, vMin2)
											.endVertex();
									builder.pos(mat, (right?0: 1-x2)*7, (right?h1: h2)*15, 0)
											.color(1F, 1F, 1F, 1F)
											.tex(uMin, vMin1)
											.endVertex();
								}
							}
							transform.pop();
						}
					}
					else if(equipped.getItem() instanceof RailgunItem)
					{
						int duration = 72000-(player.isHandActive()&&player.getActiveHand()==hand?player.getItemInUseCount(): 0);
						int chargeTime = ((RailgunItem)equipped.getItem()).getChargeTime(equipped);
						int chargeLevel = duration < 72000?Math.min(99, (int)(duration/(float)chargeTime*100)): 0;
						float scale = 2f;
						transform.push();
						transform.translate(scaledWidth-80, scaledHeight-30, 0);
						transform.scale(scale, scale, 1);
						ClientProxy.nixieFont.renderString(
								(chargeLevel < 10?"0": "")+chargeLevel, 0, 0, Lib.colour_nixieTubeText,
								true, transform.getLast().getMatrix(),
								buffer, false, 0, 0xf000f0
						);
						transform.pop();
					}
					else if(equipped.getItem() instanceof DrillItem||equipped.getItem() instanceof ChemthrowerItem||equipped.getItem() instanceof BuzzsawItem)
					{
						boolean drill = equipped.getItem() instanceof DrillItem;
						boolean buzzsaw = equipped.getItem() instanceof BuzzsawItem;
						IVertexBuilder builder = buffer.getBuffer(IERenderTypes.getGui(rl("textures/gui/hud_elements.png")));
						float dx = scaledWidth-16;
						float dy = scaledHeight;
						transform.push();
						transform.translate(dx, dy, 0);
						int w = 31;
						int h = 62;
						float uMin = 179/256f;
						float uMax = 210/256f;
						float vMin = 9/256f;
						float vMax = 71/256f;
						ClientUtils.drawTexturedRect(builder, transform, -24, -68, w, h, 1, 1, 1, 1, uMin, uMax, vMin, vMax);

						transform.translate(-23, -37, 0);
						LazyOptional<IFluidHandlerItem> handlerOpt = FluidUtil.getFluidHandler(equipped);
						handlerOpt.ifPresent(handler -> {
							int capacity = -1;
							if(handler.getTanks() > 0)
								capacity = handler.getTankCapacity(0);
							if(capacity > 0)
							{
								FluidStack fuel = handler.getFluidInTank(0);
								int amount = fuel.getAmount();
								if(!drill&&player.isHandActive()&&player.getActiveHand()==hand)
								{
									int use = player.getItemInUseMaxCount();
									amount -= use*IEConfig.TOOLS.chemthrower_consumption.get();
								}
								float cap = (float)capacity;
								float angle = 83-(166*amount/cap);
								transform.push();
								transform.rotate(new Quaternion(0, 0, angle, true));
								ClientUtils.drawTexturedRect(builder, transform, 6, -2, 24, 4, 1, 1, 1, 1, 91/256f, 123/256f, 80/256f, 87/256f);
								transform.pop();
								transform.translate(23, 37, 0);
								if(drill)
								{
									ClientUtils.drawTexturedRect(builder, transform, -54, -73, 66, 72, 1, 1, 1, 1, 108/256f, 174/256f, 4/256f, 76/256f);
									ItemStack head = ((DrillItem)equipped.getItem()).getHead(equipped);
									if(!head.isEmpty())
										ClientUtils.renderItemWithOverlayIntoGUI(buffer, transform, head, -51, -45);
								}
								else if(buzzsaw)
								{
									ClientUtils.drawTexturedRect(builder, transform, -54, -73, 66, 72, 1, 1, 1, 1, 108/256f, 174/256f, 4/256f, 76/256f);
									ItemStack blade = ((BuzzsawItem)equipped.getItem()).getSawblade(equipped);
									if(!blade.isEmpty())
										ClientUtils.renderItemWithOverlayIntoGUI(buffer, transform, blade, -51, -45);
								}
								else
								{
									ClientUtils.drawTexturedRect(builder, transform, -41, -73, 53, 72, 1, 1, 1, 1, 8/256f, 61/256f, 4/256f, 76/256f);
									boolean ignite = ItemNBTHelper.getBoolean(equipped, "ignite");
									ClientUtils.drawTexturedRect(builder, transform, -32, -43, 12, 12, 1, 1, 1, 1, 66/256f, 78/256f, (ignite?21: 9)/256f, (ignite?33: 21)/256f);

									ClientUtils.drawTexturedRect(builder, transform, -100, -20, 64, 16, 1, 1, 1, 1, 0/256f, 64/256f, 76/256f, 92/256f);
									if(!fuel.isEmpty())
									{
										String name = ClientUtils.font().trimStringToWidth(fuel.getDisplayName().getFormattedText(), 50).trim();
										ClientUtils.font().renderString(
												name, -68-ClientUtils.font().getStringWidth(name)/2, -15, 0,
												false, transform.getLast().getMatrix(), buffer, false,
												0, 0xf000f0
										);
									}
								}
							}
						});
						transform.pop();
					}
					else if(equipped.getItem() instanceof IEShieldItem)
					{
						CompoundNBT upgrades = ((IEShieldItem)equipped.getItem()).getUpgrades(equipped);
						if(!upgrades.isEmpty())
						{
							IVertexBuilder builder = buffer.getBuffer(IERenderTypes.getGui(rl("textures/gui/hud_elements.png")));
							boolean boundLeft = (player.getPrimaryHand()==HandSide.RIGHT)==(hand==Hand.OFF_HAND);
							float dx = boundLeft?16: (scaledWidth-16-64);
							float dy = scaledHeight;
							transform.push();
							transform.translate(dx, dy, 0);
							ClientUtils.drawTexturedRect(builder, transform, 0, -22, 64, 22, 0, 1, 1, 1, 1, 64/256f, 176/256f, 198/256f);

							if(upgrades.getBoolean("flash"))
							{
								ClientUtils.drawTexturedRect(builder, transform, 11, -38, 16, 16, 11/256f, 1, 1, 1, 1, 27/256f, 160/256f, 176/256f);
								if(upgrades.contains("flash_cooldown"))
								{
									float h = upgrades.getInt("flash_cooldown")/40f*16;
									ClientUtils.drawTexturedRect(builder, transform, 11, -22-h, 16, h, 1, 1, 1, 1, 11/256f, 27/256f, (214-h)/256f, 214/256f);
								}
							}
							if(upgrades.getBoolean("shock"))
							{
								ClientUtils.drawTexturedRect(builder, transform, 40, -38, 12, 16, 1, 1, 1, 1, 40/256f, 52/256f, 160/256f, 176/256f);
								if(upgrades.contains("shock_cooldown"))
								{
									float h = upgrades.getInt("shock_cooldown")/40f*16;
									ClientUtils.drawTexturedRect(builder, transform, 40, -22-h, 12, h, 1, 1, 1, 1, 40/256f, 52/256f, (214-h)/256f, 214/256f);
								}
							}
							transform.pop();
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
							RenderSystem.enableBlend();
							for(String s : text)
								if(s!=null)
								{
									s = s.trim();
									int w = ClientProxy.nixieFontOptional.getStringWidth(s);
									ClientProxy.nixieFontOptional.renderString(
											s, scaledWidth/2-w/2,
											scaledHeight/2-4-text.length*(ClientProxy.nixieFontOptional.getFontHeight()+2)+
													(i++)*(ClientProxy.nixieFontOptional.getFontHeight()+2), col,
											false, transform.getLast().getMatrix(), buffer, false,
											0, 0xf000f0
									);
								}
							RenderSystem.disableBlend();
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
									font.renderString(
											s, scaledWidth/2+8, scaledHeight/2+8+(i++)*font.FONT_HEIGHT, col, true,
											transform.getLast().getMatrix(), buffer, false, 0, 0xf000f0
									);
						}
					}
				}
			}
			buffer.finish();
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
			float saturation = Math.max(0.25f, 1-timeLeft/(float)(80+40*effect.getAmplifier()));//Total Time =  4s + 2s per amplifier

			float f1 = -2.5f+15.0F*saturation;
			if(timeLeft < 20)
				f1 += (event.getFarPlaneDistance()/4)*(1-timeLeft/20f);

			RenderSystem.fogStart(0.25f*f1);
			RenderSystem.fogEnd(f1);
			RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
			RenderSystem.setupNvFogDistance();
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
				if(IEConfig.TOOLS.chemthrower_scroll.get()&&equipped.getItem() instanceof IScrollwheel)
				{
					ImmersiveEngineering.packetHandler.sendToServer(new MessageScrollwheelItem(event.getScrollDelta() < 0));
					event.setCanceled(true);
				}
				if(equipped.getItem() instanceof RevolverItem)
				{
					ImmersiveEngineering.packetHandler.sendToServer(new MessageRevolverRotate(event.getScrollDelta() < 0));
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent()
	public void renderAdditionalBlockBounds(DrawHighlightEvent event)
	{
		if(event.getTarget().getType()==Type.BLOCK)
		{
			MatrixStack transform = event.getMatrix();
			IRenderTypeBuffer buffer = event.getBuffers();
			BlockRayTraceResult rtr = (BlockRayTraceResult)event.getTarget();
			BlockPos pos = rtr.getPos();
			Vec3d renderView = event.getInfo().getProjectedView();
			transform.push();
			transform.translate(-renderView.x, -renderView.y, -renderView.z);
			transform.translate(pos.getX(), pos.getY(), pos.getZ());
			Entity player = event.getInfo().getRenderViewEntity();
			float f1 = 0.002F;
			TileEntity tile = player.world.getTileEntity(rtr.getPos());
			ItemStack stack = player instanceof LivingEntity?((LivingEntity)player).getHeldItem(Hand.MAIN_HAND): ItemStack.EMPTY;

			if(Utils.isHammer(stack)&&tile instanceof TurntableTileEntity)
			{
				TurntableTileEntity turntableTile = ((TurntableTileEntity)tile);
				Direction side = rtr.getFace();
				Direction facing = turntableTile.getFacing();
				if(side.getAxis()!=facing.getAxis())
				{
					transform.push();
					transform.translate(0.5, 0.5, 0.5);
					ClientUtils.toModelRotation(side).getRotation().push(transform);
					transform.rotate(new Quaternion(-90, 0, 0, true));
					Rotation rotation = turntableTile.getRotationFromSide(side);
					boolean cw180 = rotation==Rotation.CLOCKWISE_180;
					double angle;
					if(cw180)
						angle = player.ticksExisted%40/20d;
					else
						angle = player.ticksExisted%80/40d;
					double stepDistance = (cw180?2: 4)*Math.PI;
					angle = -(angle-Math.sin(angle*stepDistance)/stepDistance)*Math.PI;
					drawCircularRotationArrows(buffer, transform, (float) angle, rotation==Rotation.COUNTERCLOCKWISE_90, cw180);
					transform.pop();
				}
			}

			World world = player.world;
			if(!stack.isEmpty()&&ConveyorHandler.conveyorBlocks.containsValue(Block.getBlockFromItem(stack.getItem()))&&rtr.getFace().getAxis()==Axis.Y)
			{
				Direction side = rtr.getFace();
				VoxelShape shape = world.getBlockState(pos).getRenderShape(world, pos);
				AxisAlignedBB targetedBB = null;
				if(!shape.isEmpty())
					targetedBB = shape.getBoundingBox();

				IRenderTypeBuffer buffers = event.getBuffers();
				float[][] points = new float[4][];


				if(side.getAxis()==Axis.Y)
				{
					float y = (float)(targetedBB==null?0: side==Direction.DOWN?targetedBB.minY-f1: targetedBB.maxY+f1);
					points[0] = new float[]{0-f1, y, 0-f1};
					points[1] = new float[]{1+f1, y, 1+f1};
					points[2] = new float[]{0-f1, y, 1+f1};
					points[3] = new float[]{1+f1, y, 0-f1};
				}
				else if(side.getAxis()==Axis.Z)
				{
					float z = (float)(targetedBB==null?0: side==Direction.NORTH?targetedBB.minZ-f1: targetedBB.maxZ+f1);
					points[0] = new float[]{1+f1, 1+f1, z};
					points[1] = new float[]{0-f1, 0-f1, z};
					points[2] = new float[]{0-f1, 1+f1, z};
					points[3] = new float[]{1+f1, 0-f1, z};
				}
				else
				{
					float x = (float)(targetedBB==null?0: side==Direction.WEST?targetedBB.minX-f1: targetedBB.maxX+f1);
					points[0] = new float[]{x, 1+f1, 1+f1};
					points[1] = new float[]{x, 0-f1, 0-f1};
					points[2] = new float[]{x, 1+f1, 0-f1};
					points[3] = new float[]{x, 0-f1, 1+f1};
				}
				Matrix4f mat = transform.getLast().getMatrix();
				IVertexBuilder lineBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_LINES);
				for(float[] point : points)
					lineBuilder.pos(mat, point[0], point[1], point[2])
							.color(0, 0, 0, 0.4F)
							.endVertex();

				lineBuilder.pos(mat, points[0][0], points[0][1], points[0][2]).color(0, 0, 0, 0.4F).endVertex();
				lineBuilder.pos(mat, points[2][0], points[2][1], points[2][2]).color(0, 0, 0, 0.4F).endVertex();
				lineBuilder.pos(mat, points[1][0], points[1][1], points[1][2]).color(0, 0, 0, 0.4F).endVertex();
				lineBuilder.pos(mat, points[3][0], points[3][1], points[3][2]).color(0, 0, 0, 0.4F).endVertex();
				lineBuilder.pos(mat, points[0][0], points[0][1], points[0][2]).color(0, 0, 0, 0.4F).endVertex();

				float xFromMid = side.getAxis()==Axis.X?0: (float)rtr.getHitVec().x-pos.getX()-.5f;
				float yFromMid = side.getAxis()==Axis.Y?0: (float)rtr.getHitVec().y-pos.getY()-.5f;
				float zFromMid = side.getAxis()==Axis.Z?0: (float)rtr.getHitVec().z-pos.getZ()-.5f;
				float max = Math.max(Math.abs(yFromMid), Math.max(Math.abs(xFromMid), Math.abs(zFromMid)));
				Vec3d dir = new Vec3d(max==Math.abs(xFromMid)?Math.signum(xFromMid): 0, max==Math.abs(yFromMid)?Math.signum(yFromMid): 0, max==Math.abs(zFromMid)?Math.signum(zFromMid): 0);
				drawBlockOverlayArrow(mat, buffers, dir, side, targetedBB);

			}

			if(!stack.isEmpty()&&stack.getItem() instanceof DrillItem&&
					((DrillItem)stack.getItem()).isEffective(world.getBlockState(rtr.getPos()).getMaterial()))
			{
				ItemStack head = ((DrillItem)stack.getItem()).getHead(stack);
				if(!head.isEmpty()&&player instanceof PlayerEntity)
				{
					ImmutableList<BlockPos> blocks = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world,
							(PlayerEntity)player, event.getTarget());
					drawAdditionalBlockbreak(event, (PlayerEntity)player, event.getPartialTicks(), blocks);
				}
			}
			transform.pop();
		}
	}

	private final static float[][] quarterRotationArrowCoords = {
			{.375F, 0},
			{.5F, -.125F},
			{.4375F, -.125F},
			{.4375F, -.25F},
			{.25F, -.4375F},
			{0, -.4375F},
			{0, -.3125F},
			{.1875F, -.3125F},
			{.3125F, -.1875F},
			{.3125F, -.125F},
			{.25F, -.125F}
	};
	private final static float[][] quarterRotationArrowQuads = {
			quarterRotationArrowCoords[5],
			quarterRotationArrowCoords[6],
			quarterRotationArrowCoords[4],
			quarterRotationArrowCoords[7],
			quarterRotationArrowCoords[3],
			quarterRotationArrowCoords[8],
			quarterRotationArrowCoords[2],
			quarterRotationArrowCoords[9],
			quarterRotationArrowCoords[1],
			quarterRotationArrowCoords[10],
			quarterRotationArrowCoords[0],
			quarterRotationArrowCoords[0]
	};

	private final static float[][] halfRotationArrowCoords = {
			{.375F, 0},
			{.5F, -.125F},
			{.4375F, -.125F},
			{.4375F, -.25F},
			{.25F, -.4375F},
			{-.25F, -.4375F},
			{-.4375F, -.25F},
			{-.4375F, -.0625F},
			{-.3125F, -.0625F},
			{-.3125F, -.1875F},
			{-.1875F, -.3125F},
			{.1875F, -.3125F},
			{.3125F, -.1875F},
			{.3125F, -.125F},
			{.25F, -.125F}
	};
	private final static float[][] halfRotationArrowQuads = {
			halfRotationArrowCoords[7],
			halfRotationArrowCoords[8],
			halfRotationArrowCoords[6],
			halfRotationArrowCoords[9],
			halfRotationArrowCoords[5],
			halfRotationArrowCoords[10],
			halfRotationArrowCoords[4],
			halfRotationArrowCoords[11],
			halfRotationArrowCoords[3],
			halfRotationArrowCoords[12],
			halfRotationArrowCoords[2],
			halfRotationArrowCoords[13],
			halfRotationArrowCoords[1],
			halfRotationArrowCoords[14],
			halfRotationArrowCoords[0],
			halfRotationArrowCoords[0]
	};

	public static void drawCircularRotationArrows(IRenderTypeBuffer buffer, MatrixStack transform, float rotation,  boolean flip, boolean halfCircle)
	{
		transform.push();
		transform.translate(0, 0.502, 0);
		float[][] rotationArrowCoords;
		float[][] rotationArrowQuads;
		if(halfCircle)
		{
			rotationArrowCoords = halfRotationArrowCoords;
			rotationArrowQuads = halfRotationArrowQuads;
		}
		else
		{
			rotationArrowCoords = quarterRotationArrowCoords;
			rotationArrowQuads = quarterRotationArrowQuads;
		}

		int[] vertexOrder;
		if(flip)
		{
			transform.rotate(new Quaternion(0, -rotation, 0, false));
			transform.scale(1, 1, -1);
			vertexOrder = new int[]{2, 3, 1, 0};
		}
		else
		{
			transform.rotate(new Quaternion(0, rotation, 0, false));
			vertexOrder = new int[]{0, 1, 3, 2};
		}
		transform.push();
		IVertexBuilder builder = buffer.getBuffer(IERenderTypes.LINES);
		for(int arrowId = 0; arrowId < 2; ++arrowId)
		{
			Matrix4f mat = transform.getLast().getMatrix();
			for(int i = 0; i <= rotationArrowCoords.length; i++)
			{
				float[] p = rotationArrowCoords[i%rotationArrowCoords.length];
				if(i > 0)
					builder.pos(mat, p[0], 0, p[1]).color(0, 0, 0, 0.4F).endVertex();
				if(i!=rotationArrowCoords.length)
					builder.pos(mat, p[0], 0, p[1]).color(0, 0, 0, 0.4F).endVertex();
			}
			transform.rotate(new Quaternion(0, 180, 0, true));
		}
		transform.pop();
		transform.push();
		builder = buffer.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
		for(int arrowId = 0; arrowId < 2; ++arrowId)
		{
			Matrix4f mat = transform.getLast().getMatrix();
			for(int i = 0; i+3 < rotationArrowQuads.length; i += 2)
				for(int offset : vertexOrder)
				{
					float[] p = rotationArrowQuads[i+offset];
					builder.pos(mat, p[0], 0, p[1]).color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F).endVertex();
				}
			transform.rotate(new Quaternion(0, 180, 0, true));
		}
		transform.pop();
		transform.pop();
	}

	private final static float[][] arrowCoords = {{0, .375f}, {.3125f, .0625f}, {.125f, .0625f}, {.125f, -.375f}, {-.125f, -.375f}, {-.125f, .0625f}, {-.3125f, .0625f}};

	public static void drawBlockOverlayArrow(Matrix4f transform, IRenderTypeBuffer buffers, Vec3d directionVec,
											 Direction side, AxisAlignedBB targetedBB)
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

		IVertexBuilder triBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_TRIANGLES);
		Vec3d center = translatedPositions[0];
		for(int i = 2; i < translatedPositions.length; i++)
		{
			Vec3d point = translatedPositions[i];
			Vec3d prevPoint = translatedPositions[i-1];
			for(Vec3d p : new Vec3d[]{center, prevPoint, point})
				triBuilder.pos(transform, (float)p.x, (float)p.y, (float)p.z)
						.color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F)
						.endVertex();
		}
		IVertexBuilder lineBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_LINES);
		for(int i = 0; i <= translatedPositions.length; i++)
		{
			Vec3d point = translatedPositions[i%translatedPositions.length];
			int max = i==0||i==translatedPositions.length?1: 2;
			for(int j = 0; j < max; ++j)
				lineBuilder.pos(transform, (float)point.x, (float)point.y, (float)point.z)
						.color(0, 0, 0, 0.4F)
						.endVertex();
		}
	}

	public static void drawAdditionalBlockbreak(DrawHighlightEvent ev, PlayerEntity player, float partialTicks, Collection<BlockPos> blocks)
	{
		for(BlockPos pos : blocks)
			ev.getContext().drawSelectionBox(
					ev.getMatrix(),
					ev.getBuffers().getBuffer(RenderType.getLines()),
					player,
					0, 0, 0,
					pos,
					ClientUtils.mc().world.getBlockState(pos)
			);

		PlayerController controllerMP = ClientUtils.mc().playerController;
		if(controllerMP.isHittingBlock)
			ClientUtils.drawBlockDamageTexture(ClientUtils.tes(), ev.getMatrix(), ClientUtils.tes().getBuffer(), player, partialTicks, player.world, blocks);
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
		MatrixStack transform = event.getMatrixStack();
		transform.push();
		Vec3d renderView = ClientUtils.mc().gameRenderer.getActiveRenderInfo().getProjectedView();
		transform.translate(-renderView.x, -renderView.y, -renderView.z);
		IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		if(!FractalParticle.PARTICLE_FRACTAL_DEQUE.isEmpty())
		{
			List<Pair<RenderType, List<Consumer<IVertexBuilder>>>> renders = new ArrayList<>();
			for(FractalParticle p : FractalParticle.PARTICLE_FRACTAL_DEQUE)
				for(Entry<RenderType, Consumer<IVertexBuilder>> r : p.render(partial, transform))
				{
					boolean added = false;
					for(Entry<RenderType, List<Consumer<IVertexBuilder>>> e : renders)
						if(e.getKey().equals(r.getKey()))
						{
							e.getValue().add(r.getValue());
							added = true;
							break;
						}
					if(!added)
						renders.add(Pair.of(r.getKey(), new ArrayList<>(ImmutableList.of(r.getValue()))));
				}
			for(Entry<RenderType, List<Consumer<IVertexBuilder>>> entry : renders)
			{
				IVertexBuilder bb = buffers.getBuffer(entry.getKey());
				for(Consumer<IVertexBuilder> render : entry.getValue())
					render.accept(bb);
			}
			FractalParticle.PARTICLE_FRACTAL_DEQUE.clear();
		}

		if(chunkBorders)
		{
			transform.push();
			PlayerEntity player = ClientUtils.mc().player;
			int chunkX = (int)player.getPosX() >> 4<<4;
			int chunkZ = (int)player.getPosZ() >> 4<<4;
			int y = Math.min((int)player.getPosY()-2, 0);
			float h = (float)Math.max(32, player.getPosY()-y+4);

			float r = Lib.COLOUR_F_ImmersiveOrange[0];
			float g = Lib.COLOUR_F_ImmersiveOrange[1];
			float b = Lib.COLOUR_F_ImmersiveOrange[2];
			transform.translate(chunkX, y+2, chunkZ);
			IVertexBuilder bufferBuilder = buffers.getBuffer(IERenderTypes.CHUNK_MARKER);
			Matrix4f mat = transform.getLast().getMatrix();
			bufferBuilder.pos(mat, 0, 0, 0).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 0, h, 0).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 16, 0, 0).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 16, h, 0).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 16, 0, 16).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 16, h, 16).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 0, 0, 16).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 0, h, 16).color(r, g, b, .375f).endVertex();

			bufferBuilder.pos(mat, 0, 2, 0).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 16, 2, 0).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 0, 2, 0).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 0, 2, 16).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 0, 2, 16).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 16, 2, 16).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 16, 2, 0).color(r, g, b, .375f).endVertex();
			bufferBuilder.pos(mat, 16, 2, 16).color(r, g, b, .375f).endVertex();
			transform.pop();
		}

		if(!FAILED_CONNECTIONS.isEmpty())
		{
			IVertexBuilder builder = buffers.getBuffer(IERenderTypes.CHUNK_MARKER);
			for(Entry<Connection, Pair<Collection<BlockPos>, AtomicInteger>> entry : FAILED_CONNECTIONS.entrySet())
			{
				Connection conn = entry.getKey();
				transform.push();
				transform.translate(conn.getEndA().getX(), conn.getEndA().getY(), conn.getEndA().getZ());
				Matrix4f mat = transform.getLast().getMatrix();
				int time = entry.getValue().getValue().get();
				float alpha = (float)Math.min((2+Math.sin(time*Math.PI/40))/3, time/20F);
				Vec3d prev = conn.getPoint(0, conn.getEndA());
				for(int i = 0; i < RenderData.POINTS_PER_WIRE; i++)
				{
					builder.pos(mat, (float)prev.x, (float)prev.y, (float)prev.z)
							.color(1, 0, 0, alpha).endVertex();
					alpha = (float)Math.min((2+Math.sin((time+(i+1)*8)*Math.PI/40))/3, time/20F);
					Vec3d next = conn.getPoint((i+1)/(double)RenderData.POINTS_PER_WIRE, conn.getEndA());
					builder.pos(mat, (float)next.x, (float)next.y, (float)next.z)
							.color(1, 0, 0, alpha).endVertex();
					prev = next;
				}
				transform.pop();
			}
			renderObstructingBlocks(transform, buffers);
		}
		transform.pop();
		buffers.finish();
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
			((IHasHead)m).getModelHead().showModel = shouldEnable;
	}
}
