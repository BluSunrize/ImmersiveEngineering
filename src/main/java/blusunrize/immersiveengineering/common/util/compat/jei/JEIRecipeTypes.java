/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.*;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class JEIRecipeTypes
{
	public static final RecipeType<RecipeHolder<AlloyRecipe>> ALLOY = create(IERecipeTypes.ALLOY);
	public static final RecipeType<RecipeHolder<ArcFurnaceRecipe>> ARC_FURNACE = create(IERecipeTypes.ARC_FURNACE);
	public static final RecipeType<RecipeHolder<ArcFurnaceRecipe>> ARC_FURNACE_RECYCLING = createManual(IEApi.ieLoc("arc_recycling"));
	public static final RecipeType<RecipeHolder<BlastFurnaceFuel>> BLAST_FUEL = create(IERecipeTypes.BLAST_FUEL);
	public static final RecipeType<RecipeHolder<BlastFurnaceRecipe>> BLAST_FURNACE = create(IERecipeTypes.BLAST_FURNACE);
	public static final RecipeType<RecipeHolder<BlueprintCraftingRecipe>> BLUEPRINT = create(IERecipeTypes.BLUEPRINT);
	public static final RecipeType<RecipeHolder<BottlingMachineRecipe>> BOTTLING_MACHINE = create(IERecipeTypes.BOTTLING_MACHINE);
	public static final RecipeType<RecipeHolder<BottlingMachineRecipe>> BOTTLING_MACHINE_BUCKETS = createManual(IEApi.ieLoc("bottling_buckets"));
	public static final RecipeType<RecipeHolder<BottlingMachineRecipe>> BOTTLING_MACHINE_POTIONS = createManual(IEApi.ieLoc("bottling_potions"));
	public static final RecipeType<RecipeHolder<ClocheRecipe>> CLOCHE = create(IERecipeTypes.CLOCHE);
	public static final RecipeType<RecipeHolder<ClocheFertilizer>> CLOCHE_FERTILIZER = create(IERecipeTypes.FERTILIZER);
	public static final RecipeType<RecipeHolder<CokeOvenRecipe>> COKE_OVEN = create(IERecipeTypes.COKE_OVEN);
	public static final RecipeType<RecipeHolder<CrusherRecipe>> CRUSHER = create(IERecipeTypes.CRUSHER);
	public static final RecipeType<RecipeHolder<FermenterRecipe>> FERMENTER = create(IERecipeTypes.FERMENTER);
	public static final RecipeType<RecipeHolder<MetalPressRecipe>> METAL_PRESS = create(IERecipeTypes.METAL_PRESS);
	public static final RecipeType<RecipeHolder<MixerRecipe>> MIXER = create(IERecipeTypes.MIXER);
	public static final RecipeType<RecipeHolder<MixerRecipe>> MIXER_POTIONS = createManual(IEApi.ieLoc("mixer_potions"));
	public static final RecipeType<RecipeHolder<RefineryRecipe>> REFINERY = create(IERecipeTypes.REFINERY);
	public static final RecipeType<RecipeHolder<SawmillRecipe>> SAWMILL = create(IERecipeTypes.SAWMILL);
	public static final RecipeType<RecipeHolder<SqueezerRecipe>> SQUEEZER = create(IERecipeTypes.SQUEEZER);

	private static <T extends Recipe<?>>
	RecipeType<RecipeHolder<T>> create(IERecipeTypes.TypeWithClass<T> type)
	{
		return RecipeType.createFromVanilla(type.get());
	}

	private static <T extends Recipe<?>>
	RecipeType<RecipeHolder<T>> createManual(ResourceLocation uid)
	{
		Class<? extends RecipeHolder<T>> holderClass = (Class)RecipeHolder.class;
		return new RecipeType<>(uid, holderClass);
	}
}
