/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

public abstract class BlockIEMultiblock extends BlockIETileProvider
{

	public BlockIEMultiblock(String name, Block.Properties props, Class<? extends ItemBlockIEBase> itemBlock, IProperty<?>... additionalProperties)
	{
		super(name, props, itemBlock,
				ArrayUtils.addAll(additionalProperties, IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE));
		setMobility(EnumPushReaction.BLOCK);
		setNotNormalBlock();
	}

	@Override
	public void onReplaced(IBlockState state, World world, BlockPos pos, IBlockState newState, boolean isMoving)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityMultiblockPart&&world.getGameRules().getBoolean("doTileDrops"))
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)tileEntity;
			if(!tile.formed&&tile.pos==-1&&!tile.getOriginalBlock().isEmpty())
				world.spawnEntity(new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, tile.getOriginalBlock().copy()));

			if(tile.formed&&tile instanceof IIEInventory)
			{
				IIEInventory master = (IIEInventory)tile.master();
				if(master!=null&&(!(master instanceof ITileDrop)||!((ITileDrop)master).preventInventoryDrop())&&master.getDroppedItems()!=null)
					for(ItemStack s : master.getDroppedItems())
						if(!s.isEmpty())
							world.spawnEntity(new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, s.copy()));
			}
		}
		if(tileEntity instanceof TileEntityMultiblockPart)
			((TileEntityMultiblockPart)tileEntity).disassemble();
		super.onReplaced(state, world, pos, newState, isMoving);
	}

	@Override
	public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune)
	{
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player)
	{
		ItemStack stack = getOriginalBlock(world, pos);
		if(!stack.isEmpty())
			return stack;
		return super.getPickBlock(state, target, world, pos, player);
	}

	public ItemStack getOriginalBlock(IBlockReader world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart)
			return ((TileEntityMultiblockPart)te).getOriginalBlock();
		return ItemStack.EMPTY;
	}
}