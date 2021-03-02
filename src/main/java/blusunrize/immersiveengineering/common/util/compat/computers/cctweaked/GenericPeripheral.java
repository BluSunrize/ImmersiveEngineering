package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GenericPeripheral<T> implements IDynamicPeripheral
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
		return creator.call(ctx, index, luaArgs, object);
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
}
