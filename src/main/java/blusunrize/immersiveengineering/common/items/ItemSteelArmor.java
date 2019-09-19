/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import java.util.Locale;

public class ItemSteelArmor extends ItemArmor
{
	public static ArmorMaterial mat;

	public ItemSteelArmor(EntityEquipmentSlot type)
	{
		super(mat, 0, type);
		String name = "steel_armor_"+type.getName().toLowerCase(Locale.ENGLISH);
		this.setTranslationKey(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.setMaxStackSize(1);
		IEContent.registeredIEItems.add(this);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
	{
		return "immersiveengineering:textures/models/armor_steel"+(slot==EntityEquipmentSlot.LEGS?"_legs": "")+".png";
	}

	@Override
	public boolean getIsRepairable(ItemStack itemToRepair, ItemStack stack)
	{
		return Utils.compareToOreName(stack, "ingotSteel")||Utils.compareToOreName(stack, "plateSteel");
	}
}