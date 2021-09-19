/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction.ClocheRenderReference;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.common.base.Preconditions;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ClocheRecipeBuilder extends IEFinishedRecipe<ClocheRecipeBuilder>
{
	private boolean hasRender = false;

	private ClocheRecipeBuilder()
	{
		super(ClocheRecipe.SERIALIZER.get());
		setMultipleResults(4);
	}

	public static ClocheRecipeBuilder builder(Item result)
	{
		return new ClocheRecipeBuilder().addResult(result);
	}

	public static ClocheRecipeBuilder builder(ItemStack result)
	{
		return new ClocheRecipeBuilder().addResult(result);
	}

	public static ClocheRecipeBuilder builder(Tag<Item> result, int count)
	{
		return new ClocheRecipeBuilder().addResult(new IngredientWithSize(result, count));
	}

	public ClocheRecipeBuilder addSoil(ItemLike itemProvider)
	{
		return addItem("soil", new ItemStack(itemProvider));
	}

	public ClocheRecipeBuilder addSoil(ItemStack itemStack)
	{
		return addItem("soil", itemStack);
	}

	public ClocheRecipeBuilder addSoil(Tag<Item> tag)
	{
		return addSoil(Ingredient.of(tag));
	}

	public ClocheRecipeBuilder addSoil(Ingredient ingredient)
	{
		return addWriter(jsonObject -> jsonObject.add("soil", ingredient.toJson()));
	}

	public ClocheRecipeBuilder setRender(ClocheRenderReference renderReference)
	{
		Preconditions.checkArgument(!hasRender, "This recipe already has a render set.");
		this.hasRender = true;
		return addWriter(jsonObject -> jsonObject.add("render", renderReference.serialize()));
	}

	@Override
	protected boolean isComplete()
	{
		return super.isComplete()&&hasRender;
	}
}
