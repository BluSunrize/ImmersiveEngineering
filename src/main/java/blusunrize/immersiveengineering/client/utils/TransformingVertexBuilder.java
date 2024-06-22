/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// TODO replace by TransformingVertexPipeline for most things?
public class TransformingVertexBuilder implements VertexConsumer
{
	private final VertexConsumer base;
	private final PoseStack transform;
	private final List<ObjectWithGlobal<?>> allObjects = new ArrayList<>();
	private final ObjectWithGlobal<Vec2> uv = new ObjectWithGlobal<>(this);
	private final ObjectWithGlobal<Vector3f> pos = new ObjectWithGlobal<>(this);
	private final ObjectWithGlobal<Vec2i> overlay = new ObjectWithGlobal<>(this);
	private final ObjectWithGlobal<Vec2i> lightmap = new ObjectWithGlobal<>(this);
	private final ObjectWithGlobal<Vector3f> normal = new ObjectWithGlobal<>(this);
	private final ObjectWithGlobal<Vector4f> color = new ObjectWithGlobal<>(this);
	private final VertexFormat format;

	public TransformingVertexBuilder(VertexConsumer base, PoseStack transform, VertexFormat format)
	{
		this.base = base;
		this.transform = transform;
		this.format = format;
	}

	public TransformingVertexBuilder(VertexConsumer base, VertexFormat format)
	{
		this(base, new PoseStack(), format);
	}

	public TransformingVertexBuilder(MultiBufferSource buffer, RenderType type, PoseStack transform)
	{
		this(buffer.getBuffer(type), transform, type.format());
	}

	public TransformingVertexBuilder(MultiBufferSource buffer, RenderType type)
	{
		this(buffer, type, new PoseStack());
	}

	@Nonnull
	@Override
	public VertexConsumer addVertex(float x, float y, float z)
	{
		pos.putData(new Vector3f(x, y, z));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha)
	{
		color.putData(new Vector4f(red/255f, green/255f, blue/255f, alpha/255f));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setUv(float u, float v)
	{
		uv.putData(new Vec2(u, v));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setUv1(int u, int v)
	{
		overlay.putData(new Vec2i(u, v));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setUv2(int u, int v)
	{
		lightmap.putData(new Vec2i(u, v));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setNormal(float x, float y, float z)
	{
		normal.putData(new Vector3f(x, y, z));
		return this;
	}

	//TODO
	// @Override
	public void endVertex()
	{
		for(VertexFormatElement element : format.getElements())
		{
			if(element==VertexFormatElement.POSITION)
				pos.ifPresent(pos -> base.addVertex(transform.last().pose(), (float)pos.x, (float)pos.y, (float)pos.z));
			else if(element==VertexFormatElement.COLOR)
				color.ifPresent(c -> base.setColor(c.x(), c.y(), c.z(), c.w()));
			else if(element==VertexFormatElement.UV0)
				uv.ifPresent(uv -> base.setUv(uv.x, uv.y));
			else if(element==VertexFormatElement.UV1)
				overlay.ifPresent(overlay -> base.setUv1(overlay.x, overlay.y));
			else if(element==VertexFormatElement.UV2)
				lightmap.ifPresent(lightmap -> base.setUv2(lightmap.x, lightmap.y));
			else if(element==VertexFormatElement.NORMAL)
				normal.ifPresent(normal -> base.setNormal(transform.last(), normal.x(), normal.y(), normal.z()));
		}
		allObjects.forEach(ObjectWithGlobal::clear);
	}

	public void defaultColor(float r, float g, float b, float a)
	{
		color.setGlobal(new Vector4f(r, g, b, a));
	}

	public void unsetDefaultColor()
	{
		color.setGlobal(null);
	}

	public void setUV(Vec2 uv)
	{
		this.uv.setGlobal(uv);
	}

	public void setDefaultLight(int light)
	{
		lightmap.setGlobal(new Vec2i(light&255, light>>16));
	}

	public void setDefaultNormal(float x, float y, float z)
	{
		Vector3f vec = new Vector3f(x, y, z);
		vec.normalize();
		normal.setGlobal(vec);
	}

	public void setDefaultOverlay(int packedOverlayIn)
	{
		overlay.setGlobal(new Vec2i(packedOverlayIn&0xffff, packedOverlayIn>>16));
	}

	private record Vec2i(int x, int y)
	{
	}

	private static class ObjectWithGlobal<T>
	{
		@Nullable
		private T obj;
		private boolean isGlobal;

		public ObjectWithGlobal(TransformingVertexBuilder builder)
		{
			builder.allObjects.add(this);
		}

		public void putData(T newVal)
		{
			Preconditions.checkState(obj==null||(isGlobal&&obj.equals(newVal)));
			obj = newVal;
		}

		public void setGlobal(@Nullable T obj)
		{
			this.obj = obj;
			isGlobal = obj!=null;
		}

		public T read()
		{
			T ret = Preconditions.checkNotNull(obj);
			if(!isGlobal)
				obj = null;
			return ret;
		}

		public boolean hasValue()
		{
			return obj!=null;
		}

		public void ifPresent(Consumer<T> out)
		{
			if(hasValue())
				out.accept(read());
		}

		public void clear()
		{
			if(!isGlobal)
				obj = null;
		}
	}
}
