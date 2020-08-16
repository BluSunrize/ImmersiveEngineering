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
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/* We can't use ISelectiveResourceReloadListener because it references a client-only class which crashes servers
 */
public class RecipeReloadListener implements IResourceManagerReloadListener
{
	private final DataPackRegistries dataPackRegistries;

	public RecipeReloadListener(DataPackRegistries dataPackRegistries)
	{
		this.dataPackRegistries = dataPackRegistries;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{
		if(dataPackRegistries!=null)
		{
			RecipeManager recipeManager = dataPackRegistries.getRecipeManager();
			buildRecipeLists(recipeManager);
			generateArcRecyclingRecipes(recipeManager);
		}
	}

	RecipeManager clientRecipeManager;

	@SubscribeEvent
	public void onTagsUpdated(TagsUpdatedEvent event)
	{
		if(clientRecipeManager!=null)
		{
			TagUtils.ITEM_TAG_COLLECTION = ItemTags.getCollection();
			TagUtils.BLOCK_TAG_COLLECTION = BlockTags.getCollection();
			generateArcRecyclingRecipes(clientRecipeManager);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onRecipesUpdated(RecipesUpdatedEvent event)
	{
		clientRecipeManager = event.getRecipeManager();
		if(!Minecraft.getInstance().isSingleplayer())
			buildRecipeLists(clientRecipeManager);
	}

	static void buildRecipeLists(RecipeManager recipeManager)
	{
		Collection<IRecipe<?>> recipes = recipeManager.getRecipes();

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

		ArcFurnaceRecipe.recipeList = filterRecipes(recipes, ArcFurnaceRecipe.class, ArcFurnaceRecipe.TYPE);
		BottlingMachineRecipe.recipeList = filterRecipes(recipes, BottlingMachineRecipe.class, BottlingMachineRecipe.TYPE);
		CrusherRecipe.recipeList = filterRecipes(recipes, CrusherRecipe.class, CrusherRecipe.TYPE);
		FermenterRecipe.recipeList = filterRecipes(recipes, FermenterRecipe.class, FermenterRecipe.TYPE);
		SqueezerRecipe.recipeList = filterRecipes(recipes, SqueezerRecipe.class, SqueezerRecipe.TYPE);
		RefineryRecipe.recipeList = filterRecipes(recipes, RefineryRecipe.class, RefineryRecipe.TYPE);
		MixerRecipe.recipeList = filterRecipes(recipes, MixerRecipe.class, MixerRecipe.TYPE);
		MineralMix.mineralList = filterRecipes(recipes, MineralMix.class, MineralMix.TYPE);

		MixerRecipePotion.initPotionRecipes();
	}

	private void generateArcRecyclingRecipes(RecipeManager recipeManager)
	{
		Collection<IRecipe<?>> recipes = recipeManager.getRecipes();
		ArcRecyclingThreadHandler recyclingHandler = new ArcRecyclingThreadHandler(recipes);
		recyclingHandler.start();
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
