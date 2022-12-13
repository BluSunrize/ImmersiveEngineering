package blusunrize.immersiveengineering.mixin.coremods.temp;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// TODO remove. This (and the otherr Mixins in this package) is a workaround for missing Forge API. Since these are only
//  relevant during datagen they can stay for now, even if we do CF builds.
@Mixin(VanillaRegistries.class)
public interface DatagenRegistryAccess
{
	@Accessor
	static RegistrySetBuilder getBUILDER()
	{
		throw new RuntimeException();
	}
}
