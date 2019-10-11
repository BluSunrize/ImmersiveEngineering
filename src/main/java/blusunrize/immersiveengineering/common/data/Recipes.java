/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

class Recipes extends RecipeProvider
{
	private final Path ADV_ROOT;
	private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();

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
		for(Entry<Block, Block> blockSlab : IEBlocks.toSlab.entrySet())
			addSlab(blockSlab.getKey(), blockSlab.getValue(), out);

		addRecipe(IEBlocks.StoneDecoration.alloybrick, 2, out, "sb","bs",null, 's', Tags.Items.SANDSTONE, 'b', Tags.Items.INGOTS_BRICK);
		addCornerStraightMiddle(IEBlocks.StoneDecoration.cokebrick, 3, IETags.clay, Tags.Items.INGOTS_BRICK, Tags.Items.SANDSTONE, out);
		addCornerStraightMiddle(IEBlocks.StoneDecoration.blastbrick, 3, Tags.Items.INGOTS_NETHER_BRICK, Tags.Items.INGOTS_BRICK, Items.BLAZE_POWDER, out);
		addSandwich(IEBlocks.StoneDecoration.hempcrete, 6, IETags.clay, IETags.fiberHemp, IETags.clay, out);
		add3x3Conversion(IEBlocks.StoneDecoration.coke, IEItems.Ingredients.coalCoke, out);

		addRecipe(IEBlocks.StoneDecoration.concrete, 8, out, "scs", "gbg", "scs", 's', Tags.Items.SAND, 'c', IETags.clay, 'g', Tags.Items.GRAVEL, 'b', Items.WATER_BUCKET);
		addRecipe(IEBlocks.StoneDecoration.concrete, 12, out, "scs", "gbg", "scs", 's', IEItems.Ingredients.slag, 'c', IETags.clay, 'g', Tags.Items.GRAVEL, 'b', Items.WATER_BUCKET);
		addRecipe(IEBlocks.StoneDecoration.insulatingGlass, 2, out, " g ", "idi", " g ", 'g', Tags.Items.GLASS, 'i', Tags.Items.INGOTS_IRON, 'd', Tags.Items.DYES_GREEN);
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
				.build(out, toRL(toPath(small)+"_to_")+toPath(big));
		ShapelessRecipeBuilder.shapelessRecipe(small, 9)
				.addIngredient(big)
				.addCriterion("has_"+toPath(big), hasItem(small))
				.build(out, toRL(toPath(big)+"_to_"+toPath(small)));
	}

	private void addSlab(IItemProvider block, IItemProvider slab, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(slab, 6)
				.key('s', block)
				.patternLine("sss")
				.addCriterion("has_"+toPath(slab), hasItem(slab))
				.build(out, toRL(toPath(block)+"_to_slab"));
		ShapedRecipeBuilder.shapedRecipe(block)
				.key('s', slab)
				.patternLine("s")
				.patternLine("s")
				.addCriterion("has_"+toPath(block), hasItem(block))
				.build(out, toRL(toPath(block)+"_from_slab"));
	}

	/**
	 * For Recipes like Coke or Blast Bricks, which use the same item in all corners, one for the sides and one for the middle
	 * Also work for shapes like TNT
	 *
	 * @param output the recipe's output
	 * @param corner the item in the corners
	 * @param side   the item on the sides
	 * @param middle the item in the middle
	 */
	@ParametersAreNonnullByDefault
	private void addCornerStraightMiddle(IItemProvider output, int count, Object corner, Object side, Object middle, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(output, count)
				.key('c', makeIngredient(corner))
				.key('s', makeIngredient(side))
				.key('m', makeIngredient(middle))
				.patternLine("csc")
				.patternLine("sms")
				.patternLine("csc")
				.addCriterion("has_"+toPath(output), hasItem(output))
				.build(out, toRL(toPath(output)));
	}

	/**
	 * For Recipes consisting of layers
	 *
	 * @param output the recipe's output
	 * @param top    the item on the top
	 * @param middle the item in the middle
	 * @param bottom the item on the bottom
	 */
	@ParametersAreNonnullByDefault
	private void addSandwich(IItemProvider output, int count, Object top, Object middle, Object bottom, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(output, count)
				.key('t', makeIngredient(top))
				.key('m', makeIngredient(middle))
				.key('b', makeIngredient(bottom))
				.patternLine("ttt")
				.patternLine("mmm")
				.patternLine("bbb")
				.addCriterion("has_"+toPath(output), hasItem(output))
				.build(out, toRL(toPath(output)));
	}

	/**
	 * For any other recipes
	 */
	private void addRecipe(IItemProvider output, int count, Consumer<IFinishedRecipe> out, String row1, String row2, String row3, Object... recipe)
	{
		assert recipe.length%2==0;
		ShapedRecipeBuilder builder = ShapedRecipeBuilder.shapedRecipe(output, count).addCriterion("has_"+toPath(output), hasItem(output));
		if(row1!=null)
			builder.patternLine(row1);
		if(row2!=null)
			builder.patternLine(row2);
		if(row3!=null)
			builder.patternLine(row3);
		for(int i = 0; i < recipe.length; i += 2)
		{
			assert recipe[i] instanceof Character;
			builder.key((Character)recipe[i], makeIngredient(recipe[i+1]));
		}
		builder.build(out, toRL(toPath(output)));
	}

	private String toPath(IItemProvider src)
	{
		return src.asItem().getRegistryName().getPath();
	}

	private ResourceLocation toRL(String s)
	{
		if(PATH_COUNT.containsKey(s))
		{
			int count = PATH_COUNT.get(s)+1;
			PATH_COUNT.put(s, count);
			return new ResourceLocation(ImmersiveEngineering.MODID, s+count);
		}
		PATH_COUNT.put(s, 1);
		return new ResourceLocation(ImmersiveEngineering.MODID, s);
	}

	@Nonnull
	private Ingredient makeIngredient(Object in)
	{
		assert in instanceof IItemProvider||in instanceof Tag||in instanceof Ingredient;
		if(in instanceof IItemProvider)
			return Ingredient.fromItems((IItemProvider)in);
		else if(in instanceof Tag)
			return Ingredient.fromTag((Tag)in);
		else
			return (Ingredient)in;
	}
}
