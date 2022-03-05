/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeReloadListener implements ResourceManagerReloadListener
{
	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager)
	{
		/*TODO
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
		 */
	}

	public static void buildRecipeLists(RecipeManager recipeManager)
	{
		/* TODO
		MetalPressRecipe unpackingRecipe = MetalPressPackingRecipes.getUnpackingContainer();
		MetalPressRecipe packingRecipe2x2 = MetalPressPackingRecipes.get2x2PackingContainer();
		MetalPressRecipe packingRecipe3x3 = MetalPressPackingRecipes.get3x3PackingContainer();
		MetalPressRecipe.recipeList.put(unpackingRecipe.getId(), unpackingRecipe);
		MetalPressRecipe.recipeList.put(packingRecipe2x2.getId(), packingRecipe2x2);
		MetalPressRecipe.recipeList.put(packingRecipe3x3.getId(), packingRecipe3x3);
		MetalPressPackingRecipes.CRAFTING_RECIPE_MAP = filterRecipes(recipes, CraftingRecipe.class, RecipeType.CRAFTING);
		 */
	}

	private void startArcRecyclingRecipeGen(RecipeManager recipeManager, RegistryAccess tags)
	{
		Collection<Recipe<?>> recipes = recipeManager.getRecipes();
		new ArcRecyclingCalculator(recipes, tags).run();
	}

	static <R extends Recipe<?>> Map<ResourceLocation, R> filterRecipes(Collection<Recipe<?>> recipes, Class<R> recipeClass, RecipeType<R> recipeType)
	{
		return recipes.stream()
				.filter(iRecipe -> iRecipe.getType()==recipeType)
				.flatMap(r -> {
					if(r instanceof GeneratedListRecipe genList)
						return genList.getSubRecipes().stream();
					else
						return Stream.of(r);
				})
				.map(recipeClass::cast)
				.collect(Collectors.toMap(recipe -> recipe.getId(), recipe -> recipe));
	}
}
