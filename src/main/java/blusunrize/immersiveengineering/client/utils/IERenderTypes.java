/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;
import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_PARTICLES;

//This extends RenderStateShard to get access to various protected members
public class IERenderTypes extends RenderStateShard
{
	public static final VertexFormat BLOCK_WITH_OVERLAY = new VertexFormat(
			ImmutableMap.<String, VertexFormatElement>builder()
					.put("Position", ELEMENT_POSITION)
					.put("Color", ELEMENT_COLOR)
					.put("UV0", ELEMENT_UV0)
					.put("UV1", ELEMENT_UV1)
					.put("UV2", ELEMENT_UV2)
					.put("Normal", ELEMENT_NORMAL)
					.put("Padding", ELEMENT_PADDING)
					.build()
	);
	public static final RenderType TRANSLUCENT_FULLBRIGHT;
	public static final RenderType SOLID_FULLBRIGHT;
	public static final RenderType LINES;
	public static final RenderType POINTS;
	public static final RenderType TRANSLUCENT_TRIANGLES;
	public static final RenderType TRANSLUCENT_POSITION_COLOR;
	public static final RenderType TRANSLUCENT_NO_DEPTH;
	public static final RenderType CHUNK_MARKER;
	public static final RenderType POSITION_COLOR_LIGHTMAP;
	public static final RenderType ITEM_DAMAGE_BAR;
	public static final RenderType PARTICLES;
	private static final Function<ResourceLocation, RenderType> GUI_CUTOUT;
	private static final Function<ResourceLocation, RenderType> GUI_TRANSLUCENT;
	private static final Function<ResourceLocation, RenderType> FULLBRIGHT_TRANSLUCENT;
	private static final ShaderStateShard RENDERTYPE_POSITION_COLOR = RENDERTYPE_LIGHTNING_SHADER;
	protected static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true);
	protected static final RenderStateShard.LightmapStateShard LIGHTMAP_DISABLED = new RenderStateShard.LightmapStateShard(false);
	protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}, RenderSystem::disableBlend);
	protected static final RenderStateShard.ShaderStateShard FULLBRIGHT_BLOCKS = new RenderStateShard.ShaderStateShard(IEGLShaders::getBlockFullbrightShader);
	protected static final RenderStateShard.ShaderStateShard POINTS_SHADER = new RenderStateShard.ShaderStateShard(IEGLShaders::getPointShader);
	protected static final RenderStateShard.TransparencyStateShard NO_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
			"no_transparency",
			RenderSystem::disableBlend, () -> {
	});
	protected static final RenderStateShard.DepthTestStateShard DEPTH_ALWAYS = new RenderStateShard.DepthTestStateShard("always", GL11.GL_ALWAYS);

	static
	{
		SOLID_FULLBRIGHT = createDefault(
				ImmersiveEngineering.MODID+":solid_fullbright",
				BLOCK_WITH_OVERLAY, Mode.QUADS,
				RenderType.CompositeState.builder()
						.setShaderState(FULLBRIGHT_BLOCKS)
						.setOverlayState(OVERLAY)
						.setLightmapState(LIGHTMAP_DISABLED)
						.setTextureState(BLOCK_SHEET_MIPPED)
						.createCompositeState(false)
		);
		TRANSLUCENT_FULLBRIGHT = createDefault(
				ImmersiveEngineering.MODID+":translucent_fullbright",
				BLOCK_WITH_OVERLAY, Mode.QUADS,
				RenderType.CompositeState.builder()
						.setShaderState(FULLBRIGHT_BLOCKS)
						.setLightmapState(LIGHTMAP_DISABLED)
						.setOverlayState(OVERLAY)
						.setTextureState(BLOCK_SHEET_MIPPED)
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.createCompositeState(false)
		);
		RenderType.CompositeState translucentNoDepthState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLineState(new LineStateShard(OptionalDouble.of(2)))
				.setDepthTestState(DEPTH_ALWAYS)
				.setShaderState(RENDERTYPE_POSITION_COLOR)
				.createCompositeState(false);
		LINES = createDefault(
				ImmersiveEngineering.MODID+":translucent_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, Mode.LINES,
				RenderType.CompositeState.builder()
						.setShaderState(RENDERTYPE_LINES_SHADER)
						.setLineState(new LineStateShard(OptionalDouble.of(2)))
						.setLayeringState(VIEW_OFFSET_Z_LAYERING)
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setOutputState(ITEM_ENTITY_TARGET)
						.setWriteMaskState(COLOR_DEPTH_WRITE)
						.setCullState(NO_CULL)
						.createCompositeState(false)
		);
		POINTS = createDefault(
				ImmersiveEngineering.MODID+":points", DefaultVertexFormat.POSITION_COLOR_NORMAL, Mode.QUADS,
				RenderType.CompositeState.builder()
						.setShaderState(POINTS_SHADER)
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setOutputState(RenderStateShard.PARTICLES_TARGET)
						.setWriteMaskState(COLOR_DEPTH_WRITE)
						.setCullState(NO_CULL)
						.createCompositeState(false)
		);
		TRANSLUCENT_TRIANGLES = createDefault(
				ImmersiveEngineering.MODID+":translucent_triangles", DefaultVertexFormat.POSITION_COLOR, Mode.TRIANGLES, translucentNoDepthState
		);
		RenderType.CompositeState translucentNoTextureState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setTextureState(BLOCK_SHEET_MIPPED)
				.setShaderState(RENDERTYPE_POSITION_COLOR)
				.createCompositeState(false);
		TRANSLUCENT_POSITION_COLOR = createDefault(
				ImmersiveEngineering.MODID+":translucent_pos_color", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, translucentNoTextureState
		);
		TRANSLUCENT_NO_DEPTH = createDefault(
				ImmersiveEngineering.MODID+":translucent_no_depth", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, translucentNoDepthState
		);
		CHUNK_MARKER = createDefault(
				ImmersiveEngineering.MODID+":chunk_marker",
				DefaultVertexFormat.POSITION_COLOR_NORMAL,
				//TODO figure out glitchyness
				Mode.LINES,
				RenderType.CompositeState.builder()
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setShaderState(RENDERTYPE_LINES_SHADER)
						.setLineState(new LineStateShard(OptionalDouble.of(5)))
						.setLayeringState(VIEW_OFFSET_Z_LAYERING)
						.setCullState(NO_CULL)
						.setOutputState(MAIN_TARGET)
						.setWriteMaskState(COLOR_DEPTH_WRITE)
						.createCompositeState(false)
		);
		POSITION_COLOR_LIGHTMAP = createDefault(
				ImmersiveEngineering.MODID+":pos_color_lightmap",
				DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
				Mode.QUADS,
				RenderType.CompositeState.builder()
						.setLightmapState(new LightmapStateShard(true))
						.setTextureState(new WhiteTextureStateShard())
						.setShaderState(POSITION_COLOR_LIGHTMAP_SHADER)
						.createCompositeState(false)
		);
		ITEM_DAMAGE_BAR = createDefault(
				ImmersiveEngineering.MODID+":item_damage_bar",
				DefaultVertexFormat.POSITION_COLOR,
				Mode.QUADS,
				RenderType.CompositeState.builder()
						.setDepthTestState(DEPTH_ALWAYS)
						.setTextureState(BLOCK_SHEET_MIPPED)
						.setShaderState(POSITION_COLOR_SHADER)
						.setTransparencyState(NO_TRANSPARENCY)
						.createCompositeState(false)
		);
		PARTICLES = createDefault(
				ImmersiveEngineering.MODID+":particles",
				DefaultVertexFormat.PARTICLE,
				Mode.QUADS,
				RenderType.CompositeState.builder()
						.setTextureState(new TextureStateShard(LOCATION_PARTICLES, false, false))
						.setShaderState(new ShaderStateShard(GameRenderer::getParticleShader))
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setLightmapState(NO_LIGHTMAP)
						.createCompositeState(true)
		);
		GUI_CUTOUT = Util.memoize(texture -> createDefault(
				"gui_"+texture,
				DefaultVertexFormat.POSITION_COLOR_TEX,
				Mode.QUADS,
				makeGuiState(texture).createCompositeState(false)
		));
		GUI_TRANSLUCENT = Util.memoize(texture -> createDefault(
				"gui_translucent_"+texture,
				DefaultVertexFormat.POSITION_COLOR_TEX,
				Mode.QUADS,
				makeGuiState(texture)
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.createCompositeState(false)
		));
		FULLBRIGHT_TRANSLUCENT = Util.memoize(texture -> createDefault(
				"immersiveengineering:fullbright_translucent_"+texture,
				NEW_ENTITY,
				Mode.QUADS,
				CompositeState.builder()
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setTextureState(new TextureStateShard(texture, false, false))
						.setLightmapState(LIGHTMAP_DISABLED)
						.setShaderState(FULLBRIGHT_BLOCKS)
						.setCullState(NO_CULL)
						.setOverlayState(OVERLAY)
						.createCompositeState(true)
		));
	}

	private IERenderTypes(String p_110161_, Runnable p_110162_, Runnable p_110163_)
	{
		super(p_110161_, p_110162_, p_110163_);
	}

	public static RenderType getGui(ResourceLocation texture)
	{
		return GUI_CUTOUT.apply(texture);
	}

	public static RenderType getGuiTranslucent(ResourceLocation texture)
	{
		return GUI_TRANSLUCENT.apply(texture);
	}

	private static CompositeState.CompositeStateBuilder makeGuiState(ResourceLocation texture)
	{
		return RenderType.CompositeState.builder()
				.setTextureState(new TextureStateShard(texture, false, false))
				.setShaderState(POSITION_COLOR_TEX_SHADER);
	}

	public static RenderType getLines(float lineWidth)
	{
		return getLines(lineWidth, RenderStateShard.MAIN_TARGET);
	}

	public static RenderType getParticleLines(float lineWidth)
	{
		return getLines(lineWidth, RenderStateShard.PARTICLES_TARGET);
	}

	private static RenderType getLines(float lineWidth, OutputStateShard target)
	{
		return createDefault(
				"lines_color_pos_"+lineWidth,
				DefaultVertexFormat.POSITION_COLOR_NORMAL,
				Mode.LINES,
				RenderType.CompositeState.builder()
						.setLineState(new LineStateShard(OptionalDouble.of(lineWidth)))
						.setShaderState(RENDERTYPE_LINES_SHADER)
						.setOutputState(target)
						.createCompositeState(false)
		);
	}

	public static RenderType getPositionTex(ResourceLocation texture)
	{
		return createDefault(
				ImmersiveEngineering.MODID+":pos_tex_"+texture,
				DefaultVertexFormat.POSITION_TEX,
				Mode.QUADS,
				RenderType.CompositeState.builder()
						.setTextureState(new TextureStateShard(texture, false, false))
						.setShaderState(POSITION_TEX_SHADER)
						.createCompositeState(false)
		);
	}

	private static RenderType createDefault(String name, VertexFormat format, VertexFormat.Mode mode, RenderType.CompositeState state)
	{
		return RenderType.create(name, format, mode, 256, false, false, state);
	}

	public static RenderType getFullbrightTranslucent(ResourceLocation texture)
	{
		return FULLBRIGHT_TRANSLUCENT.apply(texture);
	}

	public static MultiBufferSource wrapWithStencil(MultiBufferSource in, Consumer<VertexConsumer> setupStencilArea, String name, int ref)
	{
		return wrapWithAdditional(
				in,
				"stencil_"+name+"_"+ref,
				() -> {
					GL11.glEnable(GL11.GL_STENCIL_TEST);
					RenderSystem.colorMask(false, false, false, false);
					RenderSystem.depthMask(false);
					GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
					GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);

					GL11.glStencilMask(0xFF);
					RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, true);
					Tesselator tes = Tesselator.getInstance();
					BufferBuilder bb = tes.getBuilder();
					bb.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
					setupStencilArea.accept(bb);
					tes.end();
					RenderSystem.colorMask(true, true, true, true);
					RenderSystem.depthMask(true);
					GL11.glStencilMask(0x00);
					GL11.glStencilFunc(GL11.GL_EQUAL, ref, 0xFF);
				},
				() -> GL11.glDisable(GL11.GL_STENCIL_TEST)
		);
	}

	/**
	 * Only use with shaders using minecraft_sample_lightmap, not with minecraft_mix_light!
	 */
	public static MultiBufferSource whiteLightmap(MultiBufferSource in)
	{
		return wrapWithAdditional(
				in,
				"white_light",
				() -> WhiteTexture.INSTANCE.get().bind(),
				() -> Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer()
		);
	}

	private static MultiBufferSource wrapWithAdditional(
			MultiBufferSource in,
			String name,
			Runnable setup,
			Runnable teardown
	)
	{
		return type -> in.getBuffer(new RenderType(
				ImmersiveEngineering.MODID+":"+type+"_"+name,
				type.format(),
				type.mode(),
				type.bufferSize(),
				type.affectsCrumbling(),
				false, // needsSorting is private and shouldn't be too relevant here
				() -> {
					type.setupRenderState();
					setup.run();
				},
				() -> {
					teardown.run();
					type.clearRenderState();
				}
		)
		{
		});
	}

	private static class WhiteTextureStateShard extends EmptyTextureStateShard
	{
		public WhiteTextureStateShard()
		{
			super(() -> {
				RenderSystem.setShaderTexture(0, WhiteTexture.INSTANCE.get().getTextureLocation());
			}, () -> {
			});
		}

		@Nonnull
		@Override
		public String toString()
		{
			return "IE: White";
		}

		@Nonnull
		@Override
		protected Optional<ResourceLocation> cutoutTexture()
		{
			return Optional.of(WhiteTexture.INSTANCE.get().getTextureLocation());
		}
	}
}
