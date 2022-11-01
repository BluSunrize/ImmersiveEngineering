package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Set;

public record MultiblockRegistration<State extends IMultiblockState>(
		IMultiblockLogic<State> logic,
		RegistryObject<BlockEntityType<? extends MultiblockBlockEntityMaster<State>>> masterBE,
		RegistryObject<BlockEntityType<? extends MultiblockBlockEntityDummy<State>>> dummyBE,
		RegistryObject<? extends MultiblockPartBlock<State>> block,
		boolean mirrorable,
		BlockPos masterPosInMB
)
{
	public static <State extends IMultiblockState>
	MultiblockRegistration<State> register(
			IMultiblockLogic<State> logic,
			String name,
			DeferredRegister<Block> blockRegister,
			DeferredRegister<BlockEntityType<?>> beRegister,
			boolean mirrorable,
			BlockPos masterPosInMB,
			BlockBehaviour.Properties blockProperties
	)
	{
		final Mutable<MultiblockRegistration<State>> resultBox = new MutableObject<>();
		final RegistryObject<MultiblockPartBlock<State>> block = blockRegister.register(
				name, () -> new MultiblockPartBlock<>(blockProperties, resultBox.getValue())
		);
		RegistryObject<BlockEntityType<? extends MultiblockBlockEntityMaster<State>>> masterBE = beRegister.register(
				name+"_master", () -> makeBEType(resultBox.getValue(), MultiblockBlockEntityMaster::new)
		);
		RegistryObject<BlockEntityType<? extends MultiblockBlockEntityDummy<State>>> dummyBE = beRegister.register(
				name+"_dummy", () -> makeBEType(resultBox.getValue(), MultiblockBlockEntityDummy::new)
		);
		resultBox.setValue(new MultiblockRegistration<>(logic, masterBE, dummyBE, block, mirrorable, masterPosInMB));
		return resultBox.getValue();
	}

	private static <State extends IMultiblockState, BE extends BlockEntity>
	BlockEntityType<? extends BE> makeBEType(
			MultiblockRegistration<State> registration, BEConstructor<State, BE> construct
	)
	{
		Mutable<BlockEntityType<? extends BE>> resultBox = new MutableObject<>();
		resultBox.setValue(new BlockEntityType<>(
				(pos, state) -> construct.make(resultBox.getValue(), pos, state, registration),
				Set.of(registration.block.get()),
				null
		));
		return resultBox.getValue();
	}

	private interface BEConstructor<State extends IMultiblockState, T extends BlockEntity>
	{
		T make(BlockEntityType<?> type, BlockPos pos, BlockState state, MultiblockRegistration<State> multiblock);
	}
}
