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
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEEnergyItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class PowerpackItem extends IEBaseItem implements IIEEnergyItem
{
	public PowerpackItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
		list.add(new TranslatableComponent(Lib.DESC+"info.energyStored", stored));
	}

	@Override
	public void onArmorTick(ItemStack itemStack, Level world, Player player)
	{
		int energy = getEnergyStored(itemStack);
		if(energy > 0)
		{
			int pre = energy;
			for(EquipmentSlot slot : EquipmentSlot.values())
				if(EnergyHelper.isFluxReceiver(player.getItemBySlot(slot))&&!(player.getItemBySlot(slot).getItem() instanceof PowerpackItem))
					energy -= EnergyHelper.insertFlux(player.getItemBySlot(slot), Math.min(energy, 256), false);
			if(pre!=energy)
				EnergyHelper.extractFlux(itemStack, pre-energy, false);
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected)
	{
		// We'll just have to assume that's Curios which sets the slot of -1
		if(itemSlot==-1&&entity instanceof Player)
			onArmorTick(stack, world, (Player)entity);
	}

	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return 100000;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new ICapabilityProvider()
			{
				final LazyOptional<EnergyHelper.ItemEnergyStorage> energyStorage = CapabilityUtils.constantOptional(
						new EnergyHelper.ItemEnergyStorage(stack));

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
				{
					return capability==CapabilityEnergy.ENERGY?energyStorage.cast(): LazyOptional.empty();
				}
			};
		else
			return super.initCapabilities(stack, nbt);
	}
}