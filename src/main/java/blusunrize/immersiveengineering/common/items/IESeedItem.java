/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.fml.DeferredWorkQueue;

import javax.annotation.Nonnull;

public class IESeedItem extends IEBaseItem implements IPlantable
{
	private Block cropBlock;

	public IESeedItem(Block cropBlock)
	{
		super("seed", new Properties());
		this.cropBlock = cropBlock;
		DeferredWorkQueue.runLater(
				() -> ComposterBlock.CHANCES.putIfAbsent(this, 0.3f)
		);
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		ItemStack stack = context.getItem();
		PlayerEntity player = context.getPlayer();
		Direction side = context.getFace();
		if(side!=Direction.UP)
			return ActionResultType.PASS;
		else if(player!=null&&player.canPlayerEdit(pos, side, stack)&&player.canPlayerEdit(pos.add(0, 1, 0), side, stack))
		{
			BlockState state = world.getBlockState(pos);
			if(state.getBlock().canSustainPlant(state, world, pos, Direction.UP, this)&&world.isAirBlock(pos.add(0, 1, 0)))
			{
				world.setBlockState(pos.add(0, 1, 0), this.cropBlock.getDefaultState());
				stack.shrink(1);
				return ActionResultType.SUCCESS;
			}
			else
				return ActionResultType.PASS;
		}
		else
			return ActionResultType.PASS;
	}

	@Override
	public PlantType getPlantType(IBlockReader world, BlockPos pos)
	{
		return ((IPlantable)cropBlock).getPlantType(world, pos);
	}

	@Override
	public BlockState getPlant(IBlockReader world, BlockPos pos)
	{
		return cropBlock.getDefaultState();
	}
}