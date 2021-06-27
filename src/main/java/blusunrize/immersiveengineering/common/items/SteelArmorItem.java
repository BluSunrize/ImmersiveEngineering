/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

import javax.annotation.Nonnull;

public class SteelArmorItem extends ArmorItem
{
	public static IArmorMaterial mat = new SteelArmorMaterial();

	public SteelArmorItem(EquipmentSlotType type)
	{
		super(mat, type, new Properties().maxStackSize(1).group(ImmersiveEngineering.ITEM_GROUP));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type)
	{
		return ImmersiveEngineering.MODID+":textures/models/armor_steel"+(slot==EquipmentSlotType.LEGS?"_legs": "")+".png";
	}

	private static class SteelArmorMaterial implements IArmorMaterial
	{

		@Override
		public int getDurability(@Nonnull EquipmentSlotType slotIn)
		{
			switch(slotIn)
			{
				case FEET:
					return 273;
				case LEGS:
					return 315;
				case CHEST:
					return 336;
				case HEAD:
					return 231;
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
					return 2;
				case LEGS:
					return 6;
				case CHEST:
					return 7;
			}
			return 0;
		}

		@Override
		public int getEnchantability()
		{
			return 10;
		}

		@Nonnull
		@Override
		public SoundEvent getSoundEvent()
		{
			return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
		}

		@Nonnull
		@Override
		public Ingredient getRepairMaterial()
		{
			return Ingredient.fromTag(IETags.getTagsFor(EnumMetals.STEEL).ingot);
		}

		@Nonnull
		@Override
		public String getName()
		{
			return ImmersiveEngineering.MODID+":steel";
		}

		@Override
		public float getToughness()
		{
			return 1.0f;
		}

		@Override
		public float getKnockbackResistance()
		{
			return 0;
		}
	}
}