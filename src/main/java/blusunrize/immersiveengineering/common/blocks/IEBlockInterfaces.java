package blusunrize.immersiveengineering.common.blocks;

import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class IEBlockInterfaces
{
	public interface ICustomBoundingboxes
	{
		public Set<AxisAlignedBB> addCustomSelectionBoxesToList(World world, int x, int y, int z, EntityPlayer player);
	}
	
	public interface IBlockOverlayText
	{
		public String[] getOverlayText(EntityPlayer player, MovingObjectPosition mop, boolean hammer);
	}
	
	public interface ISoundTile
	{
	}
	
	public interface ISpawnInterdiction
	{
		public double getInterdictionRange();
	}
}