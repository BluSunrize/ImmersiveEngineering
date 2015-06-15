package blusunrize.immersiveengineering.common.blocks;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class IEBlockInterfaces
{
	public static interface ICustomBoundingboxes
	{
		public Set<AxisAlignedBB> addCustomSelectionBoxesToList(World world, int x, int y, int z, EntityPlayer player);
	}
	
	public static interface IBlockOverlayText
	{
		public String[] getOverlayText(MovingObjectPosition mop);
	}
	
	public static interface ISoundTile
	{
	}
}