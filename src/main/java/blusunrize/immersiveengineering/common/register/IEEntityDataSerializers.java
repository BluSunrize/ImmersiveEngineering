/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity.BulletData;
import blusunrize.immersiveengineering.common.fluids.IEFluid;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class IEEntityDataSerializers
{
	public static final DeferredRegister<EntityDataSerializer<?>> REGISTER = DeferredRegister.create(
			NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Lib.MODID
	);

	public static final Supplier<EntityDataSerializer<FluidStack>> FLUID_STACK = REGISTER.register(
			"fluid_stack", IEFluid.EntityFluidSerializer::new
	);
	public static final Supplier<EntityDataSerializer<RevolvershotEntity.BulletData<?>>> BULLET = REGISTER.register(
			"bullet", () -> EntityDataSerializer.forValueType(BulletData.CODECS.streamCodec())
	);
}
