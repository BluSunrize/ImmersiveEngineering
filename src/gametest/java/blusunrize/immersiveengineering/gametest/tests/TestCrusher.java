/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.gametest.tests;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEItems.Metals;
import blusunrize.immersiveengineering.gametest.GameTestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(Lib.MODID)
public class TestCrusher
{
	private static final String CRUSHER_WITH_CHEST = "with_chest";
	private static final BlockPos CHEST_RELATIVE = new BlockPos(2, 1, 3);

	@GameTest(template = CRUSHER_WITH_CHEST)
	public static void testKillsEntityWithPower(GameTestHelper helper)
	{
		GameTestUtils.formMultiblock(IEMultiblocks.CRUSHER, helper);
		helper.spawnWithNoFreeWill(EntityType.COW, 2.5f, 4, 1.5f);
		helper.setBlock(new BlockPos(4, 3, 1), MetalDevices.CAPACITOR_CREATIVE.get());
		helper.succeedWhen(() -> {
			helper.assertEntityNotPresent(EntityType.COW);
			GameTestUtils.assertContainerContainsSome(CHEST_RELATIVE, Items.BEEF, helper);
			helper.succeed();
		});
	}

	@GameTest(template = CRUSHER_WITH_CHEST)
	public static void testProcessesItem(GameTestHelper helper)
	{
		GameTestUtils.formMultiblock(IEMultiblocks.CRUSHER, helper);
		helper.spawnItem(Items.COPPER_ORE, 2.5f, 4, 1.5f);
		helper.setBlock(new BlockPos(4, 3, 1), MetalDevices.CAPACITOR_CREATIVE.get());
		helper.succeedWhen(() -> {
			helper.assertEntityNotPresent(EntityType.ITEM);
			GameTestUtils.assertContainerContainsSome(CHEST_RELATIVE, Metals.DUSTS.get(EnumMetals.COPPER), helper);
			helper.succeed();
		});
	}

	@GameTest(template = CRUSHER_WITH_CHEST, timeoutTicks = 201)
	public static void testDoesNotKillWithoutPower(GameTestHelper helper)
	{
		GameTestUtils.formMultiblock(IEMultiblocks.CRUSHER, helper);
		helper.spawnWithNoFreeWill(EntityType.COW, 2.5f, 4, 1.5f);
		helper.runAtTickTime(200, () -> {
			helper.assertEntityPresent(EntityType.COW);
			helper.succeed();
		});
	}
}
