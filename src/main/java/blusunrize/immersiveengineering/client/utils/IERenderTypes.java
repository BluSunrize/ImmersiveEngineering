package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.RenderState.AlphaState;
import net.minecraft.client.renderer.RenderState.LineState;
import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;
import java.util.function.Consumer;

public class IERenderTypes
{
	public static final RenderType SOLID_FULLBRIGHT;
	public static final RenderType TRANSLUCENT_LINES;
	public static final RenderType TRANSLUCENT_TRIANGLES;
	public static final RenderType TRANSLUCENT_POSITION_COLOR;
	public static final RenderType TRANSLUCENT_NO_DEPTH;
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
		RenderType.State translucentNoDepthState = RenderType.State.getBuilder()
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.line(new LineState(OptionalDouble.of(2)))
				.texture(new TextureState())
				.depthTest(DEPTH_ALWAYS)
				.build(true);
		RenderType.State translucentNoTextureState = RenderType.State.getBuilder()
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.texture(new TextureState())
				.build(true);
		TRANSLUCENT_LINES = RenderType.makeType(
				ImmersiveEngineering.MODID+":translucent_lines",
				DefaultVertexFormats.POSITION_COLOR,
				GL11.GL_LINES,
				256,//TODO is that a good value?
				translucentNoDepthState
		);
		TRANSLUCENT_TRIANGLES = RenderType.makeType(
				ImmersiveEngineering.MODID+":translucent_triangle_fan",
				DefaultVertexFormats.POSITION_COLOR,
				GL11.GL_TRIANGLES,
				256,//TODO is that a good value?
				translucentNoDepthState
		);
		TRANSLUCENT_POSITION_COLOR = RenderType.makeType(
				ImmersiveEngineering.MODID+":translucent_pos_color",
				DefaultVertexFormats.POSITION_COLOR,
				GL11.GL_QUADS,
				256,//TODO is that a good value?
				translucentNoTextureState
		);
		TRANSLUCENT_NO_DEPTH = RenderType.makeType(
				ImmersiveEngineering.MODID+":translucent_no_depth",
				DefaultVertexFormats.POSITION_COLOR,
				GL11.GL_QUADS,
				256,//TODO is that a good value?
				translucentNoDepthState
		);
	}

	public static RenderType getGui(ResourceLocation texture)
	{
		return RenderType.makeType(
				"gui",
				DefaultVertexFormats.POSITION_COLOR_TEX,
				GL11.GL_QUADS,
				256,
				RenderType.State.getBuilder()
						.texture(new TextureState(texture, false, false))
						.alpha(new AlphaState(0.5F))
						.build(false)
		);
	}

	public static RenderType getLines(float lineWidth)
	{
		return RenderType.makeType(
				"lines_only_pos",
				DefaultVertexFormats.POSITION,
				GL11.GL_LINES,
				256,
				//TODO more state?
				RenderType.State.getBuilder()
						.line(new LineState(OptionalDouble.of(lineWidth)))
						.texture(new TextureState())
						.build(false)
		);
	}

	public static IRenderTypeBuffer wrapWithStencil(IRenderTypeBuffer in, Consumer<IVertexBuilder> setupStencilArea, String name, int ref)
	{
		return type -> in.getBuffer(new RenderType(
				ImmersiveEngineering.MODID+":stencil_"+name+"_"+type.toString()+"_"+ref,
				type.getVertexFormat(),
				type.getDrawMode(),
				type.getBufferSize(),
				type.isUseDelegate(),
				false, // needsSorting is private and shouldn't be too relevant here
				() -> {
					type.setupRenderState();

					GL11.glEnable(GL11.GL_STENCIL_TEST);
					RenderSystem.colorMask(false, false, false, false);
					RenderSystem.depthMask(false);
					GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
					GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);

					GL11.glStencilMask(0xFF);
					RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, true);
					RenderSystem.disableTexture();
					Tessellator tes = Tessellator.getInstance();
					BufferBuilder bb = tes.getBuffer();
					bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
					setupStencilArea.accept(bb);
					tes.draw();
					RenderSystem.enableTexture();
					RenderSystem.colorMask(true, true, true, true);
					RenderSystem.depthMask(true);
					GL11.glStencilMask(0x00);
					GL11.glStencilFunc(GL11.GL_EQUAL, ref, 0xFF);
				},
				() -> {
					GL11.glDisable(GL11.GL_STENCIL_TEST);
					type.clearRenderState();
				}
		)
		{
		});
	}
}
