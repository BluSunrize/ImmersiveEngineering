/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class ComputerControlState
{
	private final AtomicInteger refCount = new AtomicInteger(0);
	private boolean isEnabled = true;

	public boolean isAttached()
	{
		return refCount.get() > 0;
	}

	public void setEnabled(boolean enabled)
	{
		isEnabled = enabled;
	}

	public void addReference()
	{
		refCount.incrementAndGet();
	}

	public void removeReference()
	{
		if(refCount.decrementAndGet() <= 0)
			clear();
	}

	public void clear()
	{
		refCount.set(0);
		// Detaching and re-attaching a computer should reset enable state
		isEnabled = true;
	}

	// Only for client-side use!
	public void setOneRef()
	{
		refCount.set(1);
	}

	public boolean isEnabled()
	{
		return isEnabled;
	}
}
