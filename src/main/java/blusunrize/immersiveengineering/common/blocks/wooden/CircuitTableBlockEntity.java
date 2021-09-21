/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.BEContainer;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CircuitTableBlockEntity extends IEBaseBlockEntity implements IIEInventory, IIEInternalFluxHandler, IStateBasedDirectional,
		IHasDummyBlocks, IInteractionObjectIE<CircuitTableBlockEntity>, IModelOffsetProvider
{
	public static final BlockPos MASTER_POS = BlockPos.ZERO;
	public static final BlockPos DUMMY_POS = new BlockPos(1, 0, 0);
	public static final String[] SLOT_TYPES = new String[]{"backplane", "logic", "solder"};

	private static final int ASSEMBLY_ENERGY = 5000;

	private final FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(32000);
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

	public CircuitTableBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CIRCUIT_TABLE.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		if(!descPacket)
		{
			ContainerHelper.loadAllItems(nbt, inventory);
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		if(!descPacket)
		{
			ContainerHelper.saveAllItems(nbt, inventory);
		}
	}

	public static int getIngredientAmount(LogicCircuitInstruction instruction, int slot)
	{
		switch(slot)
		{
			case 0: // backplane
				return 1;
			case 1: // logic
				return instruction.getOperator().getComplexity();
			case 2: // solder
				return (int)Math.ceil((instruction.getOperator().getComplexity()+instruction.getInputs().length+1)/2f);
		}
		return -1;
	}

	public boolean canAssemble(LogicCircuitInstruction instruction)
	{
		if(this.getFluxStorage().getEnergyStored() < ASSEMBLY_ENERGY)
			return false;
		for(int i = 0; i < SLOT_TYPES.length; i++)
		{
			ItemStack input = this.inventory.get(i);
			if(input.getCount() < getIngredientAmount(instruction, i))
				return false;
		}
		return true;
	}

	public void consumeInputs(LogicCircuitInstruction instruction)
	{
		this.getFluxStorage().extractEnergy(ASSEMBLY_ENERGY, false);
		for(int i = 0; i < SLOT_TYPES.length; i++)
			this.inventory.get(i).shrink(getIngredientAmount(instruction, i));
	}

	private AABB renderAABB;

	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderAABB==null)
			renderAABB = new AABB(getBlockPos().getX()-1, getBlockPos().getY(), getBlockPos().getZ()-1, getBlockPos().getX()+2, getBlockPos().getY()+2, getBlockPos().getZ()+2);
		return renderAABB;
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return true;
	}

	@Override
	public CircuitTableBlockEntity getGuiMaster()
	{
		if(!isDummy())
			return this;
		Direction dummyDir = getFacing().getCounterClockWise();
		BlockEntity tileEntity = level.getBlockEntity(worldPosition.relative(dummyDir));
		if(tileEntity instanceof CircuitTableBlockEntity)
			return (CircuitTableBlockEntity)tileEntity;
		return null;
	}

	@Override
	public BEContainer<CircuitTableBlockEntity, ?> getContainerType()
	{
		return IEContainerTypes.CIRCUIT_TABLE;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.setChanged();
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Nullable
	@Override
	public CircuitTableBlockEntity master()
	{
		if(!isDummy())
			return this;
		// Used to provide tile-dependant drops after breaking
		if(tempMasterBE!=null)
			return (CircuitTableBlockEntity)tempMasterBE;
		Direction dummyDir = isDummy()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		BlockPos masterPos = getBlockPos().relative(dummyDir);
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return (te instanceof CircuitTableBlockEntity)?(CircuitTableBlockEntity)te: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		DeskBlock.placeDummies(getBlockState(), level, worldPosition, ctx);
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		tempMasterBE = master();
		Direction dummyDir = isDummy()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		level.removeBlock(pos.relative(dummyDir), false);
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		if(isDummy())
			return DUMMY_POS;
		else
			return MASTER_POS;
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(this.isDummy())
		{
			CircuitTableBlockEntity te = master();
			if(te!=null)
				return te.getFluxStorage();
		}
		return this.energyStorage;
	}

	@Override
	public void postEnergyTransferUpdate(int energy, boolean simulate)
	{
		if(this.isDummy())
		{
			CircuitTableBlockEntity te = master();
			if(te!=null)
				te.postEnergyTransferUpdate(energy, simulate);
		}
		else if(!simulate&&energy!=0)
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return this.isDummy()&&facing==this.getFacing()?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapper = null;

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(this.isDummy()&&facing==this.getFacing())
		{
			if(wrapper==null||wrapper.side!=this.getFacing())
				wrapper = new IEForgeEnergyWrapper(this, this.getFacing());
			return wrapper;
		}
		return null;
	}
}