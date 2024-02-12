/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.IMachineInterfaceConnection;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.MachineCheckImplementation;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class MachineInterfaceBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE,
		IInteractionObjectIE<MachineInterfaceBlockEntity>, IStateBasedDirectional
{
	private final IEBlockCapabilityCache<IMachineInterfaceConnection> machine = IEBlockCapabilityCaches.forNeighbor(
			IMachineInterfaceConnection.CAPABILITY, this, this::getFacing
	);

	private Collection<MachineInterfaceConfig> configurations = Lists.newArrayList();

	private final boolean[] outputs = new boolean[DyeColor.values().length];

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
			boolean[] outPre = Arrays.copyOf(outputs, outputs.length);
			Arrays.fill(outputs, false);
			configurations.forEach(config -> outputs[config.outputColor.getId()] = config.test());
			if(!Arrays.equals(outPre, outputs))
				redstoneCap.markDirty();
		}
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{

	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{

	}

	@Nullable
	@Override
	public MachineInterfaceBlockEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public ArgContainer<? super MachineInterfaceBlockEntity, ?> getContainerType()
	{
		return null;
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return false;
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
		public void updateInput(byte[] signals, Direction side)
		{
			for(DyeColor dye : DyeColor.values())
				if(outputs[dye.getId()])
					signals[dye.getId()] = (byte)15;
		}
	};

	public static void registerCapabilities(BECapabilityRegistrar<MachineInterfaceBlockEntity> registrar)
	{
		registrar.register(
				CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION,
				(be, side) -> be.redstoneCap
		);
	}

	record MachineInterfaceConfig<T>(MachineCheckImplementation<T> imp, int selectedOption, DyeColor outputColor)
	{
		boolean test()
		{
			return imp().options()[selectedOption()].test(imp().instance());
		}
	}
}
