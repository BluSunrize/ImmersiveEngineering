package blusunrize.lib.manual.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderStateShard.AlphaStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ManualRenderTypes
{
	public static RenderType getGui(ResourceLocation texture)
	{
		return RenderType.create(
				"gui_"+texture,
				DefaultVertexFormat.POSITION_COLOR_TEX,
				GL11.GL_QUADS,
				256,
				RenderType.CompositeState.builder()
						.setTextureState(new TextureStateShard(texture, false, false))
						.setAlphaState(new AlphaStateShard(0.5F))
						.createCompositeState(false)
		);
	}
}
