/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent.StateWrapper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public record MultiblockRegistration<State extends IMultiblockState>(
		IMultiblockLogic<State> logic,
		List<ExtraComponent<State, ?>> extraComponents,
		Supplier<BlockEntityType<? extends MultiblockBlockEntityMaster<State>>> masterBE,
		Supplier<BlockEntityType<? extends MultiblockBlockEntityDummy<State>>> dummyBE,
		Supplier<? extends MultiblockPartBlock<State>> block,
		Supplier<? extends Item> blockItem,
		boolean mirrorable,
		boolean hasComparatorOutput,
		boolean redstoneInputAware,
		boolean postProcessesShape,
		Supplier<BlockPos> getMasterPosInMB,
		Function<Level, Vec3i> getSize,
		Disassembler disassemble,
		Function<Level, List<StructureBlockInfo>> getStructure,
		ResourceLocation id
)
{
	public static <State extends IMultiblockState>
	MultiblockRegistrationBuilder<State, ?> builder(IMultiblockLogic<State> logic, ResourceLocation name)
	{
		class Impl extends MultiblockRegistrationBuilder<State, Impl>
		{
			public Impl(IMultiblockLogic<State> logic, ResourceLocation name)
			{
				super(logic, name);
			}

			@Override
			protected Impl self()
			{
				return this;
			}
		}
		return new Impl(logic, name);
	}

	public BlockPos masterPosInMB()
	{
		return getMasterPosInMB.get();
	}

	public Vec3i size(Level level)
	{
		return getSize.apply(level);
	}

	public ItemStack iconStack()
	{
		return blockItem().get().getDefaultInstance();
	}

	public interface Disassembler
	{
		void disassemble(Level world, BlockPos origin, MultiblockOrientation orientation);
	}

	public record ExtraComponent<State, ComponentState>(
			IMultiblockComponent<ComponentState> component, StateWrapper<State, ComponentState> makeWrapper
	)
	{
	}
}
