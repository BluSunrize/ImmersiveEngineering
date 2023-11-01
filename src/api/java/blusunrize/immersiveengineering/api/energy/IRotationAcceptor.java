/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy;

import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;

public interface IRotationAcceptor
{
	Capability<IRotationAcceptor> CAPABILITY = CapabilityManager.get(new CapabilityToken<>()
	{
	});

	void inputRotation(double rotation);
}
