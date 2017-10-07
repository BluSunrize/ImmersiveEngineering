/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public interface IRotationAcceptor
{
	void inputRotation(double rotation, @Nonnull EnumFacing side);
}
