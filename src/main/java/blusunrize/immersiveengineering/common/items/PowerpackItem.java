/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.util.ItemGetterList;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.SimpleCapProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

import static blusunrize.immersiveengineering.common.util.EnergyHelper.*;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class PowerpackItem extends IEBaseItem
{
	public static final ItemGetterList POWERPACK_GETTER = new ItemGetterList(player -> {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if(chest.getItem() instanceof PowerpackItem)
			return chest;
		else if(ItemNBTHelper.hasKey(chest, Lib.NBT_Powerpack))
			return ItemNBTHelper.getItemStack(chest, Lib.NBT_Powerpack);
		else
			return ItemStack.EMPTY;
	});

	public PowerpackItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		IEnergyStorage energy = CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
		String stored = energy.getEnergyStored()+"/"+getMaxEnergyStored(stack);
		list.add(Component.translatable(Lib.DESC+"info.energyStored", stored).withStyle(ChatFormatting.GRAY));
	}

	@Nullable
	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack)
	{
		return EquipmentSlot.CHEST;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack heldItem = player.getItemInHand(hand);
		EquipmentSlot slot = Mob.getEquipmentSlotForItem(heldItem);
		if(!player.getItemBySlot(slot).isEmpty())
			return InteractionResultHolder.fail(heldItem);
		player.setItemSlot(slot, heldItem.copy());
		if(!world.isClientSide())
			player.awardStat(Stats.ITEM_USED.get(this));
		heldItem.setCount(0);
		return InteractionResultHolder.sidedSuccess(heldItem, world.isClientSide());
	}

	@Override
	public void onArmorTick(ItemStack itemStack, Level world, Player player)
	{
		int energy = getEnergyStored(itemStack);
		if(energy > 0)
		{
			int pre = energy;
			for(EquipmentSlot slot : EquipmentSlot.values())
			{
				ItemStack equipped = player.getItemBySlot(slot);
				if(isFluxReceiver(equipped)&&!(equipped.getItem() instanceof PowerpackItem))
					energy -= insertFlux(equipped, Math.min(energy, 256), false);
			}
			if(pre!=energy)
				extractFlux(itemStack, pre-energy, false);
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected)
	{
		// We'll just have to assume that's Curios which sets the slot of -1
		if(itemSlot==-1&&entity instanceof Player)
			onArmorTick(stack, world, (Player)entity);
	}

	public static int getMaxEnergyStored(ItemStack container)
	{
		return 100000;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new SimpleCapProvider<>(
					() -> ForgeCapabilities.ENERGY, new ItemEnergyStorage(stack, PowerpackItem::getMaxEnergyStored)
			);
		else
			return super.initCapabilities(stack, nbt);
	}
}