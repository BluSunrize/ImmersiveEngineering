/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.TNTEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TNTEntity.class)
public interface TNTEntityAccess
{
	@Accessor
	void setTntPlacedBy(LivingEntity newPlacer);
}
