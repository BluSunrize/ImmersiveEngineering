package blusunrize.immersiveengineering.api.tool;

public class RailgunHandler
{

	public static class RailgunProjectileProperties
	{
		public double damage;
		public double gravity;
		public RailgunProjectileProperties(double damage, double gravity)
		{
			this.damage = damage;
			this.gravity = gravity;
		}
	}
}