/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class IERecipeTypes
{
	private static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(
			Registry.RECIPE_TYPE_REGISTRY, Lib.MODID
	);
	public static final RegistryObject<RecipeType<AlloyRecipe>> ALLOY = register("alloy");
	public static final RegistryObject<RecipeType<ArcFurnaceRecipe>> ARC_FURNACE = register("arc_furnace");
	public static final RegistryObject<RecipeType<BlastFurnaceFuel>> BLAST_FUEL = register("blast_furnace_fuel");
	public static final RegistryObject<RecipeType<BlastFurnaceRecipe>> BLAST_FURNACE = register("blast_furnace");
	public static final RegistryObject<RecipeType<BlueprintCraftingRecipe>> BLUEPRINT = register("blueprint");
	public static final RegistryObject<RecipeType<BottlingMachineRecipe>> BOTTLING_MACHINE = register("bottling_machine");
	public static final RegistryObject<RecipeType<ClocheFertilizer>> FERTILIZER = register("fertilizer");
	public static final RegistryObject<RecipeType<ClocheRecipe>> CLOCHE = register("cloche");
	public static final RegistryObject<RecipeType<CokeOvenRecipe>> COKE_OVEN = register("coke_oven");
	public static final RegistryObject<RecipeType<CrusherRecipe>> CRUSHER = register("crusher");
	public static final RegistryObject<RecipeType<FermenterRecipe>> FERMENTER = register("fermenter");
	public static final RegistryObject<RecipeType<MetalPressRecipe>> METAL_PRESS = register("metal_press");
	public static final RegistryObject<RecipeType<MixerRecipe>> MIXER = register("mixer");
	public static final RegistryObject<RecipeType<RefineryRecipe>> REFINERY = register("refinery");
	public static final RegistryObject<RecipeType<SawmillRecipe>> SAWMILL = register("sawmill");
	public static final RegistryObject<RecipeType<SqueezerRecipe>> SQUEEZER = register("squeezer");
	public static final RegistryObject<RecipeType<MineralMix>> MINERAL_MIX = register("mineral_mix");
	public static final RegistryObject<RecipeType<GeneratorFuel>> GENERATOR_FUEL = register("generator_fuel");
	public static final RegistryObject<RecipeType<ThermoelectricSource>> THERMOELECTRIC_SOURCE = register("thermoelectric_source");

	private static <T extends Recipe<?>>
	RegistryObject<RecipeType<T>> register(String name)
	{
		return REGISTER.register(name, () -> new RecipeType<>()
		{
		});
	}

	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
