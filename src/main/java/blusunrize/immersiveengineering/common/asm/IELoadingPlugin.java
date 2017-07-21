package blusunrize.immersiveengineering.common.asm;

import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author BluSunrize - 20.07.2017
 */
//@IFMLLoadingPlugin.SortingIndex(1001)
public class IELoadingPlugin implements IFMLLoadingPlugin
{
	@Override
	public String[] getASMTransformerClass()
	{
		IELogger.info("IE~ get transformer");
		return new String[]{"blusunrize.immersiveengineering.common.asm.IEClassTransformer"};
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Nullable
	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{

	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
