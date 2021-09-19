/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BatchingRenderTypeBuffer implements MultiBufferSource
{
	private final Map<RenderType, CollectingVertexBuilder> builders = new HashMap<>();

	@Nonnull
	@Override
	public VertexConsumer getBuffer(@Nonnull RenderType type)
	{
		return builders.computeIfAbsent(type, s -> new CollectingVertexBuilder());
	}

	public void pipe(MultiBufferSource out)
	{
		// Glint uses GL_EQUAL for depth, so it needs to be drawn after everything else
		Collection<RenderType> delay = ImmutableList.of(RenderType.glint(), RenderType.entityGlint());
		for(Entry<RenderType, CollectingVertexBuilder> e : builders.entrySet())
			if(!delay.contains(e.getKey()))
				e.getValue().pipeAndClear(out.getBuffer(e.getKey()));
		for(RenderType rt : delay)
			if(builders.containsKey(rt))
				builders.get(rt).pipeAndClear(out.getBuffer(rt));
		builders.clear();
	}
}
