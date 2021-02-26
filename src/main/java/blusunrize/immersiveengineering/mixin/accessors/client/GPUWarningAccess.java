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
import net.minecraft.client.renderer.GPUWarning;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GPUWarning.class)
public interface GPUWarningAccess
{
	@Accessor("field_241688_c_")
	ImmutableMap<String, String> getWarningStrings();

	@Accessor("field_241688_c_")
	void setWarningStrings(ImmutableMap<String, String> newMap);
}
