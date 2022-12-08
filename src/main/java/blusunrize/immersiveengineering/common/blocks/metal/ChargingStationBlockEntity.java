/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.client.fx.CustomParticleManager;
import blusunrize.immersiveengineering.client.utils.DistField;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import org.joml.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChargingStationBlockEntity extends IEBaseBlockEntity implements IEClientTickableBE, IEServerTickableBE,
		IIEInventory, IStateBasedDirectional, IBlockBounds, IComparatorOverride, IPlayerInteraction
{
	public AveragingEnergyStorage energyStorage = new AveragingEnergyStorage(32000);
	public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	public final DistField<CustomParticleManager> particles = DistField.client(() -> CustomParticleManager::new);
	private boolean charging = true;
	public int comparatorOutput = 0;
	private final ResettableCapability<IEnergyStorage> energyCap = registerEnergyInput(energyStorage);

	public ChargingStationBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CHARGING_STATION.get(), pos, state);
	}

	@Override
	public void tickClient()
	{
		particles.get().clientTick();
		if(EnergyHelper.isFluxReceiver(inventory.get(0))&&charging)
		{
			float charge = 0;
			float max = EnergyHelper.getMaxEnergyStored(inventory.get(0));
			if(max > 0)
				charge = EnergyHelper.getEnergyStored(inventory.get(0))/max;

			for(int i = 0; i < 3; i++)
			{
				long time = level.getGameTime();
				if(charge >= 1||(time%12 >= i*4&&time%12 <= i*4+2))
				{
					int shift = i-1;
					double x = .5+(getFacing()==Direction.WEST?-.46875: getFacing()==Direction.EAST?.46875: getFacing()==Direction.NORTH?(-.1875*shift): (.1875*shift));
					double y = .25;
					double z = .5+(getFacing()==Direction.NORTH?-.46875: getFacing()==Direction.SOUTH?.46875: getFacing()==Direction.EAST?(-.1875*shift): (.1875*shift));
					particles.get().add(
							new DustParticleOptions(new Vector3f(1-charge, charge, 0), .5f), x, y, z, .25, .25, .25, -1
					);
				}
			}
		}
	}

	@Override
	public void tickServer()
	{
		this.energyStorage.updateAverage();
		if(EnergyHelper.isFluxReceiver(inventory.get(0)))
		{
			if(charging)
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


		if(level.getGameTime()%32==((getBlockPos().getX()^getBlockPos().getZ())&31))
		{
			float charge = 0;
			if(EnergyHelper.isFluxReceiver(inventory.get(0)))
			{
				float max = EnergyHelper.getMaxEnergyStored(inventory.get(0));
				if(max > 0)
					charge = EnergyHelper.getEnergyStored(inventory.get(0))/max;
			}
			int i = (int)(15*charge);
			if(i!=this.comparatorOutput)
			{
				this.comparatorOutput = i;
				level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
			}
		}
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		EnergyHelper.deserializeFrom(energyStorage, nbt);
		inventory.set(0, ItemStack.of(nbt.getCompound("inventory")));
		charging = nbt.getBoolean("charging");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		EnergyHelper.serializeTo(energyStorage, nbt);
		nbt.putBoolean("charging", charging);
		if(!inventory.get(0).isEmpty())
			nbt.put("inventory", inventory.get(0).save(new CompoundTag()));
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	@Override
	public Property<Direction> getFacingProperty()
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
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.box(
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
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private final ResettableCapability<IItemHandler> insertionHandler = registerCapability(new IEInventoryHandler(1, this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==ForgeCapabilities.ENERGY&&(facing==null||facing==Direction.DOWN||facing==getFacing().getOpposite()))
			return energyCap.cast();
		if(capability==ForgeCapabilities.ITEM_HANDLER)
			return insertionHandler.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(isStackValid(0, heldItem))
		{
			ItemStack stored = !inventory.get(0).isEmpty()?inventory.get(0).copy(): ItemStack.EMPTY;
			inventory.set(0, heldItem.copy());
			player.setItemInHand(hand, stored);
			setChanged();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		else if(!inventory.get(0).isEmpty())
		{
			if(!level.isClientSide)
				player.spawnAtLocation(inventory.get(0).copy(), .5f);
			inventory.set(0, ItemStack.EMPTY);
			setChanged();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}
}
