package blusunrize.immersiveengineering.mixin.accessors;

import blusunrize.immersiveengineering.api.utils.PlayerUtils;
import net.minecraft.network.play.ServerPlayNetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayNetHandler.class)
public interface ServerPlayNetHandlerAccess extends PlayerUtils.ConnectionAccess
{
	@Accessor
	void setFloating(boolean shouldFloat);

	@Accessor
	void setFloatingTickCount(int ticks);
}
