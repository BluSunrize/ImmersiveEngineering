package blusunrize.immersiveengineering.common.util;

import net.minecraft.util.math.MathHelper;

import java.util.function.IntConsumer;

public class LayeredComparatorOutput
{
	private final double maxValue;
	private final int numLayers;
	private final double layerSize;
	private final Runnable updateMaster;
	private final IntConsumer updateLayer;

	private double lastValue = -1;
	private int currentMasterOutput;
	private final int[] currentLayerOutputs;

	public LayeredComparatorOutput(
			double maxValue, int numLayers, Runnable updateMaster, IntConsumer updateLayer
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

	public void update(double newValue)
	{
		if(newValue==lastValue)
			return;
		lastValue = newValue;
		final int newMasterOutput = (int)((15*newValue)/maxValue);
		if(currentMasterOutput!=newMasterOutput)
		{
			currentMasterOutput = newMasterOutput;
			updateMaster.run();
		}
		for(int layer = 0; layer < numLayers; ++layer)
		{
			final double layerValue = newValue-layer*layerSize;
			final int newLayerOutput = (int)MathHelper.clamp((15*layerValue)/layerSize, 0, 15);
			if(newLayerOutput!=currentLayerOutputs[layer])
			{
				currentLayerOutputs[layer] = newLayerOutput;
				updateLayer.accept(layer);
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
}
