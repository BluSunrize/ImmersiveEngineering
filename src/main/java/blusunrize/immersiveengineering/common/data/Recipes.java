/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.function.Consumer;

class Recipes extends RecipeProvider
{
	private final Path ADV_ROOT;

	public Recipes(DataGenerator gen)
	{
		super(gen);
		ADV_ROOT = gen.getOutputFolder().resolve("data/minecraft/advancements/recipes/root.json");
	}

	@Override
	protected void saveRecipeAdvancement(DirectoryCache cache, JsonObject json, Path path)
	{
		if(path.equals(ADV_ROOT)) return; //We NEVER care about this.
		super.saveRecipeAdvancement(cache, json, path);
	}

	@Override
	protected void registerRecipes(@Nonnull Consumer<IFinishedRecipe> out)
	{
		for(EnumMetals metal : EnumMetals.values())
		{
			Item nugget = Metals.nuggets.get(metal);
			Item ingot = Metals.ingots.get(metal);
			Item plate = Metals.plates.get(metal);
			Block block = IEBlocks.Metals.storage.get(metal);
			Block sheetMetal = IEBlocks.Metals.sheetmetal.get(metal);
			if(!metal.isVanillaMetal())
			{

				add3x3Conversion(ingot, nugget, out);
				add3x3Conversion(block, ingot, out);
				if(IEBlocks.Metals.ores.containsKey(metal))
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(ore), ingot, metal.smeltingXP, 20)
							.addCriterion("has_"+toPath(ore), hasItem(ore))
							.build(out);
				}
			}
			ShapedRecipeBuilder.shapedRecipe(sheetMetal, 4)
					.key('p', plate)
					.patternLine(" p ")
					.patternLine("p p")
					.patternLine(" p ")
					.addCriterion("has_"+toPath(plate), hasItem(plate))
					.build(out);
		}
	}

	//TODO use tags
	private void add3x3Conversion(IItemProvider big, IItemProvider small, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(big)
				.key('s', small)
				.patternLine("sss")
				.patternLine("sss")
				.patternLine("sss")
				.addCriterion("has_"+toPath(small), hasItem(small))
				.build(out, IEDataGenerator.rl(toPath(small)+"_to_")+toPath(big));
		ShapelessRecipeBuilder.shapelessRecipe(small, 9)
				.addIngredient(big)
				.addCriterion("has_"+toPath(big), hasItem(small))
				.build(out, IEDataGenerator.rl(toPath(big)+"_to_"+toPath(small)));
	}

	private String toPath(IItemProvider src)
	{
		return src.asItem().getRegistryName().getPath();
	}
}
