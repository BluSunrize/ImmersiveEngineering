/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.gametest.tests;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(Lib.MODID)
public class TestMultiblocks
{
	@GameTestGenerator
	public static List<TestFunction> formAndDisassembleAll()
	{
		List<TestFunction> tests = new ArrayList<>();
		for(IMultiblock multiblock : MultiblockHandler.getMultiblocks())
			if(multiblock instanceof IETemplateMultiblock ieMultiblock)
				tests.add(new TestFunction(
						"multiblock", ieMultiblock.getUniqueName().getPath(), ieMultiblock.getUniqueName().toString(),
						200, 0, true, helper -> formAndDisassemble(helper, ieMultiblock)
				));
		return tests;
	}

	private static void formAndDisassemble(GameTestHelper helper, IETemplateMultiblock multiblock)
	{
		Player player = helper.makeMockPlayer();
		BlockPos triggerRelative = multiblock.getTriggerOffset().above();
		BlockPos triggerAbsolute = helper.absolutePos(triggerRelative);
		BlockPos testRelative = Util.make(() -> {
			for(StructureBlockInfo block : multiblock.getStructure(helper.getLevel()))
			{
				BlockPos testPos = block.pos.above();
				if(!testPos.equals(triggerRelative)&&!block.state.isAir())
					return testPos;
			}
			throw new GameTestAssertException("Multiblock only consists of trigger block???");
		});
		BlockState triggerState = helper.getBlockState(triggerRelative);
		Block originalTestBlock = helper.getBlockState(testRelative).getBlock();
		assertForm(player, helper, triggerAbsolute, testRelative, multiblock.getBlock());
		helper.runAfterDelay(20, () -> {
			helper.setBlock(triggerRelative, Blocks.AIR);
			helper.assertBlockPresent(originalTestBlock, testRelative);
			helper.runAfterDelay(20, () -> {
				helper.setBlock(triggerRelative, triggerState);
				assertForm(player, helper, triggerAbsolute, testRelative, multiblock.getBlock());
				helper.succeed();
			});
		});
	}

	private static void assertForm(
			Player player, GameTestHelper helper, BlockPos triggerAbsolute, BlockPos testPos, Block formed
	)
	{
		ItemStack hammer = new ItemStack(Tools.HAMMER);
		player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
		BlockHitResult hitResult = new BlockHitResult(Vec3.ZERO, Direction.SOUTH, triggerAbsolute, false);
		InteractionResult result = hammer.onItemUseFirst(
				new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult)
		);
		if(result!=InteractionResult.SUCCESS)
			helper.fail("Wrong interaction result: "+result.name());
		helper.assertBlockPresent(formed, testPos);
	}
}
