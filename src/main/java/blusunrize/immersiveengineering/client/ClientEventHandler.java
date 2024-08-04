/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.client.gui.BlastFurnaceScreen;
import blusunrize.immersiveengineering.client.render.tile.BlueprintRenderer;
import blusunrize.immersiveengineering.client.render.tile.BlueprintRenderer.BlueprintLines;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.generic.CatwalkBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WindowBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.TurntableBlockEntity;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.network.MessageMagnetEquip;
import blusunrize.immersiveengineering.common.network.MessageMinecartShaderSync;
import blusunrize.immersiveengineering.common.network.MessageRevolverRotate;
import blusunrize.immersiveengineering.common.network.MessageScrollwheelItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.mixin.accessors.client.WorldRendererAccess;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.api.IEApiDataComponents.BLUEPRINT_TYPE;

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
	public void onPlayerTick(PlayerTickEvent.Post event)
	{
		final var player = event.getEntity();
		if(player==null||!player.level().isClientSide||player!=ClientUtils.mc().player)
			return;
		if(this.shieldToggleTimer > 0)
			this.shieldToggleTimer--;
		if(IEKeybinds.keybind_magnetEquip.isDown()&&!this.shieldToggleButton)
			if(this.shieldToggleTimer <= 0)
				this.shieldToggleTimer = 7;
			else
			{
				ItemStack held = player.getItemInHand(InteractionHand.OFF_HAND);
				if(!held.isEmpty()&&held.getItem() instanceof IEShieldItem)
				{
					if(UpgradeableToolItem.getUpgradesStatic(held).get(UpgradeEffect.MAGNET).prevSlot().isPresent())
						PacketDistributor.sendToServer(new MessageMagnetEquip(-1));
				}
				else
				{
					for(int i = 0; i < player.getInventory().items.size(); i++)
					{
						ItemStack s = player.getInventory().items.get(i);
						if(!s.isEmpty()&&s.getItem() instanceof IEShieldItem&&((IEShieldItem)s.getItem()).getUpgrades(s).has(UpgradeEffect.MAGNET))
							PacketDistributor.sendToServer(new MessageMagnetEquip(i));
					}
				}
			}
		if(this.shieldToggleButton!=ClientUtils.mc().options.keyDown.isDown())
			this.shieldToggleButton = ClientUtils.mc().options.keyDown.isDown();


		if(!IEKeybinds.keybind_chemthrowerSwitch.isUnbound()&&IEKeybinds.keybind_chemthrowerSwitch.consumeClick())
		{
			ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
			if(held.getItem() instanceof IScrollwheel)
				PacketDistributor.sendToServer(new MessageScrollwheelItem(true));
		}

		if(!IEKeybinds.keybind_railgunZoom.isUnbound()&&IEKeybinds.keybind_railgunZoom.consumeClick())
			for(InteractionHand hand : InteractionHand.values())
			{
				ItemStack held = player.getItemInHand(hand);
				if(held.getItem() instanceof IZoomTool&&((IZoomTool)held.getItem()).canZoom(held, player))
				{
					ZoomHandler.isZooming = !ZoomHandler.isZooming;
					if(ZoomHandler.isZooming)
					{
						float[] steps = ((IZoomTool)held.getItem()).getZoomSteps(held, player);
						if(steps!=null&&steps.length > 0)
							ZoomHandler.fovZoom = steps[ZoomHandler.getCurrentZoomStep(steps)];
					}
				}
			}
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent.Pre event)
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
		var wrapper = event.getItemStack().getCapability(CapabilityShader.ITEM);
		if(wrapper!=null)
		{
			var shader = wrapper.getShader();
			if(shader!=null)
				event.getToolTip().add(TextUtils.applyFormat(
						ShaderItem.getShaderName(shader),
						ChatFormatting.DARK_GRAY
				));
		}
		if(event.getItemStack().has(IEDataComponents.CONTAINED_EARMUFF))
		{
			ItemStack earmuffs = event.getItemStack().get(IEDataComponents.CONTAINED_EARMUFF).attached();
			if(!earmuffs.isEmpty())
				event.getToolTip().add(TextUtils.applyFormat(
						earmuffs.getHoverName(),
						ChatFormatting.GRAY
				));
		}
		if(event.getItemStack().has(IEDataComponents.CONTAINED_POWERPACK))
		{
			ItemStack powerpack = event.getItemStack().get(IEDataComponents.CONTAINED_POWERPACK).attached();
			IEnergyStorage packStorage = powerpack.getCapability(EnergyStorage.ITEM);
			if(!powerpack.isEmpty()&&packStorage!=null)
			{
				List<Component> tooltip = event.getToolTip();
				// find gap
				int idx = IntStream.range(0, tooltip.size()).filter(i -> tooltip.get(i)==CommonComponents.EMPTY).findFirst().orElse(tooltip.size()-1);
				// put tooltip in that gap
				tooltip.add(idx++, CommonComponents.EMPTY);
				tooltip.add(idx++, TextUtils.applyFormat(powerpack.getHoverName(), ChatFormatting.GRAY));
				tooltip.add(idx++, TextUtils.applyFormat(
						Component.literal(packStorage.getEnergyStored()+"/"+packStorage.getMaxEnergyStored()+" IF"),
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
	public void onRenderItemFrame(RenderItemInFrameEvent event)
	{
		if(event.getItemStack().getItem() instanceof EngineersBlueprintItem)
		{
			double playerDistanceSq = ClientUtils.mc().player.distanceToSqr(event.getItemFrameEntity());

			if(playerDistanceSq < 1000)
			{
				List<RecipeHolder<BlueprintCraftingRecipe>> recipes = BlueprintCraftingRecipe.findRecipes(event.getItemFrameEntity().level(), event.getItemStack().get(BLUEPRINT_TYPE));
				if(!recipes.isEmpty())
				{
					int i = event.getItemFrameEntity().getRotation();
					BlueprintCraftingRecipe recipe = recipes.get(i%recipes.size()).value();
					BlueprintLines blueprint = recipe==null?null: BlueprintRenderer.getBlueprintDrawable(recipe, event.getItemFrameEntity().getCommandSenderWorld());
					if(blueprint!=null)
					{
						PoseStack transform = event.getPoseStack();
						transform.pushPose();
						MultiBufferSource buffer = event.getMultiBufferSource();
						transform.mulPose(new Quaternionf().rotateXYZ(0, 0, -i*Mth.PI/4));
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
	public void onRenderOverlayPre(RenderGuiLayerEvent.Pre event)
	{
		if(!ZoomHandler.isZooming||!event.getLayer().equals(VanillaGuiLayers.CROSSHAIR))
			return;

		event.setCanceled(true);
		MultiBufferSource.BufferSource buffers = event.getGuiGraphics().bufferSource();
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

	@SubscribeEvent()
	public void onFogUpdate(ViewportEvent.RenderFog event)
	{
		if(event.getCamera().getEntity() instanceof LivingEntity living&&living.hasEffect(IEPotions.FLASHED))
		{
			MobEffectInstance effect = living.getEffect(IEPotions.FLASHED);
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
		if(e instanceof LivingEntity&&((LivingEntity)e).hasEffect(IEPotions.FLASHED))
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
		if(player.getEffect(IEPotions.CONCRETE_FEET)!=null)
			event.setNewFovModifier(1);
	}

	@SubscribeEvent
	public void onMouseEvent(MouseScrollingEvent event)
	{
		Player player = ClientUtils.mc().player;
		if(event.getScrollDeltaY()!=0&&ClientUtils.mc().screen==null&&player!=null)
		{
			ItemStack equipped = player.getItemInHand(InteractionHand.MAIN_HAND);
			// Handle zoom steps
			if(equipped.getItem() instanceof IZoomTool&&((IZoomTool)equipped.getItem()).canZoom(equipped, player)&&ZoomHandler.isZooming)
			{
				float[] steps = ((IZoomTool)equipped.getItem()).getZoomSteps(equipped, player);
				if(steps!=null&&steps.length > 0)
				{
					int curStep = ZoomHandler.getCurrentZoomStep(steps);
					int newStep = curStep+(event.getScrollDeltaY() > 0?-1: 1);
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
					PacketDistributor.sendToServer(new MessageScrollwheelItem(event.getScrollDeltaY() < 0));
					event.setCanceled(true);
				}
				if(equipped.getItem() instanceof RevolverItem)
				{
					PacketDistributor.sendToServer(new MessageRevolverRotate(event.getScrollDeltaY() < 0));
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
			Level world = living.level();
			BlockState targetBlock = world.getBlockState(rtr.getBlockPos());
			Vec3 renderView = event.getCamera().getPosition();
			transform.pushPose();
			transform.translate(-renderView.x, -renderView.y, -renderView.z);
			transform.translate(pos.getX(), pos.getY(), pos.getZ());
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

			if(!stack.isEmpty()&&ConveyorHandler.isConveyorBlock(Block.byItem(stack.getItem()))&&rtr.getDirection().getAxis()==Axis.Y)
			{
				Direction side = rtr.getDirection();
				VoxelShape shape = world.getBlockState(pos).getBlockSupportShape(world, pos);
				AABB targetedBB = null;
				if(!shape.isEmpty())
					targetedBB = shape.bounds();
				BlockOverlayUtils.drawQuadrantX(transform, buffer, side, targetedBB, 0.002f);

				float xFromMid = side.getAxis()==Axis.X?0: (float)rtr.getLocation().x-pos.getX()-.5f;
				float yFromMid = side.getAxis()==Axis.Y?0: (float)rtr.getLocation().y-pos.getY()-.5f;
				float zFromMid = side.getAxis()==Axis.Z?0: (float)rtr.getLocation().z-pos.getZ()-.5f;
				float max = Math.max(Math.abs(yFromMid), Math.max(Math.abs(xFromMid), Math.abs(zFromMid)));
				Vec3 dir = new Vec3(max==Math.abs(xFromMid)?Math.signum(xFromMid): 0, max==Math.abs(yFromMid)?Math.signum(yFromMid): 0, max==Math.abs(zFromMid)?Math.signum(zFromMid): 0);
				BlockOverlayUtils.drawBlockOverlayArrow(transform.last(), buffer, dir, side, targetedBB);
			}

			if(targetBlock.getBlock() instanceof CatwalkBlock&&Utils.isHammer(stack)&&rtr.getDirection()==Direction.UP&&living.isShiftKeyDown())
			{
				AABB targetedBB = new AABB(0, 0, 0, 1, .125, 1);
				BlockOverlayUtils.drawQuadrantX(transform, buffer, Direction.UP, targetedBB, 0.002f);
			}


			transform.popPose();
			// fix lines overlaying on translucent blocks
			if(targetBlock.getBlock() instanceof WindowBlock)
			{
				((WorldRendererAccess)event.getLevelRenderer()).callRenderHitOutline(
						transform, buffer.getBuffer(IERenderTypes.LINES_NONTRANSLUCENT),
						living, renderView.x, renderView.y, renderView.z,
						pos, targetBlock
				);
				event.setCanceled(true);
			}

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
		if(event.getEntity().level().isClientSide&&event.getEntity() instanceof AbstractMinecart)
			PacketDistributor.sendToServer(new MessageMinecartShaderSync(event.getEntity().getId(), Optional.empty()));
	}
}
