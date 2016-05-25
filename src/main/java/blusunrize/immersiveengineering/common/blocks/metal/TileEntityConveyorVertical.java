package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class TileEntityConveyorVertical extends TileEntityConveyorBelt
{
	public TileEntityConveyorVertical()
	{
		super(false);
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(entity!=null && !entity.isDead)
		{
			if(world.isBlockIndirectlyGettingPowered(pos)>0)
				return;

			double vBase = entity instanceof EntityLivingBase?1.5:1.15;
			double distY = Math.abs(getPos().add(0,1,0).getY()+.5-entity.posY);
			double treshold = .9;
			boolean contact = distY<treshold;

			double vX = entity.motionX;
			double vY = 0.1 * vBase;
			double vZ = entity.motionZ; 

			if(!(entity instanceof EntityPlayer))
			{
				vX = 0.05 * this.facing.getFrontOffsetX();
				vZ = 0.05 * this.facing.getFrontOffsetZ();
				if(facing==EnumFacing.WEST || facing==EnumFacing.EAST)
				{
					if(entity.posZ > getPos().getZ()+0.65D)
						vZ = -0.1D * vBase;
					else if(entity.posZ < getPos().getZ()+0.35D)
						vZ = 0.1D * vBase;
				}
				else if(facing==EnumFacing.NORTH || facing==EnumFacing.SOUTH)
				{
					if(entity.posX > getPos().getX()+0.65D)
						vX = -0.1D * vBase;
					else if(entity.posX < getPos().getX()+0.35D)
						vX = 0.1D * vBase;
				}
			}
			//Little boost at the top of a conveyor to help players and minecarts to get off
			if(contact && !(world.getTileEntity(getPos().add(0,1,0)) instanceof TileEntityConveyorVertical))
				vY *= 2.25;

			entity.onGround = false;
			entity.motionX = vX;
			entity.motionY = vY;
			entity.motionZ = vZ;
			if(entity instanceof EntityItem)
			{
				((EntityItem)entity).setNoDespawn();
				TileEntity inventoryTile;
				inventoryTile = world.getTileEntity(getPos().add(0,1,0));
				if(!world.isRemote)
				{
					if(contact && inventoryTile!=null && !(inventoryTile instanceof TileEntityConveyorBelt))
					{
						ItemStack stack = ((EntityItem)entity).getEntityItem();
						if(stack!=null)
						{
							ItemStack ret = Utils.insertStackIntoInventory(inventoryTile, stack, EnumFacing.DOWN);
							if(ret==null)
								entity.setDead();
							else if(ret.stackSize<stack.stackSize)
								((EntityItem)entity).setEntityItemStack(ret);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean renderWall(int i)
	{
		return false;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public float[] getBlockBounds()
	{
		float minX = facing==EnumFacing.EAST?.875f:0;
		float maxX = facing==EnumFacing.WEST?.125f:1;
		float minZ = facing==EnumFacing.SOUTH?.875f:0;
		float maxZ = facing==EnumFacing.NORTH?.125f:1;
		return new float[]{minX,0,minZ,maxX,1,maxZ};
	}
	@Override
	public float[] getSpecialCollisionBounds()
	{
		return null;
	}
}