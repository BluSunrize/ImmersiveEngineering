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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

public class ItemFaradaySuit extends ItemArmor implements IElectricEquipment
{
	public static ArmorMaterial mat;

	public ItemFaradaySuit(EntityEquipmentSlot type)
	{
		super(mat, 0, type);
		String name = "faraday_suit_"+type.getName().toLowerCase(Locale.ENGLISH);
		this.setTranslationKey(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.setMaxStackSize(1);
//		ImmersiveEngineering.registerItem(this, name);
		IEContent.registeredIEItems.add(this);
	}

	@Override
	public void onStrike(ItemStack s, EntityEquipmentSlot eqSlot, EntityLivingBase p, Map<String, Object> cache,
						 @Nullable DamageSource dSource, ElectricSource eSource)
	{
		if(!(dSource instanceof ElectricDamageSource))
			return;
		ElectricDamageSource dmg = (ElectricDamageSource)dSource;
		if(dmg.source.level < 1.75)
		{
			if(cache.containsKey("faraday"))
				cache.put("faraday", (1<<armorType.getIndex())|((Integer)cache.get("faraday")));
			else
				cache.put("faraday", 1<<armorType.getIndex());
			if(cache.containsKey("faraday")&&(Integer)cache.get("faraday")==(1<<4)-1)
				dmg.dmg = 0;
		}
		else
		{
			dmg.dmg *= 1.2;
			if((!(p instanceof EntityPlayer)||!((EntityPlayer)p).capabilities.isCreativeMode)&&s.attemptDamageItem(2, itemRand, (dmg.getTrueSource() instanceof EntityPlayerMP)?(EntityPlayerMP)dmg.getTrueSource(): null))
				p.setItemStackToSlot(eqSlot, ItemStack.EMPTY);
		}
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
	{
		return "immersiveengineering:textures/models/armor_faraday"+(slot==EntityEquipmentSlot.LEGS?"_legs": "")+".png";
	}
}