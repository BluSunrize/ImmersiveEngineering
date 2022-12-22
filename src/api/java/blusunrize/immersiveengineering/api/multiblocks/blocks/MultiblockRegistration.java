package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockComponent.StateWrapper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public record MultiblockRegistration<State extends IMultiblockState>(
		IMultiblockLogic<State> logic,
		List<ExtraComponent<State, ?>> extraComponents,
		RegistryObject<BlockEntityType<? extends MultiblockBlockEntityMaster<State>>> masterBE,
		RegistryObject<BlockEntityType<? extends MultiblockBlockEntityDummy<State>>> dummyBE,
		RegistryObject<? extends MultiblockPartBlock<State>> block,
		RegistryObject<? extends Item> blockItem,
		boolean mirrorable,
		boolean hasComparatorOutput,
		boolean redstoneInputAware,
		boolean postProcessesShape,
		Supplier<BlockPos> getMasterPosInMB,
		Function<Level, Vec3i> getSize,
		Disassembler disassemble,
		Function<Level, List<StructureBlockInfo>> getStructure
)
{
	public static <State extends IMultiblockState>
	MultiblockRegistrationBuilder<State, ?> builder(IMultiblockLogic<State> logic, String name)
	{
		class Impl extends MultiblockRegistrationBuilder<State, Impl>
		{
			public Impl(IMultiblockLogic<State> logic, String name)
			{
				super(logic, name);
			}

			@Override
			protected Impl self()
			{
				return this;
			}
		}
		// TODO remove _new suffix
		return new Impl(logic, name+"_new");
	}

	public BlockPos masterPosInMB()
	{
		return getMasterPosInMB.get();
	}

	public Vec3i size(Level level)
	{
		return getSize.apply(level);
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
