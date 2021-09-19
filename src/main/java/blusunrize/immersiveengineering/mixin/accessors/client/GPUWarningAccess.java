/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.GpuWarnlistManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GpuWarnlistManager.class)
public interface GPUWarningAccess
{
	@Accessor
	ImmutableMap<String, String> getWarnings();

	@Accessor
	void setWarnings(ImmutableMap<String, String> newMap);
}
