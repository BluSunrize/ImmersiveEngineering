/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.client;

import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Used to render models in TERs using VBOs. For complex models this is significantly more efficient than rendering
 * the model directly. Make sure to always call {@link IVertexBufferHolder#reset()} if a vertex buffer is not going to be
 * used again. If VBOs are disabled in the IE config {@link IVertexBufferHolder#render(RenderType, int, int, MultiBufferSource, PoseStack)}
 * will render to the given render type buffer instead of actually using VBOs.
 */
public interface IVertexBufferHolder
{
	SetRestrictedField<VertexBufferHolderFactory> CREATE = SetRestrictedField.client();
	VertexFormat BUFFER_FORMAT = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Color", VertexFormatElement.COLOR)
			.add("UV0", VertexFormatElement.UV0)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.build();

	static IVertexBufferHolder create(Supplier<List<BakedQuad>> getQuads)
	{
		return CREATE.get().apply(getQuads);
	}

	static IVertexBufferHolder create(Renderer render)
	{
		return CREATE.get().create(render);
	}

	default void render(RenderType type, int light, int overlay, MultiBufferSource directOut, PoseStack transform)
	{
		render(type, light, overlay, directOut, transform, false);
	}

	void render(RenderType type, int light, int overlay, MultiBufferSource directOut, PoseStack transform,
				boolean inverted);

	void reset();

	interface Renderer
	{
		void render(VertexConsumer builder, PoseStack transform, int light, int overlay);

		default void reset()
		{
		}
	}

	interface VertexBufferHolderFactory extends Function<Supplier<List<BakedQuad>>, IVertexBufferHolder>
	{
		IVertexBufferHolder create(Renderer renderer);
	}
}
