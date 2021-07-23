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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.common.util.NonNullSupplier;

import java.util.List;
import java.util.function.Function;

/**
 * Used to render models in TERs using VBOs. For complex models this is significantly more efficient than rendering
 * the model directly. Make sure to always call {@link IVertexBufferHolder#reset()} if a vertex buffer is not going to be
 * used again. If VBOs are disabled in the IE config {@link IVertexBufferHolder#render(RenderType, int, int, MultiBufferSource, PoseStack)}
 * will render to the given render type buffer instead of actually using VBOs.
 */
public interface IVertexBufferHolder
{
	SetRestrictedField<Function<NonNullSupplier<List<BakedQuad>>, IVertexBufferHolder>> CREATE = SetRestrictedField.client();

	static IVertexBufferHolder create(NonNullSupplier<List<BakedQuad>> getQuads)
	{
		return CREATE.getValue().apply(getQuads);
	}

	default void render(RenderType type, int light, int overlay, MultiBufferSource directOut, PoseStack transform)
	{
		render(type, light, overlay, directOut, transform, false);
	}

	void render(RenderType type, int light, int overlay, MultiBufferSource directOut, PoseStack transform,
				boolean inverted);

	void reset();
}
