package blusunrize.lib.manual.utils;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;

public class PrivateAccess
{
	private static final Lazy<MethodHandles.Lookup> LOOKUP = Lazy.of(MethodHandles::lookup);
	private static final Lazy<MethodHandle> GET_RECIPES_OF_TYPE = Lazy.of(() -> {
		try
		{
			Method reflectionMethod = ObfuscationReflectionHelper.findMethod(
					RecipeManager.class, "func_215366_a", IRecipeType.class
			);
			return LOOKUP.get().unreflect(reflectionMethod);
		} catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	});

	@SuppressWarnings("unchecked")
	public static <C extends IInventory, T extends IRecipe<C>>
	Map<ResourceLocation, IRecipe<C>> getRecipes(RecipeManager manager, IRecipeType<T> recipeTypeIn)
	{
		try
		{
			return (Map<ResourceLocation, IRecipe<C>>)GET_RECIPES_OF_TYPE.get().invokeExact(manager, recipeTypeIn);
		} catch(Throwable throwable)
		{
			throw new RuntimeException(throwable);
		}
	}
}
