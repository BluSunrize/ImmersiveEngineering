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
import org.joml.Vector3f;
import org.joml.Vector4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

public class TransformingVertexBuilder implements VertexConsumer
{
	private final VertexConsumer base;
	private final PoseStack transform;
	private final List<ObjectWithGlobal<?>> allObjects = new ArrayList<>();
	private final ObjectWithGlobal<Vec2> uv = new ObjectWithGlobal<>(this);
	private final ObjectWithGlobal<Vec3> pos = new ObjectWithGlobal<>(this);
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
	public VertexConsumer vertex(double x, double y, double z)
	{
		pos.putData(new Vec3(x, y, z));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha)
	{
		color.putData(new Vector4f(red/255f, green/255f, blue/255f, alpha/255f));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer uv(float u, float v)
	{
		uv.putData(new Vec2(u, v));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer overlayCoords(int u, int v)
	{
		overlay.putData(new Vec2i(u, v));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer uv2(int u, int v)
	{
		lightmap.putData(new Vec2i(u, v));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer normal(float x, float y, float z)
	{
		normal.putData(new Vector3f(x, y, z));
		return this;
	}

	@Override
	public void endVertex()
	{
		for(VertexFormatElement element : format.getElements())
		{
			if(element==ELEMENT_POSITION)
				pos.ifPresent(pos -> base.vertex(transform.last().pose(), (float)pos.x, (float)pos.y, (float)pos.z));
			else if(element==ELEMENT_COLOR)
				color.ifPresent(c -> base.color(c.x(), c.y(), c.z(), c.w()));
			else if(element==ELEMENT_UV0)
				uv.ifPresent(uv -> base.uv(uv.x, uv.y));
			else if(element==ELEMENT_UV1)
				overlay.ifPresent(overlay -> base.overlayCoords(overlay.x, overlay.y));
			else if(element==ELEMENT_UV2)
				lightmap.ifPresent(lightmap -> base.uv2(lightmap.x, lightmap.y));
			else if(element==ELEMENT_NORMAL)
				normal.ifPresent(
						normal -> base.normal(transform.last().normal(), normal.x(), normal.y(), normal.z())
				);
		}
		base.endVertex();
		allObjects.forEach(ObjectWithGlobal::clear);
	}

	public void defaultColor(float r, float g, float b, float a)
	{
		color.setGlobal(new Vector4f(r, g, b, a));
	}

	@Override
	public void defaultColor(int r, int g, int b, int a)
	{
		defaultColor(r/255f, g/255f, b/255f, a/255f);
	}

	@Override
	public void unsetDefaultColor()
	{
		color.setGlobal(null);
	}

	public void setUV(Vec2 uv)
	{
		this.uv.setGlobal(uv);
	}

	public void setLight(int light)
	{
		lightmap.setGlobal(new Vec2i(light&255, light>>16));
	}

	public void setNormal(float x, float y, float z)
	{
		Vector3f vec = new Vector3f(x, y, z);
		vec.normalize();
		normal.setGlobal(vec);
	}

	public void setOverlay(int packedOverlayIn)
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
