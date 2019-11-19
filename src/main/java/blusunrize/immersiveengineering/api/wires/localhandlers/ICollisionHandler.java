/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.localhandlers;

import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

public interface ICollisionHandler
{
	//TODO more params!
	void onCollided(LivingEntity e, BlockPos pos, CollisionInfo info);
}
