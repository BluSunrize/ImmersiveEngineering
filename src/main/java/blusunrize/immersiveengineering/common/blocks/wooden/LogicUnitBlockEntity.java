/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.ILogicCircuitHandler;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitRegister;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.items.LogicCircuitBoardItem;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.BEContainer;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.*;

public class LogicUnitBlockEntity extends IEBaseBlockEntity implements IIEInventory, IBlockEntityDrop,
		IInteractionObjectIE<LogicUnitBlockEntity>, IStateBasedDirectional, ILogicCircuitHandler
{
	private final static int SIZE_COLORS = DyeColor.values().length;
	private final static int SIZE_REGISTERS = LogicCircuitRegister.values().length-SIZE_COLORS;

	private final NonNullList<ItemStack> inventory = NonNullList.withSize(10, ItemStack.EMPTY);

	private final Map<Direction, boolean[]> inputs = new EnumMap<>(Direction.class);
	private final boolean[] registers = new boolean[SIZE_REGISTERS];
	private final boolean[] outputs = new boolean[SIZE_COLORS];

	public LogicUnitBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.LOGIC_UNIT.get(), pos, state);
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
		return placer.isShiftKeyDown();
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		ContainerHelper.loadAllItems(nbt, inventory);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		ContainerHelper.saveAllItems(nbt, inventory);
	}

	@Override
	public List<ItemStack> getBlockEntityDrop(LootContext context)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundTag nbt = new CompoundTag();
		ContainerHelper.saveAllItems(nbt, inventory);
		if(!nbt.isEmpty())
			stack.setTag(nbt);
		return ImmutableList.of(stack);
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		if(stack.hasTag())
			readCustomNBT(stack.getOrCreateTag(), false);
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return true;
	}

	@Override
	public LogicUnitBlockEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public BEContainer<LogicUnitBlockEntity, ?> getContainerType()
	{
		return IEContainerTypes.LOGIC_UNIT;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return stack.getItem() instanceof LogicCircuitBoardItem;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 1;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.setChanged();
		redstoneCaps.values().forEach(cap -> cap.ifPresent(RedstoneBundleConnection::markDirty));
	}

	private boolean runCircuits()
	{
		boolean[] outPre = Arrays.copyOf(outputs, SIZE_COLORS);
		this.inventory.stream().map(LogicCircuitBoardItem::getInstruction).filter(Objects::nonNull)
				.forEachOrdered(instruction -> {
					instruction.apply(this);
				});
		return !Arrays.equals(outPre, outputs);
	}

	private void markConnectorsDirty()
	{
		redstoneCaps.values().forEach(cap -> cap.ifPresent(RedstoneBundleConnection::markDirty));
	}

	private Map<Direction, LazyOptional<RedstoneBundleConnection>> redstoneCaps = new EnumMap<>(Direction.class);

	{
		for(Direction f : DirectionUtils.VALUES)
		{
			LazyOptional<RedstoneBundleConnection> forSide = registerConstantCap(
					new RedstoneBundleConnection()
					{
						@Override
						public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler, Direction side)
						{
							byte[] foreignInputs = handler.getValuesExcluding(cp);
							boolean[] sideInputs = inputs.getOrDefault(side, new boolean[SIZE_COLORS]);
							boolean[] preInput = Arrays.copyOf(sideInputs, SIZE_COLORS);
							for(int i = 0; i < SIZE_COLORS; i++)
								sideInputs[i] = foreignInputs[i] > 0;
							// if the input changed, update and run circuits
							if(!Arrays.equals(preInput, sideInputs))
							{
								inputs.put(side, sideInputs);
								combinedInputs.reset();
								if(runCircuits())
									markConnectorsDirty();
							}
						}

						@Override
						public void updateInput(byte[] signals, ConnectionPoint cp, Direction side)
						{
							for(DyeColor dye : DyeColor.values())
								if(outputs[dye.getId()])
									signals[dye.getId()] = (byte)15;
						}
					}
			);
			redstoneCaps.put(f, forSide);
		}
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(capability==CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION&&facing!=null)
			return redstoneCaps.get(facing).cast();
		return super.getCapability(capability, facing);
	}

	ResettableLazy<boolean[]> combinedInputs = new ResettableLazy<>(() -> {
		boolean[] ret = new boolean[SIZE_COLORS];
		for(boolean[] side : this.inputs.values())
			for(int i = 0; i < SIZE_COLORS; ++i)
				ret[i] |= side[i];
		return ret;
	});

	@Override
	public boolean getLogicCircuitRegister(LogicCircuitRegister register)
	{
		if(register.ordinal() < SIZE_COLORS)
			return combinedInputs.get()[register.ordinal()];
		return this.registers[register.ordinal()-SIZE_COLORS];
	}

	@Override
	public void setLogicCircuitRegister(LogicCircuitRegister register, boolean state)
	{
		if(register.ordinal() < SIZE_COLORS)
			this.outputs[register.ordinal()] = state;
		else
			this.registers[register.ordinal()-SIZE_COLORS] = state;
	}
}