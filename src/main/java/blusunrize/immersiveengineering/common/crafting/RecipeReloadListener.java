/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.lang.ref.WeakReference;
import java.util.Collection;

@EventBusSubscriber(modid = Lib.MODID)
public class RecipeReloadListener
{
	// TODO total hack, but I don't see any other reasonable way to access both a (working) RecipeManager and
	//  RegistryAccess with "good" tags at once
	private static final ThreadLocal<WeakReference<RecipeManager>> recipes = new ThreadLocal<>();

	@SubscribeEvent
	public static void onRegisterReloadListeners(AddReloadListenerEvent ev)
	{
		recipes.set(new WeakReference<>(ev.getServerResources().getRecipeManager()));
	}

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent ev)
	{
		WeakReference<RecipeManager> boxedManager = recipes.get();
		if(boxedManager==null)
			return;
		RecipeManager recipeManager = boxedManager.get();
		if(recipeManager==null)
			return;
		Collection<Recipe<?>> recipes = recipeManager.getRecipes();
		new ArcRecyclingCalculator(recipes, ev.getTagManager()).run();
	}
}
