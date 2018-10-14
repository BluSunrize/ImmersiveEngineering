/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.api.CapabilitySkyhookData.SkyhookStatus.HOLDING_CONNECTING;
import static blusunrize.immersiveengineering.api.CapabilitySkyhookData.SkyhookStatus.NONE;

public class CapabilitySkyhookData
{
	@CapabilityInject(SkyhookUserData.class)
	public static Capability<SkyhookUserData> SKYHOOK_USER_DATA = null;

	public static class SkyhookUserData
	{
		private SkyhookStatus status = NONE;
		@Nullable
		public EntitySkylineHook hook = null;
		private boolean limitSpeed = true;

		public void release()
		{
			if(status.release!=null)
				status = status.release;
		}

		public void dismount()
		{
			if(hook!=null)
			{
				hook.setDead();
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

		public void setLimitSpeed(boolean limitSpeed)
		{
			this.limitSpeed = limitSpeed;
		}

		public boolean shouldLimitSpeed()
		{
			return limitSpeed;
		}

		public boolean toggleSpeedLimit()
		{
			limitSpeed = !limitSpeed;
			return limitSpeed;
		}

		public void startRiding()
		{
			status = status.mount;
		}
	}

	public static class SimpleSkyhookProvider implements ICapabilityProvider
	{
		SkyhookUserData data = new SkyhookUserData();

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability==SKYHOOK_USER_DATA&&facing==EnumFacing.UP;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
		{
			if(capability==SKYHOOK_USER_DATA&&facing==EnumFacing.UP)
				return SKYHOOK_USER_DATA.cast(data);
			return null;
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

	public static void register()
	{
		CapabilityManager.INSTANCE.register(SkyhookUserData.class, new Capability.IStorage<SkyhookUserData>()
		{
			private static final String STATUS = "status";
			private static final String LIMIT_SPEED = "limitV";

			@Override
			public NBTBase writeNBT(Capability<SkyhookUserData> capability, SkyhookUserData instance, EnumFacing side)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean(LIMIT_SPEED, instance.limitSpeed);
				nbt.setInteger(STATUS, instance.status.ordinal());
				return nbt;
			}

			@Override
			public void readNBT(Capability<SkyhookUserData> capability, SkyhookUserData instance, EnumFacing side, NBTBase nbt)
			{
				NBTTagCompound tags = (NBTTagCompound)nbt;
				instance.hook = null;
				instance.limitSpeed = tags.getBoolean(LIMIT_SPEED);
				instance.status = SkyhookStatus.values()[tags.getInteger(STATUS)];
			}
		}, SkyhookUserData::new);
	}
}
