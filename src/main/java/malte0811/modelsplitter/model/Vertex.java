package malte0811.modelsplitter.model;

import com.google.common.base.Preconditions;
import malte0811.modelsplitter.math.Vec3d;

public class Vertex
{
	private final Vec3d position;
	private final Vec3d normal;
	private final double[] uv;

	public Vertex(Vec3d position, Vec3d normal, double[] uv)
	{
		this.position = position;
		this.normal = normal;
		Preconditions.checkArgument(uv.length==2);
		this.uv = uv;
	}

	public Vec3d getPosition()
	{
		return position;
	}

	public Vec3d getNormal()
	{
		return normal;
	}

	public double getU()
	{
		return uv[0];
	}

	public double getV()
	{
		return uv[1];
	}

	// lambda * a + (1 - lambda) * b
	public static Vertex interpolate(Vertex a, Vertex b, double lambda)
	{
		return new Vertex(
				a.position.scale(lambda).add(b.position.scale(1-lambda)),
				a.normal.scale(lambda).add(b.normal.scale(1-lambda)),
				new double[]{
						lambda*a.getU()+(1-lambda)*b.getU(),
						lambda*a.getV()+(1-lambda)*b.getV(),
				}
		);
	}

	public double[] getUV()
	{
		return uv;
	}

	public Vertex translate(int axis, double amount)
	{
		double[] offsetData = new double[3];
		offsetData[axis] = amount;
		return new Vertex(
				position.add(new Vec3d(offsetData)),
				normal,
				uv
		);
	}
}
