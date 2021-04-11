/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class NetHandlerCapability
{
	@CapabilityInject(GlobalWireNetwork.class)
	public static Capability<GlobalWireNetwork> NET_CAPABILITY = null;
}
