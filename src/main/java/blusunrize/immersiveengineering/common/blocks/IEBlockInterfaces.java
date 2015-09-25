package blusunrize.immersiveengineering.common.blocks;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class IEBlockInterfaces
{
	public interface ICustomBoundingboxes
	{
		/**@return a list of custom bounding boxes here*/
		public ArrayList<AxisAlignedBB> addCustomSelectionBoxesToList(World world, int x, int y, int z);
		/**Add a special version of the given box to the list
		 * @return true to make that box the exclusive render
		 */
		public boolean addSpecifiedSubBox(World world, int x, int y, int z, EntityPlayer player, AxisAlignedBB box, Vec3 hitVec, ArrayList<AxisAlignedBB> list);
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