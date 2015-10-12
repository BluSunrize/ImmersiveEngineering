package blusunrize.immersiveengineering.api;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author BluSunrize - 27.04.2015
 * <br>
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
		 * returns name of the Multiblock. This is used for the interdiction NBT system on the hammer, so this name /must/ be unique.
		 */
		public String getUniqueName();
		
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
		
		/**
		 * An array of ItemStacks that summarizes the total amount of materials needed for the structure. Will be rendered in the Engineer's Manual
		 */
		public ItemStack[] getTotalMaterials();
		
		/**
		 * Use this to overwrite the rendering of a Multiblock's Component
		 */
		@SideOnly(Side.CLIENT)
		public boolean overwriteBlockRender(ItemStack stack, int iterator);

		/**
		 * returns the scale modifier to be applied when rendering the structure in the IE manual
		 */
		public float getManualScale();
		
		/**
		 * returns true to add a button that will switch between the assembly of multiblocks and the finished render
		 */
		@SideOnly(Side.CLIENT)
		public boolean canRenderFormedStructure();
		/**
		 * use this function to render the complete multiblock
		 */
		@SideOnly(Side.CLIENT)
		public void renderFormedStructure();
	}
}