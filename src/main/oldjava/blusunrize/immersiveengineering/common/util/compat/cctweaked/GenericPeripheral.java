/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.ExtraComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.utils.ComputerControlState;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControllable;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class GenericPeripheral<T extends BlockEntity> implements IDynamicPeripheral
{
	private final PeripheralCreator<T> creator;
	private final T object;

	public GenericPeripheral(PeripheralCreator<T> creator, T object)
	{
		this.creator = creator;
		this.object = object;
	}

	@Nonnull
	@Override
	public String[] getMethodNames()
	{
		return creator.getMethodNames();
	}

	@Nonnull
	@Override
	public MethodResult callMethod(
			@Nonnull IComputerAccess computerAccess, @Nonnull ILuaContext ctx, int index, @Nonnull IArguments luaArgs
	) throws LuaException
	{
		return creator.call(computerAccess, ctx, index, luaArgs, object);
	}

	@Nonnull
	@Override
	public String getType()
	{
		return creator.getName();
	}

	@Override
	public boolean equals(@Nullable IPeripheral other)
	{
		if(other==null) return false;
		if(other==this) return true;
		if(other.getClass()!=this.getClass()) return false;
		GenericPeripheral<?> otherGeneric = (GenericPeripheral<?>)other;
		return this.creator==otherGeneric.creator&&this.object==otherGeneric.object;
	}

	@Override
	public void attach(@Nonnull IComputerAccess computer)
	{
		forControlStates(ComputerControlState::addReference);
	}

	@Override
	public void detach(@Nonnull IComputerAccess computer)
	{
		forControlStates(ComputerControlState::removeReference);
	}

	private void forControlStates(Consumer<ComputerControlState> runner)
	{
		if(object instanceof ComputerControllable controllable)
			controllable.getAllComputerControlStates().forEach(runner);
		if(!(object instanceof IMultiblockBE<?> multiblockBE))
			return;
		ApiUtils.addFutureServerTask(object.getLevel(), () -> {
			final IMultiblockBEHelper<?> helper = multiblockBE.getHelper();
			for(final ExtraComponent<?, ?> component : helper.getMultiblock().extraComponents())
				if(component.makeWrapper() instanceof RedstoneControl<?> control)
					callOn(runner, control, helper.getState());
		});
	}

	private <S>
	void callOn(Consumer<ComputerControlState> runner, RedstoneControl<S> control, Object state)
	{
		if(state!=null)
		{
			final RSState rsState = control.wrapState((S)state);
			runner.accept(rsState.getComputerControlState());
		}
	}
}
