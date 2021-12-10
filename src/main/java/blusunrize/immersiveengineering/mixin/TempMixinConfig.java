package blusunrize.immersiveengineering.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import javax.swing.*;
import java.util.List;
import java.util.Set;

public class TempMixinConfig implements IMixinConfigPlugin
{
	private static final String OPTION = "log4j2.formatMsgNoLookups";
	private static final String MESSAGE = "Please add \"-D"+OPTION+"=true\" to your JVM arguments!";

	@Override
	public void onLoad(String mixinPackage)
	{
		if(!Boolean.getBoolean(OPTION))
		{
			System.err.println(MESSAGE);
			if(!Boolean.getBoolean("java.awt.headless"))
				JOptionPane.showMessageDialog(null, MESSAGE);
			System.exit(13);
		}
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
	}

	@Override
	public List<String> getMixins()
	{
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{

	}
}
