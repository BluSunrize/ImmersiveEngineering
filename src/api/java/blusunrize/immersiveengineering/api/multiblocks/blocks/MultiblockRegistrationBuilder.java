package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.Disassembler;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.lang.reflect.Method;
import java.util.List;
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
	private RegistryObject<? extends Item> item;
	private boolean mirrorable = true;
	private boolean hasComparatorOutput = false;
	private boolean redstoneInputAware = false;
	private boolean postProcessesShape = false;
	private Supplier<BlockPos> getMasterPosInMB;
	private Disassembler disassemble;
	private Function<Level, List<StructureBlockInfo>> structure;

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

	public MultiblockRegistrationBuilder<State> withComparator()
	{
		this.hasComparatorOutput = true;
		return this;
	}

	public MultiblockRegistrationBuilder<State> postProcessesShape()
	{
		this.postProcessesShape = true;
		return this;
	}

	public MultiblockRegistrationBuilder<State> redstoneAware()
	{
		this.redstoneInputAware = true;
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
			DeferredRegister<Block> register,
			DeferredRegister<Item> blockItemRegister,
			BlockBehaviour.Properties properties
	)
	{
		return customBlock(register, blockItemRegister, reg -> {
			if(reg.mirrorable())
				return new MultiblockPartBlock.WithMirrorState<>(properties, reg);
			else
				return new MultiblockPartBlock<>(properties, reg);
		}, MultiblockItem::new);
	}

	public MultiblockRegistrationBuilder<State> customBlock(
			DeferredRegister<Block> register,
			DeferredRegister<Item> blockItemRegister,
			Function<MultiblockRegistration<State>, ? extends MultiblockPartBlock<State>> make,
			Function<Block, Item> makeItem
	)
	{
		Preconditions.checkState(this.block==null);
		this.block = register.register(name, () -> make.apply(this.result));
		this.item = blockItemRegister.register(name, () -> makeItem.apply(this.result.block().get()));
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
		this.structure = l -> structure.get().getStructure(l);
		return this;
	}

	public MultiblockRegistration<State> build()
	{
		Objects.requireNonNull(logic);
		Objects.requireNonNull(masterBE);
		Objects.requireNonNull(dummyBE);
		Objects.requireNonNull(block);
		Objects.requireNonNull(item);
		Objects.requireNonNull(getMasterPosInMB);
		Objects.requireNonNull(disassemble);
		Objects.requireNonNull(structure);
		Preconditions.checkState(this.result==null);
		if(!postProcessesShape)
		{
			try
			{
				final Method postProcessMethod = logic.getClass().getMethod(
						"postProcessAbsoluteShape",
						IMultiblockContext.class, VoxelShape.class, CollisionContext.class, BlockPos.class
				);
				final Class<?> declaringClass = postProcessMethod.getDeclaringClass();
				Preconditions.checkState(
						declaringClass==IMultiblockLogic.class,
						"Multiblock overrides postProcessAbsoluteShape, but is not marked as post processing! ID: %s",
						name
				);
			} catch(NoSuchMethodException e)
			{
				throw new RuntimeException(e);
			}
		}
		this.result = new MultiblockRegistration<>(
				logic, masterBE, dummyBE, block, item,
				mirrorable, hasComparatorOutput, redstoneInputAware, postProcessesShape,
				getMasterPosInMB, disassemble, structure
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
