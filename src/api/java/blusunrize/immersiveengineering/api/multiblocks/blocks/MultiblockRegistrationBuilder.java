package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.Disassembler;
import com.google.common.base.Preconditions;
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

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiblockRegistrationBuilder<State extends IMultiblockState>
{
	private final IMultiblockLogic<State> logic;
	private final String name;
	private RegistryObject<BlockEntityType<? extends MultiblockBlockEntityMaster<State>>> masterBE;
	private RegistryObject<BlockEntityType<? extends MultiblockBlockEntityDummy<State>>> dummyBE;
	private RegistryObject<? extends MultiblockPartBlock<State>> block;
	private boolean mirrorable = true;
	private Supplier<BlockPos> getMasterPosInMB;
	private Disassembler disassemble;

	private MultiblockRegistration<State> result;

	public MultiblockRegistrationBuilder(IMultiblockLogic<State> logic, String name)
	{
		this.logic = logic;
		this.name = name;
	}

	public MultiblockRegistrationBuilder<State> notMirrored()
	{
		this.mirrorable = false;
		return this;
	}

	public MultiblockRegistrationBuilder<State> defaultBEs(DeferredRegister<BlockEntityType<?>> register)
	{
		Preconditions.checkState(this.masterBE==null);
		Preconditions.checkState(this.dummyBE==null);
		this.masterBE = register.register(name+"_master", () -> makeBEType(MultiblockBlockEntityMaster::new));
		this.dummyBE = register.register(name+"_dummy", () -> makeBEType(MultiblockBlockEntityDummy::new));
		return this;
	}

	public MultiblockRegistrationBuilder<State> defaultBlock(
			DeferredRegister<Block> register, BlockBehaviour.Properties properties
	)
	{
		return customBlock(register, reg -> {
			if(reg.mirrorable())
				return new MultiblockPartBlock.WithMirrorState<>(properties, reg);
			else
				return new MultiblockPartBlock<>(properties, reg);
		});
	}

	public MultiblockRegistrationBuilder<State> customBlock(
			DeferredRegister<Block> register,
			Function<MultiblockRegistration<State>, ? extends MultiblockPartBlock<State>> make
	)
	{
		Preconditions.checkState(this.block==null);
		this.block = register.register(name, () -> make.apply(this.result));
		return this;
	}

	public MultiblockRegistrationBuilder<State> structure(Supplier<TemplateMultiblock> structure)
	{
		Preconditions.checkState(this.getMasterPosInMB==null);
		Preconditions.checkState(this.disassemble==null);
		this.getMasterPosInMB = () -> structure.get().getMasterFromOriginOffset();
		this.disassemble = (level, origin, orientation) -> structure.get().disassemble(
				// TODO may need front.getOpposite or similar
				level, origin, orientation.mirrored(), orientation.front()
		);
		return this;
	}

	public MultiblockRegistration<State> build()
	{
		Objects.requireNonNull(logic);
		Objects.requireNonNull(masterBE);
		Objects.requireNonNull(dummyBE);
		Objects.requireNonNull(block);
		Objects.requireNonNull(getMasterPosInMB);
		Objects.requireNonNull(disassemble);
		Preconditions.checkState(this.result==null);
		this.result = new MultiblockRegistration<>(
				logic, masterBE, dummyBE, block, mirrorable, getMasterPosInMB, disassemble
		);
		return this.result;
	}

	private <BE extends BlockEntity>
	BlockEntityType<? extends BE> makeBEType(
			BEConstructor<State, BE> construct
	)
	{
		Mutable<BlockEntityType<? extends BE>> resultBox = new MutableObject<>();
		resultBox.setValue(new BlockEntityType<>(
				(pos, state) -> construct.make(resultBox.getValue(), pos, state, result),
				Set.of(block.get()),
				null
		));
		return resultBox.getValue();
	}

	private interface BEConstructor<State extends IMultiblockState, T extends BlockEntity>
	{
		T make(BlockEntityType<?> type, BlockPos pos, BlockState state, MultiblockRegistration<State> multiblock);
	}
}
