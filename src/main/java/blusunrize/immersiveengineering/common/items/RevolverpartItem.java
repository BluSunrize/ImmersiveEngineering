/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.items.RevolverItem.Perks;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;

public class RevolverpartItem extends IEBaseItem
{
	public RevolverpartItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Nonnull
	@Override
	public Component getName(ItemStack stack)
	{
		Component name = super.getName(stack);
		var perks = stack.get(IEDataComponents.REVOLVER_PERKS);
		if(perks!=null)
			return RevolverItem.RevolverPerk.getFormattedName(name, perks);
		return name;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		var perks = stack.getOrDefault(IEDataComponents.REVOLVER_PERKS, Perks.EMPTY);
		for(var entry : perks.perks().entrySet())
			list.add(Component.literal("  ").append(entry.getKey().getDisplayString(entry.getValue())));
	}
}