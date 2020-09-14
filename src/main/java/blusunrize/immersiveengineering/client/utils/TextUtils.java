package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class TextUtils
{
	public static <T extends IStringSerializable> ITextComponent[] sideConfigWithOpposite(
			String descBase,
			T thisConfig,
			T otherConfig
	)
	{
		return new ITextComponent[]{
				new TranslationTextComponent(Lib.DESC_INFO+"blockSide.facing")
						.appendString(": ")
						.append(new TranslationTextComponent(descBase+thisConfig.getString())),
				new TranslationTextComponent(Lib.DESC_INFO+"blockSide.opposite")
						.appendString(": ")
						.append(new TranslationTextComponent(descBase+otherConfig.getString()))
		};
	}

	public static ITextComponent formatFluidStack(FluidStack fluid)
	{
		String s;
		if(!fluid.isEmpty())
			s = fluid.getDisplayName().getString()+": "+fluid.getAmount()+"mB";
		else
			s = I18n.format(Lib.GUI+"empty");
		return new StringTextComponent(s);
	}
}
