/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EngineersBlueprintItem extends IEBaseItem
{
	public EngineersBlueprintItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Nonnull
	@Override
	public String getDescriptionId(ItemStack stack)
	{
		return this.getDescriptionId();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		String key = getCategory(stack);
		if(!key.isEmpty()
				&&BlueprintCraftingRecipe.recipeCategories.contains(key))
		{
			String formatKey = Lib.DESC_INFO+"blueprint."+key;
			String formatted = I18n.get(formatKey);
			if(formatKey.equals(formatted))
				list.add(new TextComponent(key));
			else
				list.add(new TranslatableComponent(formatKey));
			if(Screen.hasShiftDown())
			{
				list.add(new TranslatableComponent(Lib.DESC_INFO+"blueprint.creates1"));
				BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(key);
				if(recipes.length > 0)
					for(BlueprintCraftingRecipe recipe : recipes)
						list.add(new TextComponent(" ").append(recipe.output.getHoverName()));
			}
			else
				list.add(new TranslatableComponent(Lib.DESC_INFO+"blueprint.creates0"));
		}
	}


	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list)
	{
		if(this.allowdedIn(tab))
			for(String key : BlueprintCraftingRecipe.recipeCategories)
			{
				ItemStack stack = new ItemStack(this);
				ItemNBTHelper.putString(stack, "blueprint", key);
				list.add(stack);
			}
	}

	@Nonnull
	public static BlueprintCraftingRecipe[] getRecipes(ItemStack stack)
	{
		return BlueprintCraftingRecipe.findRecipes(getCategory(stack));
	}

	public static String getCategory(ItemStack stack)
	{
		return ItemNBTHelper.getString(stack, "blueprint");
	}
}