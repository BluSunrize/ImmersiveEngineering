package malte0811.modelsplitter.math;

public class Plane
{
	public final Vec3d normal;
	public final double dotProduct;

	public Plane(Vec3d normal, double dotProduct)
	{
		this.normal = normal;
		this.dotProduct = dotProduct;
	}
}
