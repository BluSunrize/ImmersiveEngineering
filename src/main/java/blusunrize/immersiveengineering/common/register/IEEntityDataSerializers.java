/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.fluids.IEFluid;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.RegistryObject;

public class IEEntityDataSerializers
{
	public static final DeferredRegister<EntityDataSerializer<?>> REGISTER = DeferredRegister.create(
			Keys.ENTITY_DATA_SERIALIZERS, Lib.MODID
	);

	public static final RegistryObject<EntityDataSerializer<FluidStack>> FLUID_STACK = REGISTER.register(
			"fluid_stack", IEFluid.EntityFluidSerializer::new
	);
}
