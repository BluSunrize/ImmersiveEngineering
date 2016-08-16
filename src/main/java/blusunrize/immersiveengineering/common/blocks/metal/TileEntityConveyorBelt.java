package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

public class TileEntityConveyorBelt extends TileEntityIEBase implements IDirectionalTile, IActiveState, IAdvancedCollisionBounds, IHammerInteraction
{
	public boolean transportUp=false;
	public boolean transportDown=false;
	public EnumFacing facing = EnumFacing.NORTH;
	public boolean dropping=false;

	public TileEntityConveyorBelt()
	{
		super();
	}

	public TileEntityConveyorBelt(boolean dropping)
	{
		super();
		this.dropping = dropping;
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(entity!=null && !entity.isDead && !(entity instanceof EntityPlayer && entity.isSneaking()) && entity.posY-getPos().getY()>=0 && entity.posY-getPos().getY()<.5)
		{
			if(world.isBlockIndirectlyGettingPowered(pos)>0)
				return;
			double vBase = 1.15;
			double vX = 0.1 * vBase*facing.getFrontOffsetX();
			double vY = entity.motionY;
			double vZ = 0.1 * vBase*facing.getFrontOffsetZ();

			if(transportUp)
				vY = 0.17D * vBase;
			else if(transportDown)
				vY = -0.07000000000000001D * vBase;

			if(transportUp||transportDown)
				entity.onGround = false;

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
			if (entity.fallDistance<3)
				entity.fallDistance = 0;
			entity.motionX = vX;
			entity.motionY = vY;
			entity.motionZ = vZ;
			double distX = Math.abs(getPos().offset(facing).getX()+.5-entity.posX);
			double distZ = Math.abs(getPos().offset(facing).getZ()+.5-entity.posZ);
			double treshold = .9;
			boolean contact = facing.getAxis()==Axis.Z?distZ<treshold: distX<treshold;
			if (contact&&transportUp&&!world.getBlockState(getPos().offset(facing).up()).isFullBlock())
			{
				double move = .4;
				entity.setPosition(entity.posX+move*facing.getFrontOffsetX(), entity.posY+1.75*move, entity.posZ+move*facing.getFrontOffsetZ());
			}
			if(entity instanceof EntityItem)
			{
				((EntityItem)entity).setNoDespawn();
				TileEntity inventoryTile;
				EnumFacing inventoryDir = facing;
				if(dropping)
				{
					inventoryTile = world.getTileEntity(getPos().add(0,-1,0));
					contact = Math.abs(facing.getAxis()==Axis.Z?(getPos().getZ()+.5-entity.posZ):(getPos().getX()+.5-entity.posX))<.2;
					inventoryDir = EnumFacing.DOWN;
				}
				else
				{
					inventoryTile = world.getTileEntity(getPos().offset(inventoryDir).add(0,(transportUp?1: transportDown?-1: 0),0));
					contact = facing.getAxis()==Axis.Z?distZ<.7: distX<.7;
				}
				if(!world.isRemote)
				{
					if(contact && inventoryTile!=null && !(inventoryTile instanceof TileEntityConveyorBelt))
					{
						ItemStack stack = ((EntityItem)entity).getEntityItem();
						if(stack!=null)
						{
							ItemStack ret = Utils.insertStackIntoInventory(inventoryTile, stack, inventoryDir.getOpposite());
							if(ret==null)
								entity.setDead();
							else if(ret.stackSize<stack.stackSize)
								((EntityItem)entity).setEntityItemStack(ret);
						}
					}
					else if(dropping && contact && world.isAirBlock(getPos().add(0,-1,0)))
					{
						entity.motionX = 0;
						entity.motionZ = 0;
						entity.setPosition(getPos().getX()+.5, getPos().getY()-.5, getPos().getZ()+.5);
					}
				}
			}
		}
	}

	//i==0: Left, i==1: Right
	public boolean renderWall(int i)
	{
		if(transportDown||transportUp)
			return false;
		EnumFacing side = i==0?facing.rotateYCCW():facing.rotateY();
		BlockPos pos = getPos().offset(side);
		TileEntity te = worldObj.getTileEntity(pos);
		if(te instanceof TileEntityConveyorBelt && ((TileEntityConveyorBelt)te).facing==side.getOpposite())
			return false;
		else 
		{
			te = worldObj.getTileEntity(pos.add(0,-1,0));
			if(te instanceof TileEntityConveyorBelt && ((TileEntityConveyorBelt)te).facing==side.getOpposite() && ((TileEntityConveyorBelt)te).transportUp)
				return false;

			te = worldObj.getTileEntity(pos.add(0,1,0));
			if(te instanceof TileEntityConveyorBelt && ((TileEntityConveyorBelt)te).facing==side.getOpposite() && ((TileEntityConveyorBelt)te).transportDown)
				return false;
		}
		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		transportUp = nbt.getBoolean("transportUp");
		transportDown = nbt.getBoolean("transportDown");
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		dropping = nbt.getBoolean("dropping");
		if(descPacket && worldObj!=null)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("transportUp", transportUp);
		nbt.setBoolean("transportDown", transportDown);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("dropping", dropping);
	}

	@Override
	public EnumFacing getFacing()
	{
		return this.facing;
	}
	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}
	@Override
	public int getFacingLimitation()
	{
		return 5;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return !entity.isSneaking();
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			if(transportUp)
			{
				transportUp = false;
				transportDown = true;
			}
			else if(transportDown)
				transportDown = false;
			else
				transportUp = true;
			this.markDirty();
			this.markContainingBlockForUpdate(null);
			worldObj.addBlockEvent(getPos(), this.getBlockType(), 0, 0);
			return true;
		}
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0,0,0,1,transportUp||transportDown?1.125f:.125f,1};
	}
	static AxisAlignedBB COLISIONBB = new AxisAlignedBB(0,0,0,1,.05F,1);
	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return Lists.newArrayList(COLISIONBB);
	}

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return IEProperties.BOOLEANS[0];
	}
	@Override
	public boolean getIsActive()
	{
		return worldObj.isBlockIndirectlyGettingPowered(pos)<=0;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}
	IItemHandler insertionHandler = new ConveyorInventoryHandler(this);
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T)insertionHandler;
		return super.getCapability(capability, facing);
	}

	public static class ConveyorInventoryHandler implements IItemHandlerModifiable
	{
		TileEntityConveyorBelt conveyor;
		public ConveyorInventoryHandler(TileEntityConveyorBelt conveyor)
		{
			this.conveyor = conveyor;
		}

		@Override
		public int getSlots()
		{
			return 1;
		}
		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return null;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(!simulate)
				conveyor.getWorld().spawnEntityInWorld(new EntityItem(conveyor.getWorld(),conveyor.getPos().getX()+.5,conveyor.getPos().getY()+.25,conveyor.getPos().getZ()+.5, stack.copy()));
			return null;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return null;
		}
		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}
}