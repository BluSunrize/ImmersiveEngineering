/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.blocks.multiblocks.StaticTemplateManager;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeReloadListener implements IResourceManagerReloadListener
{
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{
		if(EffectiveSide.get().isServer())
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			RecipeManager recipeManager = server.getRecipeManager();
			startArcRecyclingRecipeGen(recipeManager);
			buildRecipeLists(recipeManager);
			Iterator<ServerWorld> it = server.getWorlds().iterator();
			// Should only be false when no players are loaded, so the data will be synced on login
			if(it.hasNext())
				ApiUtils.addFutureServerTask(it.next(),
						() -> StaticTemplateManager.syncMultiblockTemplates(PacketDistributor.ALL.noArg(), true)
				);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
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
		ClocheFertilizer.fertilizerList = filterRecipes(recipes, ClocheFertilizer.class, ClocheFertilizer.TYPE);

		BlueprintCraftingRecipe.recipeList = filterRecipes(recipes, BlueprintCraftingRecipe.class, BlueprintCraftingRecipe.TYPE);
		BlueprintCraftingRecipe.updateRecipeCategories();

		MetalPressRecipe.recipeList = filterRecipes(recipes, MetalPressRecipe.class, MetalPressRecipe.TYPE);
		MetalPressRecipe unpackingRecipe = MetalPressPackingRecipes.getUnpackingContainer();
		MetalPressRecipe packingRecipe2x2 = MetalPressPackingRecipes.get2x2PackingContainer();
		MetalPressRecipe packingRecipe3x3 = MetalPressPackingRecipes.get3x3PackingContainer();
		MetalPressRecipe.recipeList.put(unpackingRecipe.getId(), unpackingRecipe);
		MetalPressRecipe.recipeList.put(packingRecipe2x2.getId(), packingRecipe2x2);
		MetalPressRecipe.recipeList.put(packingRecipe3x3.getId(), packingRecipe3x3);
		MetalPressPackingRecipes.CRAFTING_RECIPE_MAP = filterRecipes(recipes, ICraftingRecipe.class, IRecipeType.CRAFTING);
		MetalPressRecipe.updateRecipesByMold();

		BottlingMachineRecipe.recipeList = filterRecipes(recipes, BottlingMachineRecipe.class, BottlingMachineRecipe.TYPE);
		CrusherRecipe.recipeList = filterRecipes(recipes, CrusherRecipe.class, CrusherRecipe.TYPE);
		FermenterRecipe.recipeList = filterRecipes(recipes, FermenterRecipe.class, FermenterRecipe.TYPE);
		SqueezerRecipe.recipeList = filterRecipes(recipes, SqueezerRecipe.class, SqueezerRecipe.TYPE);
		RefineryRecipe.recipeList = filterRecipes(recipes, RefineryRecipe.class, RefineryRecipe.TYPE);
		MixerRecipe.recipeList = filterRecipes(recipes, MixerRecipe.class, MixerRecipe.TYPE);
		MineralMix.mineralList = filterRecipes(recipes, MineralMix.class, MineralMix.TYPE);
		ArcFurnaceRecipe.recipeList = filterRecipes(recipes, ArcFurnaceRecipe.class, ArcFurnaceRecipe.TYPE);
	}

	private void startArcRecyclingRecipeGen(RecipeManager recipeManager)
	{
		Collection<IRecipe<?>> recipes = recipeManager.getRecipes();
		new ArcRecyclingThreadHandler(recipes).start();
	}

	static <R extends IRecipe<?>> Map<ResourceLocation, R> filterRecipes(Collection<IRecipe<?>> recipes, Class<R> recipeClass, IRecipeType<R> recipeType)
	{
		return recipes.stream()
				.filter(iRecipe -> iRecipe.getType()==recipeType)
				.flatMap(r -> {
					if(r instanceof GeneratedListRecipe)
						return ((GeneratedListRecipe)r).getSubRecipes().stream();
					else
						return Stream.of(r);
				})
				.map(recipeClass::cast)
				.collect(Collectors.toMap(recipe -> recipe.getId(), recipe -> recipe));
	}
}
