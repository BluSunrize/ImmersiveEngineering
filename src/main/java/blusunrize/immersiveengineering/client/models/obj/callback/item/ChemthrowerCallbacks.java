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
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ChemthrowerCallbacks implements ItemCallback<ChemthrowerCallbacks.Key>
{
	public static final ChemthrowerCallbacks INSTANCE = new ChemthrowerCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		CompoundTag upgrades = ChemthrowerItem.getUpgradesStatic(stack);
		return new Key(upgrades.getInt("capacity") > 0, upgrades.getBoolean("multitank"));
	}

	@Override
	public boolean shouldRenderGroup(Key stack, String group)
	{
		if("base".equals(group)||"grip".equals(group)||"cage".equals(group)||"tanks".equals(group))
			return true;
		if("large_tank".equals(group)&&stack.upgradedCapacity())
			return true;
		else if("multi_tank".equals(group)&&stack.multitank())
			return true;
		else
			return "tank".equals(group);
	}

	public record Key(boolean upgradedCapacity, boolean multitank)
	{
	}
}
