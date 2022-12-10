package blusunrize.immersiveengineering.mixin.coremods.temp;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// TODO remove. This (and most other Mixins in this package) is a workaround for either missing Forge API or general
//  lack of understanding on my side
@Mixin(VanillaRegistries.class)
public interface DatagenRegistryAccess
{
	@Accessor
	static RegistrySetBuilder getBUILDER()
	{
		throw new RuntimeException();
	}
}
