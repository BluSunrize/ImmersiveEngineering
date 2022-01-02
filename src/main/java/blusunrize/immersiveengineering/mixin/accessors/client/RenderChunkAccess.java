/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.accessors.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderChunk.class)
public interface RenderChunkAccess
{
	@Invoker
	void invokeBeginLayer(BufferBuilder pBuilder);
}
