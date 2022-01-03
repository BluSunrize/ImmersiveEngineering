/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.accessors.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(CompiledChunk.class)
public interface CompiledChunkAccess
{
	@Accessor
	Set<RenderType> getHasBlocks();

	@Accessor
	Set<RenderType> getHasLayer();

	@Accessor
	void setIsCompletelyEmpty(boolean newEmpty);
}
