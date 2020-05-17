package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderState.AlphaState;
import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class IERenderTypes
{
	public static final RenderType SOLID_FULLBRIGHT;
	protected static final RenderState.ShadeModelState SHADE_ENABLED = new RenderState.ShadeModelState(true);
	protected static final RenderState.TextureState BLOCK_SHEET_MIPPED = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, true);
	protected static final RenderState.LightmapState LIGHTMAP_DISABLED = new RenderState.LightmapState(false);

	static
	{
		RenderType.State fullbrightSolidState = RenderType.State.getBuilder()
				.shadeModel(SHADE_ENABLED)
				.lightmap(LIGHTMAP_DISABLED)
				.texture(BLOCK_SHEET_MIPPED)
				.build(true);
		SOLID_FULLBRIGHT = RenderType.makeType(
				ImmersiveEngineering.MODID+":block_fullbright",
				DefaultVertexFormats.BLOCK,
				GL11.GL_QUADS,
				256,//TODO is that a good value?
				fullbrightSolidState
		);
	}

	public static RenderType getGui(ResourceLocation texture)
	{
		return RenderType.makeType(
				"gui",
				DefaultVertexFormats.POSITION_TEX,
				GL11.GL_QUADS,
				256,
				RenderType.State.getBuilder()
						.texture(new TextureState(texture, false, false))
						.alpha(new AlphaState(0.5F))
						.build(false)
		);
	}
}
