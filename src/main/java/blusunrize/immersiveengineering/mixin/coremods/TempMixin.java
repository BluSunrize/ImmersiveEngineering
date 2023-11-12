/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods;

import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ChunkMap.class)
public abstract class TempMixin
{
	@Shadow
	@Final
	ServerLevel level;

	@Shadow
	@Nullable
	public abstract LevelChunk getChunkToSend(long p_300929_);

	@Shadow @Final private PlayerMap playerMap;

	@Inject(
			method = "applyChunkTrackingView",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkTrackingView;difference(Lnet/minecraft/server/level/ChunkTrackingView;Lnet/minecraft/server/level/ChunkTrackingView;Ljava/util/function/Consumer;Ljava/util/function/Consumer;)V")
	)
	private void fireWatchEvents(ServerPlayer player, ChunkTrackingView newChunks, CallbackInfo ci)
	{
		ChunkTrackingView oldChunks = player.getChunkTrackingView();
		ChunkTrackingView.difference(
				oldChunks,
				newChunks,
				newChunk -> {
					final var chunk = getChunkToSend(newChunk.toLong());
					if (chunk != null)
						EventHooks.fireChunkWatch(player, chunk, level);
				},
				unwatchedChunk -> EventHooks.fireChunkUnWatch(player, unwatchedChunk, level)
		);
	}

	@Inject(method = "onChunkReadyToSend", at = @At("HEAD"))
	private void fireWatchEvent2(LevelChunk chunkToSend, CallbackInfo ci) {
		ChunkPos pos = chunkToSend.getPos();
		for(ServerPlayer player : this.playerMap.getAllPlayers()) {
			if (player.getChunkTrackingView().contains(pos)) {
				EventHooks.fireChunkWatch(player, chunkToSend, level);
			}
		}
	}
}
