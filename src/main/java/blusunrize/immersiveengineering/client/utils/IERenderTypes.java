package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderState.AlphaState;
import net.minecraft.client.renderer.RenderState.LineState;
import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class IERenderTypes
{
	public static final RenderType SOLID_FULLBRIGHT;
	public static final RenderType TRANSLUCENT_LINES;
	public static final RenderType TRANSLUCENT_TRIANGLES;
	protected static final RenderState.ShadeModelState SHADE_ENABLED = new RenderState.ShadeModelState(true);
	protected static final RenderState.TextureState BLOCK_SHEET_MIPPED = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, true);
	protected static final RenderState.LightmapState LIGHTMAP_DISABLED = new RenderState.LightmapState(false);
	protected static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = new RenderState.TransparencyState("translucent_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}, RenderSystem::disableBlend);
	protected static final RenderState.DepthTestState DEPTH_ALWAYS = new RenderState.DepthTestState(GL11.GL_ALWAYS);

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
		RenderType.State translucentLinesState = RenderType.State.getBuilder()
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.line(new LineState(OptionalDouble.of(2)))
				.texture(new TextureState())
				.depthTest(DEPTH_ALWAYS)
				.build(true);
		TRANSLUCENT_LINES = RenderType.makeType(
				ImmersiveEngineering.MODID+":translucent_lines",
				DefaultVertexFormats.POSITION_COLOR,
				GL11.GL_LINES,
				256,//TODO is that a good value?
				translucentLinesState
		);
		TRANSLUCENT_TRIANGLES = RenderType.makeType(
				ImmersiveEngineering.MODID+":translucent_triangle_fan",
				DefaultVertexFormats.POSITION_COLOR,
				GL11.GL_TRIANGLES,
				256,//TODO is that a good value?
				translucentLinesState
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
