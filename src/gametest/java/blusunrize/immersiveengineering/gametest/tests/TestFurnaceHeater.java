/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.gametest.tests;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(Lib.MODID)
public class TestFurnaceHeater
{
	@GameTest(timeoutTicks = AbstractFurnaceBlockEntity.BURN_TIME_STANDARD+1)
	public static void heatsFurnace(GameTestHelper helper)
	{
		final BlockPos furnacePos = new BlockPos(2, 1, 1);
		helper.setBlock(new BlockPos(0, 1, 1), MetalDevices.CAPACITOR_CREATIVE.get());
		helper.succeedWhen(() -> helper.assertContainerContains(furnacePos, Items.COOKED_PORKCHOP));
	}
}
