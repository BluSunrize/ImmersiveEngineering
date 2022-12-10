package blusunrize.immersiveengineering.mixin.coremods.temp;

import blusunrize.immersiveengineering.common.world.IEWorldGen.RegistryStubDuck;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.core.RegistrySetBuilder$RegistryStub")
public abstract class RegistryStubMixin<T> implements RegistryStubDuck<T>
{
}
