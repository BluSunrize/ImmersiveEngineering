/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

public class FaradaySuitItem extends ArmorItem implements IElectricEquipment
{
	public static IArmorMaterial mat = new FaradayArmorMaterial();

	public FaradaySuitItem(EquipmentSlotType type)
	{
		super(mat, type, new Properties().maxStackSize(1).group(ImmersiveEngineering.itemGroup));
		String name = "armor_faraday_"+type.getName().toLowerCase(Locale.ENGLISH);
		setRegistryName(ImmersiveEngineering.MODID, name);
		IEContent.registeredIEItems.add(this);
	}

	@Override
	public void onStrike(ItemStack equipped, EquipmentSlotType eqSlot, LivingEntity owner, Map<String, Object> cache,
						 @Nullable DamageSource dSource, ElectricSource eSource)
	{
		if(!(dSource instanceof ElectricDamageSource))
			return;
		ElectricDamageSource dmg = (ElectricDamageSource)dSource;
		if(dmg.source.level < 1.75)
		{
			if(cache.containsKey("faraday"))
				cache.put("faraday", (1<<this.slot.getIndex())|((Integer)cache.get("faraday")));
			else
				cache.put("faraday", 1<<this.slot.getIndex());
			if(cache.containsKey("faraday")&&(Integer)cache.get("faraday")==(1<<4)-1)
				dmg.dmg = 0;
		}
		else
		{
			dmg.dmg *= 1.2;
			if((!(owner instanceof PlayerEntity)||!((PlayerEntity)owner).abilities.isCreativeMode)&&
					equipped.attemptDamageItem(2, Item.random, (dmg.getTrueSource() instanceof ServerPlayerEntity)?(ServerPlayerEntity)dmg.getTrueSource(): null))
				owner.setItemStackToSlot(eqSlot, ItemStack.EMPTY);
		}
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type)
	{
		return ImmersiveEngineering.MODID+":textures/models/armor_faraday"+(slot==EquipmentSlotType.LEGS?"_legs": "")+".png";
	}

	private static class FaradayArmorMaterial implements IArmorMaterial
	{

		@Override
		public int getDurability(@Nonnull EquipmentSlotType slotIn)
		{
			switch(slotIn)
			{
				case FEET:
					return 13;
				case LEGS:
					return 15;
				case CHEST:
					return 16;
				case HEAD:
					return 11;
			}
			return 0;
		}

		@Override
		public int getDamageReductionAmount(EquipmentSlotType slotIn)
		{
			switch(slotIn)
			{
				case FEET:
				case HEAD:
					return 1;
				case LEGS:
					return 2;
				case CHEST:
					return 3;
			}
			return 0;
		}

		@Override
		public int getEnchantability()
		{
			return 0;
		}

		@Nonnull
		@Override
		public SoundEvent getSoundEvent()
		{
			return SoundEvents.ITEM_ARMOR_EQUIP_CHAIN;
		}

		@Nonnull
		@Override
		public Ingredient getRepairMaterial()
		{
			return Ingredient.EMPTY;
		}

		@Nonnull
		@Override
		public String getName()
		{
			return ImmersiveEngineering.MODID+":faraday";
		}

		@Override
		public float getToughness()
		{
			return 0;
		}

		@Override
		public float getKnockbackResistance()
		{
			return 0;
		}
	}
}