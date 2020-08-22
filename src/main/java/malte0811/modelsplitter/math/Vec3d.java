package malte0811.modelsplitter.math;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public class Vec3d
{
	public static final Vec3d ZERO = new Vec3d(0, 0, 0);
	private final double[] elements;

	public Vec3d(double[] data)
	{
		Preconditions.checkArgument(data.length==3);
		for(int i = 0; i < 3; ++i)
		{
			Preconditions.checkArgument(Double.isFinite(data[i]));
		}
		elements = Arrays.copyOf(data, 3);
	}

	public Vec3d(double x, double y, double z)
	{
		this(new double[]{x, y, z});
	}

	public double dotProduct(Vec3d other)
	{
		double ret = 0;
		for(int i = 0; i < 3; ++i)
		{
			ret += get(i)*other.get(i);
		}
		return ret;
	}

	public double get(int index)
	{
		return elements[index];
	}

	public double lengthSquared()
	{
		double ret = 0;
		for(int i = 0; i < 3; ++i)
		{
			ret += get(i)*get(i);
		}
		return ret;
	}

	public double length()
	{
		return Math.sqrt(lengthSquared());
	}

	public Vec3d scale(double lambda)
	{
		return new Vec3d(get(0)*lambda, get(1)*lambda, get(2)*lambda);
	}

	public Vec3d add(Vec3d other)
	{
		return new Vec3d(
				get(0)+other.get(0),
				get(1)+other.get(1),
				get(2)+other.get(2)
		);
	}

	public Vec3d subtract(Vec3d other)
	{
		return new Vec3d(
				get(0)-other.get(0),
				get(1)-other.get(1),
				get(2)-other.get(2)
		);
	}

	@Override
	public String toString()
	{
		return get(0)+" "+get(1)+" "+get(2);
	}

	public Vec3d normalize()
	{
		final double len = length();
		if(len < 1e-4)
			return this;
		else
			return scale(1/len);
	}
}
