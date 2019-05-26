/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityChargingStation extends TileEntityIEBase implements ITickable, IIEInternalFluxHandler, IIEInventory,
		IDirectionalTile, IBlockBounds, IComparatorOverride, IPlayerInteraction
{
	public static TileEntityType<TileEntityChargingStation> TYPE;

	public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(32000);
	public EnumFacing facing = EnumFacing.NORTH;
	public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	private boolean charging = true;
	public int comparatorOutput = 0;

	public TileEntityChargingStation()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(EnergyHelper.isFluxItem(inventory.get(0)))
		{
			if(world.isRemote&&charging)
			{
				float charge = 0;
				float max = EnergyHelper.getMaxEnergyStored(inventory.get(0));
				if(max > 0)
					charge = EnergyHelper.getEnergyStored(inventory.get(0))/max;

				for(int i = 0; i < 3; i++)
				{
					long time = world.getGameTime();
					if(charge >= 1||(time%12 >= i*4&&time%12 <= i*4+2))
					{
						int shift = i-1;
						double x = getPos().getX()+.5+(facing==EnumFacing.WEST?-.46875: facing==EnumFacing.EAST?.46875: facing==EnumFacing.NORTH?(-.1875*shift): (.1875*shift));
						double y = getPos().getY()+.25;
						double z = getPos().getZ()+.5+(facing==EnumFacing.NORTH?-.46875: facing==EnumFacing.SOUTH?.46875: facing==EnumFacing.EAST?(-.1875*shift): (.1875*shift));
						ImmersiveEngineering.proxy.spawnRedstoneFX(world, x, y, z, .25, .25, .25, .5f, 1-charge, charge, 0);
					}
				}
			}
			else if(charging)
			{
				if(energyStorage.getEnergyStored()==0)
				{
					charging = false;
					this.markContainingBlockForUpdate(null);
					return;
				}
				if(EnergyHelper.isFluxItem(inventory.get(0)))
				{
					int stored = EnergyHelper.getEnergyStored(inventory.get(0));
					int max = EnergyHelper.getMaxEnergyStored(inventory.get(0));
					int space = max-stored;
					if(space > 0)
					{
						int energyDec = (10*stored)/max;
						int insert = Math.min(space, Math.max(energyStorage.getAverageInsertion(), IEConfig.Machines.charger_consumption));
						int accepted = Math.min(EnergyHelper.insertFlux(inventory.get(0), insert, true), this.energyStorage.extractEnergy(insert, true));
						if((accepted = this.energyStorage.extractEnergy(accepted, false)) > 0)
							stored += EnergyHelper.insertFlux(inventory.get(0), accepted, false);
						int energyDecNew = (10*stored)/max;
						if(energyDec!=energyDecNew)
							this.markContainingBlockForUpdate(null);
					}
				}
			}
			else if(energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored()*.95)
			{
				charging = true;
				this.markContainingBlockForUpdate(null);
			}
		}


		if(!world.isRemote&&world.getGameTime()%32==((getPos().getX()^getPos().getZ())&31))
		{
			float charge = 0;
			if(EnergyHelper.isFluxItem(inventory.get(0)))
			{
				float max = EnergyHelper.getMaxEnergyStored(inventory.get(0));
				if(max > 0)
					charge = EnergyHelper.getEnergyStored(inventory.get(0))/max;
			}
			//				else
			//					charge = (float)(IC2Helper.getCurrentItemCharge(inventory)/IC2Helper.getMaxItemCharge(inventory));
			int i = (int)(15*charge);
			if(i!=this.comparatorOutput)
			{
				this.comparatorOutput = i;
				world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
			}
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		facing = EnumFacing.byIndex(nbt.getInt("facing"));
		inventory.set(0, ItemStack.read(nbt.getCompound("inventory")));
		charging = nbt.getBoolean("charging");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.setInt("facing", facing.ordinal());
		nbt.setBoolean("charging", charging);
		if(!inventory.get(0).isEmpty())
			nbt.setTag("inventory", inventory.get(0).write(new NBTTagCompound()));
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		return energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		return facing==EnumFacing.DOWN||facing==this.facing.getOpposite()?SideConfig.INPUT: SideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapperDown = new IEForgeEnergyWrapper(this, EnumFacing.DOWN);
	IEForgeEnergyWrapper wrapperDir = new IEForgeEnergyWrapper(this, facing.getOpposite());

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(facing==EnumFacing.DOWN)
			return wrapperDown;
		else if(facing==this.facing.getOpposite())
		{
			if(wrapperDir.side!=this.facing.getOpposite())
				wrapperDir = new IEForgeEnergyWrapper(this, this.facing.getOpposite());
			return wrapperDir;
		}
		return null;
	}

	@Override
	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{facing.getAxis()==Axis.X?0: .125f, 0, facing.getAxis()==Axis.Z?0: .125f, facing.getAxis()==Axis.X?1: .875f, 1, facing.getAxis()==Axis.Z?1: .875f};
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return EnergyHelper.isFluxItem(stack);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 1;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(new IEInventoryHandler(1, this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return insertionHandler.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(EnergyHelper.isFluxItem(heldItem))
		{
			ItemStack stored = !inventory.get(0).isEmpty()?inventory.get(0).copy(): ItemStack.EMPTY;
			inventory.set(0, heldItem.copy());
			player.setHeldItem(hand, stored);
			markDirty();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		else if(!inventory.get(0).isEmpty())
		{
			if(!world.isRemote)
				player.entityDropItem(inventory.get(0).copy(), .5f);
			inventory.set(0, ItemStack.EMPTY);
			markDirty();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}
}
