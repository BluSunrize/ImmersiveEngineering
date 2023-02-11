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
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ItemGetterList;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.util.EnergyHelper.*;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class PowerpackItem extends UpgradeableToolItem
{
	private static Map<Item, CapacitorConfig> capacitorConfigMap = new HashMap<>();

	static
	{
		capacitorConfigMap.put(MetalDevices.CAPACITOR_LV.asItem(), IEServerConfig.MACHINES.lvCapConfig);
		capacitorConfigMap.put(MetalDevices.CAPACITOR_MV.asItem(), IEServerConfig.MACHINES.mvCapConfig);
		capacitorConfigMap.put(MetalDevices.CAPACITOR_HV.asItem(), IEServerConfig.MACHINES.hvCapConfig);
		capacitorConfigMap.put(MetalDevices.CAPACITOR_CREATIVE.asItem(), IEServerConfig.Machines.CapacitorConfig.CREATIVE);
	}

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
		super(new Properties().stacksTo(1), "powerpack");
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		IEnergyStorage energy = CapabilityUtils.getPresentCapability(stack, CapabilityEnergy.ENERGY);
		String stored = energy.getEnergyStored()+"/"+getMaxEnergyStored(stack);
		list.add(new TranslatableComponent(Lib.DESC+"info.energyStored", stored).withStyle(ChatFormatting.GRAY));
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
				if(isFluxReceiver(equipped)&&!(equipped.getItem() instanceof PowerpackItem)&&!(equipped.getItem() instanceof BlockItem))
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

	public static ItemStack getCapacitorStatic(ItemStack container)
	{
		if(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY==null)
			return ItemStack.EMPTY;
		LazyOptional<IItemHandler> cap = container.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if(cap.isPresent())
		{
			ItemStack capacitor = cap.map(handler -> handler.getStackInSlot(0)).orElse(ItemStack.EMPTY);
			return capacitorConfigMap.containsKey(capacitor.getItem())?capacitor: ItemStack.EMPTY;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack getBannerStatic(ItemStack container)
	{
		if(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY==null)
			return ItemStack.EMPTY;
		LazyOptional<IItemHandler> cap = container.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if(cap.isPresent())
		{
			ItemStack banner = cap.map(handler -> handler.getStackInSlot(1)).orElse(ItemStack.EMPTY);
			return banner.getItem() instanceof BannerItem?banner:ItemStack.EMPTY;
		}
		return ItemStack.EMPTY;
	}

	public static int getMaxEnergyStored(ItemStack container)
	{
		ItemStack capacitor = getCapacitorStatic(container);
		if(!capacitor.isEmpty())
		{
			CapacitorConfig cfg = capacitorConfigMap.get(capacitor.getItem());
			if(cfg!=null)
				return cfg.storage.getAsInt();
		}
		return 0;
	}

	@Override
	public int getSlotCount()
	{
		return 2;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final LazyOptional<ItemEnergyStorage> energyStorage = CapabilityUtils.constantOptional(
						new EnergyHelper.ItemEnergyStorage(stack, PowerpackItem::getMaxEnergyStored)
				);

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
				{
					if(capability==CapabilityEnergy.ENERGY)
						return energyStorage.cast();
					return super.getCapability(capability, facing);
				}
			};
		return super.initCapabilities(stack, nbt);
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
				new IESlot.WithPredicate(toolInventory, 0, 98, 22, (itemStack) -> capacitorConfigMap.containsKey(itemStack.getItem())),
				new IESlot.WithPredicate(toolInventory, 1, 118, 52, (itemStack) -> itemStack.getItem() instanceof BannerItem)
		};
	}
}