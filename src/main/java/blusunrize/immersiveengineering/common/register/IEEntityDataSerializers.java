package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.fluids.IEFluid;
import blusunrize.immersiveengineering.common.fluids.IEFluid.EntityFluidSerializer;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.RegistryObject;

public class IEEntityDataSerializers
{
	public static final DeferredRegister<DataSerializerEntry> REGISTER = DeferredRegister.create(
			Keys.DATA_SERIALIZERS, Lib.MODID
	);

	public static final EntityDataSerializer<FluidStack> FLUID_STACK = register(
			"fluid_stack", new IEFluid.EntityFluidSerializer()
	);

	private static <T> EntityDataSerializer<T> register(String name, EntityDataSerializer<T> serializer)
	{
		REGISTER.register(name, () -> new DataSerializerEntry(serializer));
		return serializer;
	}
}
