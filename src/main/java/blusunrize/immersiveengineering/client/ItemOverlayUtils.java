package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.RevolverScreen;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class ItemOverlayUtils
{
	public static IVertexBuilder getHudElementsBuilder(IRenderTypeBuffer.Impl buffer)
	{
		return buffer.getBuffer(IERenderTypes.getGui(rl("textures/gui/hud_elements.png")));
	}

	public static void renderRevolverOverlay(IRenderTypeBuffer.Impl buffer, MatrixStack transform, int scaledWidth, int scaledHeight,
											 PlayerEntity player, Hand hand, ItemStack equipped)
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

					IVertexBuilder builder = getHudElementsBuilder(buffer);
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

	public static void renderRailgunOverlay(IRenderTypeBuffer.Impl buffer, MatrixStack transform, int scaledWidth, int scaledHeight,
											PlayerEntity player, Hand hand, ItemStack equipped)
	{
		int duration = 72000-(player.isHandActive()&&player.getActiveHand()==hand?player.getItemInUseCount(): 0);
		int chargeTime = ((RailgunItem)equipped.getItem()).getChargeTime(equipped);
		int chargeLevel = duration < 72000?Math.min(99, (int)(duration/(float)chargeTime*100)): 0;
		float scale = 1.5f;

		IVertexBuilder builder = getHudElementsBuilder(buffer);
		boolean boundLeft = (player.getPrimaryHand()==HandSide.RIGHT)==(hand==Hand.OFF_HAND);
		float dx = boundLeft?24: (scaledWidth-24-64);
		float dy = scaledHeight-16;
		transform.push();
		transform.translate(dx, dy, 0);
		ClientUtils.drawTexturedRect(builder, transform, 0, -32, 64, 32, 1, 1, 1, 1, 0, 64/256f, 96/256f, 128/256f);

		ItemStack ammo = RailgunItem.findAmmo(equipped, player);
		if(!ammo.isEmpty())
			ClientUtils.renderItemWithOverlayIntoGUI(buffer, transform, ammo, 6, -22);

		transform.translate(30, -27.5, 0);
		transform.scale(scale, scale, 1);
		String chargeTxt = chargeLevel < 10?"0 "+chargeLevel: chargeLevel/10+" "+chargeLevel%10;
		ClientUtils.font().renderString(
				chargeTxt, 0, 0, Lib.COLOUR_I_ImmersiveOrange,
				true, transform.getLast().getMatrix(), buffer, false,
				0, 0xf000f0
		);
		transform.pop();
	}

	public static void renderFluidTankOverlay(IRenderTypeBuffer.Impl buffer, MatrixStack transform, int scaledWidth, int scaledHeight,
											  PlayerEntity player, Hand hand, ItemStack equipped, boolean renderFluidUse,
											  BiConsumer<IVertexBuilder, IFluidHandlerItem> additionalRender)
	{
		IVertexBuilder builder = getHudElementsBuilder(buffer);
		int rightOffset = 0;
		if(ClientUtils.mc().gameSettings.showSubtitles)
			rightOffset += 100;
		float dx = scaledWidth-rightOffset-16;
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
				if(renderFluidUse&&player.isHandActive()&&player.getActiveHand()==hand)
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

				additionalRender.accept(builder, handler);
			}
		});
		transform.pop();
	}


	public static void renderDrillOverlay(IRenderTypeBuffer.Impl buffer, MatrixStack transform, int scaledWidth, int scaledHeight,
										  PlayerEntity player, Hand hand, ItemStack equipped)
	{
		renderFluidTankOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped, false, (builder, handler) -> {
			ClientUtils.drawTexturedRect(builder, transform, -54, -73, 66, 72, 1, 1, 1, 1, 108/256f, 174/256f, 4/256f, 76/256f);
			ItemStack head = ((DrillItem)equipped.getItem()).getHead(equipped);
			if(!head.isEmpty())
				ClientUtils.renderItemWithOverlayIntoGUI(buffer, transform, head, -51, -45);
		});
	}

	public static void renderBuzzsawOverlay(IRenderTypeBuffer.Impl buffer, MatrixStack transform, int scaledWidth, int scaledHeight,
											PlayerEntity player, Hand hand, ItemStack equipped)
	{
		renderFluidTankOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped, false, (builder, handler) -> {
			ClientUtils.drawTexturedRect(builder, transform, -54, -73, 66, 72, 1, 1, 1, 1, 108/256f, 174/256f, 4/256f, 76/256f);
			ItemStack blade = ((BuzzsawItem)equipped.getItem()).getSawblade(equipped);
			if(!blade.isEmpty())
				ClientUtils.renderItemWithOverlayIntoGUI(buffer, transform, blade, -51, -45);
		});
	}

	public static void renderChemthrowerOverlay(IRenderTypeBuffer.Impl buffer, MatrixStack transform, int scaledWidth, int scaledHeight,
												PlayerEntity player, Hand hand, ItemStack equipped)
	{
		renderFluidTankOverlay(buffer, transform, scaledWidth, scaledHeight, player, hand, equipped, true, (builder, handler) -> {
			ClientUtils.drawTexturedRect(builder, transform, -41, -73, 53, 72, 1, 1, 1, 1, 8/256f, 61/256f, 4/256f, 76/256f);
			boolean ignite = ChemthrowerItem.isIgniteEnable(equipped);
			ClientUtils.drawTexturedRect(builder, transform, -32, -43, 12, 12, 1, 1, 1, 1, 66/256f, 78/256f, (ignite?21: 9)/256f, (ignite?33: 21)/256f);

			ClientUtils.drawTexturedRect(builder, transform, -100, -20, 64, 16, 1, 1, 1, 1, 0/256f, 64/256f, 76/256f, 92/256f);
			FluidStack fuel = handler.getFluidInTank(0);
			if(!fuel.isEmpty())
			{
				String name = ClientUtils.font().trimStringToWidth(fuel.getDisplayName().getFormattedText(), 50).trim();
				ClientUtils.font().renderString(
						name, -68-ClientUtils.font().getStringWidth(name)/2, -15, 0,
						false, transform.getLast().getMatrix(), buffer, false,
						0, 0xf000f0
				);
			}
		});
	}

	public static void renderShieldOverlay(IRenderTypeBuffer.Impl buffer, MatrixStack transform, int scaledWidth, int scaledHeight,
										   PlayerEntity player, Hand hand, ItemStack equipped)
	{
		CompoundNBT upgrades = ((IEShieldItem)equipped.getItem()).getUpgrades(equipped);
		if(!upgrades.isEmpty())
		{
			IVertexBuilder builder = getHudElementsBuilder(buffer);
			boolean boundLeft = (player.getPrimaryHand()==HandSide.RIGHT)==(hand==Hand.OFF_HAND);
			float dx = boundLeft?16: (scaledWidth-16-64);
			float dy = scaledHeight;
			transform.push();
			transform.translate(dx, dy, 0);
			ClientUtils.drawTexturedRect(builder, transform, 0, -22, 64, 22, 1, 1, 1, 1, 0, 64/256f, 176/256f, 198/256f);

			if(upgrades.getBoolean("flash"))
			{
				ClientUtils.drawTexturedRect(builder, transform, 11, -38, 16, 16, 1, 1, 1, 1, 11/256f, 27/256f, 160/256f, 176/256f);
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
}
