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
import net.minecraft.world.item.crafting.RecipeType;

public class IERecipes
{
	public static void registerRecipeTypes()
	{
		AlloyRecipe.TYPE = RecipeType.register(Lib.MODID+":alloy");
		ArcFurnaceRecipe.TYPE = RecipeType.register(Lib.MODID+":arc_furnace");
		BlastFurnaceFuel.TYPE = RecipeType.register(Lib.MODID+":blast_furnace_fuel");
		BlastFurnaceRecipe.TYPE = RecipeType.register(Lib.MODID+":blast_furnace");
		BlueprintCraftingRecipe.TYPE = RecipeType.register(Lib.MODID+":blueprint");
		BottlingMachineRecipe.TYPE = RecipeType.register(Lib.MODID+":bottling_machine");
		ClocheFertilizer.TYPE = RecipeType.register(Lib.MODID+":fertilizer");
		ClocheRecipe.TYPE = RecipeType.register(Lib.MODID+":cloche");
		CokeOvenRecipe.TYPE = RecipeType.register(Lib.MODID+":coke_oven");
		CrusherRecipe.TYPE = RecipeType.register(Lib.MODID+":crusher");
		FermenterRecipe.TYPE = RecipeType.register(Lib.MODID+":fermenter");
		MetalPressRecipe.TYPE = RecipeType.register(Lib.MODID+":metal_press");
		MixerRecipe.TYPE = RecipeType.register(Lib.MODID+":mixer");
		RefineryRecipe.TYPE = RecipeType.register(Lib.MODID+":refinery");
		SawmillRecipe.TYPE = RecipeType.register(Lib.MODID+":sawmill");
		SqueezerRecipe.TYPE = RecipeType.register(Lib.MODID+":squeezer");
		MineralMix.TYPE = RecipeType.register(Lib.MODID+":mineral_mix");
		GeneratedListRecipe.init();
	}
}
