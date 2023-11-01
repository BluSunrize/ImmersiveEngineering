/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.utils.client;

import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class ModelDataUtils
{
	public static <T> ModelData single(ModelProperty<T> prop, T value)
	{
		return ModelData.builder()
				.with(prop, value)
				.build();
	}
}
