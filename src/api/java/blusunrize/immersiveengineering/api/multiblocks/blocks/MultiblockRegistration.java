package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public record MultiblockRegistration<State extends IMultiblockState>(
		IMultiblockLogic<State> logic,
		RegistryObject<BlockEntityType<? extends MultiblockBlockEntityMaster<State>>> masterBE,
		RegistryObject<BlockEntityType<? extends MultiblockBlockEntityDummy<State>>> dummyBE,
		RegistryObject<? extends MultiblockPartBlock<State>> block,
		RegistryObject<? extends Item> blockItem,
		boolean mirrorable,
		boolean hasComparatorOutput,
		boolean redstoneInputAware,
		Supplier<BlockPos> getMasterPosInMB,
		Disassembler disassemble
)
{
	public static <State extends IMultiblockState>
	MultiblockRegistrationBuilder<State> builder(IMultiblockLogic<State> logic, String name)
	{
		// TODO remove _new suffix
		return new MultiblockRegistrationBuilder<>(logic, name+"_new");
	}

	public BlockPos masterPosInMB()
	{
		return getMasterPosInMB.get();
	}

	public interface Disassembler
	{
		void disassemble(Level world, BlockPos origin, MultiblockOrientation orientation);
	}
}
