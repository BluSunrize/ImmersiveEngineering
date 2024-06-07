/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.IMachineInterfaceConnection;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.MachineCheckImplementation;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IRedstoneOutput;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MachineInterfaceBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE,
		IPlayerInteraction, IStateBasedDirectional, IRedstoneOutput
{
	public final IEBlockCapabilityCache<IMachineInterfaceConnection> machine = IEBlockCapabilityCaches.forNeighbor(
			IMachineInterfaceConnection.CAPABILITY, this, this::getFacing
	);

	public List<MachineInterfaceConfig<?>> configurations = new ArrayList<>();

	private final int[] outputs = new int[DyeColor.values().length];

	public DyeColor inputColor = DyeColor.WHITE;
	private byte inputSignalStrength = 0;

	public MachineInterfaceBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.MACHINE_INTERFACE.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		IMachineInterfaceConnection machineCapability = machine.getCapability();
		if(machineCapability!=null)
		{
			int[] outPre = Arrays.copyOf(outputs, outputs.length);
			Arrays.fill(outputs, 0);
			configurations.forEach(config -> outputs[config.outputColor.getId()] = config.getValue(machineCapability));
			if(!Arrays.equals(outPre, outputs))
				redstoneCap.markDirty();
		}
		else if(!this.configurations.isEmpty())
		{
			this.configurations.clear();
			this.markChunkDirty();
			this.markContainingBlockForUpdate(getBlockState());
		}
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		ListTag list = nbt.getList("configurations", Tag.TAG_COMPOUND);
		configurations.clear();
		for(int i = 0; i < list.size(); i++)
			configurations.add(MachineInterfaceConfig.readFromNBT(list.getCompound(i)));
		inputColor = DyeColor.byId(nbt.getInt("inputColor"));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		ListTag list = new ListTag();
		configurations.forEach(conf -> list.add(conf.writeToNBT()));
		nbt.put("configurations", list);
		nbt.putInt("inputColor", inputColor.getId());
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("configuration"))
		{
			int idx = message.getInt("idx");
			if(idx >= this.configurations.size())
				this.configurations.add(MachineInterfaceConfig.readFromNBT(message.getCompound("configuration")));
			else
				this.configurations.set(idx, MachineInterfaceConfig.readFromNBT(message.getCompound("configuration")));
		}
		else if(message.getBoolean("delete"))
			this.configurations.remove(message.getInt("idx"));
		else if(message.contains("inputColor"))
			this.inputColor = DyeColor.byId(message.getInt("inputColor"));
		setChanged();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(getLevelNonnull().isClientSide)
			ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_MachineInterface, this);
		return true;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_PREFER_SIDE;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}


	private final RedstoneBundleConnection redstoneCap = new RedstoneBundleConnection()
	{
		@Override
		public void onChange(byte[] externalInputs, Direction side)
		{
			if(externalInputs[inputColor.getId()]!=inputSignalStrength)
			{
				inputSignalStrength = externalInputs[inputColor.getId()];
				markChunkDirty();
				markContainingBlockForUpdate(getBlockState());
			}
		}

		@Override
		public void updateInput(byte[] signals, Direction side)
		{
			for(DyeColor dye : DyeColor.values())
				signals[dye.getId()] = (byte)outputs[dye.getId()];
		}
	};

	public static void registerCapabilities(BECapabilityRegistrar<MachineInterfaceBlockEntity> registrar)
	{
		registrar.register(
				CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION,
				(be, side) -> be.redstoneCap
		);
	}

	@Override
	public int getStrongRSOutput(Direction side)
	{
		return canConnectRedstone(side)?inputSignalStrength: 0;
	}

	@Override
	public boolean canConnectRedstone(Direction side)
	{
		return side==getFacing().getOpposite();
	}

	public static final class MachineInterfaceConfig<T>
	{
		private int selectedCheck;
		private int selectedOption;
		private DyeColor outputColor;

		public MachineInterfaceConfig(int selectedCheck, int selectedOption, DyeColor outputColor)
		{
			this.selectedCheck = selectedCheck;
			this.selectedOption = selectedOption;
			this.outputColor = outputColor;
		}

		@SuppressWarnings("unchecked")
		int getValue(IMachineInterfaceConnection connection)
		{
			MachineCheckImplementation<T>[] checks = (MachineCheckImplementation<T>[])connection.getAvailableChecks();
			if(selectedCheck < checks.length&&selectedOption < checks[selectedCheck].options().length)
				return checks[selectedCheck].options()[selectedOption].getValue(checks[selectedCheck].instance());
			return 0;
		}

		public CompoundTag writeToNBT()
		{
			CompoundTag nbt = new CompoundTag();
			nbt.putInt("selectedCheck", selectedCheck);
			nbt.putInt("selectedOption", selectedOption);
			nbt.putInt("outputColor", outputColor.getId());
			return nbt;
		}

		static MachineInterfaceConfig<?> readFromNBT(CompoundTag nbt)
		{
			return new MachineInterfaceConfig<>(
					nbt.getInt("selectedCheck"),
					nbt.getInt("selectedOption"),
					DyeColor.byId(nbt.getInt("outputColor"))
			);
		}

		public int getSelectedCheck()
		{
			return selectedCheck;
		}

		public MachineInterfaceConfig<T> setSelectedCheck(int selectedCheck)
		{
			this.selectedCheck = selectedCheck;
			return this;
		}

		public int getSelectedOption()
		{
			return selectedOption;
		}

		public MachineInterfaceConfig<T> setSelectedOption(int selectedOption)
		{
			this.selectedOption = selectedOption;
			return this;
		}

		public DyeColor getOutputColor()
		{
			return outputColor;
		}

		public MachineInterfaceConfig<T> setOutputColor(DyeColor outputColor)
		{
			this.outputColor = outputColor;
			return this;
		}
	}
}
