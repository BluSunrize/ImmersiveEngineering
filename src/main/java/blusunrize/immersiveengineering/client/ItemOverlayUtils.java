/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.Lib.GuiLayers;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.client.gui.RevolverScreen;
import blusunrize.immersiveengineering.client.utils.FontUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.SpacerComponent;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.network.MessageRequestEnergyUpdate;
import blusunrize.immersiveengineering.common.network.MessageRequestRedstoneUpdate;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;
import static blusunrize.immersiveengineering.api.Lib.getRedstoneColorComponent;

@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID, bus = Bus.MOD)
public class ItemOverlayUtils
{
	private static final List<SubtitleOffset> SUBTITLE_OFFSETS = List.of(
			new SubtitleOffset(s -> s.getItem() instanceof ChemthrowerItem, 60f),
			new SubtitleOffset(s -> s.getItem() instanceof DrillItem||s.getItem() instanceof BuzzsawItem, 70f),
			new SubtitleOffset(s -> s.getItem() instanceof RevolverItem||s.getItem() instanceof SpeedloaderItem
					||(s.getItem() instanceof IEShieldItem shield&&!shield.getUpgrades(s).entries().isEmpty()), 80f),
			new SubtitleOffset(s -> s.getItem() instanceof RailgunItem, 90f)
	);

	public static void handleTooltipOffset(GuiGraphics guiGraphics, boolean pre)
	{
		Player player = ClientUtils.mc().player;
		if(player==null)
			return;
		ItemStack rightHandItem = HumanoidArm.RIGHT==player.getMainArm()?player.getMainHandItem(): player.getOffhandItem();
		SUBTITLE_OFFSETS.forEach(c -> {
			if(c.cond.test(rightHandItem))
				guiGraphics.pose().translate(pre?-c.offset: c.offset, 0, 0);
		});
	}

	@SubscribeEvent
	public static void register(RegisterGuiLayersEvent ev)
	{
		ev.registerBelow(
				VanillaGuiLayers.DEBUG_OVERLAY,
				GuiLayers.ITEMS,
				ItemOverlayUtils::renderItemOverlays
		);
	}

	public static void renderItemOverlays(GuiGraphics graphics, DeltaTracker delta)
	{
		Player player = ClientUtils.mc().player;
		if(player==null)
			return;
		int scaledWidth = ClientUtils.mc().getWindow().getGuiScaledWidth();
		int scaledHeight = ClientUtils.mc().getWindow().getGuiScaledHeight();

		for(InteractionHand hand : InteractionHand.values())
			if(!player.getItemInHand(hand).isEmpty())
			{
				ItemStack equipped = player.getItemInHand(hand);
				if(equipped.is(Tools.VOLTMETER.asItem())||equipped.getItem() instanceof IWireCoil)
					renderWireCoilOverlay(graphics, equipped, player, scaledWidth, scaledHeight);
				else if(equipped.getItem()==Misc.FLUORESCENT_TUBE.get())
					renderFluorescentTubeOverlay(graphics, equipped, scaledWidth, scaledHeight);
				else if(equipped.getItem() instanceof RevolverItem||equipped.getItem() instanceof SpeedloaderItem)
					renderRevolverOverlay(graphics, scaledWidth, scaledHeight, player, hand, equipped);
				else if(equipped.getItem() instanceof RailgunItem)
					renderRailgunOverlay(graphics, scaledWidth, scaledHeight, player, hand, equipped);
				else if(equipped.getItem() instanceof DrillItem)
					renderDrillOverlay(graphics, scaledWidth, scaledHeight, player, hand, equipped);
				else if(equipped.getItem() instanceof BuzzsawItem)
					renderBuzzsawOverlay(graphics, scaledWidth, scaledHeight, player, hand, equipped);
				else if(equipped.getItem() instanceof ChemthrowerItem)
					renderChemthrowerOverlay(graphics, scaledWidth, scaledHeight, player, hand, equipped);
				else if(equipped.getItem() instanceof IEShieldItem)
					renderShieldOverlay(graphics, scaledWidth, scaledHeight, player, hand, equipped);
				if(equipped.getItem()==Tools.VOLTMETER.get())
					renderVoltmeterOverlay(graphics, player, scaledWidth, scaledHeight);
			}
	}

	private static void renderWireCoilOverlay(
			GuiGraphics graphics, ItemStack equipped, Player player, int scaledWidth, int scaledHeight
	)
	{
		if(!equipped.has(IEApiDataComponents.WIRE_LINK))
			return;
		WireLink link = equipped.get(IEApiDataComponents.WIRE_LINK);
		BlockPos pos = link.cp().position();
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
		graphics.drawCenteredString(ClientUtils.font(), s, scaledWidth/2, scaledHeight-20-leftHeight(), col);
	}

	private static void renderFluorescentTubeOverlay(
			GuiGraphics graphics, ItemStack equipped, int scaledWidth, int scaledHeight
	)
	{
		int color = FluorescentTubeItem.getRGBInt(equipped, 1);
		String s = I18n.get(Lib.DESC_INFO+"colour")+"#"+FontUtils.hexColorString(color);
		graphics.drawCenteredString(
				ClientUtils.font(), s, scaledWidth/2, scaledHeight-20-leftHeight(), FluorescentTubeItem.getRGBInt(equipped, 1)
		);
	}

	public static void renderRevolverOverlay(GuiGraphics graphics, int scaledWidth, int scaledHeight,
											 Player player, InteractionHand hand, ItemStack equipped)
	{
		NonNullList<ItemStack> bullets = ((IBulletContainer)equipped.getItem()).getBullets(equipped);
		if(bullets!=null)
		{
			int bulletAmount = ((IBulletContainer)equipped.getItem()).getBulletCount(equipped);
			boolean boundLeft = ItemUtils.getLivingHand(player, hand)==HumanoidArm.LEFT;
			float dx = boundLeft?48: scaledWidth-32-48;
			float dy = scaledHeight-64;
			PoseStack transform = graphics.pose();
			transform.pushPose();
			transform.translate(dx, dy, 0);
			transform.scale(.5f, .5f, 1);
			RevolverScreen.drawExternalGUI(bullets, bulletAmount, graphics);
			transform.popPose();
		}
	}

	public static void renderRailgunOverlay(GuiGraphics graphics, int scaledWidth, int scaledHeight,
											Player player, InteractionHand hand, ItemStack equipped)
	{
		int duration = 72000-(player.isUsingItem()&&player.getUsedItemHand()==hand?player.getUseItemRemainingTicks(): 0);
		int chargeTime = RailgunItem.getChargeTime(equipped);
		int chargeLevel = duration < 72000?Math.min(99, (int)(duration/(float)chargeTime*100)): 0;
		float scale = 1.5f;

		boolean boundLeft = ItemUtils.getLivingHand(player, hand)==HumanoidArm.LEFT;
		int dx = boundLeft?24: (scaledWidth-24-64);
		int dy = scaledHeight-16;
		var transform = graphics.pose();
		transform.pushPose();
		transform.translate(dx, dy, 0);
		graphics.blitSprite(ieLoc("hud/railgun_base"), 0, -32, 64, 32);

		ItemStack ammo = RailgunItem.findAmmo(equipped, player);
		if(!ammo.isEmpty())
			GuiHelper.renderItemWithOverlayIntoGUI(graphics, ammo, 6, -22, player.level());

		transform.translate(30, -27.5, 0);
		transform.scale(scale, scale, 1);
		String chargeTxt = chargeLevel < 10?"0 "+chargeLevel: chargeLevel/10+" "+chargeLevel%10;
		graphics.drawString(
				ClientUtils.font(), chargeTxt, 0, 0, Lib.COLOUR_I_ImmersiveOrange, true
		);
		transform.popPose();
	}

	public static void renderFluidTankOverlay(GuiGraphics graphics, int xStart, int scaledHeight,
											  Player player, InteractionHand hand, ItemStack equipped, boolean renderFluidUse,
											  BiConsumer<GuiGraphics, IFluidHandlerItem> additionalRender)
	{
		var transform = graphics.pose();
		float dx = xStart;
		float dy = scaledHeight;
		transform.pushPose();
		transform.translate(dx, dy, 0);
		graphics.blitSprite(ieLoc("hud/gauge_full_empty"), -24, -68, 31, 62);

		transform.translate(-23, -37, 0);
		IFluidHandlerItem handler = equipped.getCapability(FluidHandler.ITEM);
		if(handler!=null)
		{
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
				transform.mulPose(new Quaternionf().rotateZ(angle*Mth.DEG_TO_RAD));
				graphics.blitSprite(ieLoc("hud/gauge_pointer"), 6, -2, 24, 4);
				transform.popPose();
				transform.translate(23, 37, 0);

				additionalRender.accept(graphics, handler);
			}
		}
		transform.popPose();
	}


	public static void renderDrillOverlay(GuiGraphics graphics, int scaledWidth, int scaledHeight,
										  Player player, InteractionHand hand, ItemStack equipped)
	{
		boolean boundLeft = ItemUtils.getLivingHand(player, hand)==HumanoidArm.LEFT;
		int xStart = boundLeft?60: scaledWidth-16;
		renderFluidTankOverlay(graphics, xStart, scaledHeight, player, hand, equipped, false, (builder, handler) -> {
			builder.blitSprite(ieLoc("hud/gauge_with_item"), -54, -73, 66, 72);
			ItemStack head = ((DrillItem)equipped.getItem()).getHead(equipped);
			if(!head.isEmpty())
				GuiHelper.renderItemWithOverlayIntoGUI(graphics, head, -51, -45, player.level());
		});
	}

	public static void renderBuzzsawOverlay(GuiGraphics graphics, int scaledWidth, int scaledHeight,
											Player player, InteractionHand hand, ItemStack equipped)
	{
		boolean boundLeft = ItemUtils.getLivingHand(player, hand)==HumanoidArm.LEFT;
		int xStart = boundLeft?60: scaledWidth-16;
		renderFluidTankOverlay(graphics, xStart, scaledHeight, player, hand, equipped, false, (builder, handler) -> {
			builder.blitSprite(ieLoc("hud/gauge_with_item"), -54, -73, 66, 72);
			ItemStack blade = ((BuzzsawItem)equipped.getItem()).getHead(equipped);
			if(!blade.isEmpty())
				GuiHelper.renderItemWithOverlayIntoGUI(graphics, blade, -51, -45, player.level());
		});
	}

	public static void renderChemthrowerOverlay(GuiGraphics graphics, int scaledWidth, int scaledHeight,
												Player player, InteractionHand hand, ItemStack equipped)
	{
		boolean boundLeft = ItemUtils.getLivingHand(player, hand)==HumanoidArm.LEFT;
		int xStart = boundLeft?54: scaledWidth-16;
		renderFluidTankOverlay(graphics, xStart, scaledHeight, player, hand, equipped, true, (builder, handler) -> {
			builder.blitSprite(ieLoc("hud/gauge_no_item"), -41, -73, 53, 72);
			boolean ignite = ChemthrowerItem.isIgniteEnable(equipped);
			builder.blitSprite(ieLoc(ignite?"hud/with_flame": "hud/no_flame"), -32, -43, 12, 12);
			builder.blitSprite(ieLoc("hud/text_label"), -52, -93, 64, 16);
			FluidStack fuel = handler.getFluidInTank(0);
			if(!fuel.isEmpty())
			{
				String name = ClientUtils.font().substrByWidth(fuel.getHoverName(), 50).getString().trim();
				int width = ClientUtils.font().width(name);
				graphics.drawString(ClientUtils.font(), name, -20-width/2, -88, 0x333333, false);
			}
		});
	}

	public static void renderShieldOverlay(GuiGraphics graphics, int scaledWidth, int scaledHeight,
										   Player player, InteractionHand hand, ItemStack equipped)
	{
		var upgrades = ((IEShieldItem)equipped.getItem()).getUpgrades(equipped);
		if(upgrades.entries().isEmpty())
			return;
		boolean boundLeft = ItemUtils.getLivingHand(player, hand)==HumanoidArm.LEFT;
		float dx = boundLeft?16: (scaledWidth-16-64);
		float dy = scaledHeight-16;
		var transform = graphics.pose();
		transform.pushPose();
		transform.translate(dx, dy, 0);
		graphics.blitSprite(ieLoc("hud/shield_upgrades_base"), 0, -22, 64, 22);

		if(upgrades.has(UpgradeEffect.FLASH))
		{
			graphics.blitSprite(ieLoc("hud/shield_upgrade_flash"), 11, -38, 16, 16);
			final var cooldown = upgrades.get(UpgradeEffect.FLASH);
			if(cooldown.isOnCooldown())
			{
				int h = (int)(cooldown.remainingCooldown()/40f*16);
				graphics.blitSprite(
						ieLoc("hud/shield_flash_cooldown"), 16, 16, 0, 16-h, 11, -22-h, 16, h
				);
			}
		}
		if(upgrades.has(UpgradeEffect.SHOCK))
		{
			graphics.blitSprite(ieLoc("hud/shield_upgrade_shock"), 40, -38, 12, 16);
			final var cooldown = upgrades.get(UpgradeEffect.SHOCK);
			if(cooldown.isOnCooldown())
			{
				int h = (int)(cooldown.remainingCooldown()/40f*16);
				graphics.blitSprite(
						ieLoc("hud/shield_shock_cooldown"), 12, 16, 0, 16-h, 40, -22-h, 12, h
				);
			}
		}
		transform.popPose();
	}

	private static void renderVoltmeterOverlay(GuiGraphics graphics, Player player, int scaledWidth, int scaledHeight)
	{
		HitResult rrt = ClientUtils.mc().hitResult;
		Either<BlockPos, Integer> pos = null;
		if(rrt instanceof BlockHitResult mop)
			pos = Either.left(mop.getBlockPos());
		else if(rrt instanceof EntityHitResult ehr)
			pos = Either.right(ehr.getEntity().getId());

		if(pos==null)
			return;

		ArrayList<Component> text = new ArrayList<>();

		boolean matches = VoltmeterItem.lastEnergyUpdate.pos().equals(pos);
		long sinceLast = player.level().getGameTime()-VoltmeterItem.lastEnergyUpdate.measuredInTick();
		if(!matches||sinceLast > 20)
			PacketDistributor.sendToServer(new MessageRequestEnergyUpdate(pos));

		if(VoltmeterItem.lastEnergyUpdate.isValid()&&matches)
		{
			int maxStorage = VoltmeterItem.lastEnergyUpdate.capacity();
			int storage = VoltmeterItem.lastEnergyUpdate.stored();
			String storageText = Utils.toScientificNotation(storage, "0##", 100000);
			String capacityText = Utils.toScientificNotation(maxStorage, "0##", 100000);
			text.add(Component.translatable(Lib.DESC_INFO+"energyStored", storageText+" / "+capacityText));
		}

		if(rrt instanceof BlockHitResult mop)
		{
			matches = VoltmeterItem.lastRedstoneUpdate.pos().equals(mop.getBlockPos());
			sinceLast = player.level().getGameTime()-VoltmeterItem.lastRedstoneUpdate.measuredInTick();
			if(!matches||sinceLast > 20)
				PacketDistributor.sendToServer(new MessageRequestRedstoneUpdate(mop.getBlockPos()));

			if(VoltmeterItem.lastRedstoneUpdate.isSignalSource()&&matches)
				VoltmeterItem.lastRedstoneUpdate.rsLevels().consume(
						aByte -> text.add(Component.translatable(Lib.DESC_INFO+"redstone_level", String.valueOf(aByte))),
						pairs -> {
							if(pairs.length > 0)
							{
								text.add(Component.translatable(Lib.DESC_INFO+"redstone_level", ""));
								for(Pair<DyeColor, Byte> p : pairs)
								{
									Component c = Component.translatable(Lib.DESC_INFO+"redstone_level_on_channel",
											p.getSecond(),
											getRedstoneColorComponent(p.getFirst())
									);
									// if the value is less than 10, we want a space before it which has the same width as a digit
									text.add(p.getSecond() < 10?new SpacerComponent("0").append(c): c);
								}
							}
						}
				);
		}

		BlockOverlayUtils.drawBlockOverlayText(graphics, text, scaledWidth, scaledHeight);
	}

	private static int leftHeight()
	{
		return Minecraft.getInstance().gui.leftHeight;
	}

	private static int rightHeight()
	{
		return Minecraft.getInstance().gui.rightHeight;
	}

	record SubtitleOffset(Predicate<ItemStack> cond, float offset)
	{
	}
}
