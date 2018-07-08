/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Use {@link IElectricEquipment} instead
 */
@Deprecated
public interface ITeslaEquipment extends IElectricEquipment
{
	void onStrike(ItemStack s, EntityEquipmentSlot eqSlot, EntityLivingBase p, Map<String, Object> cache, DamageSource dmg);

	@Override
	default void onStrike(ItemStack s, EntityEquipmentSlot eqSlot, EntityLivingBase p, Map<String, Object> cache, @Nullable DamageSource dmg, ElectricSource desc)
	{
		if(dmg!=null)
			onStrike(s, eqSlot, p, cache, dmg);
	}
}
