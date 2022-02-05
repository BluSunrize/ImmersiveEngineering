/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.oc2;

import li.cil.oc2.api.bus.device.rpc.RPCParameter;

import javax.annotation.Nonnull;

public record IERPCParameter(Class<?> type) implements RPCParameter
{
	@Nonnull
	@Override
	public Class<?> getType()
	{
		return type();
	}
}
