/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.client.models.obj.callback.ItemCallback;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RailgunCallbacks implements ItemCallback<RailgunCallbacks.Key>
{
	public static RailgunCallbacks INSTANCE = new RailgunCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		CompoundTag upgrades = RailgunItem.getUpgradesStatic(stack);
		return new Key(upgrades.getBoolean("scope"), upgrades.getDouble("speed") > 0);
	}

	@Override
	public boolean shouldRenderGroup(Key stack, String group)
	{
		if(group.equals("upgrade_scope"))
			return stack.scope();
		if(group.equals("upgrade_speed"))
			return stack.speed();
		if(group.equals("barrel_top"))
			return !stack.speed();
		return true;
	}

	public record Key(boolean scope, boolean speed)
	{
	}
}
