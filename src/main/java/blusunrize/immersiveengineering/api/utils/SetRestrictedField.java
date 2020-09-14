/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.utils;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.base.Preconditions;
import net.minecraftforge.fml.ModLoadingContext;

public class SetRestrictedField<T>
{
	private T value;

	public void setValue(T value)
	{
		String currentMod = ModLoadingContext.get().getActiveNamespace();
		Preconditions.checkState(
				Lib.MODID.equals(currentMod),
				"Restricted fields may only be set by Immersive Engineering, current mod is %s", currentMod
		);
		this.value = value;
	}

	public T getValue()
	{
		return Preconditions.checkNotNull(value);
	}
}
