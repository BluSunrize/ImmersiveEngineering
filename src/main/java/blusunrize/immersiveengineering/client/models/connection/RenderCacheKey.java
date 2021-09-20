/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

public record RenderCacheKey(
		BlockState state,
		ModelState modelTransform,
		RenderType layer,
		// Do *not* make this an array/varargs, otherwise the record uses Object[]::equals and everything breaks
		List<Object> additionalProperties
)
{
	public RenderCacheKey(
			BlockState state,
			ModelState modelTransform,
			RenderType layer,
			Object... additional
	)
	{
		this(state, modelTransform, layer, Arrays.asList(additional));
	}
}
