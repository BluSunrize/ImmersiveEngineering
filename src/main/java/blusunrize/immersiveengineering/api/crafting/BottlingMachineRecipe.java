/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ListUtils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author BluSunrize - 14.01.2016
 * <br>
 * The recipe for the bottling machine
 */
public class BottlingMachineRecipe extends MultiblockRecipe
{
	public static IRecipeType<BottlingMachineRecipe> TYPE = IRecipeType.register(Lib.MODID+":bottling_machine");
	public static RegistryObject<IERecipeSerializer<BottlingMachineRecipe>> SERIALIZER;

	public final Ingredient input;
	public final FluidStack fluidInput;
	public final ItemStack output;

	public BottlingMachineRecipe(ResourceLocation id, ItemStack output, Ingredient input, FluidStack fluidInput)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input = input;
		this.fluidInput = fluidInput;

		setInputList(Lists.newArrayList(this.input));
		this.fluidInputList = Lists.newArrayList(this.fluidInput);
		this.outputList = ListUtils.fromItem(this.output);
	}

	@Override
	protected IERecipeSerializer<BottlingMachineRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	public static List<BottlingMachineRecipe> recipeList = new ArrayList<>();

	public static BottlingMachineRecipe findRecipe(ItemStack input, FluidStack fluid)
	{
		if(!input.isEmpty()&&fluid!=null)
			for(BottlingMachineRecipe recipe : recipeList)
				if(ApiUtils.stackMatchesObject(input, recipe.input)&&fluid.containsFluid(recipe.fluidInput))
					return recipe;
		return null;
	}

	public static List<BottlingMachineRecipe> removeRecipes(ItemStack stack)
	{
		List<BottlingMachineRecipe> list = new ArrayList<>();
		Iterator<BottlingMachineRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			BottlingMachineRecipe ir = it.next();
			if(ItemStack.areItemsEqual(ir.output, stack))
			{
				list.add(ir);
				it.remove();
			}
		}
		return list;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}