/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.BEContainer;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CircuitTableBlockEntity extends IEBaseBlockEntity implements IIEInventory, IStateBasedDirectional,
		IHasDummyBlocks, IInteractionObjectIE<CircuitTableBlockEntity>, IModelOffsetProvider
{
	public static final BlockPos MASTER_POS = BlockPos.ZERO;
	public static final BlockPos DUMMY_POS = new BlockPos(1, 0, 0);
	public static final String[] SLOT_TYPES = new String[]{"backplane", "logic", "solder"};

	private static final int ASSEMBLY_ENERGY = 5000;

	public final MutableEnergyStorage energyStorage = new MutableEnergyStorage(32000);
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

	public CircuitTableBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CIRCUIT_TABLE.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		energyStorage.deserializeNBT(nbt.get("energy"));
		if(!descPacket)
			ContainerHelper.loadAllItems(nbt, inventory);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.put("energy", energyStorage.serializeNBT());
		if(!descPacket)
			ContainerHelper.saveAllItems(nbt, inventory);
	}

	public static int getIngredientAmount(LogicCircuitInstruction instruction, int slot)
	{
		return switch(slot)
				{
					case 0 -> // backplane
							1;
					case 1 -> // logic
							instruction.getOperator().getComplexity();
					case 2 -> // solder
							(int)Math.ceil((instruction.getOperator().getComplexity()+instruction.getInputs().length+1)/2f);
					default -> -1;
				};
	}

	public boolean canAssemble(LogicCircuitInstruction instruction)
	{
		if(energyStorage.getEnergyStored() < ASSEMBLY_ENERGY)
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
		energyStorage.extractEnergy(ASSEMBLY_ENERGY, false);
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
		return master();
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
	public void doGraphicalUpdates()
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

	private final MultiblockCapability<?, IEnergyStorage> energyCap = new MultiblockCapability<>(
			be -> be.energyCap, CircuitTableBlockEntity::master, this, registerEnergyInput(energyStorage)
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==CapabilityEnergy.ENERGY&&(side==null||(side==getFacing()&&isDummy())))
			return energyCap.getAndCast();
		return super.getCapability(cap, side);
	}
}