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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public class SteelArmorItem extends ArmorItem
{
	public static ArmorMaterial mat = new SteelArmorMaterial();

	public SteelArmorItem(Type type)
	{
		super(mat, type, new Properties().stacksTo(1));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type)
	{
		return ImmersiveEngineering.MODID+":textures/models/armor_steel"+(slot==EquipmentSlot.LEGS?"_legs": "")+".png";
	}

	private static class SteelArmorMaterial implements ArmorMaterial
	{

		@Override
		public int getDurabilityForType(@Nonnull Type slotIn)
		{
			return switch(slotIn)
					{
						case BOOTS -> 273;
						case LEGGINGS -> 315;
						case CHESTPLATE -> 336;
						case HELMET -> 231;
						default -> 0;
					};
		}

		@Override
		public int getDefenseForType(Type slotIn)
		{
			return switch(slotIn)
					{
						case BOOTS, HELMET -> 2;
						case LEGGINGS -> 6;
						case CHESTPLATE -> 7;
						default -> 0;
					};
		}

		@Override
		public int getEnchantmentValue()
		{
			return 10;
		}

		@Nonnull
		@Override
		public SoundEvent getEquipSound()
		{
			return SoundEvents.ARMOR_EQUIP_IRON;
		}

		@Nonnull
		@Override
		public Ingredient getRepairIngredient()
		{
			return Ingredient.of(IETags.getTagsFor(EnumMetals.STEEL).ingot);
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