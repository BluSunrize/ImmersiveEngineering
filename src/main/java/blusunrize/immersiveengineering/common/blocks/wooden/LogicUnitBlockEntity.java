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
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.items.LogicCircuitBoardItem;
import blusunrize.immersiveengineering.common.items.components.DirectNBT;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class LogicUnitBlockEntity extends IEBaseBlockEntity implements IIEInventory, IBlockEntityDrop,
		IInteractionObjectIE<LogicUnitBlockEntity>, IStateBasedDirectional, ILogicCircuitHandler
{
	private final static int SIZE_COLORS = DyeColor.values().length;
	private final static int SIZE_REGISTERS = LogicCircuitRegister.values().length-SIZE_COLORS;
	public static final int NUM_SLOTS = 10;

	private final NonNullList<ItemStack> inventory = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);

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
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		ContainerHelper.loadAllItems(nbt, inventory, provider);
		updateOutputs();
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		ContainerHelper.saveAllItems(nbt, inventory, provider);
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundTag nbt = new CompoundTag();
		ContainerHelper.saveAllItems(nbt, inventory, context.getLevel().registryAccess());
		if(!nbt.isEmpty())
			stack.set(IEDataComponents.LOGIC_UNIT_DATA, new DirectNBT(nbt));
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final var data = ctx.getItemInHand().get(IEDataComponents.LOGIC_UNIT_DATA);
		if(data!=null)
			readCustomNBT(data.tag(), false, ctx.getLevel().registryAccess());
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
	public ArgContainer<LogicUnitBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.LOGIC_UNIT;
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
	public void doGraphicalUpdates()
	{
		this.setChanged();
		updateOutputs();
		markContainingBlockForUpdate(null);
	}

	private void updateOutputs()
	{
		boolean[] outPre = Arrays.copyOf(outputs, SIZE_COLORS);
		Arrays.fill(registers, false);
		Arrays.fill(outputs, false);
		this.inventory.stream()
				.map(s -> s.get(IEDataComponents.CIRCUIT_INSTRUCTION))
				.filter(Objects::nonNull)
				.forEachOrdered(instruction -> instruction.apply(this));
		if(!Arrays.equals(outPre, outputs))
			markConnectorsDirty();
	}

	private void markConnectorsDirty()
	{
		redstoneCaps.values().forEach(RedstoneBundleConnection::markDirty);
	}

	private final Map<Direction, RedstoneBundleConnection> redstoneCaps = new EnumMap<>(Direction.class);

	{
		for(Direction f : DirectionUtils.VALUES)
		{
			RedstoneBundleConnection forSide = new RedstoneBundleConnection()
			{
				@Override
				public void onChange(byte[] externalInputs, Direction side)
				{
					boolean[] sideInputs = inputs.getOrDefault(side, new boolean[SIZE_COLORS]);
					boolean[] preInput = Arrays.copyOf(sideInputs, SIZE_COLORS);
					for(int i = 0; i < SIZE_COLORS; i++)
						sideInputs[i] = externalInputs[i] > 0;
					// if the input changed, update and run circuits
					if(!Arrays.equals(preInput, sideInputs))
					{
						inputs.put(side, sideInputs);
						combinedInputs.reset();
						updateOutputs();
					}
				}

				@Override
				public void updateInput(byte[] signals, Direction side)
				{
					for(DyeColor dye : DyeColor.values())
						if(outputs[dye.getId()])
							signals[dye.getId()] = (byte)15;
				}
			};
			redstoneCaps.put(f, forSide);
		}
	}

	public static void registerCapabilities(BECapabilityRegistrar<LogicUnitBlockEntity> registrar)
	{
		registrar.register(
				CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION,
				(be, side) -> side!=null?be.redstoneCaps.get(side): null
		);
	}

	private final ResettableLazy<boolean[]> combinedInputs = new ResettableLazy<>(() -> {
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