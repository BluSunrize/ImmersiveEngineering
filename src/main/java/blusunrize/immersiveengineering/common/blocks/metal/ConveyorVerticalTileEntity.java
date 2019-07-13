/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class ConveyorVerticalTileEntity extends ConveyorBeltTileEntity
{
	public ConveyorVerticalTileEntity(ResourceLocation typeName)
	{
		super(typeName);
	}

	private CapabilityReference<IItemHandler> output = CapabilityReference.forNeighbor(this, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(entity!=null&&entity.isAlive())
		{
			if(world.getRedstonePowerFromNeighbors(pos) > 0)
				return;

			double vBase = entity instanceof LivingEntity?1.5: 1.15;
			double distY = Math.abs(getPos().add(0, 1, 0).getY()+.5-entity.posY);
			double treshold = .9;
			boolean contact = distY < treshold;

			double vX = entity.motionX;
			double vY = 0.1*vBase;
			double vZ = entity.motionZ;
			if(entity.motionY < 0)
				vY += entity.motionY*.9;

			if(!(entity instanceof PlayerEntity))
			{
				vX = 0.05*this.facing.getXOffset();
				vZ = 0.05*this.facing.getZOffset();
				if(facing==Direction.WEST||facing==Direction.EAST)
				{
					if(entity.posZ > getPos().getZ()+0.65D)
						vZ = -0.1D*vBase;
					else if(entity.posZ < getPos().getZ()+0.35D)
						vZ = 0.1D*vBase;
				}
				else if(facing==Direction.NORTH||facing==Direction.SOUTH)
				{
					if(entity.posX > getPos().getX()+0.65D)
						vX = -0.1D*vBase;
					else if(entity.posX < getPos().getX()+0.35D)
						vX = 0.1D*vBase;
				}
			}
			//Little boost at the top of a conveyor to help players and minecarts to get off
			if(contact&&!(world.getTileEntity(getPos().add(0, 1, 0)) instanceof ConveyorVerticalTileEntity))
				vY *= 2.25;
			entity.onGround = false;
			if(entity.fallDistance < 3)
				entity.fallDistance = 0;
			else
				entity.fallDistance *= .9;
			entity.motionX = vX;
			entity.motionY = vY;
			entity.motionZ = vZ;
			if(entity instanceof ItemEntity)
			{
				((ItemEntity)entity).setNoDespawn();
				TileEntity inventoryTile;
				inventoryTile = world.getTileEntity(getPos().add(0, 1, 0));
				if(!world.isRemote)
				{
					if(contact&&inventoryTile!=null&&!(inventoryTile instanceof ConveyorBeltTileEntity))
					{
						ItemStack stack = ((ItemEntity)entity).getItem();
						if(!stack.isEmpty())
						{
							ItemStack ret = Utils.insertStackIntoInventory(output, stack, false);
							if(ret.isEmpty())
								entity.remove();
							else if(ret.getCount() < stack.getCount())
								((ItemEntity)entity).setItem(ret);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return true;
	}

	@Override
	public float[] getBlockBounds()
	{
		float minX = facing==Direction.EAST?.875f: 0;
		float maxX = facing==Direction.WEST?.125f: 1;
		float minZ = facing==Direction.SOUTH?.875f: 0;
		float maxZ = facing==Direction.NORTH?.125f: 1;
		return new float[]{minX, 0, minZ, maxX, 1, maxZ};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return null;
	}
}