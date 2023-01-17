/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.Disassembler;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.ExtraComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent.StateWrapper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
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
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder.RegistrationMethod.fromDeferred;

public abstract class MultiblockRegistrationBuilder<
		State extends IMultiblockState,
		Self extends MultiblockRegistrationBuilder<State, Self>
		>
{
	public static final String DUMMY_BE_SUFFIX = "_dummy";
	public static final String MASTER_BE_SUFFIX = "_master";

	private final IMultiblockLogic<State> logic;
	private final ResourceLocation name;
	private final List<ExtraComponent<State, ?>> extraComponents = new ArrayList<>();
	private Supplier<BlockEntityType<? extends MultiblockBlockEntityMaster<State>>> masterBE;
	private Supplier<BlockEntityType<? extends MultiblockBlockEntityDummy<State>>> dummyBE;
	private Supplier<? extends MultiblockPartBlock<State>> block;
	private Supplier<? extends Item> item;
	private boolean mirrorable = true;
	private boolean hasComparatorOutput = false;
	private boolean redstoneInputAware = false;
	private boolean postProcessesShape = false;
	private Supplier<BlockPos> getMasterPosInMB;
	private Function<Level, Vec3i> getSize;
	private Disassembler disassemble;
	private Function<Level, List<StructureBlockInfo>> structure;

	private MultiblockRegistration<State> result;

	public MultiblockRegistrationBuilder(IMultiblockLogic<State> logic, ResourceLocation name)
	{
		this.logic = logic;
		this.name = name;
	}

	public Self notMirrored()
	{
		this.mirrorable = false;
		return self();
	}

	public Self withComparator()
	{
		this.hasComparatorOutput = true;
		return self();
	}

	public Self postProcessesShape()
	{
		this.postProcessesShape = true;
		return self();
	}

	public Self redstoneAware()
	{
		this.redstoneInputAware = true;
		return self();
	}

	public Self defaultBEs(DeferredRegister<BlockEntityType<?>> register)
	{
		return defaultBEs(fromDeferred(register));
	}

	public Self defaultBEs(RegistrationMethod<BlockEntityType<?>> register)
	{
		Preconditions.checkState(this.masterBE==null);
		Preconditions.checkState(this.dummyBE==null);
		this.masterBE = register.register(name.getPath()+MASTER_BE_SUFFIX, () -> makeBEType(MultiblockBlockEntityMaster::new));
		this.dummyBE = register.register(name.getPath()+DUMMY_BE_SUFFIX, () -> makeBEType(MultiblockBlockEntityDummy::new));
		return self();
	}

	public Self defaultBlock(
			DeferredRegister<Block> register,
			DeferredRegister<Item> blockItemRegister,
			BlockBehaviour.Properties properties
	)
	{
		return defaultBlock(fromDeferred(register), fromDeferred(blockItemRegister), properties);
	}

	public Self defaultBlock(
			RegistrationMethod<Block> register,
			RegistrationMethod<Item> blockItemRegister,
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

	public Self customBlock(
			DeferredRegister<Block> register,
			DeferredRegister<Item> blockItemRegister,
			Function<MultiblockRegistration<State>, ? extends MultiblockPartBlock<State>> make,
			Function<Block, Item> makeItem
	)
	{
		return customBlock(fromDeferred(register), fromDeferred(blockItemRegister), make, makeItem);
	}

	public Self customBlock(
			RegistrationMethod<Block> register,
			RegistrationMethod<Item> blockItemRegister,
			Function<MultiblockRegistration<State>, ? extends MultiblockPartBlock<State>> make,
			Function<Block, Item> makeItem
	)
	{
		Preconditions.checkState(this.block==null);
		this.block = register.register(name.getPath(), () -> make.apply(this.result));
		this.item = blockItemRegister.register(name.getPath(), () -> makeItem.apply(this.result.block().get()));
		return self();
	}

	public Self structure(Supplier<TemplateMultiblock> structure)
	{
		Preconditions.checkState(this.getMasterPosInMB==null);
		Preconditions.checkState(this.disassemble==null);
		this.getMasterPosInMB = () -> structure.get().getMasterFromOriginOffset();
		this.getSize = l -> structure.get().getSize(l);
		this.disassemble = (level, origin, orientation) -> structure.get().disassemble(
				level, origin, orientation.mirrored(), orientation.front()
		);
		this.structure = l -> structure.get().getStructure(l);
		return self();
	}

	public Self component(IMultiblockComponent<State> extraComponent)
	{
		return component(extraComponent, s -> s);
	}

	public <CS> Self component(IMultiblockComponent<CS> extraComponent, StateWrapper<State, CS> makeState)
	{
		extraComponents.add(new ExtraComponent<>(extraComponent, makeState));
		return self();
	}

	public <CS, C extends IMultiblockComponent<CS> & StateWrapper<State, CS>>
	Self selfWrappingComponent(C extraComponent)
	{
		return component(extraComponent, extraComponent);
	}

	public MultiblockRegistration<State> build()
	{
		Objects.requireNonNull(logic);
		Objects.requireNonNull(masterBE);
		Objects.requireNonNull(dummyBE);
		Objects.requireNonNull(block);
		Objects.requireNonNull(item);
		Objects.requireNonNull(getMasterPosInMB);
		Objects.requireNonNull(getSize);
		Objects.requireNonNull(disassemble);
		Objects.requireNonNull(structure);
		Preconditions.checkState(this.result==null);
		if(!postProcessesShape)
		{
			try
			{
				final Method postProcessMethod = logic.getClass().getMethod(
						"postProcessAbsoluteShape",
						IMultiblockContext.class, VoxelShape.class, CollisionContext.class, BlockPos.class, ShapeType.class
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
				logic, extraComponents, masterBE, dummyBE, block, item,
				mirrorable, hasComparatorOutput, redstoneInputAware, postProcessesShape,
				getMasterPosInMB, getSize, disassemble, structure, name
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

	protected abstract Self self();

	public interface RegistrationMethod<Base>
	{
		<T extends Base>
		Supplier<T> register(String path, Supplier<T> makeInstance);

		static <B> RegistrationMethod<B> fromDeferred(DeferredRegister<B> register)
		{
			return new RegistrationMethod<>()
			{
				@Override
				public <T extends B> Supplier<T> register(String path, Supplier<T> makeInstance)
				{
					return register.register(path, makeInstance);
				}
			};
		}
	}

	private interface BEConstructor<State extends IMultiblockState, T extends BlockEntity>
	{
		T make(BlockEntityType<?> type, BlockPos pos, BlockState state, MultiblockRegistration<State> multiblock);
	}
}
