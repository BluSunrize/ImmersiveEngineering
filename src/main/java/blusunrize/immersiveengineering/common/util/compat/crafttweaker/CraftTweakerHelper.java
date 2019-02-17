/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.oredict.IOreDictEntry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CraftTweakerHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
		CraftTweakerAPI.registerClass(AlloySmelter.class);
		CraftTweakerAPI.registerClass(BlastFurnace.class);
		CraftTweakerAPI.registerClass(CokeOven.class);
		CraftTweakerAPI.registerClass(Blueprint.class);
		CraftTweakerAPI.registerClass(Crusher.class);
		CraftTweakerAPI.registerClass(Squeezer.class);
		CraftTweakerAPI.registerClass(Fermenter.class);
		CraftTweakerAPI.registerClass(Refinery.class);
		CraftTweakerAPI.registerClass(ArcFurnace.class);
		CraftTweakerAPI.registerClass(Excavator.class);
		CraftTweakerAPI.registerClass(Excavator.MTMineralMix.class);
		CraftTweakerAPI.registerClass(BottlingMachine.class);
		CraftTweakerAPI.registerClass(MetalPress.class);
		CraftTweakerAPI.registerClass(Mixer.class);
		CraftTweakerAPI.registerClass(DieselHelper.class);
		CraftTweakerAPI.registerClass(Thermoelectric.class);
	}

	@Override
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
	}

	/**
	 * Helper Methods
	 */
	public static ItemStack toStack(IItemStack iStack)
	{
		if(iStack==null)
			return ItemStack.EMPTY;
		return (ItemStack)iStack.getInternal();
	}

	public static Object toObject(IIngredient iStack)
	{
		if(iStack==null)
			return null;
		else
		{
			if(iStack instanceof IOreDictEntry)
				return ((IOreDictEntry)iStack).getName();
			else if(iStack instanceof IItemStack)
				return toStack((IItemStack)iStack);
			else if(iStack instanceof IngredientStack)
			{
				IIngredient ingr = ReflectionHelper.getPrivateValue(IngredientStack.class, (IngredientStack)iStack, "ingredient");
				Object o = toObject(ingr);
				if(o instanceof String)
					return new blusunrize.immersiveengineering.api.crafting.IngredientStack((String)o, iStack.getAmount());
				else
					return o;
			}
			else
				return null;
		}
	}

	public static blusunrize.immersiveengineering.api.crafting.IngredientStack toIEIngredientStack(IIngredient iStack)
	{
		if(iStack==null)
			return null;
		else
		{
			if(iStack instanceof IOreDictEntry)
				return new blusunrize.immersiveengineering.api.crafting.IngredientStack(((IOreDictEntry)iStack).getName());
			else if(iStack instanceof IItemStack)
				return new blusunrize.immersiveengineering.api.crafting.IngredientStack(toStack((IItemStack)iStack));
			else if(iStack instanceof IngredientStack)
			{
				IIngredient ingr = ReflectionHelper.getPrivateValue(IngredientStack.class, (IngredientStack)iStack, "ingredient");
				blusunrize.immersiveengineering.api.crafting.IngredientStack ingrStack = toIEIngredientStack(ingr);
				ingrStack.inputSize = iStack.getAmount();
				return ingrStack;
			}
			else
				return null;
		}
	}

	public static Object[] toObjects(IIngredient[] iStacks)
	{
		Object[] oA = new Object[iStacks.length];
		for(int i = 0; i < iStacks.length; i++)
			oA[i] = toObject(iStacks[i]);
		return oA;
	}

	public static FluidStack toFluidStack(ILiquidStack iStack)
	{
		if(iStack==null)
		{
			return null;
		}
		return (FluidStack)iStack.getInternal();
	}
}
