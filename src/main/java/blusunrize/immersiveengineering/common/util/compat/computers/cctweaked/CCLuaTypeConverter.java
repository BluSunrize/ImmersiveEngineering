/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.LuaTypeConverter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.HashMap;
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
		Map<String, Object> result = new HashMap<>();
		result.put("name", getNameOrNull(stack.getItem(), BuiltInRegistries.ITEM));
		result.put("count", stack.getCount());
		result.put("damage", stack.getDamageValue());
		result.put("maxDamage", stack.getMaxDamage());
		return result;
	}

	public Object serialize(FluidStack stack)
	{
		Map<String, Object> result = new HashMap<>();
		result.put("name", getNameOrNull(stack.getFluid(), BuiltInRegistries.FLUID));
		result.put("amount", stack.getAmount());
		return result;
	}

	@Nullable
	private <T>
	String getNameOrNull(T entry, Registry<T> registry)
	{
		ResourceLocation name = registry.getKey(entry);
		if(name!=null)
			return name.toString();
		else
			return null;
	}
}
