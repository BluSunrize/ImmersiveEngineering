/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import net.minecraft.world.entity.Entity;

public interface IElectricDamageSource
{
	boolean apply(Entity e);

	float getDamage();
}
