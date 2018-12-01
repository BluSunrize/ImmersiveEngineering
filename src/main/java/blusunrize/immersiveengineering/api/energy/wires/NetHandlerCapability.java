/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class NetHandlerCapability
{
	@CapabilityInject(AbstractWireNetwork.class)
	public static Capability<AbstractWireNetwork> SHADER_CAPABILITY = null;

	public static void register()
	{
		CapabilityManager.INSTANCE.register(AbstractWireNetwork.class, new Capability.IStorage<AbstractWireNetwork>()
		{
			@Override
			public NBTBase writeNBT(Capability<AbstractWireNetwork> capability, AbstractWireNetwork instance, EnumFacing side)
			{
				return instance.writeToNBT();
			}

			@Override
			public void readNBT(Capability<AbstractWireNetwork> capability, AbstractWireNetwork instance, EnumFacing side, NBTBase nbt)
			{
				instance.readFromNBT((NBTTagCompound)nbt);
			}
			//TODO does this work? It doesn't seem to be used anywhere
		}, ServerWireNetwork::new);
	}
}
