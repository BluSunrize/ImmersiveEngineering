/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic;

public record EventWaiterResult(InterruptibleRunnable waitUntilDone, String name)
{
	public void startAsync(Runnable callback)
	{
		new Thread(() -> {
			try
			{
				waitUntilDone.run();
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			callback.run();
		}).start();
	}

	public void runSync()
	{
		try
		{
			waitUntilDone.run();
		} catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@FunctionalInterface
	public interface InterruptibleRunnable
	{
		void run() throws InterruptedException;
	}
}
