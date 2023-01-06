/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MultiblockBEType<T extends BlockEntity & IGeneralMultiblock> implements BiFunction<BlockPos, BlockState, T>
{
	private final RegistryObject<BlockEntityType<T>> master;
	private final RegistryObject<BlockEntityType<T>> dummy;
	private final Predicate<BlockState> isMaster;

	public MultiblockBEType(
			String name, DeferredRegister<BlockEntityType<?>> register,
			BEWithTypeConstructor<T> make, Supplier<? extends Block> block, Predicate<BlockState> isMaster
	)
	{
		this.isMaster = isMaster;
		this.master = register.register(name+"_master", makeType(make, block));
		this.dummy = register.register(name+"_dummy", makeType(make, block));
	}

	@Override
	@Nullable
	public T apply(BlockPos pos, BlockState state)
	{
		if(isMaster.test(state))
			return master.get().create(pos, state);
		else
			return dummy.get().create(pos, state);
	}

	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeType(BEWithTypeConstructor<T> create, Supplier<? extends Block> valid)
	{
		return () -> {
			Mutable<BlockEntityType<T>> typeMutable = new MutableObject<>();
			BlockEntityType<T> type = new BlockEntityType<>(
					(pos, state) -> create.create(typeMutable.getValue(), pos, state), ImmutableSet.of(valid.get()), null
			);
			typeMutable.setValue(type);
			return type;
		};
	}

	public BlockEntityType<T> master()
	{
		return master.get();
	}

	public BlockEntityType<T> dummy()
	{
		return dummy.get();
	}

	public RegistryObject<BlockEntityType<T>> dummyHolder()
	{
		return dummy;
	}

	public RegistryObject<BlockEntityType<T>> masterHolder()
	{
		return master;
	}

	public interface BEWithTypeConstructor<T extends BlockEntity>
	{
		T create(BlockEntityType<T> type, BlockPos p_155268_, BlockState p_155269_);
	}
}
