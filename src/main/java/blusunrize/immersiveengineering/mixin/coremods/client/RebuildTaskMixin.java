/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods.client;

import blusunrize.immersiveengineering.client.render.ConnectionRenderer;
import blusunrize.immersiveengineering.common.mixin.CaptureOwner;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public class RebuildTaskMixin
{
	@Shadow
	@Nullable
	protected RenderChunkRegion region;
	private BlockAndTintGetter immersiveengineering$regionCopy;
	private Set<RenderType> immersiveengineering$layers;
	// Second alias is required with Optifine in production
	@Shadow(aliases = {"f_112859_", "this$1"})
	@Final
	private RenderChunk this$1;

	// Extraction is called for every block, so we just extract the region here and render later
	@CaptureOwner(
			method = "compile",
			at = {
					// Vanilla
					@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0),
					// Vanilla, remapped manually. The Mixin AP is not aware of CaptureOwner, and this is less work than
					// creating an AP just for this
					@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0),
					// Optifine
					@At(value = "INVOKE", target = "Lnet/optifine/override/ChunkCacheOF;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
					// Optifine, remapped manually. An AP would not help here, since the Optifine chunk cache class
					// isn't known at compile time.
					@At(value = "INVOKE", target = "Lnet/optifine/override/ChunkCacheOF;m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
			}
	)
	public BlockAndTintGetter extractRegion(BlockAndTintGetter region)
	{
		immersiveengineering$regionCopy = region;
		return region;
	}

	@ModifyVariable(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;create()Lnet/minecraft/util/RandomSource;"))
	public Set<RenderType> extractLayers(Set<RenderType> set)
	{
		immersiveengineering$layers = set;
		return set;
	}

	@Inject(
			method = "compile",
			at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false)
	)
	public void addConnectionQuads(
			float pX, float pY, float pZ, ChunkBufferBuilderPack pBuffers, CallbackInfoReturnable<?> cir
	)
	{
		ConnectionRenderer.renderConnectionsInSection(
				this.immersiveengineering$layers, pBuffers, this.immersiveengineering$regionCopy, this$1
		);
		this.immersiveengineering$regionCopy = null;
		this.immersiveengineering$layers = null;
	}
}
