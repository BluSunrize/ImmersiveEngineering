/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public interface IRotationAcceptor
{
	Capability<IRotationAcceptor> CAPABILITY = CapabilityManager.get(new CapabilityToken<>()
	{
	});

	void inputRotation(double rotation);
}
