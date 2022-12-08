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
import blusunrize.immersiveengineering.api.energy.WindmillBiome;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class IERecipeTypes
{
	private static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(
			Registries.RECIPE_TYPE, Lib.MODID
	);
	public static final TypeWithClass<AlloyRecipe> ALLOY = register("alloy", AlloyRecipe.class);
	public static final TypeWithClass<ArcFurnaceRecipe> ARC_FURNACE = register("arc_furnace", ArcFurnaceRecipe.class);
	public static final TypeWithClass<BlastFurnaceFuel> BLAST_FUEL = register("blast_furnace_fuel", BlastFurnaceFuel.class);
	public static final TypeWithClass<BlastFurnaceRecipe> BLAST_FURNACE = register("blast_furnace", BlastFurnaceRecipe.class);
	public static final TypeWithClass<BlueprintCraftingRecipe> BLUEPRINT = register("blueprint", BlueprintCraftingRecipe.class);
	public static final TypeWithClass<BottlingMachineRecipe> BOTTLING_MACHINE = register("bottling_machine", BottlingMachineRecipe.class);
	public static final TypeWithClass<ClocheFertilizer> FERTILIZER = register("fertilizer", ClocheFertilizer.class);
	public static final TypeWithClass<ClocheRecipe> CLOCHE = register("cloche", ClocheRecipe.class);
	public static final TypeWithClass<CokeOvenRecipe> COKE_OVEN = register("coke_oven", CokeOvenRecipe.class);
	public static final TypeWithClass<CrusherRecipe> CRUSHER = register("crusher", CrusherRecipe.class);
	public static final TypeWithClass<FermenterRecipe> FERMENTER = register("fermenter", FermenterRecipe.class);
	public static final TypeWithClass<MetalPressRecipe> METAL_PRESS = register("metal_press", MetalPressRecipe.class);
	public static final TypeWithClass<MixerRecipe> MIXER = register("mixer", MixerRecipe.class);
	public static final TypeWithClass<RefineryRecipe> REFINERY = register("refinery", RefineryRecipe.class);
	public static final TypeWithClass<SawmillRecipe> SAWMILL = register("sawmill", SawmillRecipe.class);
	public static final TypeWithClass<SqueezerRecipe> SQUEEZER = register("squeezer", SqueezerRecipe.class);
	public static final TypeWithClass<MineralMix> MINERAL_MIX = register("mineral_mix", MineralMix.class);
	public static final TypeWithClass<GeneratorFuel> GENERATOR_FUEL = register("generator_fuel", GeneratorFuel.class);
	public static final TypeWithClass<ThermoelectricSource> THERMOELECTRIC_SOURCE = register("thermoelectric_source", ThermoelectricSource.class);
	public static final TypeWithClass<WindmillBiome> WINDMILL_BIOME = register("windmill_biome", WindmillBiome.class);

	private static <T extends Recipe<?>>
	TypeWithClass<T> register(String name, Class<T> type)
	{
		RegistryObject<RecipeType<T>> regObj = REGISTER.register(name, () -> new RecipeType<>()
		{
		});
		return new TypeWithClass<>(regObj, type);
	}

	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public record TypeWithClass<T extends Recipe<?>>(
			RegistryObject<RecipeType<T>> type, Class<T> recipeClass
	) implements Supplier<RecipeType<T>>
	{
		public RecipeType<T> get()
		{
			return type.get();
		}
	}
}
