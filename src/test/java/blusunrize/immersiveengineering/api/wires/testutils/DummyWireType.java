/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.testutils;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.WireType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class DummyWireType extends WireType
{
	private final double slack;

	public DummyWireType(double slack)
	{
		this.slack = slack;
	}

	@Override
	public String getUniqueName()
	{
		return "test wire";
	}

	@Override
	public int getColour(Connection connection)
	{
		return 0;
	}

	@Override
	public double getSlack()
	{
		return 1+slack;
	}

	@Override
	public int getMaxLength()
	{
		return 0;
	}

	@Override
	public ItemStack getWireCoil(Connection con)
	{
		return null;
	}

	@Override
	public double getRenderDiameter()
	{
		return 0;
	}

	@Nonnull
	@Override
	public String getCategory()
	{
		return "TEST_WIRE";
	}
}
