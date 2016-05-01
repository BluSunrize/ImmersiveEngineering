package blusunrize.immersiveengineering.api.energy.immersiveflux;

/**
 * An advanced implementation of {@link IFluxStorage}, keeps track of the last 20 in- and outputs to allow transfer evaluation.
 * 
 * @author BluSunrize - 02.02.2016
 * 
 */
public class FluxStorageAdvanced extends FluxStorage
{
	int averageInsertion=0;
	int averageExtraction=0;
	
	public FluxStorageAdvanced(int capacity, int limitReceive, int limitExtract)
	{
		super(capacity, limitReceive, limitExtract);
	}
	public FluxStorageAdvanced(int capacity, int limitTransfer)
	{
		super(capacity, limitTransfer);
	}
	public FluxStorageAdvanced(int capacity)
	{
		super(capacity, capacity, capacity);
	}

	@Override
	public int receiveEnergy(int energy, boolean simulate)
	{
		int received = super.receiveEnergy(energy,simulate);
		if(!simulate)
			averageInsertion = averageInsertion/2 + received/2;
		return received;
	}

	@Override
	public int extractEnergy(int energy, boolean simulate)
	{
		int extracted = super.extractEnergy(energy, simulate);
		if(!simulate)
			averageExtraction = averageInsertion/2 + extracted/2;
		return extracted;
	}

	public int getAverageInsertion()
	{
		return averageInsertion;
	}
	public int getAverageExtraction()
	{
		return averageExtraction;
	}
}
