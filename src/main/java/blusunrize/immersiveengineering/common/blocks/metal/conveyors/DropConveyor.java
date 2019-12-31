/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author BluSunrize - 20.08.2016
 */
public class DropConveyor extends BasicConveyor
{
	public DropConveyor(TileEntity tile)
	{
		super(tile);
	}

	@Override
	public void handleInsertion(ItemEntity entity, ConveyorDirection conDir, double distX, double distZ)
	{
		BlockPos posDown = getTile().getPos().down();
		TileEntity inventoryTile = getTile().getWorld().getTileEntity(posDown);
		boolean contact = Math.abs(getFacing().getAxis()==Axis.Z?(getTile().getPos().getZ()+.5-entity.posZ): (getTile().getPos().getX()+.5-entity.posX)) < .2;

		if(contact&&inventoryTile!=null&&!(inventoryTile instanceof IConveyorTile))
		{
			if(!getTile().getWorld().isRemote)
			{
				ItemStack stack = entity.getItem();
				if(!stack.isEmpty())
				{
					ItemStack ret = ApiUtils.insertStackIntoInventory(inventoryTile, stack, Direction.UP);
					if(ret.isEmpty())
						entity.remove();
					else if(ret.getCount() < stack.getCount())
						entity.setItem(ret);
				}
			}
		}
		else if(contact&&isEmptySpace(getTile().getWorld(), posDown, inventoryTile))
		{
			entity.setMotion(0, entity.getMotion().y, 0);
			entity.setPosition(getTile().getPos().getX()+.5, getTile().getPos().getY()-.5, getTile().getPos().getZ()+.5);
			if(!(inventoryTile instanceof IConveyorTile))
				ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)getTile());
		}
		else
			super.handleInsertion(entity, conDir, distX, distZ);
	}

	boolean isEmptySpace(World world, BlockPos pos, TileEntity tile)
	{
		if(world.isAirBlock(pos))
			return true;
		if(tile instanceof IConveyorTile)
			return true;
		BlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof TrapDoorBlock)
			return state.get(TrapDoorBlock.OPEN);
		return false;
	}

	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:block/conveyor/dropper");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:block/conveyor/dropper_off");

	@Override
	public ResourceLocation getActiveTexture()
	{
		return texture_on;
	}

	@Override
	public ResourceLocation getInactiveTexture()
	{
		return texture_off;
	}
}
