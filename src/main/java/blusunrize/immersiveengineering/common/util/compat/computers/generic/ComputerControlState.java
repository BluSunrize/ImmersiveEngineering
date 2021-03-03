package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import java.util.function.BooleanSupplier;

public class ComputerControlState
{
	public static final ComputerControlState NO_COMPUTER = new ComputerControlState(() -> false, false);

	private final BooleanSupplier isStillAttached;
	private final boolean isEnabled;

	public ComputerControlState(BooleanSupplier isStillAttached, boolean isEnabled)
	{
		this.isStillAttached = isStillAttached;
		this.isEnabled = isEnabled;
	}

	public boolean isStillAttached()
	{
		return isStillAttached.getAsBoolean();
	}

	public boolean isEnabled()
	{
		return isEnabled;
	}
}
