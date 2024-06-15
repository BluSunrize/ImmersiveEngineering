/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.common.entities.SkyhookUserData;
import blusunrize.immersiveengineering.common.entities.SkyhookUserData.SkyhookStatus;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.register.IEDataAttachments;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class SkyhookItem extends UpgradeableToolItem
{
	public SkyhookItem()
	{
		super(new Properties().stacksTo(1).component(IEDataComponents.SKYHOOK_SPEED_LIMIT, false), "SKYHOOK");
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		if(shouldLimitSpeed(stack))
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"skyhook.speedLimit"));
		else
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"skyhook.noLimit"));
		list.add(Component.translatable(Lib.DESC_FLAVOUR+"skyhook"));
	}

	public static boolean shouldLimitSpeed(ItemStack stack)
	{
		return stack.getOrDefault(IEDataComponents.SKYHOOK_SPEED_LIMIT, false);
	}

	public static void setLimitSpeed(ItemStack stack, boolean doLimit)
	{
		stack.set(IEDataComponents.SKYHOOK_SPEED_LIMIT, doLimit);
	}

	public static boolean toggleSpeedLimit(ItemStack stack)
	{
		boolean wasActive = shouldLimitSpeed(stack);
		setLimitSpeed(stack, !wasActive);
		return !wasActive;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{

		ItemStack stack = player.getItemInHand(hand);
		if(player.getCooldowns().isOnCooldown(this))
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		if(player.isShiftKeyDown())
		{
			boolean limitSpeed = toggleSpeedLimit(stack);
			if(limitSpeed)
				player.displayClientMessage(Component.translatable("chat.immersiveengineering.info.skyhookLimited"), true);
			else
				player.displayClientMessage(Component.translatable("chat.immersiveengineering.info.skyhookUnlimited"), true);
		}
		else
		{
			SkyhookUserData data = player.getData(IEDataAttachments.SKYHOOK_USER.get());
			if(data.hook!=null&&!world.isClientSide)
			{
				data.dismount();
				IELogger.logger.info("Player left voluntarily");
			}
			else
			{
				data.startHolding();
				player.startUsingItem(hand);
			}
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count)
	{
		super.onUseTick(level, player, stack, count);
		SkyhookUserData data = player.getData(IEDataAttachments.SKYHOOK_USER.get());
		if(data.getStatus()!=SkyhookStatus.HOLDING_CONNECTING)
			return;
		Connection con = WireUtils.getConnectionMovedThrough(level, player);
		if(con!=null)
			SkylineHelper.spawnHook(player, con, player.getUsedItemHand(), shouldLimitSpeed(stack));
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity player, int timeLeft)
	{
		super.releaseUsing(stack, worldIn, player, timeLeft);
		if(!worldIn.isClientSide)
			player.getData(IEDataAttachments.SKYHOOK_USER.get()).release();
	}

	public float getSkylineSpeed(ItemStack stack)
	{
		return 3f+this.getUpgrades(stack).getFloat("speed");
	}

	@Override
	public int getUseDuration(ItemStack p_41454_, LivingEntity p_344979_)
	{
		return 72000;
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]{
				new IESlot.Upgrades(container, toolInventory, 0, 102, 42, "SKYHOOK", stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 1, 102, 22, "SKYHOOK", stack, true, level, getPlayer),
		};
	}

	@Override
	public int getSlotCount()
	{
		return 2;
	}
}