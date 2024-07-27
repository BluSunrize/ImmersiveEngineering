/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.LuaTypeConverter;
import dan200.computercraft.api.detail.ForgeDetailRegistries;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Map;

public class CCLuaTypeConverter extends LuaTypeConverter
{
	public static final CCLuaTypeConverter INSTANCE = new CCLuaTypeConverter();

	@Override
	@Nullable
	protected <T> Converter<T, ?> getInternalConverter(Class<T> type)
	{
		if(type==ItemStack.class)
			return new Converter<>(t -> serialize((ItemStack)t), Map.class);
		else if(type==FluidStack.class)
			return new Converter<>(t -> serialize((FluidStack)t), Map.class);
		else
			return null;
	}

	public Object serialize(ItemStack stack)
	{
		return VanillaDetailRegistries.ITEM_STACK.getDetails(stack);
	}

	public Object serialize(FluidStack stack)
	{
		return ForgeDetailRegistries.FLUID_STACK.getDetails(stack);
	}
}
