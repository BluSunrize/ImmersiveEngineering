package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockComponent.StateWrapper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ComparatorManager;
import blusunrize.immersiveengineering.common.blocks.multiblocks.component.MultiblockGui;
import blusunrize.immersiveengineering.common.blocks.multiblocks.component.RedstoneControl;
import blusunrize.immersiveengineering.common.blocks.multiblocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;

public class IEMultiblockBuilder<S extends IMultiblockState>
		extends MultiblockRegistrationBuilder<S, IEMultiblockBuilder<S>>
{
	public IEMultiblockBuilder(IMultiblockLogic<S> logic, String name)
	{
		super(logic, name);
	}

	public IEMultiblockBuilder<S> gui(ArgContainer<IMultiblockContext<S>, ?> menu)
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