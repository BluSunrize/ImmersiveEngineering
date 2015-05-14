package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.Config;


public class ModCompatability
{
	
	
	public static double convertRFtoEU(int rf, int maxTier)
	{
		return Math.min(rf/Config.getInt("euConversion"), maxTier==1?32:maxTier==2?128:maxTier==3?512: 2048);
	}
	public static int convertEUtoRF(double eu)
	{
		return (int)eu*Config.getInt("euConversion");
	}
}