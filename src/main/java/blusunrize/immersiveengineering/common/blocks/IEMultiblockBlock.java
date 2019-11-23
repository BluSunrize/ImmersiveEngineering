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
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraft.world.storage.loot.LootParameters;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.List;

public abstract class IEMultiblockBlock extends IETileProviderBlock
{

	public IEMultiblockBlock(String name, Block.Properties props, Class<? extends BlockItemIE> itemBlock, IProperty<?>... additionalProperties)
	{
		super(name, props, itemBlock,
				ArrayUtils.addAll(additionalProperties, IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE));
		setMobility(PushReaction.BLOCK);
		setNotNormalBlock();
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if(state.getBlock()!=newState.getBlock())
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity instanceof IEBaseTileEntity)
				((IEBaseTileEntity)tileEntity).setOverrideState(state);
			if(tileEntity instanceof MultiblockPartTileEntity&&world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
			{
				MultiblockPartTileEntity tile = (MultiblockPartTileEntity)tileEntity;
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
		}
		super.onReplaced(state, world, pos, newState, isMoving);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		TileEntity te = builder.get(LootParameters.BLOCK_ENTITY);
		if(te instanceof MultiblockPartTileEntity)
		{
			MultiblockPartTileEntity<?> multiblockTile = (MultiblockPartTileEntity<?>)te;
			return Utils.getDrops(multiblockTile.getOriginalBlock(), new Builder(builder.getWorld())
					.withParameter(LootParameters.TOOL, builder.get(LootParameters.TOOL))
					.withParameter(LootParameters.POSITION, builder.get(LootParameters.POSITION))
			);
		}
		return Collections.emptyList();
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof MultiblockPartTileEntity)
			return Utils.getPickBlock(((MultiblockPartTileEntity)te).getOriginalBlock(), target, player);
		return ItemStack.EMPTY;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
	{
		//Don't add multiblocks to the creative tab/JEI
	}
}