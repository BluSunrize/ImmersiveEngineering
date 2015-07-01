package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.Config;


public class ModCompatability
{
	public static double convertRFtoEU(int rf, int maxTier)
	{
		return rf/(double)Config.getInt("euConversion");
	}
	public static int convertEUtoRF(double eu)
	{
		return (int)eu*Config.getInt("euConversion");
	}
}