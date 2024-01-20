/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;

public class IERecipeBuilder<B extends IERecipeBuilder<B>>
{
	protected final List<ICondition> conditions = new ArrayList<>();

	public B addCondition(ICondition condition)
	{
		conditions.add(condition);
		return (B)this;
	}

	protected ICondition[] getConditions()
	{
		return conditions.toArray(ICondition[]::new);
	}
}
