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
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
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
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if(resourcePredicate.test(VanillaResourceType.TEXTURES))
			ImmersiveEngineering.proxy.clearRenderCaches();
	}

	public static final Map<Connection, Pair<Collection<BlockPos>, AtomicInteger>> FAILED_CONNECTIONS = new HashMap<>();

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side==LogicalSide.CLIENT&&event.player!=null&&event.player==ClientUtils.mc().getCameraEntity())
		{
			if(event.phase==Phase.END)
			{
				if(this.shieldToggleTimer > 0)
					this.shieldToggleTimer--;
				if(ClientProxy.keybind_magnetEquip.isDown()&&!this.shieldToggleButton)
					if(this.shieldToggleTimer <= 0)
						this.shieldToggleTimer = 7;
					else
					{
						Player player = event.player;
						ItemStack held = player.getItemInHand(InteractionHand.OFF_HAND);
						if(!held.isEmpty()&&held.getItem() instanceof IEShieldItem)
						{
							if(((IEShieldItem)held.getItem()).getUpgrades(held).getBoolean("magnet")&&
									((IEShieldItem)held.getItem()).getUpgrades(held).contains("prevSlot"))
								ImmersiveEngineering.packetHandler.sendToServer(new MessageMagnetEquip(-1));
						}
						else
						{
							for(int i = 0; i < player.inventory.items.size(); i++)
							{
								ItemStack s = player.inventory.items.get(i);
								if(!s.isEmpty()&&s.getItem() instanceof IEShieldItem&&((IEShieldItem)s.getItem()).getUpgrades(s).getBoolean("magnet"))
									ImmersiveEngineering.packetHandler.sendToServer(new MessageMagnetEquip(i));
							}
						}
					}
				if(this.shieldToggleButton!=ClientUtils.mc().options.keyDown.isDown())
					this.shieldToggleButton = ClientUtils.mc().options.keyDown.isDown();


				if(!ClientProxy.keybind_chemthrowerSwitch.isUnbound()&&ClientProxy.keybind_chemthrowerSwitch.consumeClick())
				{
					ItemStack held = event.player.getItemInHand(InteractionHand.MAIN_HAND);
					if(held.getItem() instanceof IScrollwheel)
						ImmersiveEngineering.packetHandler.sendToServer(new MessageScrollwheelItem(true));
				}

				if(!ClientProxy.keybind_railgunZoom.isUnbound()&&ClientProxy.keybind_railgunZoom.consumeClick())
					for(InteractionHand hand : InteractionHand.values())
					{
						ItemStack held = event.player.getItemInHand(hand);
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
						shader.getHoverName(),
						ChatFormatting.DARK_GRAY
				));
		});
		if(ItemNBTHelper.hasKey(event.getItemStack(), Lib.NBT_Earmuffs))
		{
			ItemStack earmuffs = ItemNBTHelper.getItemStack(event.getItemStack(), Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty())
				event.getToolTip().add(TextUtils.applyFormat(
						earmuffs.getHoverName(),
						ChatFormatting.GRAY
				));
		}
		if(ItemNBTHelper.hasKey(event.getItemStack(), Lib.NBT_Powerpack))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(event.getItemStack(), Lib.NBT_Powerpack);
			if(!powerpack.isEmpty())
			{
				event.getToolTip().add(TextUtils.applyFormat(
						powerpack.getHoverName(),
						ChatFormatting.GRAY
				));
				event.getToolTip().add(TextUtils.applyFormat(
						new TextComponent(EnergyHelper.getEnergyStored(powerpack)+"/"+EnergyHelper.getMaxEnergyStored(powerpack)+" IF"),
						ChatFormatting.GRAY
				));
			}
		}
		if(ClientUtils.mc().screen!=null
				&&ClientUtils.mc().screen instanceof BlastFurnaceScreen
				&&BlastFurnaceFuel.isValidBlastFuel(event.getItemStack()))
			event.getToolTip().add(TextUtils.applyFormat(
					new TranslatableComponent("desc.immersiveengineering.info.blastFuelTime", BlastFurnaceFuel.getBlastFuelTime(event.getItemStack())),
					ChatFormatting.GRAY
			));

		if(IEClientConfig.tagTooltips.get()&&event.getFlags().isAdvanced())
		{
			for(ResourceLocation oid : ItemTags.getAllTags().getMatchingTags(event.getItemStack().getItem()))
				event.getToolTip().add(TextUtils.applyFormat(
						new TextComponent(oid.toString()),
						ChatFormatting.GRAY
				));
		}

		if(event.getItemStack().getItem() instanceof IBulletContainer)
			for(String s : BULLET_TOOLTIP)
				event.getToolTip().add(new TextComponent(s));
	}

	@SubscribeEvent
	public void onRenderTooltip(RenderTooltipEvent.PostText event)
	{
		ItemStack stack = event.getStack();
		if(stack.getItem() instanceof IBulletContainer)
		{
			NonNullList<ItemStack> bullets = ((IBulletContainer)stack.getItem()).getBullets(stack);
			if(bullets!=null)
			{
				int bulletAmount = ((IBulletContainer)stack.getItem()).getBulletCount(stack);
				List<String> linesString = event.getLines().stream()
						.map(FormattedText::getString)
						.collect(Collectors.toList());
				int line = event.getLines().size()-Utils.findSequenceInList(linesString, BULLET_TOOLTIP, (a, b) -> b.endsWith(a));

				int currentX = event.getX();
				int currentY = line > 0?event.getY()+(event.getHeight()+1-line*10): event.getY()-42;

				PoseStack transform = new PoseStack();
				transform.pushPose();
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
			event.getSound().getSource();
		}
		if(!EarmuffsItem.affectedSoundCategories.contains(event.getSound().getSource().getName()))
			return;
		if(ClientUtils.mc().player!=null)
		{
			ItemStack head = ClientUtils.mc().player.getItemBySlot(EquipmentSlot.HEAD);
			ItemStack earmuffs = ItemStack.EMPTY;
			if(!head.isEmpty()&&(head.getItem()==Misc.earmuffs||ItemNBTHelper.hasKey(head, Lib.NBT_Earmuffs)))
				earmuffs = head.getItem()==Misc.earmuffs?head: ItemNBTHelper.getItemStack(head, Lib.NBT_Earmuffs);
			else if(ModList.get().isLoaded("curios"))
				earmuffs = CuriosCompatModule.getEarmuffs(ClientUtils.mc().player);

			if(!earmuffs.isEmpty()&&
					!ItemNBTHelper.getBoolean(earmuffs, "IE:Earmuffs:Cat_"+event.getSound().getSource().getName()))
			{
				for(String blacklist : IEClientConfig.earDefenders_SoundBlacklist.get())
					if(blacklist!=null&&blacklist.equalsIgnoreCase(event.getSound().getLocation().toString()))
						return;
				if(event.getSound() instanceof TickableSoundInstance)
					event.setResultSound(new IEMuffledTickableSound((TickableSoundInstance)event.getSound(), EarmuffsItem.getVolumeMod(earmuffs)));
				else
					event.setResultSound(new IEMuffledSound(event.getSound(), EarmuffsItem.getVolumeMod(earmuffs)));
			}
		}
	}

	private void renderObstructingBlocks(PoseStack transform, MultiBufferSource buffers)
	{
		VertexConsumer baseBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
		TransformingVertexBuilder builder = new TransformingVertexBuilder(baseBuilder);
		builder.setColor(1, 0, 0, 0.5F);
		for(Entry<Connection, Pair<Collection<BlockPos>, AtomicInteger>> entry : FAILED_CONNECTIONS.entrySet())
		{
			for(BlockPos obstruction : entry.getValue().getKey())
			{
				transform.pushPose();
				transform.translate(obstruction.getX(), obstruction.getY(), obstruction.getZ());
				final float eps = 1e-3f;
				RenderUtils.renderBox(builder, transform, -eps, -eps, -eps, 1+eps, 1+eps, 1+eps);
				transform.popPose();
			}
		}
	}

	@SubscribeEvent
	public void onRenderItemFrame(RenderItemInFrameEvent event)
	{
		if(event.getItem().getItem() instanceof EngineersBlueprintItem)
		{
			double playerDistanceSq = ClientUtils.mc().player.distanceToSqr(event.getEntityItemFrame());

			if(playerDistanceSq < 1000)
			{
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(event.getItem(), "blueprint"));
				if(recipes.length > 0)
				{
					int i = event.getEntityItemFrame().getRotation();
					BlueprintCraftingRecipe recipe = recipes[i%recipes.length];
					BlueprintLines blueprint = recipe==null?null: AutoWorkbenchRenderer.getBlueprintDrawable(recipe, event.getEntityItemFrame().getCommandSenderWorld());
					if(blueprint!=null)
					{
						PoseStack transform = event.getMatrix();
						transform.pushPose();
						MultiBufferSource buffer = event.getBuffers();
						transform.mulPose(new Quaternion(0, 0, -i*45, true));
						transform.translate(-.5, .5, -.001);
						VertexConsumer builder = buffer.getBuffer(IERenderTypes.getGui(rl("textures/models/blueprint_frame.png")));
						GuiHelper.drawTexturedColoredRect(builder, transform, .125f, -.875f, .75f, .75f, 1, 1, 1, 1, 1, 0, 1, 0);
						//Width depends on distance
						float lineWidth = playerDistanceSq < 3?3: playerDistanceSq < 25?2: playerDistanceSq < 40?1: .5f;
						transform.translate(.75, -.25, -.002);
						float scale = .0375f/(blueprint.getTextureScale()/16f);
						transform.scale(-scale, -scale, scale);

						blueprint.draw(lineWidth, transform, buffer);

						transform.popPose();
						event.setCanceled(true);
					}
				}
			}
		}
	}

	private static void handleSubtitleOffset(boolean pre)
	{
		float offset = 0;
		Player player = ClientUtils.mc().player;
		for(InteractionHand hand : InteractionHand.values())
			if(!player.getItemInHand(hand).isEmpty())
			{
				Item equipped = player.getItemInHand(hand).getItem();
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
			GlStateManager._translatef(0, offset, 0);
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
				MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
				PoseStack transform = new PoseStack();
				transform.pushPose();
				int width = ClientUtils.mc().getWindow().getGuiScaledWidth();
				int height = ClientUtils.mc().getWindow().getGuiScaledHeight();
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
				VertexConsumer builder = buffers.getBuffer(IERenderTypes.getGui(rl("textures/gui/scope.png")));
				GuiHelper.drawTexturedColoredRect(builder, transform, 0, 0, resMin, resMin, 1, 1, 1, 1, 0f, 1f, 0f, 1f);

				builder = buffers.getBuffer(IERenderTypes.getGui(rl("textures/gui/hud_elements.png")));
				GuiHelper.drawTexturedColoredRect(builder, transform, 218/256f*resMin, 64/256f*resMin, 24/256f*resMin, 128/256f*resMin, 1, 1, 1, 1, 64/256f, 88/256f, 96/256f, 224/256f);
				ItemStack equipped = ClientUtils.mc().player.getItemInHand(InteractionHand.MAIN_HAND);
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
							ClientUtils.font().drawInBatch((1/steps[curStep])+"x", (int)(16/256f*resMin), 0, 0xffffff, true,
									transform.last().pose(), buffers, false, 0, 0xf000f0);
							transform.translate(-6/256f*resMin, -curStep*stepLength/256*resMin, 0);
						}
						transform.translate(0, -((5+stepOffset)/256*resMin), 0);
						transform.translate(-223/256f*resMin, -64/256f*resMin, 0);
					}
				}

				transform.translate(-offsetX, -offsetY, 0);
				buffers.endBatch();
			}
		}
	}

	@SubscribeEvent()
	public void onRenderOverlayPost(RenderGameOverlayEvent.Post event)
	{
		int scaledWidth = ClientUtils.mc().getWindow().getGuiScaledWidth();
		int scaledHeight = ClientUtils.mc().getWindow().getGuiScaledHeight();

		if(event.getType()==RenderGameOverlayEvent.ElementType.SUBTITLES)
			handleSubtitleOffset(false);
		if(ClientUtils.mc().player!=null&&event.getType()==RenderGameOverlayEvent.ElementType.TEXT)
		{
			Player player = ClientUtils.mc().player;
			PoseStack transform = new PoseStack();

			for(InteractionHand hand : InteractionHand.values())
				if(!player.getItemInHand(hand).isEmpty())
				{
					MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					ItemStack equipped = player.getItemInHand(hand);
					if(ItemStack.isSame(new ItemStack(Tools.voltmeter), equipped)||equipped.getItem() instanceof IWireCoil)
					{
						if(WirecoilUtils.hasWireLink(equipped))
						{
							WireLink link = WireLink.readFromItem(equipped);
							BlockPos pos = link.cp.getPosition();
							String s = I18n.get(Lib.DESC_INFO+"attachedTo", pos.getX(), pos.getY(), pos.getZ());
							int col = WireType.ELECTRUM.getColour(null);
							if(equipped.getItem() instanceof IWireCoil)
							{
								//TODO use actual connection offset rather than pos
								HitResult rtr = ClientUtils.mc().hitResult;
								double d;
								if(rtr instanceof BlockHitResult)
									d = ((BlockHitResult)rtr).getBlockPos().distSqr(pos.getX(), pos.getY(), pos.getZ(), false);
								else
									d = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
								int max = ((IWireCoil)equipped.getItem()).getWireType(equipped).getMaxLength();
								if(d > max*max)
									col = 0xdd3333;
							}
							ClientUtils.font().drawInBatch(
									s, scaledWidth/2-ClientUtils.font().width(s)/2, scaledHeight-ForgeIngameGui.left_height-20, col,
									true, transform.last().pose(), buffer, false, 0, 0xf000f0);
						}
					}
					else if(equipped.getItem()==Misc.fluorescentTube)
					{
						int color = FluorescentTubeItem.getRGBInt(equipped, 1);
						String s = I18n.get(Lib.DESC_INFO+"colour")+"#"+FontUtils.hexColorString(color);
						ClientUtils.font().drawInBatch(s, scaledWidth/2-ClientUtils.font().width(s)/2,
								scaledHeight-ForgeIngameGui.left_height-20, FluorescentTubeItem.getRGBInt(equipped, 1),
								true, transform.last().pose(), buffer, false, 0, 0xf000f0
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
						HitResult rrt = ClientUtils.mc().hitResult;
						IFluxReceiver receiver = null;
						Direction side = null;
						if(rrt instanceof BlockHitResult)
						{
							BlockHitResult mop = (BlockHitResult)rrt;
							BlockEntity tileEntity = player.level.getBlockEntity(mop.getBlockPos());
							if(tileEntity instanceof IFluxReceiver)
								receiver = (IFluxReceiver)tileEntity;
							side = mop.getDirection();
							if(player.level.getGameTime()%20==0)
								ImmersiveEngineering.packetHandler.sendToServer(new MessageRequestBlockUpdate(mop.getBlockPos()));
						}
						else if(rrt instanceof EntityHitResult&&((EntityHitResult)rrt).getEntity() instanceof IFluxReceiver)
							receiver = (IFluxReceiver)((EntityHitResult)rrt).getEntity();
						if(receiver!=null)
						{
							String[] text = new String[0];
							int maxStorage = receiver.getMaxEnergyStored(side);
							int storage = receiver.getEnergyStored(side);
							if(maxStorage > 0)
								text = I18n.get(Lib.DESC_INFO+"energyStored", "<br>"+Utils.toScientificNotation(storage, "0##", 100000)+" / "+Utils.toScientificNotation(maxStorage, "0##", 100000)).split("<br>");
							int col = 0xffffff;
							int i = 0;
							RenderSystem.enableBlend();
							for(String s : text)
								if(s!=null)
								{
									s = s.trim();
									int w = ClientUtils.font().width(s);
									ClientUtils.font().drawInBatch(
											s, scaledWidth/2-w/2,
											scaledHeight/2-4-text.length*(ClientUtils.font().lineHeight+2)+
													(i++)*(ClientUtils.font().lineHeight+2), col,
											false, transform.last().pose(), buffer, false,
											0, 0xf000f0
									);
								}
							RenderSystem.disableBlend();
						}
					}
					buffer.endBatch();
				}
			if(ClientUtils.mc().hitResult!=null)
			{
				ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
				boolean hammer = !held.isEmpty()&&Utils.isHammer(held);
				HitResult mop = ClientUtils.mc().hitResult;
				if(mop instanceof EntityHitResult)
				{
					Entity entity = ((EntityHitResult)mop).getEntity();
					if(entity instanceof ItemFrame)
						BlockOverlayUtils.renderOreveinMapOverlays(transform, (ItemFrame)entity, mop, scaledWidth, scaledHeight);
					else if(entity instanceof IEMinecartEntity)
					{
						IEBaseTileEntity containedTile = ((IEMinecartEntity<?>)entity).getContainedTileEntity();
						if(containedTile instanceof IBlockOverlayText)
						{
							Component[] text = ((IBlockOverlayText)containedTile).getOverlayText(player, mop, false);
							BlockOverlayUtils.drawBlockOverlayText(transform, text, scaledWidth, scaledHeight);
						}
					}
				}
				else if(mop instanceof BlockHitResult)
				{
					BlockPos pos = ((BlockHitResult)mop).getBlockPos();
					Direction face = ((BlockHitResult)mop).getDirection();
					BlockEntity tileEntity = player.level.getBlockEntity(pos);
					if(tileEntity instanceof IBlockOverlayText)
					{
						IBlockOverlayText overlayBlock = (IBlockOverlayText)tileEntity;
						Component[] text = overlayBlock.getOverlayText(ClientUtils.mc().player, mop, hammer);
						BlockOverlayUtils.drawBlockOverlayText(transform, text, scaledWidth, scaledHeight);
					}
					else
					{
						List<ItemFrame> list = player.level.getEntitiesOfClass(ItemFrame.class,
								new AABB(pos.relative(face)), entity -> entity!=null&&entity.getDirection()==face);
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
		Entity e = event.getInfo().getEntity();
		if(e instanceof LivingEntity&&((LivingEntity)e).hasEffect(IEPotions.flashed))
		{
			MobEffectInstance effect = ((LivingEntity)e).getEffect(IEPotions.flashed);
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
		Entity e = event.getInfo().getEntity();
		if(e instanceof LivingEntity&&((LivingEntity)e).hasEffect(IEPotions.flashed))
		{
			event.setRed(1);
			event.setGreen(1);
			event.setBlue(1);
		}
	}

	@SubscribeEvent()
	public void onFOVUpdate(FOVUpdateEvent event)
	{
		Player player = ClientUtils.mc().player;

		// Check if player is holding a zoom-allowing item
		ItemStack equipped = player.getItemInHand(InteractionHand.MAIN_HAND);
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
		if(player.getEffect(IEPotions.concreteFeet)!=null)
			event.setNewfov(1);
	}

	@SubscribeEvent
	public void onMouseEvent(MouseScrollEvent event)
	{
		Player player = ClientUtils.mc().player;
		if(event.getScrollDelta()!=0&&ClientUtils.mc().screen==null&&player!=null)
		{
			ItemStack equipped = player.getItemInHand(InteractionHand.MAIN_HAND);
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
			if(player.isShiftKeyDown())
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
			PoseStack transform = event.getMatrix();
			MultiBufferSource buffer = event.getBuffers();
			BlockHitResult rtr = (BlockHitResult)event.getTarget();
			BlockPos pos = rtr.getBlockPos();
			Vec3 renderView = event.getInfo().getPosition();
			transform.pushPose();
			transform.translate(-renderView.x, -renderView.y, -renderView.z);
			transform.translate(pos.getX(), pos.getY(), pos.getZ());
			Entity player = event.getInfo().getEntity();
			float f1 = 0.002F;
			BlockEntity tile = player.level.getBlockEntity(rtr.getBlockPos());
			ItemStack stack = player instanceof LivingEntity?((LivingEntity)player).getItemInHand(InteractionHand.MAIN_HAND): ItemStack.EMPTY;

			if(tile instanceof TurntableTileEntity&&Utils.isHammer(stack))
			{
				TurntableTileEntity turntableTile = ((TurntableTileEntity)tile);
				Direction side = rtr.getDirection();
				Direction facing = turntableTile.getFacing();
				if(side.getAxis()!=facing.getAxis())
				{
					transform.pushPose();
					transform.translate(0.5, 0.5, 0.5);
					ClientUtils.toModelRotation(side).getRotation().push(transform);
					transform.mulPose(new Quaternion(-90, 0, 0, true));
					Rotation rotation = turntableTile.getRotationFromSide(side);
					boolean cw180 = rotation==Rotation.CLOCKWISE_180;
					double angle;
					if(cw180)
						angle = player.tickCount%40/20d;
					else
						angle = player.tickCount%80/40d;
					double stepDistance = (cw180?2: 4)*Math.PI;
					angle = -(angle-Math.sin(angle*stepDistance)/stepDistance)*Math.PI;
					BlockOverlayUtils.drawCircularRotationArrows(buffer, transform, (float)angle, rotation==Rotation.COUNTERCLOCKWISE_90, cw180);
					transform.popPose();
					transform.popPose();
				}
			}

			Level world = player.level;
			if(!stack.isEmpty()&&ConveyorHandler.conveyorBlocks.containsValue(Block.byItem(stack.getItem()))&&rtr.getDirection().getAxis()==Axis.Y)
			{
				Direction side = rtr.getDirection();
				VoxelShape shape = world.getBlockState(pos).getBlockSupportShape(world, pos);
				AABB targetedBB = null;
				if(!shape.isEmpty())
					targetedBB = shape.bounds();

				MultiBufferSource buffers = event.getBuffers();
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
				Matrix4f mat = transform.last().pose();
				VertexConsumer lineBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_LINES);
				for(float[] point : points)
					lineBuilder.vertex(mat, point[0], point[1], point[2])
							.color(0, 0, 0, 0.4F)
							.endVertex();

				lineBuilder.vertex(mat, points[0][0], points[0][1], points[0][2]).color(0, 0, 0, 0.4F).endVertex();
				lineBuilder.vertex(mat, points[2][0], points[2][1], points[2][2]).color(0, 0, 0, 0.4F).endVertex();
				lineBuilder.vertex(mat, points[1][0], points[1][1], points[1][2]).color(0, 0, 0, 0.4F).endVertex();
				lineBuilder.vertex(mat, points[3][0], points[3][1], points[3][2]).color(0, 0, 0, 0.4F).endVertex();
				lineBuilder.vertex(mat, points[0][0], points[0][1], points[0][2]).color(0, 0, 0, 0.4F).endVertex();

				float xFromMid = side.getAxis()==Axis.X?0: (float)rtr.getLocation().x-pos.getX()-.5f;
				float yFromMid = side.getAxis()==Axis.Y?0: (float)rtr.getLocation().y-pos.getY()-.5f;
				float zFromMid = side.getAxis()==Axis.Z?0: (float)rtr.getLocation().z-pos.getZ()-.5f;
				float max = Math.max(Math.abs(yFromMid), Math.max(Math.abs(xFromMid), Math.abs(zFromMid)));
				Vec3 dir = new Vec3(max==Math.abs(xFromMid)?Math.signum(xFromMid): 0, max==Math.abs(yFromMid)?Math.signum(yFromMid): 0, max==Math.abs(zFromMid)?Math.signum(zFromMid): 0);
				BlockOverlayUtils.drawBlockOverlayArrow(mat, buffers, dir, side, targetedBB);

			}

			transform.popPose();
			if(!stack.isEmpty()&&stack.getItem() instanceof DrillItem&&
					((DrillItem)stack.getItem()).isEffective(stack, world.getBlockState(rtr.getBlockPos()).getMaterial()))
			{
				ItemStack head = ((DrillItem)stack.getItem()).getHead(stack);
				if(!head.isEmpty()&&player instanceof Player&&!player.isShiftKeyDown())
				{
					ImmutableList<BlockPos> blocks = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world,
							(Player)player, event.getTarget());
					BlockOverlayUtils.drawAdditionalBlockbreak(event, (Player)player, blocks);
				}
			}
		}
	}

	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent event)
	{
		float partial = event.getPartialTicks();
		PoseStack transform = event.getMatrixStack();
		transform.pushPose();
		Vec3 renderView = ClientUtils.mc().gameRenderer.getMainCamera().getPosition();
		transform.translate(-renderView.x, -renderView.y, -renderView.z);
		MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		if(!FractalParticle.PARTICLE_FRACTAL_DEQUE.isEmpty())
		{
			List<Pair<RenderType, List<Consumer<VertexConsumer>>>> renders = new ArrayList<>();
			for(FractalParticle p : FractalParticle.PARTICLE_FRACTAL_DEQUE)
				for(Entry<RenderType, Consumer<VertexConsumer>> r : p.render(partial, transform))
				{
					boolean added = false;
					for(Entry<RenderType, List<Consumer<VertexConsumer>>> e : renders)
						if(e.getKey().equals(r.getKey()))
						{
							e.getValue().add(r.getValue());
							added = true;
							break;
						}
					if(!added)
						renders.add(Pair.of(r.getKey(), new ArrayList<>(ImmutableList.of(r.getValue()))));
				}
			for(Entry<RenderType, List<Consumer<VertexConsumer>>> entry : renders)
			{
				VertexConsumer bb = buffers.getBuffer(entry.getKey());
				for(Consumer<VertexConsumer> render : entry.getValue())
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
			if(Minecraft.getInstance().options.keyShift.isDefault())
				show = Screen.hasControlDown();
			else
				show = Screen.hasShiftDown();
		}
		if(show)
		{
			ResourceKey<Level> dimension = ClientUtils.mc().player.getCommandSenderWorld().dimension();
			List<ResourceLocation> keyList = new ArrayList<>(MineralMix.mineralList.keySet());
			keyList.sort(Comparator.comparing(ResourceLocation::toString));
			Multimap<ResourceKey<Level>, MineralVein> minerals;
			final ColumnPos playerCol = new ColumnPos(ClientUtils.mc().player.blockPosition());
			// 24: very roughly 16 * sqrt(2)
			final long maxDistance = ClientUtils.mc().options.renderDistance*24L;
			final long maxDistanceSq = maxDistance*maxDistance;
			synchronized(minerals = ExcavatorHandler.getMineralVeinList())
			{
				for(MineralVein vein : minerals.get(dimension))
				{
					if(vein.getMineral()==null)
						continue;
					transform.pushPose();
					ColumnPos pos = vein.getPos();
					final long xDiff = pos.x-playerCol.x;
					final long zDiff = pos.z-playerCol.z;
					long distToPlayerSq = xDiff*xDiff+zDiff*zDiff;
					if(distToPlayerSq > maxDistanceSq)
						continue;
					int iC = keyList.indexOf(vein.getMineral().getId());
					DyeColor color = DyeColor.values()[iC%16];
					float[] rgb = color.getTextureDiffuseColors();
					float r = rgb[0];
					float g = rgb[1];
					float b = rgb[2];
					transform.translate(pos.x, 0, pos.z);
					VertexConsumer bufferBuilder = buffers.getBuffer(IERenderTypes.CHUNK_MARKER);
					Matrix4f mat = transform.last().pose();
					bufferBuilder.vertex(mat, 0, 0, 0).color(r, g, b, .75f).endVertex();
					bufferBuilder.vertex(mat, 0, 128, 0).color(r, g, b, .75f).endVertex();

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
						bufferBuilder.vertex(mat, (float)x1, 70, (float)z1).color(r, g, b, .75f).endVertex();
					}
					transform.popPose();
				}
			}
		}

		if(!FAILED_CONNECTIONS.isEmpty())
		{
			VertexConsumer builder = buffers.getBuffer(IERenderTypes.CHUNK_MARKER);
			for(Entry<Connection, Pair<Collection<BlockPos>, AtomicInteger>> entry : FAILED_CONNECTIONS.entrySet())
			{
				Connection conn = entry.getKey();
				transform.pushPose();
				transform.translate(conn.getEndA().getX(), conn.getEndA().getY(), conn.getEndA().getZ());
				Matrix4f mat = transform.last().pose();
				int time = entry.getValue().getValue().get();
				float alpha = (float)Math.min((2+Math.sin(time*Math.PI/40))/3, time/20F);
				Vec3 prev = conn.getPoint(0, conn.getEndA());
				for(int i = 0; i < RenderData.POINTS_PER_WIRE; i++)
				{
					builder.vertex(mat, (float)prev.x, (float)prev.y, (float)prev.z)
							.color(1, 0, 0, alpha).endVertex();
					alpha = (float)Math.min((2+Math.sin((time+(i+1)*8)*Math.PI/40))/3, time/20F);
					Vec3 next = conn.getPoint((i+1)/(double)RenderData.POINTS_PER_WIRE, conn.getEndA());
					builder.vertex(mat, (float)next.x, (float)next.y, (float)next.z)
							.color(1, 0, 0, alpha).endVertex();
					prev = next;
				}
				transform.popPose();
			}
			renderObstructingBlocks(transform, buffers);
		}
		transform.popPose();
		buffers.endBatch();
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
			GPUWarningAccess gpuWarning = (GPUWarningAccess)Minecraft.getInstance().getGpuWarnlistManager();
			final String key = "renderer";
			final String suffix = "tencil enabled in Immersive Engineering config";
			Map<String, String> oldWarnings = gpuWarning.getWarnings();
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
				gpuWarning.setWarnings(builder.build());
			}
		}
	}

	private static void enableHead(LivingEntityRenderer renderer, boolean shouldEnable)
	{
		EntityModel m = renderer.getModel();
		if(m instanceof HeadedModel)
			((HeadedModel)m).getHead().visible = shouldEnable;
	}

	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinWorldEvent event)
	{
		if(event.getEntity().level.isClientSide&&event.getEntity() instanceof AbstractMinecart&&
				event.getEntity().getCapability(CapabilityShader.SHADER_CAPABILITY).isPresent())
			ImmersiveEngineering.packetHandler.sendToServer(new MessageMinecartShaderSync(event.getEntity(), null));
	}
}
