/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecartRenderer.class)
public interface MinecartRendererAccess
{
	@Accessor
	EntityModel<?> getModel();
}
