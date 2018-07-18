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
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author BluSunrize - 20.08.2016
 */
public class ConveyorDrop extends ConveyorBasic
{
	@Override
	public void handleInsertion(TileEntity tile, EntityItem entity, EnumFacing facing, ConveyorDirection conDir, double distX, double distZ)
	{
		BlockPos posDown = tile.getPos().down();
		TileEntity inventoryTile = tile.getWorld().getTileEntity(posDown);
		boolean contact = Math.abs(facing.getAxis()==Axis.Z?(tile.getPos().getZ()+.5-entity.posZ): (tile.getPos().getX()+.5-entity.posX)) < .2;

		if(contact&&inventoryTile!=null&&!(inventoryTile instanceof IConveyorTile))
		{
			if(!tile.getWorld().isRemote)
			{
				ItemStack stack = entity.getItem();
				if(!stack.isEmpty())
				{
					ItemStack ret = ApiUtils.insertStackIntoInventory(inventoryTile, stack, EnumFacing.UP);
					if(ret.isEmpty())
						entity.setDead();
					else if(ret.getCount() < stack.getCount())
						entity.setItem(ret);
				}
			}
		}
		else if(contact&&isEmptySpace(tile.getWorld(), posDown, inventoryTile))
		{
			entity.motionX = 0;
			entity.motionZ = 0;
			entity.setPosition(tile.getPos().getX()+.5, tile.getPos().getY()-.5, tile.getPos().getZ()+.5);
			if(!(inventoryTile instanceof IConveyorTile))
				ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile);
		}
		else
			super.handleInsertion(tile, entity, facing, conDir, distX, distZ);
	}

	boolean isEmptySpace(World world, BlockPos pos, TileEntity tile)
	{
		if(world.isAirBlock(pos))
			return true;
		if(tile instanceof IConveyorTile)
			return true;
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof BlockTrapDoor)
			return state.getValue(BlockTrapDoor.OPEN).booleanValue();
		return false;
	}

	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:blocks/conveyor_dropper");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:blocks/conveyor_dropper_off");

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
