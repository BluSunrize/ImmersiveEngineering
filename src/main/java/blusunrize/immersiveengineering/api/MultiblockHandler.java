/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

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


	public interface IMultiblock
	{
		/**
		 * returns name of the Multiblock. This is used for the interdiction NBT system on the hammer, so this name /must/ be unique.
		 */
		String getUniqueName();

		/**
		 * Check whether the given block can be used to trigger the structure creation of the multiblock.<br>
		 * Basically, a less resource-intensive preliminary check to avoid checking every structure.
		 */
		boolean isBlockTrigger(IBlockState state);

		/**
		 * This method checks the structure and sets the new one.
		 *
		 * @return if the structure was valid and transformed
		 */
		boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player);

		/**
		 * A three-dimensional array (height, length, width) of the structure to be rendered in the Engineers Manual
		 */
		ItemStack[][][] getStructureManual();

		default IBlockState getBlockstateFromStack(int index, ItemStack stack)
		{
			if(!stack.isEmpty()&&stack.getItem() instanceof ItemBlock)
				return ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(stack.getItemDamage());
			return null;
		}

		/**
		 * An array of ItemStacks that summarizes the total amount of materials needed for the structure. Will be rendered in the Engineer's Manual
		 */
		IngredientStack[] getTotalMaterials();

		/**
		 * Use this to overwrite the rendering of a Multiblock's Component
		 */
		@SideOnly(Side.CLIENT)
		boolean overwriteBlockRender(ItemStack stack, int iterator);

		/**
		 * returns the scale modifier to be applied when rendering the structure in the IE manual
		 */
		float getManualScale();

		/**
		 * returns true to add a button that will switch between the assembly of multiblocks and the finished render
		 */
		@SideOnly(Side.CLIENT)
		boolean canRenderFormedStructure();

		/**
		 * use this function to render the complete multiblock
		 */
		@SideOnly(Side.CLIENT)
		void renderFormedStructure();
	}

	@Deprecated
	public static MultiblockFormEvent postMultiblockFormationEvent(EntityPlayer player, IMultiblock multiblock, BlockPos clickedBlock, ItemStack hammer)
	{
		return fireMultiblockFormationEventPre(player, multiblock, clickedBlock, hammer);
	}

	public static MultiblockFormEvent fireMultiblockFormationEventPre(EntityPlayer player, IMultiblock multiblock, BlockPos clickedBlock, ItemStack hammer)
	{
		MultiblockFormEvent event = new MultiblockFormEvent.Pre(player, multiblock, clickedBlock, hammer);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	/**
	 * Only fire this if the multiblock structure has been checked positive
	 */
	public static MultiblockFormEvent fireMultiblockFormationEventPost(EntityPlayer player, IMultiblock multiblock, BlockPos clickedBlock, ItemStack hammer)
	{
		MultiblockFormEvent event = new MultiblockFormEvent.Post(player, multiblock, clickedBlock, hammer);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	@Cancelable
	public abstract static class MultiblockFormEvent extends PlayerEvent
	{
		private final IMultiblock multiblock;
		private final BlockPos clickedBlock;
		private final ItemStack hammer;

		public MultiblockFormEvent(EntityPlayer player, IMultiblock multiblock, BlockPos clickedBlock, ItemStack hammer)
		{
			super(player);
			this.multiblock = multiblock;
			this.clickedBlock = clickedBlock;
			this.hammer = hammer;
		}

		public IMultiblock getMultiblock()
		{
			return multiblock;
		}

		public BlockPos getClickedBlock()
		{
			return clickedBlock;
		}

		public ItemStack getHammer()
		{
			return hammer;
		}

		/**
		 * This event is fired BEFORE the multiblock is attempted to be formed.<br>
		 * No checks of the structure have been made. The event simply exists to cancel the formation of the multiblock before it ever happens.
		 */
		public static class Pre extends MultiblockFormEvent
		{
			public Pre(EntityPlayer player, IMultiblock multiblock, BlockPos clickedBlock, ItemStack hammer)
			{
				super(player, multiblock, clickedBlock, hammer);
			}
		}

		/**
		 * This event is fired AFTER the multiblock is attempted to be formed.<br>
		 * The structure has been checked positively, but not replaced with the Multiblock.
		 */
		public static class Post extends MultiblockFormEvent
		{
			public Post(EntityPlayer player, IMultiblock multiblock, BlockPos clickedBlock, ItemStack hammer)
			{
				super(player, multiblock, clickedBlock, hammer);
			}
		}
	}


}