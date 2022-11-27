package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.fluids.IEFluid;
import blusunrize.immersiveengineering.common.fluids.IEFluid.EntityFluidSerializer;
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
