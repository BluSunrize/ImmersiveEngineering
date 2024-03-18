/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.*;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Recipe;

public class JEIRecipeTypes
{
	public static final RecipeType<AlloyRecipe> ALLOY = create(IERecipeTypes.ALLOY);
	public static final RecipeType<ArcFurnaceRecipe> ARC_FURNACE = create(IERecipeTypes.ARC_FURNACE);
	public static final RecipeType<ArcFurnaceRecipe> ARC_FURNACE_RECYCLING = new RecipeType<>(
			ImmersiveEngineering.rl("arc_recycling"), ArcFurnaceRecipe.class
	);
	public static final RecipeType<BlastFurnaceFuel> BLAST_FUEL = create(IERecipeTypes.BLAST_FUEL);
	public static final RecipeType<BlastFurnaceRecipe> BLAST_FURNACE = create(IERecipeTypes.BLAST_FURNACE);
	public static final RecipeType<BlueprintCraftingRecipe> BLUEPRINT = create(IERecipeTypes.BLUEPRINT);
	public static final RecipeType<BottlingMachineRecipe> BOTTLING_MACHINE = create(IERecipeTypes.BOTTLING_MACHINE);
	public static final RecipeType<ClocheRecipe> CLOCHE = create(IERecipeTypes.CLOCHE);
	public static final RecipeType<ClocheFertilizer> CLOCHE_FERTILIZER = create(IERecipeTypes.FERTILIZER);
	public static final RecipeType<CokeOvenRecipe> COKE_OVEN = create(IERecipeTypes.COKE_OVEN);
	public static final RecipeType<CrusherRecipe> CRUSHER = create(IERecipeTypes.CRUSHER);
	public static final RecipeType<FermenterRecipe> FERMENTER = create(IERecipeTypes.FERMENTER);
	public static final RecipeType<MetalPressRecipe> METAL_PRESS = create(IERecipeTypes.METAL_PRESS);
	public static final RecipeType<MixerRecipe> MIXER = create(IERecipeTypes.MIXER);
	public static final RecipeType<RefineryRecipe> REFINERY = create(IERecipeTypes.REFINERY);
	public static final RecipeType<SawmillRecipe> SAWMILL = create(IERecipeTypes.SAWMILL);
	public static final RecipeType<SqueezerRecipe> SQUEEZER = create(IERecipeTypes.SQUEEZER);

	private static <T extends Recipe<?>>
	RecipeType<T> create(IERecipeTypes.TypeWithClass<T> type)
	{
		return new RecipeType<>(type.type().getId(), type.recipeClass());
	}
}
