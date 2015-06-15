package blusunrize.immersiveengineering.common.util.compat;

import java.util.HashSet;
import java.util.Set;

import blusunrize.immersiveengineering.common.util.compat.mfr.MFRHelper;
import blusunrize.immersiveengineering.common.util.compat.minetweaker.MTHelper;

public abstract class IECompatModule
{
	public static Set<IECompatModule> modules = new HashSet<IECompatModule>();
	static{
		modules.add(new MFRHelper());
		modules.add(new MTHelper());
		modules.add(new DenseOresHelper());
		modules.add(new EE3Helper());
		modules.add(new ForestryHelper());
	}
	
	public String modId;
	public IECompatModule(String modId)
	{
		this.modId = modId;
	}
	
	public abstract void init();
	public abstract void postInit();
}
