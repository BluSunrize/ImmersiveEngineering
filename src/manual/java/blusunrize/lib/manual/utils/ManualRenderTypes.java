package blusunrize.lib.manual.utils;

import net.minecraft.client.renderer.RenderState.AlphaState;
import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ManualRenderTypes
{
	public static RenderType getGui(ResourceLocation texture)
	{
		return RenderType.makeType(
				"gui_"+texture,
				DefaultVertexFormats.POSITION_COLOR_TEX,
				GL11.GL_QUADS,
				256,
				RenderType.State.getBuilder()
						.texture(new TextureState(texture, false, false))
						.alpha(new AlphaState(0.5F))
						.build(false)
		);
	}
}
