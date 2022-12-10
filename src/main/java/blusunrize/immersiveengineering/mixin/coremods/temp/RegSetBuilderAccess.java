package blusunrize.immersiveengineering.mixin.coremods.temp;

import blusunrize.immersiveengineering.common.world.IEWorldGen.RegistryStubDuck;
import net.minecraft.core.RegistrySetBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RegistrySetBuilder.class)
public interface RegSetBuilderAccess
{
	@Accessor
	List<RegistryStubDuck<?>> getEntries();

}
