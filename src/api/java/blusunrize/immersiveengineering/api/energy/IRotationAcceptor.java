/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public interface IRotationAcceptor
{
	BlockCapability<IRotationAcceptor, @Nullable Direction> CAPABILITY = BlockCapability.createSided(
			IEApi.ieLoc("rotation_acceptor"), IRotationAcceptor.class
	);

	void inputRotation(double rotation);
}
