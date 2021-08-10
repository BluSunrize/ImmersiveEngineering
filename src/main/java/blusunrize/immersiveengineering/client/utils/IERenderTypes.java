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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;
import java.util.function.Consumer;

import static net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_PARTICLES;

//This extends RenderStateShard to get access to various protected members
public class IERenderTypes extends RenderStateShard
{
	public static final RenderType TRANSLUCENT_FULLBRIGHT;
	public static final RenderType SOLID_FULLBRIGHT;
	public static final RenderType LINES;
	public static final RenderType TRANSLUCENT_TRIANGLES;
	public static final RenderType TRANSLUCENT_POSITION_COLOR;
	public static final RenderType TRANSLUCENT_NO_DEPTH;
	public static final RenderType CHUNK_MARKER;
	public static final RenderType POSITION_COLOR_TEX_LIGHTMAP;
	public static final RenderType POSITION_COLOR_LIGHTMAP;
	public static final RenderType ITEM_DAMAGE_BAR;
	public static final RenderType PARTICLES;
	private static final ShaderStateShard RENDERTYPE_POSITION_COLOR = RENDERTYPE_LIGHTNING_SHADER;
	protected static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true);
	protected static final RenderStateShard.LightmapStateShard LIGHTMAP_DISABLED = new RenderStateShard.LightmapStateShard(false);
	protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}, RenderSystem::disableBlend);
	protected static final RenderStateShard.ShaderStateShard FULLBRIGHT_BLOCKS = new RenderStateShard.ShaderStateShard(IEGLShaders::getBlockFullbrightShader);
	protected static final RenderStateShard.TransparencyStateShard NO_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
			"no_transparency",
			RenderSystem::disableBlend, () -> {
	});
	protected static final RenderStateShard.DepthTestStateShard DEPTH_ALWAYS = new RenderStateShard.DepthTestStateShard("always", GL11.GL_ALWAYS);

	static
	{
		SOLID_FULLBRIGHT = createDefault(
				ImmersiveEngineering.MODID+":solid_fullbright",
				DefaultVertexFormat.BLOCK, Mode.QUADS,
				RenderType.CompositeState.builder()
						.setShaderState(FULLBRIGHT_BLOCKS)
						.setLightmapState(LIGHTMAP_DISABLED)
						.setTextureState(BLOCK_SHEET_MIPPED)
						.createCompositeState(false)
		);
		TRANSLUCENT_FULLBRIGHT = createDefault(
				ImmersiveEngineering.MODID+":translucent_fullbright",
				DefaultVertexFormat.BLOCK, Mode.QUADS,
				RenderType.CompositeState.builder()
						.setShaderState(FULLBRIGHT_BLOCKS)
						.setLightmapState(LIGHTMAP_DISABLED)
						.setTextureState(BLOCK_SHEET_MIPPED)
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setOutputState(TRANSLUCENT_TARGET)
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
						.setOutputState(ITEM_ENTITY_TARGET)
						.setWriteMaskState(COLOR_DEPTH_WRITE)
						.createCompositeState(false)
		);
		POSITION_COLOR_TEX_LIGHTMAP = createDefault(
				ImmersiveEngineering.MODID+":pos_color_tex_lightmap",
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
				Mode.QUADS,
				RenderType.CompositeState.builder()
						.setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
						.setLightmapState(new LightmapStateShard(true))
						.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
						.createCompositeState(false)
		);
		POSITION_COLOR_LIGHTMAP = createDefault(
				ImmersiveEngineering.MODID+":pos_color_lightmap",
				DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
				Mode.QUADS,
				RenderType.CompositeState.builder()
						.setTextureState(BLOCK_SHEET_MIPPED)
						.setLightmapState(new LightmapStateShard(true))
						.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
						.createCompositeState(false)
		);
		ITEM_DAMAGE_BAR = createDefault(
				ImmersiveEngineering.MODID+":item_damage_bar",
				DefaultVertexFormat.POSITION_COLOR,
				Mode.QUADS,
				RenderType.CompositeState.builder()
						.setDepthTestState(DEPTH_ALWAYS)
						.setTextureState(BLOCK_SHEET_MIPPED)
						.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
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
						.setOutputState(TRANSLUCENT_TARGET)
						.setLightmapState(NO_LIGHTMAP)
						.createCompositeState(true)
		);
	}

	private IERenderTypes(String p_110161_, Runnable p_110162_, Runnable p_110163_)
	{
		super(p_110161_, p_110162_, p_110163_);
	}

	public static RenderType getGui(ResourceLocation texture)
	{
		return createDefault(
				"gui_"+texture,
				DefaultVertexFormat.POSITION_COLOR_TEX,
				Mode.QUADS,
				RenderType.CompositeState.builder()
						.setTextureState(new TextureStateShard(texture, false, false))
						.setShaderState(POSITION_COLOR_TEX_SHADER)
						.createCompositeState(false)
		);
	}

	public static RenderType getLines(float lineWidth)
	{
		//TODO fix all usages with normals
		return createDefault(
				"lines_color_pos_"+lineWidth,
				DefaultVertexFormat.POSITION_COLOR_NORMAL,
				Mode.LINES,
				RenderType.CompositeState.builder()
						.setLineState(new LineStateShard(OptionalDouble.of(lineWidth)))
						.setShaderState(RENDERTYPE_LINES_SHADER)
						.createCompositeState(false)
		);
	}

	public static RenderType getPoints(float pointSize)
	{
		//TODO
		throw new NotImplementedException("Needs to be updated for 1.17");
		//Not really a fog state, but using it like this makes using RenderType.State with custom states possible
		/*
		FogStateShard setPointSize = new FogStateShard(
				ImmersiveEngineering.MODID+":pointsize_"+pointSize,
				() -> GL11.glPointSize(pointSize),
				() -> {
					GL11.glPointSize(1);
				}
		);
		return createDefault(
				"point_pos_color_"+pointSize,
				DefaultVertexFormat.POSITION_COLOR,
				Mode.POINTS,
				RenderType.CompositeState.builder()
						.setFogState(setPointSize)
						.setTextureState(BLOCK_SHEET_MIPPED)
						.createCompositeState(false)
		);
		 */
	}

	public static RenderType getPositionTex(ResourceLocation texture)
	{
		return createDefault(
				ImmersiveEngineering.MODID+":pos_tex_"+texture,
				DefaultVertexFormat.POSITION_TEX,
				Mode.QUADS,
				RenderType.CompositeState.builder()
						.setTextureState(new TextureStateShard(
								texture,
								false, false))
						.createCompositeState(false)
		);
	}

	private static RenderType createDefault(String name, VertexFormat format, VertexFormat.Mode mode, RenderType.CompositeState state)
	{
		return RenderType.create(name, format, mode, 256, false, false, state);
	}

	public static RenderType getFullbrightTranslucent(ResourceLocation resourceLocation)
	{
		//TODO memoize
		RenderType.CompositeState glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setTextureState(new TextureStateShard(resourceLocation, false, false))
				.setLightmapState(new LightmapStateShard(false))
				.setShaderState(FULLBRIGHT_BLOCKS)
				.createCompositeState(false);
		return createDefault("immersiveengineering:fullbright_translucent_"+resourceLocation, DefaultVertexFormat.BLOCK, Mode.QUADS, glState);
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
					RenderSystem.disableTexture();
					Tesselator tes = Tesselator.getInstance();
					BufferBuilder bb = tes.getBuilder();
					bb.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
					setupStencilArea.accept(bb);
					tes.end();
					RenderSystem.enableTexture();
					RenderSystem.colorMask(true, true, true, true);
					RenderSystem.depthMask(true);
					GL11.glStencilMask(0x00);
					GL11.glStencilFunc(GL11.GL_EQUAL, ref, 0xFF);
				},
				() -> GL11.glDisable(GL11.GL_STENCIL_TEST)
		);
	}

	@Deprecated
	//TODO remove all usages, this is completely broken and unfixable (in general) in 1.17
	public static MultiBufferSource disableLighting(MultiBufferSource in)
	{
		return wrapWithAdditional(
				in,
				"no_lighting",
				() -> Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer(),
				() -> Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer()
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
}
