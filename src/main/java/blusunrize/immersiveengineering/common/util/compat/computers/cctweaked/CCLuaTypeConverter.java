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

public class CCLuaTypeConverter implements LuaTypeConverter
{
	public static final CCLuaTypeConverter INSTANCE = new CCLuaTypeConverter();

	@Override
	public Function<Object, Object> getSerializer(Class<?> type)
	{
		if(type==ItemStack.class)
			return t -> serialize((ItemStack)t);
		else if(type==FluidStack.class)
			return t -> serialize((FluidStack)t);
		else
			return Function.identity();
	}

	public Object serialize(ItemStack stack)
	{
		Map<String, Object> result = new HashMap<>();
		result.put("name", getNameOrNull(stack.getItem()));
		result.put("count", stack.getCount());
		result.put("damage", stack.getDamage());
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
