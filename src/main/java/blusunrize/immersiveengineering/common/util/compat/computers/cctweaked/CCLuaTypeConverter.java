/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.LuaTypeConverter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CCLuaTypeConverter extends LuaTypeConverter
{
	public static final CCLuaTypeConverter INSTANCE = new CCLuaTypeConverter();

	@Override
	@Nullable
	protected Function<Object, Object> getInternalConverter(Class<?> type)
	{
		if(type==ItemStack.class)
			return t -> serialize((ItemStack)t);
		else if(type==FluidStack.class)
			return t -> serialize((FluidStack)t);
		else
			return null;
	}

	public Object serialize(ItemStack stack)
	{
		Map<String, Object> result = new HashMap<>();
		result.put("name", getNameOrNull(stack.getItem()));
		result.put("count", stack.getCount());
		result.put("damage", stack.getDamage());
		result.put("maxDamage", stack.getMaxDamage());
		return result;
	}

	public Object serialize(FluidStack stack)
	{
		Map<String, Object> result = new HashMap<>();
		result.put("name", getNameOrNull(stack.getFluid()));
		result.put("amount", stack.getAmount());
		return result;
	}

	@Nullable
	private String getNameOrNull(IForgeRegistryEntry<?> entry)
	{
		ResourceLocation name = entry.getRegistryName();
		if(name!=null)
			return name.toString();
		else
			return null;
	}
}
