/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.util.IELogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class SkyhookUserData
{
	private SkyhookStatus status = SkyhookStatus.NONE;
	@Nullable
	public SkylineHookEntity hook = null;

	public void release()
	{
		if(status.release!=null)
			status = status.release;
	}

	public void dismount()
	{
		if(hook!=null)
		{
			IELogger.logger.debug("Dismounting");
			hook.discard();
			hook = null;
		}
		if(status.dismount!=null)
			status = status.dismount;
	}

	public SkyhookStatus getStatus()
	{
		return status;
	}

	public void startHolding()
	{
		status = SkyhookStatus.HOLDING_CONNECTING;
	}

	public void startRiding()
	{
		status = status.mount;
	}

	public enum SkyhookStatus
	{
		NONE(null, null),
		RIDING(NONE, null),
		HOLDING_CONNECTING(null, NONE),
		HOLDING_FAILED(null, NONE),
		HOLDING_RIDING(HOLDING_FAILED, RIDING);

		static
		{
			NONE.mount = RIDING;
			HOLDING_CONNECTING.mount = HOLDING_RIDING;
		}

		@Nullable
		//The state after leaving the skyhook entity
		public final SkyhookStatus dismount;
		@Nullable
		//The state after stopping to use the skyhook item
		public final SkyhookStatus release;
		@Nullable
		//The state after mounting the skyhook entity
		public SkyhookStatus mount;

		SkyhookStatus(@Nullable SkyhookStatus dismount, @Nullable SkyhookStatus release)
		{
			this.dismount = dismount;
			this.release = release;
		}
	}
}
