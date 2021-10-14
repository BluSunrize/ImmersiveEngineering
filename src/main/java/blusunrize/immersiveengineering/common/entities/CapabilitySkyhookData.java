/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SkyhookStatus.HOLDING_CONNECTING;
import static blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SkyhookStatus.NONE;

public class CapabilitySkyhookData
{
	public static final Capability<SkyhookUserData> SKYHOOK_USER_DATA = CapabilityManager.get(new CapabilityToken<>()
	{
	});

	public static class SkyhookUserData
	{
		private SkyhookStatus status = NONE;
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
			status = HOLDING_CONNECTING;
		}

		public void startRiding()
		{
			status = status.mount;
		}
	}

	public static class SimpleSkyhookProvider implements ICapabilityProvider, INBTSerializable<IntTag>
	{
		private final SkyhookUserData data = new SkyhookUserData();
		private final LazyOptional<SkyhookUserData> opt = CapabilityUtils.constantOptional(data);

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
		{
			if(capability==SKYHOOK_USER_DATA)
				return opt.cast();
			return LazyOptional.empty();
		}

		@Override
		public IntTag serializeNBT()
		{
			return IntTag.valueOf(data.status.ordinal());
		}

		@Override
		public void deserializeNBT(IntTag nbt)
		{
			data.status = SkyhookStatus.values()[nbt.getAsInt()];
		}
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
