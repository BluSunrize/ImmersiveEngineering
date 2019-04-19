/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPost extends BlockIEBase
{
	public BlockPost(String name, Properties blockProps)
	{
		super(name, blockProps, ItemBlockIEBase.class, IEProperties.MULTIBLOCKSLAVE);
		setNotNormalBlock();
		setMobility(EnumPushReaction.BLOCK);
		lightOpacity = 0;
	}

	@Override
	public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune)
	{
	}

	@Override
	public void onReplaced(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, IBlockState newState, boolean moving)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityPost&&!((TileEntityPost)tileEntity).isDummy())
			spawnAsEntity(world, pos, new ItemStack(this));
		super.onReplaced(state, world, pos, newState, moving);
	}

	@Override
	public boolean canIEBlockBePlaced(@Nonnull IBlockState newState, BlockItemUseContext context)
	{
		BlockPos startingPos = context.getPos();
		World world = context.getWorld();
		for(int hh = 1; hh <= 3; hh++)
		{
			BlockPos pos = startingPos.up(hh);
			BlockItemUseContext dummyContext = new BlockItemUseContext(
					context.getWorld(), context.getPlayer(), context.getItem(), pos, context.getFace(),
					context.getHitX(), context.getHitY(), context.getHitZ()
			);
			if(World.isOutsideBuildHeight(pos)||
					!world.getBlockState(pos).getBlock().isReplaceable(newState, dummyContext))
				return false;
		}
		return true;
	}

	@Override
	public boolean isLadder(IBlockState state, IWorldReader world, BlockPos pos, EntityLivingBase entity)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(IBlockState state, IBlockReader world)
	{
		return new TileEntityPost();
	}
}
