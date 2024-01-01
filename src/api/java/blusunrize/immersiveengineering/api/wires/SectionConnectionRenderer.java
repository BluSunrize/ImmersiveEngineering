/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;

import java.util.function.Function;

/**
 * This class provides the functionality required to hook up IEs custom connection rendering to other chunk renderers,
 * primarily Sodium ports.
 * This is necessary since Forge does not have an API for adding arbitrary static geometry to chunks, so the default
 * approach uses a Mixin into the vanilla chunk rendering code. Any mod bypassing the vanilla renderer will not be able
 * to render wires without *some* IE-specific code, and these hooks minimize the amount of IE-specific code required.
 */
public class SectionConnectionRenderer
{
	public static final SetRestrictedField<Renderer> RENDER_CONNECTIONS = SetRestrictedField.client();
	public static final SetRestrictedField<RenderChecker> SHOULD_RENDER_CONNECTIONS = SetRestrictedField.client();

	public interface Renderer
	{
		void renderConnectionsInSection(
				Function<RenderType, VertexConsumer> getBuffer, BlockAndTintGetter region, BlockPos sectionOrigin
		);
	}

	public interface RenderChecker
	{
		boolean needsRenderingInSection(SectionPos section);
	}
}
