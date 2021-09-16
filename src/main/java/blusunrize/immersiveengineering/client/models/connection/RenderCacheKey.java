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

public record RenderCacheKey(
		BlockState state,
		ModelState modelTransform,
		RenderType layer,
		Object... additionalProperties
)
{
}
