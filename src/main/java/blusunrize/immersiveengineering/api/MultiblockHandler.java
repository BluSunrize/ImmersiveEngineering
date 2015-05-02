package blusunrize.immersiveengineering.api;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author BluSunrize - 27.04.2015
 *
 * The handler for IE multiblocks. TO handle custom structures, create a class implementing IMultiblock and register it
 */
public class MultiblockHandler
{
	static ArrayList<IMultiblock> multiblocks = new ArrayList<IMultiblock>();

	public static void registerMultiblock(IMultiblock multiblock)
	{
		multiblocks.add(multiblock);
	}
	public static ArrayList<IMultiblock> getMultiblocks()
	{
		return multiblocks;
	}


	public static interface IMultiblock
	{
		/**
		 * Check whether the given block can be used to trigger the structure creation of the multiblock.<br>
		 * Basically, a less resource-intensive preliminary check to avoid checking every structure.
		 */
		public boolean isBlockTrigger(Block b, int meta);

		/**
		 * This method checks the structure and sets the new one.
		 * @return if the structure was valid and transformed
		 */
		public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player);

		/**
		 * A three-dimensional array (height, length, width) of the structure to be rendered in the Engineers Manual
		 */
		public ItemStack[][][] getStructureManual();
	}
}