/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent.StateWrapper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.multiblocks.component.MultiblockGui;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.MultiblockContainer;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;

public class IEMultiblockBuilder<S extends IMultiblockState>
		extends MultiblockRegistrationBuilder<S, IEMultiblockBuilder<S>>
{
	public IEMultiblockBuilder(IMultiblockLogic<S> logic, String name)
	{
		super(logic, ImmersiveEngineering.rl(name));
	}

	public IEMultiblockBuilder<S> gui(MultiblockContainer<S, ?> menu)
	{
		return component(new MultiblockGui<>(menu));
	}

	public IEMultiblockBuilder<S> redstoneNoComputer(StateWrapper<S, RSState> getState, BlockPos... positions)
	{
		redstoneAware();
		return selfWrappingComponent(new RedstoneControl<>(getState, false, positions));
	}

	public IEMultiblockBuilder<S> redstone(StateWrapper<S, RSState> getState, BlockPos... positions)
	{
		redstoneAware();
		return selfWrappingComponent(new RedstoneControl<>(getState, positions));
	}

	public IEMultiblockBuilder<S> comparator(ComparatorManager<S> comparator)
	{
		withComparator();
		return super.selfWrappingComponent(comparator);
	}

	@Override
	public <CS, C extends IMultiblockComponent<CS> & StateWrapper<S, CS>>
	IEMultiblockBuilder<S> selfWrappingComponent(C extraComponent)
	{
		Preconditions.checkArgument(!(extraComponent instanceof ComparatorManager<?>));
		return super.selfWrappingComponent(extraComponent);
	}

	@Override
	protected IEMultiblockBuilder<S> self()
	{
		return this;
	}
}
