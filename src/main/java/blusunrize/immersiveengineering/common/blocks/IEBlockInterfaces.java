package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.util.MovingObjectPosition;

public class IEBlockInterfaces
{
	public static interface ICustomBoundingboxes
	{
	}
	
	public static interface IBlockOverlayText
	{
		public String[] getOverlayText(MovingObjectPosition mop);
	}
}
