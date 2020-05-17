/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import java.util.Map;

public class BlastFurnaceFuel extends IESerializableRecipe
{
	public static IRecipeType<BlastFurnaceFuel> TYPE = IRecipeType.register(Lib.MODID+":blast_furnace_fuel");
	public static RegistryObject<IERecipeSerializer<BlastFurnaceFuel>> SERIALIZER;

	// Initialized by reload listener
	public static Map<ResourceLocation, BlastFurnaceFuel> blastFuels;

	public final Ingredient input;
	public final int burnTime;

	public BlastFurnaceFuel(ResourceLocation id, Ingredient input, int burnTime)
	{
		super(ItemStack.EMPTY, TYPE, id);
		this.input = input;
		this.burnTime = burnTime;
	}

	public static int getBlastFuelTime(ItemStack stack)
	{
		for(BlastFurnaceFuel e : blastFuels.values())
			if(e.input.test(stack))
				return e.burnTime;
		return 0;
	}

	public static boolean isValidBlastFuel(ItemStack stack)
	{
		return getBlastFuelTime(stack) > 0;
	}

	@Override
	protected IERecipeSerializer<BlastFurnaceFuel> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return ItemStack.EMPTY;
	}
}
