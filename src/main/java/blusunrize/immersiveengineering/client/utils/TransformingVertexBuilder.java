package blusunrize.immersiveengineering.client.utils;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class TransformingVertexBuilder implements IVertexBuilder
{
	private final IVertexBuilder base;
	private final MatrixStack transform;
	ObjectWithGlobal<Vec2f> uv = new ObjectWithGlobal<>();
	ObjectWithGlobal<Vec3d> pos = new ObjectWithGlobal<>();
	ObjectWithGlobal<Vec2i> overlay = new ObjectWithGlobal<>();
	ObjectWithGlobal<Vec2i> lightmap = new ObjectWithGlobal<>();
	ObjectWithGlobal<Vector3f> normal = new ObjectWithGlobal<>();
	ObjectWithGlobal<Vector4f> color = new ObjectWithGlobal<>();

	public TransformingVertexBuilder(IVertexBuilder base, MatrixStack transform)
	{
		this.base = base;
		this.transform = transform;
	}

	public TransformingVertexBuilder(IVertexBuilder base)
	{
		this(base, new MatrixStack());
	}

	@Nonnull
	@Override
	public IVertexBuilder pos(double x, double y, double z)
	{
		pos.putData(new Vec3d(x, y, z));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder color(int red, int green, int blue, int alpha)
	{
		color.putData(new Vector4f(red, green, blue, alpha));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder tex(float u, float v)
	{
		uv.putData(new Vec2f(u, v));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder overlay(int u, int v)
	{
		overlay.putData(new Vec2i(u, v));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder lightmap(int u, int v)
	{
		lightmap.putData(new Vec2i(u, v));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder normal(float x, float y, float z)
	{
		normal.putData(new Vector3f(x, y, z));
		return this;
	}

	@Override
	public void endVertex()
	{
		pos.ifPresent(pos -> base.pos(transform.getLast().getMatrix(), (float)pos.x, (float)pos.y, (float)pos.z));
		color.ifPresent(c -> base.color(c.getX(), c.getY(), c.getZ(), c.getW()));
		uv.ifPresent(uv -> base.tex(uv.x, uv.y));
		overlay.ifPresent(overlay -> base.overlay(overlay.x, overlay.y));
		lightmap.ifPresent(lightmap -> base.lightmap(lightmap.x, lightmap.y));
		normal.ifPresent(
				normal -> base.normal(transform.getLast().getNormal(), normal.getX(), normal.getY(), normal.getZ())
		);
		base.endVertex();
	}

	public void setLight(int light)
	{
		lightmap.setGlobal(new Vec2i(light&255, light >> 16));
	}

	public void setColor(float r, float g, float b, float a)
	{
		color.setGlobal(new Vector4f(r, g, b, a));
	}

	private static class Vec2i
	{
		final int x, y;

		private Vec2i(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	}

	private static class ObjectWithGlobal<T>
	{
		@Nullable
		T obj;
		boolean isGlobal;

		public void putData(T newVal)
		{
			Preconditions.checkState(obj==null);
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
	}
}
