package blusunrize.immersiveengineering.api;

/**
 * @author BluSunrize - 08.03.2015
 *
 * The three different Cable Types introduced by IE
 */
public enum WireType
{
	COPPER,
	ELECTRUM,
	STEEL,
	STRUCTURE_ROPE,
	STRUCTURE_STEEL,
	TELECOMMUNICATION;

	public static WireType getValue(int i)
	{
		if(i>=0 && i<values().length)
			return values()[i];
		return COPPER;
	}
	public static double[] cableLossRatio;
	public double getLossRatio()
	{
		return cableLossRatio[ordinal()];
	}
	public static int[] cableTransferRate;
	public int getTransferRate()
	{
		return cableTransferRate[ordinal()];
	}
	public static int[] cableColouration;
	public int getColour()
	{
		return cableColouration[ordinal()];
	}
	public static int[] cableLength;
	public int getMaxLength()
	{
		return cableLength[ordinal()];
	}
}
