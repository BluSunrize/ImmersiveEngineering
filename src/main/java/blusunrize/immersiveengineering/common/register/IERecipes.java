/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class IERecipes
{
	@SubscribeEvent
	// Just need *some* registry event, since all registries are apparently unfrozen during those
	public static void register(RegistryEvent.Register<Block> ev)
	{
		AlloyRecipe.TYPE = register("alloy");
		ArcFurnaceRecipe.TYPE = register("arc_furnace");
		BlastFurnaceFuel.TYPE = register("blast_furnace_fuel");
		BlastFurnaceRecipe.TYPE = register("blast_furnace");
		BlueprintCraftingRecipe.TYPE = register("blueprint");
		BottlingMachineRecipe.TYPE = register("bottling_machine");
		ClocheFertilizer.TYPE = register("fertilizer");
		ClocheRecipe.TYPE = register("cloche");
		CokeOvenRecipe.TYPE = register("coke_oven");
		CrusherRecipe.TYPE = register("crusher");
		FermenterRecipe.TYPE = register("fermenter");
		MetalPressRecipe.TYPE = register("metal_press");
		MixerRecipe.TYPE = register("mixer");
		RefineryRecipe.TYPE = register("refinery");
		SawmillRecipe.TYPE = register("sawmill");
		SqueezerRecipe.TYPE = register("squeezer");
		MineralMix.TYPE = register("mineral_mix");
		GeneratorFuel.TYPE = register("generator_fuel");
		ThermoelectricSource.TYPE = register("thermoelectric_source");
		GeneratedListRecipe.init();
	}

	private static <T extends Recipe<?>> RecipeType<T> register(String path)
	{
		ResourceLocation name = ImmersiveEngineering.rl(path);
		return Registry.register(Registry.RECIPE_TYPE, name, new RecipeType<T>()
		{
			@Override
			public String toString()
			{
				return name.toString();
			}
		});
	}
}
