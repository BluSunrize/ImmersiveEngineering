/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChargingStationTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInternalFluxHandler, IIEInventory,
		IStateBasedDirectional, IBlockBounds, IComparatorOverride, IPlayerInteraction
{
	public static TileEntityType<ChargingStationTileEntity> TYPE;

	public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(32000);
	public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	private boolean charging = true;
	public int comparatorOutput = 0;

	public ChargingStationTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		if(EnergyHelper.isFluxReceiver(inventory.get(0)))
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
						double x = getPos().getX()+.5+(getFacing()==Direction.WEST?-.46875: getFacing()==Direction.EAST?.46875: getFacing()==Direction.NORTH?(-.1875*shift): (.1875*shift));
						double y = getPos().getY()+.25;
						double z = getPos().getZ()+.5+(getFacing()==Direction.NORTH?-.46875: getFacing()==Direction.SOUTH?.46875: getFacing()==Direction.EAST?(-.1875*shift): (.1875*shift));
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
				int stored = EnergyHelper.getEnergyStored(inventory.get(0));
				int max = EnergyHelper.getMaxEnergyStored(inventory.get(0));
				int space = max-stored;
				if(space > 0)
				{
					int energyDec = (10*stored)/max;
					int insert = Math.min(space, Math.max(energyStorage.getAverageInsertion(), IEServerConfig.MACHINES.charger_consumption.get()));
					int accepted = Math.min(EnergyHelper.insertFlux(inventory.get(0), insert, true), this.energyStorage.extractEnergy(insert, true));
					if((accepted = this.energyStorage.extractEnergy(accepted, false)) > 0)
						stored += EnergyHelper.insertFlux(inventory.get(0), accepted, false);
					int energyDecNew = (10*stored)/max;
					if(energyDec!=energyDecNew)
						this.markContainingBlockForUpdate(null);
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
			if(EnergyHelper.isFluxReceiver(inventory.get(0)))
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
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		inventory.set(0, ItemStack.read(nbt.getCompound("inventory")));
		charging = nbt.getBoolean("charging");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.putBoolean("charging", charging);
		if(!inventory.get(0).isEmpty())
			nbt.put("inventory", inventory.get(0).write(new CompoundNBT()));
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
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return facing==Direction.DOWN||facing==this.getFacing().getOpposite()?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapperDown = new IEForgeEnergyWrapper(this, Direction.DOWN);
	IEForgeEnergyWrapper wrapperDir = null;

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing==Direction.DOWN)
			return wrapperDown;
		else if(facing==this.getFacing().getOpposite())
		{
			if(wrapperDir==null||wrapperDir.side!=this.getFacing().getOpposite())
				wrapperDir = new IEForgeEnergyWrapper(this, this.getFacing().getOpposite());
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
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return VoxelShapes.create(
				getFacing().getAxis()==Axis.X?0: .125f, 0, getFacing().getAxis()==Axis.Z?0: .125f,
				getFacing().getAxis()==Axis.X?1: .875f, 1, getFacing().getAxis()==Axis.Z?1: .875f
		);
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return EnergyHelper.isFluxReceiver(stack);
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
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return insertionHandler.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(isStackValid(0, heldItem))
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
