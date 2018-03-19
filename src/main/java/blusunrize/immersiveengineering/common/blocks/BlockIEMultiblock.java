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
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockIEMultiblock<E extends Enum<E> & BlockIEBase.IBlockEnum> extends BlockIETileProvider<E>
{
	protected final boolean[] hasMultiblockTile;

	public BlockIEMultiblock(String name, Material material, PropertyEnum<E> mainProperty, Class<? extends ItemBlockIEBase> itemBlock, Object... additionalProperties)
	{
		super(name, material, mainProperty, itemBlock, combineProperties(additionalProperties, IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE));
		this.hasMultiblockTile = new boolean[this.enumValues.length];
		for(int i = 0; i < this.hasMultiblockTile.length; i++)
			this.hasMultiblockTile[i] = true;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		return state;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
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
		super.breakBlock(world, pos, state);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		int meta = this.getMetaFromState(state);
		if(meta >= 0&&meta < this.hasMultiblockTile.length&&!this.hasMultiblockTile[meta])
			super.getDrops(drops, world, pos, state, fortune);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		ItemStack stack = getOriginalBlock(world, pos);
		if(!stack.isEmpty())
			return stack;
		return super.getPickBlock(state, target, world, pos, player);
	}

	public ItemStack getOriginalBlock(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart)
			return ((TileEntityMultiblockPart)te).getOriginalBlock();
		return ItemStack.EMPTY;
	}
}