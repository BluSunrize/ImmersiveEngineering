package blusunrize.immersiveengineering.common.util.compat;

import java.util.HashSet;
import java.util.Set;

import blusunrize.immersiveengineering.common.util.compat.hydcraft.HydCraftHelper;
import blusunrize.immersiveengineering.common.util.compat.mfr.MFRHelper;
import blusunrize.immersiveengineering.common.util.compat.minetweaker.MTHelper;
import blusunrize.immersiveengineering.common.util.compat.waila.WailaHelper;

public abstract class IECompatModule
{
	public static Set<IECompatModule> modules = new HashSet<IECompatModule>();
	static{
		modules.add(new MFRHelper());
		modules.add(new MTHelper());
		modules.add(new DenseOresHelper());
//		modules.add(new EE3Helper());
		modules.add(new FMPHelper());
		modules.add(new ForestryHelper());
		modules.add(new BacktoolsHelper());
		modules.add(new WailaHelper());
		modules.add(new GregTechHelper());
		modules.add(new HydCraftHelper());
	}
	
	public String modId;
	public IECompatModule(String modId)
	{
		this.modId = modId;
	}
	
	public abstract void init();
	public abstract void postInit();
}
