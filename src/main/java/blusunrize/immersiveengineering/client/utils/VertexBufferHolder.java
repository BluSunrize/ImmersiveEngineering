/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.client.IVertexBufferHolder;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.NonNullSupplier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.Map.Entry;

public class VertexBufferHolder implements IVertexBufferHolder
{
	private static final Lazy<Boolean> HAS_OPTIFINE = Lazy.of(() -> {
		try
		{
			Class.forName("net.optifine.Config");
			IELogger.logger.warn(
					"OptiFine detected! Automatically disabling VBOs, this will make windmills and some"+
							" other objects render much less efficiently"
			);
			return true;
		} catch(Exception x)
		{
			return false;
		}
	});
	//TODO also sort by buffer to get rid of bindBuffer calls?
	private static final Map<RenderType, List<BufferedJob>> JOBS = new IdentityHashMap<>();
	private final ResettableLazy<VertexBuffer> buffer;
	private final Renderer renderer;

	private static VertexBufferHolder forQuads(NonNullSupplier<List<BakedQuad>> quads)
	{
		final ResettableLazy<List<BakedQuad>> cachedQuads = new ResettableLazy<>(quads);
		return new VertexBufferHolder(new Renderer()
		{
			@Override
			public void render(VertexConsumer builder, PoseStack transform, int light, int overlay)
			{
				for(BakedQuad quad : quads.get())
					builder.putBulkData(transform.last(), quad, 1, 1, 1, light, overlay);
			}

			@Override
			public void reset()
			{
				cachedQuads.reset();
			}
		});
	}

	private VertexBufferHolder(Renderer renderer)
	{
		this.renderer = renderer;
		this.buffer = new ResettableLazy<>(
				() -> {
					// TODO WTF does "Usage" do?
					VertexBuffer vb = new VertexBuffer(Usage.STATIC);
					RenderSystem.setShader(IEGLShaders::getVboShader);
					Tesselator tes = Tesselator.getInstance();
					BufferBuilder bb = tes.getBuilder();
					bb.begin(Mode.QUADS, BUFFER_FORMAT);
					this.renderer.render(bb, new PoseStack(), 0, 0);
					vb.bind();
					vb.upload(bb.end());
					VertexBuffer.unbind();
					return vb;
				},
				VertexBuffer::close
		);
	}

	public static void addToAPI()
	{
		IVertexBufferHolder.CREATE.setValue(new VertexBufferHolderFactory()
		{
			@Override
			public IVertexBufferHolder create(Renderer renderer)
			{
				return new VertexBufferHolder(renderer);
			}

			@Override
			public IVertexBufferHolder apply(NonNullSupplier<List<BakedQuad>> quads)
			{
				return forQuads(quads);
			}
		});
	}

	@Override
	public void render(RenderType type, int light, int overlay, MultiBufferSource directOut, PoseStack transform, boolean inverted)
	{
		if(IEClientConfig.enableVBOs.get()&&!HAS_OPTIFINE.get())
			JOBS.computeIfAbsent(type, t -> new ArrayList<>())
					.add(new BufferedJob(this, light, overlay, transform, inverted));
		else
			renderToBuilder(directOut.getBuffer(type), transform, light, overlay, inverted);
	}

	@Override
	public void reset()
	{
		buffer.reset();
		renderer.reset();
	}

	private void renderToBuilder(VertexConsumer builder, PoseStack transform, int light, int overlay, boolean inverted)
	{
		if(inverted)
			builder = new InvertingVertexBuffer(4, builder);
		renderer.render(builder, transform, light, overlay);
	}

	public static void afterTERRendering()
	{
		if(JOBS.isEmpty())
			return;
		for(Entry<RenderType, List<BufferedJob>> typeEntry : JOBS.entrySet())
		{
			RenderType type = typeEntry.getKey();
			type.setupRenderState();
			boolean inverted = false;
			for(BufferedJob job : typeEntry.getValue())
			{
				if(job.inverted&&!inverted)
					GL11.glCullFace(GL11.GL_FRONT);
				else if(!job.inverted&&inverted)
					GL11.glCullFace(GL11.GL_BACK);
				inverted = job.inverted;
				VertexBuffer buffer = job.buffer.buffer.get();
				buffer.bind();
				ShaderInstance shader = IEGLShaders.getVboShader();
				RenderSystem.setShader(() -> shader);
				Objects.requireNonNull(shader.getUniform("LightUV"))
						.set(job.light&0xffff, (job.light>>16)&0xffff);
				Objects.requireNonNull(shader.getUniform("OverlayUV"))
						.set(job.overlay&0xffff, (job.overlay>>16)&0xffff);
				buffer.drawWithShader(job.transform, RenderSystem.getProjectionMatrix(), shader);
			}
			if(inverted)
				GL11.glCullFace(GL11.GL_BACK);
			type.clearRenderState();
		}
		VertexBuffer.unbind();
		JOBS.clear();
	}

	private static class BufferedJob
	{
		private final VertexBufferHolder buffer;
		private final int light;
		private final int overlay;
		private final Matrix4f transform;
		private final boolean inverted;

		private BufferedJob(VertexBufferHolder buffer, int light, int overlay, PoseStack transform, boolean inverted)
		{
			this.buffer = buffer;
			this.light = light;
			this.overlay = overlay;
			this.transform = new Matrix4f(transform.last().pose());
			this.inverted = inverted;
		}
	}
}
