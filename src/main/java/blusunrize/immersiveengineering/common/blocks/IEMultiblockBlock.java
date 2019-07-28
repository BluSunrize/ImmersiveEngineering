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
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.List;

public abstract class IEMultiblockBlock extends IETileProviderBlock
{

	public IEMultiblockBlock(String name, Block.Properties props, Class<? extends ItemBlockIEBase> itemBlock, IProperty<?>... additionalProperties)
	{
		super(name, props, itemBlock,
				ArrayUtils.addAll(additionalProperties, IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE));
		setMobility(PushReaction.BLOCK);
		setNotNormalBlock();
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof MultiblockPartTileEntity&&world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
		{
			MultiblockPartTileEntity tile = (MultiblockPartTileEntity)tileEntity;
			if(!tile.formed&&tile.posInMultiblock==-1&&!tile.getOriginalBlock().isEmpty())
				world.addEntity(new ItemEntity(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, tile.getOriginalBlock().copy()));

			if(tile.formed&&tile instanceof IIEInventory)
			{
				IIEInventory master = (IIEInventory)tile.master();
				if(master!=null&&(!(master instanceof ITileDrop)||!((ITileDrop)master).preventInventoryDrop())&&master.getDroppedItems()!=null)
					for(ItemStack s : master.getDroppedItems())
						if(!s.isEmpty())
							world.addEntity(new ItemEntity(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, s.copy()));
			}
		}
		if(tileEntity instanceof MultiblockPartTileEntity)
			((MultiblockPartTileEntity)tileEntity).disassemble();
		super.onReplaced(state, world, pos, newState, isMoving);
	}

	@Override
	@SuppressWarnings("deprecation")
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		// @todo review: Presuming the drop list has to be empty, as items are manually spawned in `onReplaced()` above.
		return Collections.emptyList();
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		ItemStack stack = getOriginalBlock(world, pos);
		if(!stack.isEmpty())
			return stack;
		return super.getPickBlock(state, target, world, pos, player);
	}

	public ItemStack getOriginalBlock(IBlockReader world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof MultiblockPartTileEntity)
			return ((MultiblockPartTileEntity)te).getOriginalBlock();
		return ItemStack.EMPTY;
	}
}