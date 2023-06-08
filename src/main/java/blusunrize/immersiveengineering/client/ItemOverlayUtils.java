/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.client.gui.RevolverScreen;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class ItemOverlayUtils
{
	public static VertexConsumer getHudElementsBuilder(MultiBufferSource.BufferSource buffer)
	{
		return buffer.getBuffer(IERenderTypes.getGui(rl("textures/gui/hud_elements.png")));
	}

	public static void renderRevolverOverlay(MultiBufferSource.BufferSource buffer, GuiGraphics graphics, int scaledWidth, int scaledHeight,
											 Player player, InteractionHand hand, ItemStack equipped)
	{
		NonNullList<ItemStack> bullets = ((IBulletContainer)equipped.getItem()).getBullets(equipped);
		if(bullets!=null)
		{
			int bulletAmount = ((IBulletContainer)equipped.getItem()).getBulletCount(equipped);
			HumanoidArm side = ItemUtils.getLivingHand(player, hand);
			boolean right = side==HumanoidArm.RIGHT;
			float dx = right?scaledWidth-32-48: 48;
			float dy = scaledHeight-64;
			PoseStack transform = graphics.pose();
			transform.pushPose();
			transform.pushPose();
			transform.translate(dx, dy, 0);
			transform.scale(.5f, .5f, 1);
			RevolverScreen.drawExternalGUI(bullets, bulletAmount, graphics);
			transform.popPose();

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

					VertexConsumer builder = getHudElementsBuilder(buffer);
					Matrix4f mat = transform.last().pose();
					builder.vertex(mat, (right?0: 1-x2)*7, 15, 0)
							.color(1F, 1F, 1F, 1F)
							.uv(uMin, 127/256f)
							.endVertex();
					builder.vertex(mat, (right?x2: 1)*7, 15, 0)
							.color(1F, 1F, 1F, 1F)
							.uv(uMax, 127/256f)
							.endVertex();
					builder.vertex(mat, (right?x2: 1)*7, (right?h2: h1)*15, 0)
							.color(1F, 1F, 1F, 1F)
							.uv(uMax, vMin2)
							.endVertex();
					builder.vertex(mat, (right?0: 1-x2)*7, (right?h1: h2)*15, 0)
							.color(1F, 1F, 1F, 1F)
							.uv(uMin, vMin1)
							.endVertex();
				}
			}
			transform.popPose();
		}
	}

	public static void renderRailgunOverlay(MultiBufferSource.BufferSource buffer, PoseStack transform, int scaledWidth, int scaledHeight,
											Player player, InteractionHand hand, ItemStack equipped)
	{
		int duration = 72000-(player.isUsingItem()&&player.getUsedItemHand()==hand?player.getUseItemRemainingTicks(): 0);
		int chargeTime = ((RailgunItem)equipped.getItem()).getChargeTime(equipped);
		int chargeLevel = duration < 72000?Math.min(99, (int)(duration/(float)chargeTime*100)): 0;
		float scale = 1.5f;

		VertexConsumer builder = getHudElementsBuilder(buffer);
		boolean boundLeft = (player.getMainArm()==HumanoidArm.RIGHT)==(hand==InteractionHand.OFF_HAND);
		float dx = boundLeft?24: (scaledWidth-24-64);
		float dy = scaledHeight-16;
		transform.pushPose();
		transform.translate(dx, dy, 0);
		GuiHelper.drawTexturedColoredRect(builder, transform, 0, -32, 64, 32, 1, 1, 1, 1, 0, 64/256f, 96/256f, 128/256f);

		ItemStack ammo = RailgunItem.findAmmo(equipped, player);
		if(!ammo.isEmpty())
			GuiHelper.renderItemWithOverlayIntoGUI(buffer, transform, ammo, 6, -22, player.level());

		transform.translate(30, -27.5, 0);
		transform.scale(scale, scale, 1);
		String chargeTxt = chargeLevel < 10?"0 "+chargeLevel: chargeLevel/10+" "+chargeLevel%10;
		ClientUtils.font().drawInBatch(
				chargeTxt, 0, 0, Lib.COLOUR_I_ImmersiveOrange,
				true, transform.last().pose(), buffer, DisplayMode.NORMAL,
				0, 0xf000f0
		);
		transform.popPose();
	}

	public static void renderFluidTankOverlay(MultiBufferSource.BufferSource buffer, PoseStack transform, int scaledWidth, int scaledHeight,
											  Player player, InteractionHand hand, ItemStack equipped, boolean renderFluidUse,
											  BiConsumer<VertexConsumer, IFluidHandlerItem> additionalRender)
	{
		VertexConsumer builder = getHudElementsBuilder(buffer);
		float dx = scaledWidth-16;
		float dy = scaledHeight;
		transform.pushPose();
		transform.translate(dx, dy, 0);
		int w = 31;
		int h = 62;
		float uMin = 179/256f;
		float uMax = 210/256f;
		float vMin = 9/256f;
		float vMax = 71/256f;
		GuiHelper.drawTexturedColoredRect(builder, transform, -24, -68, w, h, 1, 1, 1, 1, uMin, uMax, vMin, vMax);

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
				if(renderFluidUse&&player.isUsingItem()&&player.getUsedItemHand()==hand)
				{
					int use = player.getTicksUsingItem();
					amount -= use*IEServerConfig.TOOLS.chemthrower_consumption.get();
				}
				float cap = (float)capacity;
				float angle = 83-(166*amount/cap);
				transform.pushPose();
				transform.mulPose(new Quaternionf().rotateZ(angle *Mth.DEG_TO_RAD));
				GuiHelper.drawTexturedColoredRect(builder, transform, 6, -2, 24, 4, 1, 1, 1, 1, 91/256f, 123/256f, 80/256f, 87/256f);
				transform.popPose();
				transform.translate(23, 37, 0);

				additionalRender.accept(builder, handler);
			}
		});
		transform.popPose();
	}


	public static void renderDrillOverlay(MultiBufferSource.BufferSource buffer, PoseStack transform, int scaledWidth, int scaledHeight,
										  Player player, InteractionHand hand, ItemStack equipped)
	{
		renderFluidTankOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped, false, (builder, handler) -> {
			GuiHelper.drawTexturedColoredRect(builder, transform, -54, -73, 66, 72, 1, 1, 1, 1, 108/256f, 174/256f, 4/256f, 76/256f);
			ItemStack head = ((DrillItem)equipped.getItem()).getHead(equipped);
			if(!head.isEmpty())
				GuiHelper.renderItemWithOverlayIntoGUI(buffer, transform, head, -51, -45, player.level());
		});
	}

	public static void renderBuzzsawOverlay(MultiBufferSource.BufferSource buffer, PoseStack transform, int scaledWidth, int scaledHeight,
											Player player, InteractionHand hand, ItemStack equipped)
	{
		renderFluidTankOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped, false, (builder, handler) -> {
			GuiHelper.drawTexturedColoredRect(builder, transform, -54, -73, 66, 72, 1, 1, 1, 1, 108/256f, 174/256f, 4/256f, 76/256f);
			ItemStack blade = ((BuzzsawItem)equipped.getItem()).getHead(equipped);
			if(!blade.isEmpty())
				GuiHelper.renderItemWithOverlayIntoGUI(buffer, transform, blade, -51, -45, player.level());
		});
	}

	public static void renderChemthrowerOverlay(MultiBufferSource.BufferSource buffer, PoseStack transform, int scaledWidth, int scaledHeight,
												Player player, InteractionHand hand, ItemStack equipped)
	{
		renderFluidTankOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped, true, (builder, handler) -> {
			GuiHelper.drawTexturedColoredRect(builder, transform, -41, -73, 53, 72, 1, 1, 1, 1, 8/256f, 61/256f, 4/256f, 76/256f);
			boolean ignite = ChemthrowerItem.isIgniteEnable(equipped);
			GuiHelper.drawTexturedColoredRect(builder, transform, -32, -43, 12, 12, 1, 1, 1, 1, 66/256f, 78/256f, (ignite?21: 9)/256f, (ignite?33: 21)/256f);

			GuiHelper.drawTexturedColoredRect(builder, transform, -100, -20, 64, 16, 1, 1, 1, 1, 0/256f, 64/256f, 76/256f, 92/256f);
			FluidStack fuel = handler.getFluidInTank(0);
			if(!fuel.isEmpty())
			{
				String name = ClientUtils.font().substrByWidth(fuel.getDisplayName(), 50).getString().trim();
				ClientUtils.font().drawInBatch(
						name, -68-ClientUtils.font().width(name)/2, -15, 0,
						false, transform.last().pose(), buffer, DisplayMode.NORMAL,
						0, 0xf000f0
				);
			}
		});
	}

	public static void renderShieldOverlay(MultiBufferSource.BufferSource buffer, PoseStack transform, int scaledWidth, int scaledHeight,
										   Player player, InteractionHand hand, ItemStack equipped)
	{
		CompoundTag upgrades = ((IEShieldItem)equipped.getItem()).getUpgrades(equipped);
		if(!upgrades.isEmpty())
		{
			VertexConsumer builder = getHudElementsBuilder(buffer);
			boolean boundLeft = (player.getMainArm()==HumanoidArm.RIGHT)==(hand==InteractionHand.OFF_HAND);
			float dx = boundLeft?16: (scaledWidth-16-64);
			float dy = scaledHeight;
			transform.pushPose();
			transform.translate(dx, dy, 0);
			GuiHelper.drawTexturedColoredRect(builder, transform, 0, -22, 64, 22, 1, 1, 1, 1, 0, 64/256f, 176/256f, 198/256f);

			if(upgrades.getBoolean("flash"))
			{
				GuiHelper.drawTexturedColoredRect(builder, transform, 11, -38, 16, 16, 1, 1, 1, 1, 11/256f, 27/256f, 160/256f, 176/256f);
				if(upgrades.contains("flash_cooldown"))
				{
					float h = upgrades.getInt("flash_cooldown")/40f*16;
					GuiHelper.drawTexturedColoredRect(builder, transform, 11, -22-h, 16, h, 1, 1, 1, 1, 11/256f, 27/256f, (214-h)/256f, 214/256f);
				}
			}
			if(upgrades.getBoolean("shock"))
			{
				GuiHelper.drawTexturedColoredRect(builder, transform, 40, -38, 12, 16, 1, 1, 1, 1, 40/256f, 52/256f, 160/256f, 176/256f);
				if(upgrades.contains("shock_cooldown"))
				{
					float h = upgrades.getInt("shock_cooldown")/40f*16;
					GuiHelper.drawTexturedColoredRect(builder, transform, 40, -22-h, 12, h, 1, 1, 1, 1, 40/256f, 52/256f, (214-h)/256f, 214/256f);
				}
			}
			transform.popPose();
		}
	}
}
