/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.village.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PointOfInterestType.class)
public interface PoITypeAccess
{
	@Invoker
	static PointOfInterestType callRegisterBlockStates(PointOfInterestType poit)
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
