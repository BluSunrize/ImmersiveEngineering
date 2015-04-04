package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.common.Config;

/**
 * @author BluSunrize - 08.03.2015
 *
 * The three different Cable Types introduced by IE
 */
public enum WireType
{
	COPPER(),
	ELECTRUM(),
	STEEL();
	
	
	public static WireType getValue(int i)
	{
		if(i>=0 && i<values().length)
			return values()[i];
		return COPPER;
	}
	public double getLossRatio()
	{
		return Config.getDoubleArray("cableLossRatio")[ordinal()];
	}
	public int getTransferRate()
	{
		return Config.getIntArray("cableTransferRate")[ordinal()];
	}
	public int getColour()
	{
		return Config.getIntArray("cableColouration")[ordinal()];
	}
}
