/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.data.models.ItemModelGenerators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ItemModelGenerators.class)
public interface ItemModelGeneratorsAccess
{
	@Accessor("GENERATED_TRIM_MODELS")
	static List<TrimModelDataAccess> getGeneratedTrimModels()
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}


}

