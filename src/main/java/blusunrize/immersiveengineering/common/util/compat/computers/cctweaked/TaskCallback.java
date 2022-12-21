/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;


import dan200.computercraft.api.lua.*;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Copied from CC:Tweaked, see https://github.com/SquidDev-CC/CC-Tweaked/discussions/728
 */
public final class TaskCallback implements ILuaCallback
{
	private final MethodResult pull = MethodResult.pullEvent("task_complete", this);
	private final long task;

	private TaskCallback(long task)
	{
		this.task = task;
	}

	@Nonnull
	@Override
	public MethodResult resume(Object[] response) throws LuaException
	{
		if(response.length < 3||!(response[1] instanceof Number)||!(response[2] instanceof Boolean))
			return pull;

		if(((Number)response[1]).longValue()!=task)
			return pull;

		if((Boolean)response[2])
			// Extract the return values from the event and return them
			return MethodResult.of(Arrays.copyOfRange(response, 3, response.length));
		else if(response.length >= 4&&response[3] instanceof String)
			// Extract the error message from the event and raise it
			throw new LuaException((String)response[3]);
		else
			throw new LuaException("error");
	}

	public static MethodResult make(ILuaContext context, LuaTask func) throws LuaException
	{
		long task = context.issueMainThreadTask(func);
		return new TaskCallback(task).pull;
	}
}
