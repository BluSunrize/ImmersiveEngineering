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
import net.minecraft.nbt.NBTTagInt;
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
			@Override
			public NBTBase writeNBT(Capability<SkyhookUserData> capability, SkyhookUserData instance, EnumFacing side)
			{
				return new NBTTagInt(instance.status.ordinal());
			}

			@Override
			public void readNBT(Capability<SkyhookUserData> capability, SkyhookUserData instance, EnumFacing side, NBTBase nbt)
			{
				instance.status = SkyhookStatus.values()[((NBTTagInt)nbt).getInt()];
			}
		}, SkyhookUserData::new);
	}
}
