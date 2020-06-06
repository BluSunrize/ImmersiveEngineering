/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class RecipeReloadListener implements IResourceManagerReloadListener
{
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{
		if(EffectiveSide.get().isServer())
		{
			buildRecipeLists(ServerLifecycleHooks.getCurrentServer().getRecipeManager());
		}
	}

	@SubscribeEvent
	public void onRecipesUpdated(RecipesUpdatedEvent event)
	{
		buildRecipeLists(event.getRecipeManager());
	}

	static void buildRecipeLists(RecipeManager recipeManager)
	{
		Collection<IRecipe<?>> recipes = recipeManager.getRecipes();

		// Start recycling
		ArcRecyclingThreadHandler recyclingHandler = new ArcRecyclingThreadHandler(recipes);
		recyclingHandler.start();

		AlloyRecipe.recipeList = filterRecipes(recipes, AlloyRecipe.class, AlloyRecipe.TYPE);
		BlastFurnaceRecipe.recipeList = filterRecipes(recipes, BlastFurnaceRecipe.class, BlastFurnaceRecipe.TYPE);
		BlastFurnaceFuel.blastFuels = filterRecipes(recipes, BlastFurnaceFuel.class, BlastFurnaceFuel.TYPE);
		CokeOvenRecipe.recipeList = filterRecipes(recipes, CokeOvenRecipe.class, CokeOvenRecipe.TYPE);
		ClocheRecipe.recipeList = filterRecipes(recipes, ClocheRecipe.class, ClocheRecipe.TYPE);

		BlueprintCraftingRecipe.recipeList = filterRecipes(recipes, BlueprintCraftingRecipe.class, BlueprintCraftingRecipe.TYPE);
		BlueprintCraftingRecipe.updateRecipeCategories();

		MetalPressRecipe.recipeList = filterRecipes(recipes, MetalPressRecipe.class, MetalPressRecipe.TYPE);
		MetalPressRecipe.updateRecipesByMold();

		ArcFurnaceRecipe.recipeList = filterRecipes(recipes, ArcFurnaceRecipe.class, ArcFurnaceRecipe.TYPE);
		BottlingMachineRecipe.recipeList = filterRecipes(recipes, BottlingMachineRecipe.class, BottlingMachineRecipe.TYPE);
		CrusherRecipe.recipeList = filterRecipes(recipes, CrusherRecipe.class, CrusherRecipe.TYPE);
		FermenterRecipe.recipeList = filterRecipes(recipes, FermenterRecipe.class, FermenterRecipe.TYPE);
		SqueezerRecipe.recipeList = filterRecipes(recipes, SqueezerRecipe.class, SqueezerRecipe.TYPE);
		RefineryRecipe.recipeList = filterRecipes(recipes, RefineryRecipe.class, RefineryRecipe.TYPE);
		MixerRecipe.recipeList = filterRecipes(recipes, MixerRecipe.class, MixerRecipe.TYPE);
		ExcavatorHandler.mineralList = filterRecipes(recipes, MineralMix.class, MineralMix.TYPE);

		MixerRecipePotion.initPotionRecipes();

		// Wrap up recycling
		try
		{
			recyclingHandler.join();
			recyclingHandler.finishUp();
		} catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	static <R extends IRecipe<?>> Map<ResourceLocation, R> filterRecipes(Collection<IRecipe<?>> recipes, Class<R> recipeClass, IRecipeType<R> recipeType)
	{
		return recipes.stream()
				.filter(iRecipe -> iRecipe.getType()==recipeType)
				.map(recipeClass::cast)
				.collect(Collectors.toMap(recipe -> recipe.getId(), recipe -> recipe));
	}
}
