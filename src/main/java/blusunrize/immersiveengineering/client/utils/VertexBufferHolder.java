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
import blusunrize.immersiveengineering.common.IEConfig;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.common.util.NonNullSupplier;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;

public class VertexBufferHolder implements IVertexBufferHolder
{
	public static final VertexFormat BUFFER_FORMAT = new VertexFormat(ImmutableList.of(
			POSITION_3F, COLOR_4UB, TEX_2F, NORMAL_3B, PADDING_1B
	));
	//TODO also sort by buffer to get rid of bindBuffer calls?
	private static final Map<RenderType, List<BufferedJob>> JOBS = new IdentityHashMap<>();
	private final ResettableLazy<VertexBuffer> buffer;
	private final ResettableLazy<List<BakedQuad>> quads;

	private VertexBufferHolder(NonNullSupplier<List<BakedQuad>> quads)
	{
		this.quads = new ResettableLazy<>(quads);
		this.buffer = new ResettableLazy<>(
				() -> {
					VertexBuffer vb = new VertexBuffer(BUFFER_FORMAT);
					Tessellator tes = Tessellator.getInstance();
					BufferBuilder bb = tes.getBuffer();
					bb.begin(GL11.GL_QUADS, BUFFER_FORMAT);
					renderToBuilder(bb, new MatrixStack(), 0, 0);
					bb.finishDrawing();
					vb.upload(bb);
					return vb;
				},
				VertexBuffer::close
		);
	}

	public static void addToAPI()
	{
		IVertexBufferHolder.CREATE.setValue(VertexBufferHolder::new);
	}

	@Override
	public void render(RenderType type, int light, int overlay, IRenderTypeBuffer directOut, MatrixStack transform)
	{
		if(IEConfig.GENERAL.enableVBOs.get())
			JOBS.computeIfAbsent(type, t -> new ArrayList<>())
					.add(new BufferedJob(this, light, overlay, transform));
		else
			renderToBuilder(directOut.getBuffer(type), transform, light, overlay);
	}

	@Override
	public void reset()
	{
		buffer.reset();
		quads.reset();
	}

	private void renderToBuilder(IVertexBuilder builder, MatrixStack transform, int light, int overlay)
	{
		for(BakedQuad quad : quads.get())
			builder.addQuad(transform.getLast(), quad, 1, 1, 1, light, overlay);
	}

	//Called from aftertesr.js
	public static void afterTERRendering()
	{
		if(!JOBS.isEmpty())
		{
			for(Entry<RenderType, List<BufferedJob>> typeEntry : JOBS.entrySet())
			{
				RenderType type = typeEntry.getKey();
				type.setupRenderState();
				for(BufferedJob job : typeEntry.getValue())
				{
					RenderSystem.glMultiTexCoord2f(33986, 16*LightTexture.getLightBlock(job.light), 16*LightTexture.getLightSky(job.light));
					RenderSystem.glMultiTexCoord2f(33985, job.overlay&0xffff, job.overlay >>> 16);
					VertexBuffer buffer = job.buffer.buffer.get();
					buffer.bindBuffer();
					BUFFER_FORMAT.setupBufferState(0);
					buffer.draw(job.transform, GL11.GL_QUADS);
				}
				type.clearRenderState();
			}
			VertexBuffer.unbindBuffer();
			BUFFER_FORMAT.clearBufferState();
			JOBS.clear();
		}
	}

	private static class BufferedJob
	{
		private final VertexBufferHolder buffer;
		private final int light;
		private final int overlay;
		private final Matrix4f transform;

		private BufferedJob(VertexBufferHolder buffer, int light, int overlay, MatrixStack transform)
		{
			this.buffer = buffer;
			this.light = light;
			this.overlay = overlay;
			this.transform = transform.getLast().getMatrix();
		}
	}
}
