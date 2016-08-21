package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;

/**
 * @author BluSunrize - 20.08.2016
 */
public class ConveyorDrop extends ConveyorBasic
{
	@Override
	public void handleInsertion(TileEntity tile, EntityItem entity, EnumFacing facing, ConveyorDirection conDir, double distX, double distZ)
	{
		TileEntity inventoryTile = tile.getWorld().getTileEntity(tile.getPos().add(0, -1, 0));
		boolean contact = Math.abs(facing.getAxis() == Axis.Z ? (tile.getPos().getZ() + .5 - entity.posZ) : (tile.getPos().getX() + .5 - entity.posX)) < .2;

		if(contact && inventoryTile != null && !(inventoryTile instanceof IConveyorTile))
		{
			if(!tile.getWorld().isRemote)
			{
				ItemStack stack = entity.getEntityItem();
				if(stack != null)
				{
					ItemStack ret = ApiUtils.insertStackIntoInventory(inventoryTile, stack, EnumFacing.UP);
					if(ret == null)
						entity.setDead();
					else if(ret.stackSize < stack.stackSize)
						entity.setEntityItemStack(ret);
				}
			}
		} else if(contact && tile.getWorld().isAirBlock(tile.getPos().add(0, -1, 0)))
		{
			entity.motionX = 0;
			entity.motionZ = 0;
			entity.setPosition(tile.getPos().getX() + .5, tile.getPos().getY() - .5, tile.getPos().getZ() + .5);
		}
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
