/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.ObjIntConsumer;

public class LayeredComparatorOutput<CTX>
{
	private final double maxValue;
	private final int numLayers;
	private final double layerSize;
	private final ObjIntConsumer<CTX> updateMaster;
	private final LayerUpdater<CTX> updateLayer;

	private double lastValue = -1;
	private int currentMasterOutput;
	private final int[] currentLayerOutputs;

	public LayeredComparatorOutput(
			double maxValue, int numLayers, ObjIntConsumer<CTX> updateMaster, LayerUpdater<CTX> updateLayer
	)
	{
		this.maxValue = maxValue;
		this.numLayers = numLayers;
		this.updateMaster = updateMaster;
		this.updateLayer = updateLayer;
		this.currentMasterOutput = 0;
		this.currentLayerOutputs = new int[numLayers];
		this.layerSize = maxValue/numLayers;
	}

	public static LayeredComparatorOutput<IMultiblockContext<?>> makeForSiloLike(int maxSize, int numLayers)
	{
		interface Updater
		{
			void update(IMultiblockContext<?> ctx, BlockPos pos, int value);
		}
		final BlockPos masterPos = new BlockPos(1, 0, 1);
		final Updater update = (ctx, pos, value) -> {
			final IMultiblockLevel level = ctx.getLevel();
			ctx.setComparatorOutputFor(pos, value);
			final BlockPos absPos = level.toAbsolute(masterPos);
			final BlockState stateAt = level.getBlockState(masterPos);
			level.getRawLevel().updateNeighborsAt(absPos, stateAt.getBlock());
		};
		return new LayeredComparatorOutput<>(
				maxSize,
				numLayers,
				(ctx, value) -> update.update(ctx, masterPos, value),
				(ctx, layer, value) -> {
					for(int x = 0; x <= 2; x++)
						for(int z = 0; z <= 2; z++)
							if(x!=1||z!=1)
								update.update(ctx, new BlockPos(x, layer, z), value);
				}
		);
	}

	public void update(CTX ctx, double newValue)
	{
		if(newValue==lastValue)
			return;
		lastValue = newValue;
		final int newMasterOutput = (int)((15*newValue)/maxValue);
		if(currentMasterOutput!=newMasterOutput)
		{
			currentMasterOutput = newMasterOutput;
			updateMaster.accept(ctx, newMasterOutput);
		}
		for(int layer = 0; layer < numLayers; ++layer)
		{
			final double layerValue = newValue-layer*layerSize;
			final int newLayerOutput = (int)Mth.clamp((15*layerValue)/layerSize, 0, 15);
			if(newLayerOutput!=currentLayerOutputs[layer])
			{
				currentLayerOutputs[layer] = newLayerOutput;
				updateLayer.update(ctx, layer, newLayerOutput);
			}
		}
	}

	public int getCurrentMasterOutput()
	{
		return currentMasterOutput;
	}

	public int getLayerOutput(int layer)
	{
		return currentLayerOutputs[layer];
	}

	public interface LayerUpdater<CTX>
	{
		void update(CTX ctx, int layer, int value);
	}
}
