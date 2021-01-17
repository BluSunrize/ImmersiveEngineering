/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;

public class ClocheFertilizerBuilder extends IEFinishedRecipe<ClocheFertilizerBuilder>
{
	private ClocheFertilizerBuilder()
	{
		super(ClocheFertilizer.SERIALIZER.get());
	}

	public static ClocheFertilizerBuilder builder(float growthModifier)
	{
		return new ClocheFertilizerBuilder().addWriter(jsonObject -> jsonObject.addProperty("growthModifier", growthModifier));
	}
}
