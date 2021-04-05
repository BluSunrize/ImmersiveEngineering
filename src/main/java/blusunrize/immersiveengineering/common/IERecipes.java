/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import net.minecraft.item.crafting.IRecipeType;

public class IERecipes
{
	public static void registerRecipeTypes()
	{
		AlloyRecipe.TYPE = IRecipeType.register(Lib.MODID+":alloy");
		ArcFurnaceRecipe.TYPE = IRecipeType.register(Lib.MODID+":arc_furnace");
		BlastFurnaceFuel.TYPE = IRecipeType.register(Lib.MODID+":blast_furnace_fuel");
		BlastFurnaceRecipe.TYPE = IRecipeType.register(Lib.MODID+":blast_furnace");
		BlueprintCraftingRecipe.TYPE = IRecipeType.register(Lib.MODID+":blueprint");
		BottlingMachineRecipe.TYPE = IRecipeType.register(Lib.MODID+":bottling_machine");
		ClocheFertilizer.TYPE = IRecipeType.register(Lib.MODID+":fertilizer");
		ClocheRecipe.TYPE = IRecipeType.register(Lib.MODID+":cloche");
		CokeOvenRecipe.TYPE = IRecipeType.register(Lib.MODID+":coke_oven");
		CrusherRecipe.TYPE = IRecipeType.register(Lib.MODID+":crusher");
		FermenterRecipe.TYPE = IRecipeType.register(Lib.MODID+":fermenter");
		MetalPressRecipe.TYPE = IRecipeType.register(Lib.MODID+":metal_press");
		MixerRecipe.TYPE = IRecipeType.register(Lib.MODID+":mixer");
		RefineryRecipe.TYPE = IRecipeType.register(Lib.MODID+":refinery");
		SawmillRecipe.TYPE = IRecipeType.register(Lib.MODID+":sawmill");
		SqueezerRecipe.TYPE = IRecipeType.register(Lib.MODID+":squeezer");
		MineralMix.TYPE = IRecipeType.register(Lib.MODID+":mineral_mix");
		GeneratedListRecipe.init();
	}
}
