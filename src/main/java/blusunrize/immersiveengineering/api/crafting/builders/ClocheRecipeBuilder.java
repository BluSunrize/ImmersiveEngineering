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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;

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

	public static ClocheRecipeBuilder builder(ITag<Item> result, int count)
	{
		return new ClocheRecipeBuilder().addResult(new IngredientWithSize(result, count));
	}

	public ClocheRecipeBuilder addSoil(IItemProvider itemProvider)
	{
		return addItem("soil", new ItemStack(itemProvider));
	}

	public ClocheRecipeBuilder addSoil(ItemStack itemStack)
	{
		return addItem("soil", itemStack);
	}

	public ClocheRecipeBuilder addSoil(ITag<Item> tag)
	{
		return addSoil(Ingredient.fromTag(tag));
	}

	public ClocheRecipeBuilder addSoil(Ingredient ingredient)
	{
		return addWriter(jsonObject -> jsonObject.add("soil", ingredient.serialize()));
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
