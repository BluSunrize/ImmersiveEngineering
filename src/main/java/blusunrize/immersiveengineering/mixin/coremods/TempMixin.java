package blusunrize.immersiveengineering.mixin.coremods;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO This mixin is a workaround for a Forge bug (DeferredRegister not working with non-Forge registries). It should
//  be removed before a release!
@Mixin(MappedRegistry.class)
public abstract class TempMixin<T>
{
	@Inject(
			method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder$Reference;",
			at = @At(value = "TAIL")
	)
	public void bindDirectly(
			int p_256563_, ResourceKey<T> p_256594_, T value, Lifecycle p_256469_, CallbackInfoReturnable<Reference<T>> cir
	) {
		cir.getReturnValue().bindValue(value);
	}
}
