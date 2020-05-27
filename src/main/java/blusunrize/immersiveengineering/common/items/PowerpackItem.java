/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.client.models.ModelPowerpack;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEEnergyItem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
		super("powerpack");
	}

	@Nullable
	@Override
	public EquipmentSlotType getEquipmentSlot(ItemStack stack)
	{
		return EquipmentSlotType.CHEST;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type)
	{
		return "immersiveengineering:textures/models/powerpack.png";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default)
	{
		return ModelPowerpack.getModel();
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
		list.add(new TranslationTextComponent(Lib.DESC+"info.energyStored", stored));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@Override
	public void onArmorTick(ItemStack itemStack, World world, PlayerEntity player)
	{
		int energy = getEnergyStored(itemStack);
		if(energy > 0)
		{
			int pre = energy;
			for(EquipmentSlotType slot : EquipmentSlotType.values())
				if(EnergyHelper.isFluxReceiver(player.getItemStackFromSlot(slot))&&!(player.getItemStackFromSlot(slot).getItem() instanceof PowerpackItem))
					energy -= EnergyHelper.insertFlux(player.getItemStackFromSlot(slot), Math.min(energy, 256), false);
			if(pre!=energy)
				EnergyHelper.extractFlux(itemStack, pre-energy, false);
		}
	}

	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return 100000;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
	{
		if(!stack.isEmpty())
			return new ICapabilityProvider()
			{
				final LazyOptional<EnergyHelper.ItemEnergyStorage> energyStorage = ApiUtils.constantOptional(
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