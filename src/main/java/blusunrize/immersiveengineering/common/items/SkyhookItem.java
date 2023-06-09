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
import blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SkyhookStatus;
import blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SkyhookUserData;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SKYHOOK_USER_DATA;

public class SkyhookItem extends UpgradeableToolItem
{
	public SkyhookItem()
	{
		super(new Properties().stacksTo(1), "SKYHOOK");
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		if(shouldLimitSpeed(stack))
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"skyhook.speedLimit"));
		else
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"skyhook.noLimit"));
		list.add(Component.translatable(Lib.DESC_FLAVOUR+"skyhook"));
	}

	private static final String LIMIT_SPEED = "limitSpeed";

	public static boolean shouldLimitSpeed(ItemStack stack)
	{
		return ItemNBTHelper.getBoolean(stack, LIMIT_SPEED);
	}

	public static void setLimitSpeed(ItemStack stack, boolean doLimit)
	{
		ItemNBTHelper.putBoolean(stack, LIMIT_SPEED, doLimit);
	}

	public static boolean toggleSpeedLimit(ItemStack stack)
	{
		CompoundTag nbt = stack.getOrCreateTag();
		boolean wasActive = nbt.getBoolean(LIMIT_SPEED);
		nbt.putBoolean(LIMIT_SPEED, !wasActive);
		return !wasActive;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity ent, int slot, boolean inHand)
	{
		super.inventoryTick(stack, world, ent, slot, inHand);
		if(getUpgrades(stack).getBoolean("fallBoost"))
		{
			float dmg = (float)Math.ceil(ent.fallDistance/5);
			ItemNBTHelper.putFloat(stack, "fallDamageBoost", dmg);
		}
	}
	/*@Override
	public Multimap getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
	{
		Multimap multimap = super.getAttributeModifiers(slot, stack);
		if(slot == EntityEquipmentSlot.MAINHAND)
		{
			float dmg = 5 + ItemNBTHelper.getFloat(stack, "fallDamageBoost");
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", dmg, 0));
		}
		return multimap;
	}*/


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
			SkyhookUserData data = player.getCapability(SKYHOOK_USER_DATA, Direction.UP).orElseThrow(RuntimeException::new);
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
		SkyhookUserData data = player.getCapability(SKYHOOK_USER_DATA, Direction.UP).orElseThrow(RuntimeException::new);
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
			player.getCapability(SKYHOOK_USER_DATA, Direction.UP).ifPresent(SkyhookUserData::release);
	}

	public float getSkylineSpeed(ItemStack stack)
	{
		return 3f+this.getUpgrades(stack).getFloat("speed");
	}

	@Override
	public int getUseDuration(ItemStack stack)
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