/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BluSunrize - 19.01.2020
 */
public class JEIIngredientStackListBuilder
{
	private final List<List<ItemStack>> list;

	private JEIIngredientStackListBuilder()
	{
		this.list = new ArrayList<>();
	}

	public static JEIIngredientStackListBuilder make(IngredientStack... ingredientStacks)
	{
		JEIIngredientStackListBuilder builder = new JEIIngredientStackListBuilder();
		builder.add(ingredientStacks);
		return builder;
	}

	public JEIIngredientStackListBuilder add(IngredientStack... ingredientStacks)
	{
		for(IngredientStack ingr : ingredientStacks)
			this.list.add(ingr.getSizedStackList());
		return this;
	}

	public List<List<ItemStack>> build()
	{
		return this.list;
	}
}
