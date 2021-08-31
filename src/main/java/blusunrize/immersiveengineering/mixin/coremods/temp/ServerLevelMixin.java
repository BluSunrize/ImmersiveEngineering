/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.coremods.temp;

import blusunrize.immersiveengineering.common.util.ServerLevelDuck;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements ServerLevelDuck
{
	private boolean immersiveengineering$isUnloadingBEs;

	@Inject(method = "unload", at = @At("HEAD"))
	public void startUnloading(LevelChunk p_8713_, CallbackInfo ci)
	{
		immersiveengineering$isUnloadingBEs = true;
	}

	@Inject(method = "unload", at = @At("RETURN"))
	public void unloadingDone(LevelChunk p_8713_, CallbackInfo ci)
	{
		immersiveengineering$isUnloadingBEs = false;
	}

	@Override
	public boolean immersiveengineering$isUnloadingBlockEntities()
	{
		return immersiveengineering$isUnloadingBEs;
	}
}
