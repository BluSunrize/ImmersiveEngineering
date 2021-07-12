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
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.RenderData;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.client.gui.BlastFurnaceScreen;
import blusunrize.immersiveengineering.client.gui.RevolverScreen;
import blusunrize.immersiveengineering.client.render.tile.AutoWorkbenchRenderer;
import blusunrize.immersiveengineering.client.render.tile.AutoWorkbenchRenderer.BlueprintLines;
import blusunrize.immersiveengineering.client.utils.*;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.wooden.TurntableTileEntity;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.IEMinecartEntity;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.network.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.CuriosCompatModule;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledSound;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledTickableSound;
import blusunrize.immersiveengineering.mixin.accessors.client.GPUWarningAccess;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.VideoSettingsScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
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
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class ClientEventHandler implements ISelectiveResourceReloadListener
{
	private static final boolean ENABLE_VEIN_DEBUG = false;
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

				if(!ClientProxy.keybind_railgunZoom.isInvalid()&&ClientProxy.keybind_railgunZoom.isPressed())
					for(Hand hand : Hand.values())
					{
						ItemStack held = event.player.getHeldItem(hand);
						if(held.getItem() instanceof IZoomTool&&((IZoomTool)held.getItem()).canZoom(held, event.player))
						{
							ZoomHandler.isZooming = !ZoomHandler.isZooming;
							if(ZoomHandler.isZooming)
							{
								float[] steps = ((IZoomTool)held.getItem()).getZoomSteps(held, event.player);
								if(steps!=null&&steps.length > 0)
									ZoomHandler.fovZoom = steps[ZoomHandler.getCurrentZoomStep(steps)];
							}
						}
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
		if(event.getItemStack().isEmpty())
			return;
		event.getItemStack().getCapability(CapabilityShader.SHADER_CAPABILITY).ifPresent(wrapper ->
		{
			ItemStack shader = wrapper.getShaderItem();
			if(!shader.isEmpty())
				event.getToolTip().add(TextUtils.applyFormat(
						shader.getDisplayName(),
						TextFormatting.DARK_GRAY
				));
		});
		if(ItemNBTHelper.hasKey(event.getItemStack(), Lib.NBT_Earmuffs))
		{
			ItemStack earmuffs = ItemNBTHelper.getItemStack(event.getItemStack(), Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty())
				event.getToolTip().add(TextUtils.applyFormat(
						earmuffs.getDisplayName(),
						TextFormatting.GRAY
				));
		}
		if(ItemNBTHelper.hasKey(event.getItemStack(), Lib.NBT_Powerpack))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(event.getItemStack(), Lib.NBT_Powerpack);
			if(!powerpack.isEmpty())
			{
				event.getToolTip().add(TextUtils.applyFormat(
						powerpack.getDisplayName(),
						TextFormatting.GRAY
				));
				event.getToolTip().add(TextUtils.applyFormat(
						new StringTextComponent(EnergyHelper.getEnergyStored(powerpack)+"/"+EnergyHelper.getMaxEnergyStored(powerpack)+" IF"),
						TextFormatting.GRAY
				));
			}
		}
		if(ClientUtils.mc().currentScreen!=null
				&&ClientUtils.mc().currentScreen instanceof BlastFurnaceScreen
				&&BlastFurnaceFuel.isValidBlastFuel(event.getItemStack()))
			event.getToolTip().add(TextUtils.applyFormat(
					new TranslationTextComponent("desc.immersiveengineering.info.blastFuelTime", BlastFurnaceFuel.getBlastFuelTime(event.getItemStack())),
					TextFormatting.GRAY
			));

		if(IEClientConfig.tagTooltips.get()&&event.getFlags().isAdvanced())
		{
			for(ResourceLocation oid : ItemTags.getCollection().getOwningTags(event.getItemStack().getItem()))
				event.getToolTip().add(TextUtils.applyFormat(
						new StringTextComponent(oid.toString()),
						TextFormatting.GRAY
				));
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
				List<String> linesString = event.getLines().stream()
						.map(ITextProperties::getString)
						.collect(Collectors.toList());
				int line = event.getLines().size()-Utils.findSequenceInList(linesString, BULLET_TOOLTIP, (a, b) -> b.endsWith(a));

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
		if(ClientUtils.mc().player!=null)
		{
			ItemStack head = ClientUtils.mc().player.getItemStackFromSlot(EquipmentSlotType.HEAD);
			ItemStack earmuffs = ItemStack.EMPTY;
			if(!head.isEmpty()&&(head.getItem()==Misc.earmuffs||ItemNBTHelper.hasKey(head, Lib.NBT_Earmuffs)))
				earmuffs = head.getItem()==Misc.earmuffs?head: ItemNBTHelper.getItemStack(head, Lib.NBT_Earmuffs);
			else if(ModList.get().isLoaded("curios"))
				earmuffs = CuriosCompatModule.getEarmuffs(ClientUtils.mc().player);

			if(!earmuffs.isEmpty()&&
					!ItemNBTHelper.getBoolean(earmuffs, "IE:Earmuffs:Cat_"+event.getSound().getCategory().getName()))
			{
				for(String blacklist : IEClientConfig.earDefenders_SoundBlacklist.get())
					if(blacklist!=null&&blacklist.equalsIgnoreCase(event.getSound().getSoundLocation().toString()))
						return;
				if(event.getSound() instanceof ITickableSound)
					event.setResultSound(new IEMuffledTickableSound((ITickableSound)event.getSound(), EarmuffsItem.getVolumeMod(earmuffs)));
				else
					event.setResultSound(new IEMuffledSound(event.getSound(), EarmuffsItem.getVolumeMod(earmuffs)));
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
				RenderUtils.renderBox(builder, transform, -eps, -eps, -eps, 1+eps, 1+eps, 1+eps);
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
						GuiHelper.drawTexturedColoredRect(builder, transform, .125f, -.875f, .75f, .75f, 1, 1, 1, 1, 1, 0, 1, 0);
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

	private static void handleSubtitleOffset(boolean pre)
	{
		float offset = 0;
		PlayerEntity player = ClientUtils.mc().player;
		for(Hand hand : Hand.values())
			if(!player.getHeldItem(hand).isEmpty())
			{
				Item equipped = player.getHeldItem(hand).getItem();
				if(equipped instanceof RevolverItem||equipped instanceof SpeedloaderItem)
					offset = 50f;
				else if(equipped instanceof DrillItem||equipped instanceof ChemthrowerItem||equipped instanceof BuzzsawItem)
					offset = 50f;
				else if(equipped instanceof RailgunItem||equipped instanceof IEShieldItem)
					offset = 20f;
			}
		if(offset!=0)
		{
			if(pre)
				offset *= -1;
			GlStateManager.translatef(0, offset, 0);
		}
	}

	@SubscribeEvent
	public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event)
	{
		if(event.getType()==RenderGameOverlayEvent.ElementType.SUBTITLES)
			handleSubtitleOffset(true);
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
					GuiHelper.drawColouredRect(0, 0, width, (int)offsetY+1, 0xff000000, buffers, transform);
					GuiHelper.drawColouredRect(0, (int)offsetY+resMin, width, (int)offsetY+1, 0xff000000, buffers, transform);
				}
				else
				{
					GuiHelper.drawColouredRect(0, 0, (int)offsetX+1, height, 0xff000000, buffers, transform);
					GuiHelper.drawColouredRect((int)offsetX+resMin, 0, (int)offsetX+1, height, 0xff000000, buffers, transform);
				}
				transform.translate(offsetX, offsetY, 0);
				IVertexBuilder builder = buffers.getBuffer(IERenderTypes.getGui(rl("textures/gui/scope.png")));
				GuiHelper.drawTexturedColoredRect(builder, transform, 0, 0, resMin, resMin, 1, 1, 1, 1, 0f, 1f, 0f, 1f);

				builder = buffers.getBuffer(IERenderTypes.getGui(rl("textures/gui/hud_elements.png")));
				GuiHelper.drawTexturedColoredRect(builder, transform, 218/256f*resMin, 64/256f*resMin, 24/256f*resMin, 128/256f*resMin, 1, 1, 1, 1, 64/256f, 88/256f, 96/256f, 224/256f);
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
							GuiHelper.drawTexturedColoredRect(builder, transform, 0, 0, 8/256f*resMin, 7/256f*resMin, 1, 1, 1, 1, 88/256f, 96/256f, 96/256f, 103/256f);
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
							GuiHelper.drawTexturedColoredRect(builder, transform, 0, 0, 8/256f*resMin, 7/256f*resMin, 1, 1, 1, 1, 88/256f, 98/256f, 103/256f, 110/256f);
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

		if(event.getType()==RenderGameOverlayEvent.ElementType.SUBTITLES)
			handleSubtitleOffset(false);
		if(ClientUtils.mc().player!=null&&event.getType()==RenderGameOverlayEvent.ElementType.TEXT)
		{
			PlayerEntity player = ClientUtils.mc().player;
			MatrixStack transform = new MatrixStack();

			for(Hand hand : Hand.values())
				if(!player.getHeldItem(hand).isEmpty())
				{
					IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
					ItemStack equipped = player.getHeldItem(hand);
					if(ItemStack.areItemsEqual(new ItemStack(Tools.voltmeter), equipped)||equipped.getItem() instanceof IWireCoil)
					{
						if(WirecoilUtils.hasWireLink(equipped))
						{
							WireLink link = WireLink.readFromItem(equipped);
							BlockPos pos = link.cp.getPosition();
							String s = I18n.format(Lib.DESC_INFO+"attachedTo", pos.getX(), pos.getY(), pos.getZ());
							int col = WireType.ELECTRUM.getColour(null);
							if(equipped.getItem() instanceof IWireCoil)
							{
								//TODO use actual connection offset rather than pos
								RayTraceResult rtr = ClientUtils.mc().objectMouseOver;
								double d;
								if(rtr instanceof BlockRayTraceResult)
									d = ((BlockRayTraceResult)rtr).getPos().distanceSq(pos.getX(), pos.getY(), pos.getZ(), false);
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
						int color = FluorescentTubeItem.getRGBInt(equipped, 1);
						String s = I18n.format(Lib.DESC_INFO+"colour")+"#"+FontUtils.hexColorString(color);
						ClientUtils.font().renderString(s, scaledWidth/2-ClientUtils.font().getStringWidth(s)/2,
								scaledHeight-ForgeIngameGui.left_height-20, FluorescentTubeItem.getRGBInt(equipped, 1),
								true, transform.getLast().getMatrix(), buffer, false, 0, 0xf000f0
						);
					}
					else if(equipped.getItem() instanceof RevolverItem||equipped.getItem() instanceof SpeedloaderItem)
						ItemOverlayUtils.renderRevolverOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped);
					else if(equipped.getItem() instanceof RailgunItem)
						ItemOverlayUtils.renderRailgunOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped);
					else if(equipped.getItem() instanceof DrillItem)
						ItemOverlayUtils.renderDrillOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped);
					else if(equipped.getItem() instanceof BuzzsawItem)
						ItemOverlayUtils.renderBuzzsawOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped);
					else if(equipped.getItem() instanceof ChemthrowerItem)
						ItemOverlayUtils.renderChemthrowerOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped);
					else if(equipped.getItem() instanceof IEShieldItem)
						ItemOverlayUtils.renderShieldOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped);
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
							int col = 0xffffff;
							int i = 0;
							RenderSystem.enableBlend();
							for(String s : text)
								if(s!=null)
								{
									s = s.trim();
									int w = ClientUtils.font().getStringWidth(s);
									ClientUtils.font().renderString(
											s, scaledWidth/2-w/2,
											scaledHeight/2-4-text.length*(ClientUtils.font().FONT_HEIGHT+2)+
													(i++)*(ClientUtils.font().FONT_HEIGHT+2), col,
											false, transform.getLast().getMatrix(), buffer, false,
											0, 0xf000f0
									);
								}
							RenderSystem.disableBlend();
						}
					}
					buffer.finish();
				}
			if(ClientUtils.mc().objectMouseOver!=null)
			{
				ItemStack held = player.getHeldItem(Hand.MAIN_HAND);
				boolean hammer = !held.isEmpty()&&Utils.isHammer(held);
				RayTraceResult mop = ClientUtils.mc().objectMouseOver;
				if(mop instanceof EntityRayTraceResult)
				{
					Entity entity = ((EntityRayTraceResult)mop).getEntity();
					if(entity instanceof ItemFrameEntity)
						BlockOverlayUtils.renderOreveinMapOverlays(transform, (ItemFrameEntity)entity, mop, scaledWidth, scaledHeight);
					else if(entity instanceof IEMinecartEntity)
					{
						IEBaseTileEntity containedTile = ((IEMinecartEntity<?>)entity).getContainedTileEntity();
						if(containedTile instanceof IBlockOverlayText)
						{
							ITextComponent[] text = ((IBlockOverlayText)containedTile).getOverlayText(player, mop, false);
							BlockOverlayUtils.drawBlockOverlayText(transform, text, scaledWidth, scaledHeight);
						}
					}
				}
				else if(mop instanceof BlockRayTraceResult)
				{
					BlockPos pos = ((BlockRayTraceResult)mop).getPos();
					Direction face = ((BlockRayTraceResult)mop).getFace();
					TileEntity tileEntity = player.world.getTileEntity(pos);
					if(tileEntity instanceof IBlockOverlayText)
					{
						IBlockOverlayText overlayBlock = (IBlockOverlayText)tileEntity;
						ITextComponent[] text = overlayBlock.getOverlayText(ClientUtils.mc().player, mop, hammer);
						BlockOverlayUtils.drawBlockOverlayText(transform, text, scaledWidth, scaledHeight);
					}
					else
					{
						List<ItemFrameEntity> list = player.world.getEntitiesWithinAABB(ItemFrameEntity.class,
								new AxisAlignedBB(pos.offset(face)), entity -> entity!=null&&entity.getHorizontalFacing()==face);
						if(list.size()==1)
							BlockOverlayUtils.renderOreveinMapOverlays(transform, list.get(0), mop, scaledWidth, scaledHeight);
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

		// Check if player is holding a zoom-allowing item
		ItemStack equipped = player.getHeldItem(Hand.MAIN_HAND);
		boolean mayZoom = equipped.getItem() instanceof IZoomTool&&((IZoomTool)equipped.getItem()).canZoom(equipped, player);
		// Set zoom if allowed, otherwise stop zooming
		if(ZoomHandler.isZooming)
		{
			if(mayZoom)
				event.setNewfov(ZoomHandler.fovZoom);
			else
				ZoomHandler.isZooming = false;
		}

		// Concrete feet slow you, but shouldn't break FoV
		if(player.getActivePotionEffect(IEPotions.concreteFeet)!=null)
			event.setNewfov(1);
	}

	@SubscribeEvent
	public void onMouseEvent(MouseScrollEvent event)
	{
		PlayerEntity player = ClientUtils.mc().player;
		if(event.getScrollDelta()!=0&&ClientUtils.mc().currentScreen==null&&player!=null)
		{
			ItemStack equipped = player.getHeldItem(Hand.MAIN_HAND);
			// Handle zoom steps
			if(equipped.getItem() instanceof IZoomTool&&((IZoomTool)equipped.getItem()).canZoom(equipped, player)&&ZoomHandler.isZooming)
			{
				float[] steps = ((IZoomTool)equipped.getItem()).getZoomSteps(equipped, player);
				if(steps!=null&&steps.length > 0)
				{
					int curStep = ZoomHandler.getCurrentZoomStep(steps);
					int newStep = curStep+(event.getScrollDelta() > 0?-1: 1);
					if(newStep >= 0&&newStep < steps.length)
						ZoomHandler.fovZoom = steps[newStep];
					event.setCanceled(true);
				}
			}

			// Handle sneak + scrolling
			if(player.isSneaking())
			{
				if(IEServerConfig.TOOLS.chemthrower_scroll.get()&&equipped.getItem() instanceof IScrollwheel)
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
			Vector3d renderView = event.getInfo().getProjectedView();
			transform.push();
			transform.translate(-renderView.x, -renderView.y, -renderView.z);
			transform.translate(pos.getX(), pos.getY(), pos.getZ());
			Entity player = event.getInfo().getRenderViewEntity();
			float f1 = 0.002F;
			TileEntity tile = player.world.getTileEntity(rtr.getPos());
			ItemStack stack = player instanceof LivingEntity?((LivingEntity)player).getHeldItem(Hand.MAIN_HAND): ItemStack.EMPTY;

			if(tile instanceof TurntableTileEntity&&Utils.isHammer(stack))
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
					BlockOverlayUtils.drawCircularRotationArrows(buffer, transform, (float)angle, rotation==Rotation.COUNTERCLOCKWISE_90, cw180);
					transform.pop();
					transform.pop();
				}
			}

			World world = player.world;
			if(!stack.isEmpty()&&ConveyorHandler.conveyorBlocks.containsValue(Block.getBlockFromItem(stack.getItem()))&&rtr.getFace().getAxis()==Axis.Y)
			{
				Direction side = rtr.getFace();
				VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
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
				Vector3d dir = new Vector3d(max==Math.abs(xFromMid)?Math.signum(xFromMid): 0, max==Math.abs(yFromMid)?Math.signum(yFromMid): 0, max==Math.abs(zFromMid)?Math.signum(zFromMid): 0);
				BlockOverlayUtils.drawBlockOverlayArrow(mat, buffers, dir, side, targetedBB);

			}

			transform.pop();
			if(!stack.isEmpty()&&stack.getItem() instanceof DrillItem&&
					((DrillItem)stack.getItem()).isEffective(stack, world.getBlockState(rtr.getPos()).getMaterial()))
			{
				ItemStack head = ((DrillItem)stack.getItem()).getHead(stack);
				if(!head.isEmpty()&&player instanceof PlayerEntity&&!player.isSneaking())
				{
					ImmutableList<BlockPos> blocks = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world,
							(PlayerEntity)player, event.getTarget());
					BlockOverlayUtils.drawAdditionalBlockbreak(event, (PlayerEntity)player, blocks);
				}
			}
		}
	}

	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent event)
	{
		float partial = event.getPartialTicks();
		MatrixStack transform = event.getMatrixStack();
		transform.push();
		Vector3d renderView = ClientUtils.mc().gameRenderer.getActiveRenderInfo().getProjectedView();
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

		/* Debug for Mineral Veins */
		// !isProduction: Safety feature to make sure this doesn't run even if the enable flag is left on by accident
		boolean show = ENABLE_VEIN_DEBUG&&!FMLLoader.isProduction();
		if(show)
		{
			// Default <=> shift is sneak, use ctrl instead
			if(Minecraft.getInstance().gameSettings.keyBindSneak.isDefault())
				show = Screen.hasControlDown();
			else
				show = Screen.hasShiftDown();
		}
		if(show)
		{
			RegistryKey<World> dimension = ClientUtils.mc().player.getEntityWorld().getDimensionKey();
			List<ResourceLocation> keyList = new ArrayList<>(MineralMix.mineralList.keySet());
			keyList.sort(Comparator.comparing(ResourceLocation::toString));
			Multimap<RegistryKey<World>, MineralVein> minerals;
			final ColumnPos playerCol = new ColumnPos(ClientUtils.mc().player.getPosition());
			// 24: very roughly 16 * sqrt(2)
			final long maxDistance = ClientUtils.mc().gameSettings.renderDistanceChunks*24L;
			final long maxDistanceSq = maxDistance*maxDistance;
			synchronized(minerals = ExcavatorHandler.getMineralVeinList())
			{
				for(MineralVein vein : minerals.get(dimension))
				{
					if(vein.getMineral()==null)
						continue;
					transform.push();
					ColumnPos pos = vein.getPos();
					final long xDiff = pos.x-playerCol.x;
					final long zDiff = pos.z-playerCol.z;
					long distToPlayerSq = xDiff*xDiff+zDiff*zDiff;
					if(distToPlayerSq > maxDistanceSq)
						continue;
					int iC = keyList.indexOf(vein.getMineral().getId());
					DyeColor color = DyeColor.values()[iC%16];
					float[] rgb = color.getColorComponentValues();
					float r = rgb[0];
					float g = rgb[1];
					float b = rgb[2];
					transform.translate(pos.x, 0, pos.z);
					IVertexBuilder bufferBuilder = buffers.getBuffer(IERenderTypes.CHUNK_MARKER);
					Matrix4f mat = transform.getLast().getMatrix();
					bufferBuilder.pos(mat, 0, 0, 0).color(r, g, b, .75f).endVertex();
					bufferBuilder.pos(mat, 0, 128, 0).color(r, g, b, .75f).endVertex();

					bufferBuilder = buffers.getBuffer(IERenderTypes.VEIN_MARKER);
					int radius = vein.getRadius();
					float angle;
					double x1;
					double z1;
					for(int p = 0; p < 12; p++)
					{
						angle = 360.0f/12*p;
						x1 = radius*Math.cos(angle*Math.PI/180);
						z1 = radius*Math.sin(angle*Math.PI/180);
						bufferBuilder.pos(mat, (float)x1, 70, (float)z1).color(r, g, b, .75f).endVertex();
					}
					transform.pop();
				}
			}
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
				Vector3d prev = conn.getPoint(0, conn.getEndA());
				for(int i = 0; i < RenderData.POINTS_PER_WIRE; i++)
				{
					builder.pos(mat, (float)prev.x, (float)prev.y, (float)prev.z)
							.color(1, 0, 0, alpha).endVertex();
					alpha = (float)Math.min((2+Math.sin((time+(i+1)*8)*Math.PI/40))/3, time/20F);
					Vector3d next = conn.getPoint((i+1)/(double)RenderData.POINTS_PER_WIRE, conn.getEndA());
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

	@SubscribeEvent
	public void onScreenOpened(GuiScreenEvent.InitGuiEvent.Pre event)
	{
		if(event.getGui() instanceof VideoSettingsScreen&&ClientProxy.stencilEnabled)
		{
			GPUWarningAccess gpuWarning = (GPUWarningAccess)Minecraft.getInstance().getGPUWarning();
			final String key = "renderer";
			final String suffix = "tencil enabled in Immersive Engineering config";
			Map<String, String> oldWarnings = gpuWarning.getWarningStrings();
			;
			if(!oldWarnings.containsKey(key)||!oldWarnings.get(key).endsWith(suffix))
			{
				ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
				for(Entry<String, String> e : oldWarnings.entrySet())
					if(key.equals(e.getKey()))
						builder.put(key, e.getValue()+", s"+suffix);
					else
						builder.put(e.getKey(), e.getValue());
				if(!oldWarnings.containsKey(key))
					builder.put(key, "S"+suffix);
				gpuWarning.setWarningStrings(builder.build());
			}
		}
	}

	private static void enableHead(LivingRenderer renderer, boolean shouldEnable)
	{
		EntityModel m = renderer.getEntityModel();
		if(m instanceof IHasHead)
			((IHasHead)m).getModelHead().showModel = shouldEnable;
	}

	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinWorldEvent event)
	{
		if(event.getEntity().world.isRemote&&event.getEntity() instanceof AbstractMinecartEntity&&
				event.getEntity().getCapability(CapabilityShader.SHADER_CAPABILITY).isPresent())
			ImmersiveEngineering.packetHandler.sendToServer(new MessageMinecartShaderSync(event.getEntity(), null));
	}
}
