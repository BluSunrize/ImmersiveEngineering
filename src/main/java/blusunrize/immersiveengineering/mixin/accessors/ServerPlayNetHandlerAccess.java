/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.accessors;

import blusunrize.immersiveengineering.api.utils.PlayerUtils;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerPlayNetHandlerAccess extends PlayerUtils.ConnectionAccess
{
	@Accessor
	@Override
	void setClientIsFloating(boolean shouldFloat);

	@Accessor
	@Override
	void setAboveGroundTickCount(int ticks);
}
