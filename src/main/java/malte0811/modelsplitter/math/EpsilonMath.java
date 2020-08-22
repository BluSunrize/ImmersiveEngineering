package malte0811.modelsplitter.math;

public class EpsilonMath
{
	private final double epsilon;

	public EpsilonMath(double epsilon)
	{
		this.epsilon = epsilon;
	}

	public Sign sign(double firstProduct)
	{
		if(firstProduct < -epsilon)
		{
			return Sign.NEGATIVE;
		}
		else if(firstProduct > epsilon)
		{
			return Sign.POSITIVE;
		}
		else
		{
			return Sign.ZERO;
		}
	}

	public boolean areSame(Vec3d a, Vec3d b)
	{
		Vec3d diff = a.subtract(b);
		return diff.lengthSquared() < epsilon*epsilon;
	}

	public int floor(double in)
	{
		return (int)Math.floor(in+epsilon);
	}

	public int ceil(double in)
	{
		return (int)Math.ceil(in-epsilon);
	}

	public enum Sign
	{
		POSITIVE,
		ZERO,
		NEGATIVE;

		public Sign invert()
		{
			switch(this)
			{
				case POSITIVE:
					return NEGATIVE;
				case ZERO:
					return ZERO;
				case NEGATIVE:
					return POSITIVE;
				default:
					throw new IllegalArgumentException("Unknown sign "+this);
			}
		}
	}
}
