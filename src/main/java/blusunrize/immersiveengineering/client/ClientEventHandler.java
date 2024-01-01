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
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import blusunrize.immersiveengineering.client.gui.BlastFurnaceScreen;
import blusunrize.immersiveengineering.client.render.tile.BlueprintRenderer;
import blusunrize.immersiveengineering.client.render.tile.BlueprintRenderer.BlueprintLines;
import blusunrize.immersiveengineering.client.utils.FontUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBOverlayText;
import blusunrize.immersiveengineering.common.blocks.wooden.TurntableBlockEntity;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.IEMinecartEntity;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.network.*;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledSound;
import blusunrize.immersiveengineering.common.util.sound.IEMuffledTickableSound;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class ClientEventHandler implements ResourceManagerReloadListener
{
	private boolean shieldToggleButton = false;
	private int shieldToggleTimer = 0;

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager)
	{
		ImmersiveEngineering.proxy.clearRenderCaches();
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side==LogicalSide.CLIENT&&event.player!=null&&event.player==ClientUtils.mc().getCameraEntity())
		{
			if(event.phase==Phase.END)
			{
				if(this.shieldToggleTimer > 0)
					this.shieldToggleTimer--;
				if(IEKeybinds.keybind_magnetEquip.isDown()&&!this.shieldToggleButton)
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
							for(int i = 0; i < player.getInventory().items.size(); i++)
							{
								ItemStack s = player.getInventory().items.get(i);
								if(!s.isEmpty()&&s.getItem() instanceof IEShieldItem&&((IEShieldItem)s.getItem()).getUpgrades(s).getBoolean("magnet"))
									ImmersiveEngineering.packetHandler.sendToServer(new MessageMagnetEquip(i));
							}
						}
					}
				if(this.shieldToggleButton!=ClientUtils.mc().options.keyDown.isDown())
					this.shieldToggleButton = ClientUtils.mc().options.keyDown.isDown();


				if(!IEKeybinds.keybind_chemthrowerSwitch.isUnbound()&&IEKeybinds.keybind_chemthrowerSwitch.consumeClick())
				{
					ItemStack held = event.player.getItemInHand(InteractionHand.MAIN_HAND);
					if(held.getItem() instanceof IScrollwheel)
						ImmersiveEngineering.packetHandler.sendToServer(new MessageScrollwheelItem(true));
				}

				if(!IEKeybinds.keybind_railgunZoom.isUnbound()&&IEKeybinds.keybind_railgunZoom.consumeClick())
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
		LevelStageRenders.FAILED_CONNECTIONS.entrySet().removeIf(entry -> entry.getValue().getSecond().decrementAndGet() <= 0);
		ClientLevel world = Minecraft.getInstance().level;
		if(world!=null)
			GlobalWireNetwork.getNetwork(world).update(world);
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
				List<Component> tooltip = event.getToolTip();
				// find gap
				int idx = IntStream.range(0,tooltip.size() ).filter(i -> tooltip.get(i) == CommonComponents.EMPTY).findFirst().orElse(tooltip.size()-1);
				// put tooltip in that gap
				tooltip.add(idx++, CommonComponents.EMPTY);
				tooltip.add(idx++, TextUtils.applyFormat(powerpack.getHoverName(), ChatFormatting.GRAY));
				tooltip.add(idx++, TextUtils.applyFormat(
						Component.literal(EnergyHelper.getEnergyStored(powerpack)+"/"+EnergyHelper.getMaxEnergyStored(powerpack)+" IF"),
						ChatFormatting.GRAY
				));
				tooltip.add(idx, TextUtils.applyFormat(Component.translatable("desc.immersiveengineering.info.noChargeOnArmor"), ChatFormatting.DARK_GRAY));
			}
		}
		Level clientLevel = ClientUtils.mc().level;
		if(ClientUtils.mc().screen!=null
				&&ClientUtils.mc().screen instanceof BlastFurnaceScreen
				&&BlastFurnaceFuel.isValidBlastFuel(clientLevel, event.getItemStack()))
			event.getToolTip().add(TextUtils.applyFormat(
					Component.translatable("desc.immersiveengineering.info.blastFuelTime", BlastFurnaceFuel.getBlastFuelTime(clientLevel, event.getItemStack())),
					ChatFormatting.GRAY
			));

		if(IEClientConfig.tagTooltips.get()&&event.getFlags().isAdvanced())
			event.getItemStack().getItem().builtInRegistryHolder().tags()
					.map(TagKey::location)
					.forEach(oid ->
							event.getToolTip().add(TextUtils.applyFormat(
									Component.literal(oid.toString()),
									ChatFormatting.GRAY
							)));
	}

	@SubscribeEvent
	public void onPlaySound(PlaySoundEvent event)
	{
		if(event.getSound()==null)
			return;
		else
			event.getSound().getSource();
		if(!EarmuffsItem.affectedSoundCategories.contains(event.getSound().getSource().getName()))
			return;
		if(ClientUtils.mc().player!=null)
		{
			ItemStack earmuffs = EarmuffsItem.EARMUFF_GETTERS.getFrom(ClientUtils.mc().player);
			if(!earmuffs.isEmpty()&&
					!ItemNBTHelper.getBoolean(earmuffs, "IE:Earmuffs:Cat_"+event.getSound().getSource().getName()))
			{
				for(String blacklist : IEClientConfig.earDefenders_SoundBlacklist.get())
					if(blacklist!=null&&blacklist.equalsIgnoreCase(event.getSound().getLocation().toString()))
						return;
				if(event.getSound() instanceof TickableSoundInstance)
					event.setSound(new IEMuffledTickableSound((TickableSoundInstance)event.getSound(), EarmuffsItem.getVolumeMod(earmuffs)));
				else
					event.setSound(new IEMuffledSound(event.getSound(), EarmuffsItem.getVolumeMod(earmuffs)));
			}
		}
	}

	@SubscribeEvent
	public void onRenderItemFrame(RenderItemInFrameEvent event)
	{
		if(event.getItemStack().getItem() instanceof EngineersBlueprintItem)
		{
			double playerDistanceSq = ClientUtils.mc().player.distanceToSqr(event.getItemFrameEntity());

			if(playerDistanceSq < 1000)
			{
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(event.getItemFrameEntity().level(), ItemNBTHelper.getString(event.getItemStack(), "blueprint"));
				if(recipes.length > 0)
				{
					int i = event.getItemFrameEntity().getRotation();
					BlueprintCraftingRecipe recipe = recipes[i%recipes.length];
					BlueprintLines blueprint = recipe==null?null: BlueprintRenderer.getBlueprintDrawable(recipe, event.getItemFrameEntity().getCommandSenderWorld());
					if(blueprint!=null)
					{
						PoseStack transform = event.getPoseStack();
						transform.pushPose();
						MultiBufferSource buffer = event.getMultiBufferSource();
						transform.mulPose(new Quaternionf().rotateXYZ(0, 0, -i*Mth.PI / 4));
						transform.translate(-.5, .5, -.001);
						VertexConsumer builder = buffer.getBuffer(IERenderTypes.getGui(rl("textures/models/blueprint_frame.png")));
						GuiHelper.drawTexturedColoredRect(builder, transform, .125f, -.875f, .75f, .75f, 1, 1, 1, 1, 1, 0, 1, 0);
						transform.translate(.75, -.25, -.002);
						float scale = .0375f/(blueprint.getTextureScale()/16f);
						transform.scale(-scale, -scale, scale);

						blueprint.draw(transform, buffer, event.getPackedLight());

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
			RenderSystem.getModelViewStack().translate(0, offset, 0);
			RenderSystem.applyModelViewMatrix();
		}
	}

	@SubscribeEvent
	public void onRenderOverlayPre(RenderGuiOverlayEvent.Pre event)
	{
		if(event.getOverlay().id().equals(VanillaGuiOverlay.SUBTITLES.id()))
			handleSubtitleOffset(true);
		if(ZoomHandler.isZooming&&event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()))
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
				VertexConsumer builder = buffers.getBuffer(IERenderTypes.getGuiTranslucent(rl("textures/gui/scope.png")));
				GuiHelper.drawTexturedColoredRect(builder, transform, 0, 0, resMin, resMin, 1, 1, 1, 1, 0f, 1f, 0f, 1f);

				builder = buffers.getBuffer(IERenderTypes.getGui(rl("textures/gui/hud_elements.png")));
				GuiHelper.drawTexturedColoredRect(builder, transform, 218/256f*resMin, 64/256f*resMin, 24/256f*resMin, 128/256f*resMin, 1, 1, 1, 1, 64/256f, 88/256f, 96/256f, 224/256f);
				ItemStack equipped = ClientUtils.mc().player.getItemInHand(InteractionHand.MAIN_HAND);
				if(!equipped.isEmpty()&&equipped.getItem() instanceof IZoomTool tool)
				{
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
									transform.last().pose(), buffers, DisplayMode.NORMAL, 0, 0xf000f0);
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
	public void onRenderOverlayPost(RenderGuiOverlayEvent.Post event)
	{
		int scaledWidth = ClientUtils.mc().getWindow().getGuiScaledWidth();
		int scaledHeight = ClientUtils.mc().getWindow().getGuiScaledHeight();

		if(event.getOverlay().id().equals(VanillaGuiOverlay.SUBTITLES.id()))
			handleSubtitleOffset(false);
		int leftHeight;
		if(Minecraft.getInstance().gui instanceof ForgeGui forgeUI)
			leftHeight = forgeUI.leftHeight;
		else
			leftHeight = 0;
		if(ClientUtils.mc().player!=null&&event.getOverlay().id().equals(VanillaGuiOverlay.ITEM_NAME.id()))
		{
			Player player = ClientUtils.mc().player;
			GuiGraphics graphics = event.getGuiGraphics();
			PoseStack transform = graphics.pose();

			for(InteractionHand hand : InteractionHand.values())
				if(!player.getItemInHand(hand).isEmpty())
				{
					MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
					ItemStack equipped = player.getItemInHand(hand);
					if(equipped.is(Tools.VOLTMETER.asItem())||equipped.getItem() instanceof IWireCoil)
					{
						if(WirecoilUtils.hasWireLink(equipped))
						{
							WireLink link = WireLink.readFromItem(equipped);
							BlockPos pos = link.cp.position();
							String s = I18n.get(Lib.DESC_INFO+"attachedTo", pos.getX(), pos.getY(), pos.getZ());
							int col = WireType.ELECTRUM.getColour(null);
							if(equipped.getItem() instanceof IWireCoil)
							{
								//TODO use actual connection offset rather than pos
								HitResult rtr = ClientUtils.mc().hitResult;
								double d;
								if(rtr instanceof BlockHitResult)
									d = ((BlockHitResult)rtr).getBlockPos().distSqr(pos);
								else
									d = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
								int max = ((IWireCoil)equipped.getItem()).getWireType(equipped).getMaxLength();
								if(d > max*max)
									col = 0xdd3333;
							}
							ClientUtils.font().drawInBatch(
									s, scaledWidth/2-ClientUtils.font().width(s)/2, scaledHeight-leftHeight-20, col,
									true, transform.last().pose(), buffer, DisplayMode.NORMAL, 0, 0xf000f0);
						}
					}
					else if(equipped.getItem()==Misc.FLUORESCENT_TUBE.get())
					{
						int color = FluorescentTubeItem.getRGBInt(equipped, 1);
						String s = I18n.get(Lib.DESC_INFO+"colour")+"#"+FontUtils.hexColorString(color);
						ClientUtils.font().drawInBatch(s, scaledWidth/2-ClientUtils.font().width(s)/2,
								scaledHeight-leftHeight-20, FluorescentTubeItem.getRGBInt(equipped, 1),
								true, transform.last().pose(), buffer, DisplayMode.NORMAL, 0, 0xf000f0
						);
					}
					else if(equipped.getItem() instanceof RevolverItem||equipped.getItem() instanceof SpeedloaderItem)
						ItemOverlayUtils.renderRevolverOverlay(buffer, graphics, scaledWidth, scaledHeight, player, hand, equipped);
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
					if(equipped.getItem()==Tools.VOLTMETER.get())
						renderVoltmeterOverlay(player, scaledWidth, scaledHeight, transform, buffer);
					buffer.endBatch();
					Lighting.setupFor3DItems();
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
						BlockOverlayUtils.renderOreveinMapOverlays(graphics, (ItemFrame)entity, mop, scaledWidth, scaledHeight);
					else if(entity instanceof IEMinecartEntity<?> ieMinecart&&ieMinecart.getContainedBlockEntity() instanceof IBlockOverlayText overlayText)
					{
						Component[] text = overlayText.getOverlayText(player, mop, false);
						BlockOverlayUtils.drawBlockOverlayText(transform, text, scaledWidth, scaledHeight);
					}
				}
				else if(mop instanceof BlockHitResult)
				{
					BlockPos pos = ((BlockHitResult)mop).getBlockPos();
					Direction face = ((BlockHitResult)mop).getDirection();
					BlockEntity tileEntity = player.level().getBlockEntity(pos);
					if(tileEntity instanceof IBlockOverlayText overlayBlock)
					{
						Component[] text = overlayBlock.getOverlayText(ClientUtils.mc().player, mop, hammer);
						BlockOverlayUtils.drawBlockOverlayText(transform, text, scaledWidth, scaledHeight);
					}
					else if(!(tileEntity instanceof IMultiblockBE<?> multiblock)||!renderMultiblockOverlay(
							multiblock, hammer, transform, scaledWidth, scaledHeight
					))
					{
						List<ItemFrame> list = player.level().getEntitiesOfClass(ItemFrame.class,
								new AABB(pos.relative(face)), entity -> entity!=null&&entity.getDirection()==face);
						if(list.size()==1)
							BlockOverlayUtils.renderOreveinMapOverlays(graphics, list.get(0), mop, scaledWidth, scaledHeight);
					}
				}
			}
		}
	}

	private <S extends IMultiblockState> boolean renderMultiblockOverlay(
			IMultiblockBE<S> be, boolean hammer, PoseStack transform, int scaledWidth, int scaledHeight
	)
	{
		final IMultiblockBEHelper<S> helper = be.getHelper();
		if(!(helper.getMultiblock().logic() instanceof MBOverlayText<S> overlayHandler))
			return false;
		final List<Component> overlayText = overlayHandler.getOverlayText(
				helper.getState(), ClientUtils.mc().player, hammer
		);
		if(overlayText==null)
			return false;
		BlockOverlayUtils.drawBlockOverlayText(transform, overlayText, scaledWidth, scaledHeight);
		return true;
	}

	private void renderVoltmeterOverlay(Player player, float scaledWidth, float scaledHeight, PoseStack transform, MultiBufferSource buffer)
	{
		HitResult rrt = ClientUtils.mc().hitResult;
		FastEither<BlockPos, Integer> pos = null;
		if(rrt instanceof BlockHitResult mop)
			pos = FastEither.left(mop.getBlockPos());
		else if(rrt instanceof EntityHitResult ehr)
			pos = FastEither.right(ehr.getEntity().getId());

		if(pos==null)
			return;

		ArrayList<String> text = new ArrayList<>();

		boolean matches = VoltmeterItem.lastEnergyUpdate.pos().equals(pos);
		long sinceLast = player.level().getGameTime()-VoltmeterItem.lastEnergyUpdate.measuredInTick();
		if(!matches||sinceLast > 20)
			ImmersiveEngineering.packetHandler.sendToServer(new MessageRequestEnergyUpdate(pos));

		if(VoltmeterItem.lastEnergyUpdate.isValid()&&matches)
		{
			int maxStorage = VoltmeterItem.lastEnergyUpdate.capacity();
			int storage = VoltmeterItem.lastEnergyUpdate.stored();
			String storageText = Utils.toScientificNotation(storage, "0##", 100000);
			String capacityText = Utils.toScientificNotation(maxStorage, "0##", 100000);
			text.addAll(Arrays.asList(I18n.get(Lib.DESC_INFO+"energyStored", "<br>"+storageText+" / "+capacityText)
					.split("<br>")));
		}

		if(pos.isLeft())
		{
			matches = VoltmeterItem.lastRedstoneUpdate.pos().equals(pos.leftNonnull());
			sinceLast = player.level().getGameTime()-VoltmeterItem.lastRedstoneUpdate.measuredInTick();
			if(!matches||sinceLast > 20)
				ImmersiveEngineering.packetHandler.sendToServer(new MessageRequestRedstoneUpdate(pos.leftNonnull()));

			if(VoltmeterItem.lastRedstoneUpdate.isSignalSource()&&matches)
			{
				text.addAll(Arrays.asList(I18n.get(Lib.DESC_INFO+"redstoneLevel", "<br>"+VoltmeterItem.lastRedstoneUpdate.rsLevel())
						.split("<br>")));
			}
		}

		if(text!=null)
		{
			int col = 0xffffff;
			int i = 0;
			RenderSystem.enableBlend();
			for(String s : text)
				if(s!=null)
				{
					s = s.trim();
					int w = ClientUtils.font().width(s);
					ClientUtils.font().drawInBatch(
							s, scaledWidth/2-w/2f,
							scaledHeight/2+4+(i++)*(ClientUtils.font().lineHeight+2), col,
							false, transform.last().pose(), buffer, DisplayMode.NORMAL,
							0, 0xf000f0
					);
				}
			RenderSystem.disableBlend();
		}
	}

	@SubscribeEvent()
	public void onFogUpdate(ViewportEvent.RenderFog event)
	{
		if(event.getCamera().getEntity() instanceof LivingEntity living&&living.hasEffect(IEPotions.FLASHED.get()))
		{
			MobEffectInstance effect = living.getEffect(IEPotions.FLASHED.get());
			int timeLeft = effect.getDuration();
			float saturation = Math.max(0.25f, 1-timeLeft/(float)(80+40*effect.getAmplifier()));//Total Time =  4s + 2s per amplifier

			float f1 = -2.5f+15.0F*saturation;
			if(timeLeft < 20)
				f1 += (event.getFarPlaneDistance()/4)*(1-timeLeft/20f);

			RenderSystem.setShaderFogStart(0.25f*f1);
			RenderSystem.setShaderFogEnd(f1);
		}
	}

	@SubscribeEvent()
	public void onFogColourUpdate(ViewportEvent.ComputeFogColor event)
	{
		Entity e = event.getCamera().getEntity();
		if(e instanceof LivingEntity&&((LivingEntity)e).hasEffect(IEPotions.FLASHED.get()))
		{
			event.setRed(1);
			event.setGreen(1);
			event.setBlue(1);
		}
	}

	@SubscribeEvent()
	public void onFOVUpdate(ComputeFovModifierEvent event)
	{
		Player player = ClientUtils.mc().player;

		// Check if player is holding a zoom-allowing item
		ItemStack equipped = player.getItemInHand(InteractionHand.MAIN_HAND);
		boolean mayZoom = equipped.getItem() instanceof IZoomTool&&((IZoomTool)equipped.getItem()).canZoom(equipped, player);
		// Set zoom if allowed, otherwise stop zooming
		if(ZoomHandler.isZooming)
		{
			if(mayZoom)
				event.setNewFovModifier(ZoomHandler.fovZoom);
			else
				ZoomHandler.isZooming = false;
		}

		// Concrete feet slow you, but shouldn't break FoV
		if(player.getEffect(IEPotions.CONCRETE_FEET.get())!=null)
			event.setNewFovModifier(1);
	}

	@SubscribeEvent
	public void onMouseEvent(MouseScrollingEvent event)
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
	public void renderAdditionalBlockBounds(RenderHighlightEvent.Block event)
	{
		if(event.getTarget().getType()==Type.BLOCK&&event.getCamera().getEntity() instanceof LivingEntity living)
		{
			PoseStack transform = event.getPoseStack();
			MultiBufferSource buffer = event.getMultiBufferSource();
			BlockHitResult rtr = event.getTarget();
			BlockPos pos = rtr.getBlockPos();
			Vec3 renderView = event.getCamera().getPosition();
			transform.pushPose();
			transform.translate(-renderView.x, -renderView.y, -renderView.z);
			transform.translate(pos.getX(), pos.getY(), pos.getZ());
			float eps = 0.002F;
			BlockEntity tile = living.level().getBlockEntity(rtr.getBlockPos());
			ItemStack stack = living.getItemInHand(InteractionHand.MAIN_HAND);

			if(tile instanceof TurntableBlockEntity turntableTile&&Utils.isHammer(stack))
			{
				Direction side = rtr.getDirection();
				Direction facing = turntableTile.getFacing();
				if(side.getAxis()!=facing.getAxis())
				{
					transform.pushPose();
					transform.translate(0.5, 0.5, 0.5);
					transform.pushTransformation(ClientUtils.toModelRotation(side).getRotation());
					transform.mulPose(new Quaternionf().rotateXYZ(-Mth.HALF_PI, 0, 0));
					Rotation rotation = turntableTile.getRotationFromSide(side);
					boolean cw180 = rotation==Rotation.CLOCKWISE_180;
					double angle;
					if(cw180)
						angle = living.tickCount%40/20d;
					else
						angle = living.tickCount%80/40d;
					double stepDistance = (cw180?2: 4)*Math.PI;
					angle = -(angle-Math.sin(angle*stepDistance)/stepDistance)*Math.PI;
					BlockOverlayUtils.drawCircularRotationArrows(buffer, transform, (float)angle, rotation==Rotation.COUNTERCLOCKWISE_90, cw180);
					transform.popPose();
					transform.popPose();
				}
			}

			Level world = living.level();
			if(!stack.isEmpty()&&ConveyorHandler.isConveyorBlock(Block.byItem(stack.getItem()))&&rtr.getDirection().getAxis()==Axis.Y)
			{
				Direction side = rtr.getDirection();
				VoxelShape shape = world.getBlockState(pos).getBlockSupportShape(world, pos);
				AABB targetedBB = null;
				if(!shape.isEmpty())
					targetedBB = shape.bounds();

				MultiBufferSource buffers = event.getMultiBufferSource();

				float y = (float)(targetedBB==null?0: side==Direction.DOWN?targetedBB.minY-eps: targetedBB.maxY+eps);
				Matrix4f mat = transform.last().pose();
				Matrix3f matN = transform.last().normal();
				VertexConsumer lineBuilder = buffers.getBuffer(IERenderTypes.LINES);
				float sqrt2Half = (float)(Math.sqrt(2)/2);
				lineBuilder.vertex(mat, 0-eps, y, 0-eps)
						.color(0, 0, 0, 0.4F)
						.normal(matN, sqrt2Half, 0, sqrt2Half)
						.endVertex();
				lineBuilder.vertex(mat, 1+eps, y, 1+eps)
						.color(0, 0, 0, 0.4F)
						.normal(matN, sqrt2Half, 0, sqrt2Half)
						.endVertex();
				lineBuilder.vertex(mat, 0-eps, y, 1+eps)
						.color(0, 0, 0, 0.4F)
						.normal(matN, sqrt2Half, 0, -sqrt2Half)
						.endVertex();
				lineBuilder.vertex(mat, 1+eps, y, 0-eps)
						.color(0, 0, 0, 0.4F)
						.normal(matN, sqrt2Half, 0, -sqrt2Half)
						.endVertex();

				float xFromMid = side.getAxis()==Axis.X?0: (float)rtr.getLocation().x-pos.getX()-.5f;
				float yFromMid = side.getAxis()==Axis.Y?0: (float)rtr.getLocation().y-pos.getY()-.5f;
				float zFromMid = side.getAxis()==Axis.Z?0: (float)rtr.getLocation().z-pos.getZ()-.5f;
				float max = Math.max(Math.abs(yFromMid), Math.max(Math.abs(xFromMid), Math.abs(zFromMid)));
				Vec3 dir = new Vec3(max==Math.abs(xFromMid)?Math.signum(xFromMid): 0, max==Math.abs(yFromMid)?Math.signum(yFromMid): 0, max==Math.abs(zFromMid)?Math.signum(zFromMid): 0);
				BlockOverlayUtils.drawBlockOverlayArrow(transform.last(), buffers, dir, side, targetedBB);
			}

			transform.popPose();
			BlockState targetBlock = world.getBlockState(rtr.getBlockPos());
			if(stack.getItem() instanceof DrillItem drillItem&&drillItem.isEffective(stack, targetBlock))
			{
				ItemStack head = drillItem.getHead(stack);
				if(!head.isEmpty()&&living instanceof Player player&&!living.isShiftKeyDown()&&!DrillItem.isSingleBlockMode(stack))
				{
					ImmutableList<BlockPos> potentialBlocks = ((IDrillHead)head.getItem()).getExtraBlocksDug(
							head, world, player, event.getTarget()
					);
					List<BlockPos> breakingBlocks = new ArrayList<>();
					for(BlockPos candidate : potentialBlocks)
					{
						BlockState targetState = world.getBlockState(candidate);
						if(drillItem.canBreakExtraBlock(world, candidate, targetState, player, stack, head))
							breakingBlocks.add(candidate);
					}
					BlockOverlayUtils.drawAdditionalBlockbreak(event, player, breakingBlocks);
				}
			}
		}
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

	private static void enableHead(LivingEntityRenderer renderer, boolean shouldEnable)
	{
		EntityModel m = renderer.getModel();
		if(m instanceof HeadedModel)
			((HeadedModel)m).getHead().visible = shouldEnable;
	}

	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinLevelEvent event)
	{
		if(event.getEntity().level().isClientSide&&event.getEntity() instanceof AbstractMinecart&&
				event.getEntity().getCapability(CapabilityShader.SHADER_CAPABILITY).isPresent())
			ImmersiveEngineering.packetHandler.sendToServer(new MessageMinecartShaderSync(event.getEntity()));
	}
}
