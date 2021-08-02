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
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeReloadListener implements ResourceManagerReloadListener
{
	private final ServerResources serverResources;

	public RecipeReloadListener(ServerResources serverResources)
	{
		this.serverResources = serverResources;
	}

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager)
	{
		if(serverResources!=null)
		{
			startArcRecyclingRecipeGen(serverResources.getRecipeManager(), serverResources.getTags());
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server!=null)
			{
				Iterator<ServerLevel> it = server.getAllLevels().iterator();
				// Should only be false when no players are loaded, so the data will be synced on login
				if(it.hasNext())
					ApiUtils.addFutureServerTask(it.next(),
							() -> StaticTemplateManager.syncMultiblockTemplates(PacketDistributor.ALL.noArg(), true)
					);
			}
		}
	}

	RecipeManager clientRecipeManager;

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onRecipesUpdated(RecipesUpdatedEvent event)
	{
		clientRecipeManager = event.getRecipeManager();
		if(!Minecraft.getInstance().hasSingleplayerServer())
			buildRecipeLists(clientRecipeManager);
	}

	public static void buildRecipeLists(RecipeManager recipeManager)
	{
		Collection<Recipe<?>> recipes = recipeManager.getRecipes();
		// Empty recipe list shouldn't happen, but has been known to be caused by other mods
		if(recipes.size()==0)
			return;

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
		MetalPressPackingRecipes.CRAFTING_RECIPE_MAP = filterRecipes(recipes, CraftingRecipe.class, RecipeType.CRAFTING);
		MetalPressRecipe.updateRecipesByMold();

		BottlingMachineRecipe.recipeList = filterRecipes(recipes, BottlingMachineRecipe.class, BottlingMachineRecipe.TYPE);
		CrusherRecipe.recipeList = filterRecipes(recipes, CrusherRecipe.class, CrusherRecipe.TYPE);
		SawmillRecipe.recipeList = filterRecipes(recipes, SawmillRecipe.class, SawmillRecipe.TYPE);
		FermenterRecipe.recipeList = filterRecipes(recipes, FermenterRecipe.class, FermenterRecipe.TYPE);
		SqueezerRecipe.recipeList = filterRecipes(recipes, SqueezerRecipe.class, SqueezerRecipe.TYPE);
		RefineryRecipe.recipeList = filterRecipes(recipes, RefineryRecipe.class, RefineryRecipe.TYPE);
		MixerRecipe.recipeList = filterRecipes(recipes, MixerRecipe.class, MixerRecipe.TYPE);
		MineralMix.mineralList = filterRecipes(recipes, MineralMix.class, MineralMix.TYPE);
		ArcFurnaceRecipe.recipeList = filterRecipes(recipes, ArcFurnaceRecipe.class, ArcFurnaceRecipe.TYPE);
	}

	private void startArcRecyclingRecipeGen(RecipeManager recipeManager, TagContainer tags)
	{
		Collection<Recipe<?>> recipes = recipeManager.getRecipes();
		new ArcRecyclingCalculator(recipes, tags).run();
	}

	static <R extends Recipe<?>> Map<ResourceLocation, R> filterRecipes(Collection<Recipe<?>> recipes, Class<R> recipeClass, RecipeType<R> recipeType)
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
