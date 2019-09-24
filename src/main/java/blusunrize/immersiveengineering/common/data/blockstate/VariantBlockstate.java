/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.blockstate;

import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator.ConfiguredModel;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class VariantBlockstate implements BlockstateGenerator.IVariantModelGenerator
{
	private final Map<BlockState, ConfiguredModel> models;

	private VariantBlockstate(Map<BlockState, ConfiguredModel> models)
	{

		this.models = models;
	}

	@Override
	public ConfiguredModel getModel(BlockState state)
	{
		return models.get(state);
	}

	public static class Builder
	{
		private final Block b;
		private final Map<BlockState, ConfiguredModel> models = new HashMap<>();

		public Builder(Block b)
		{
			this.b = b;
		}

		public Builder setModel(BlockState state, ConfiguredModel model)
		{
			Preconditions.checkNotNull(state);
			Preconditions.checkNotNull(model);
			Preconditions.checkArgument(state.getBlock()==b);
			Preconditions.checkArgument(!models.containsKey(state));
			models.put(state, model);
			return this;
		}

		public Builder setForAllMatching(Predicate<BlockState> matches, ConfiguredModel model)
		{
			Preconditions.checkNotNull(matches);
			for(BlockState state : b.getStateContainer().getValidStates())
				if(matches.test(state))
					setModel(state, model);
			return this;
		}

		public <T extends Comparable<T>> Builder setForAllWithState(Map<IProperty<?>, ?> partialState, ConfiguredModel model)
		{
			Preconditions.checkNotNull(partialState);
			Preconditions.checkArgument(b.getStateContainer().getProperties().containsAll(partialState.keySet()));
			return setForAllMatching(blockState -> {
				for(IProperty<?> prop : partialState.keySet())
					if(blockState.get(prop)!=partialState.get(prop))
						return false;
				return true;
			}, model);
		}

		public VariantBlockstate build()
		{
			for(BlockState state : b.getStateContainer().getValidStates())
				Preconditions.checkArgument(models.containsKey(state));
			return new VariantBlockstate(models);
		}
	}
}
