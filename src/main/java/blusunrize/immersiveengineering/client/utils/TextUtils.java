package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.fluids.FluidStack;

public class TextUtils
{
	public static <T extends StringRepresentable> Component[] sideConfigWithOpposite(
			String descBase,
			T thisConfig,
			T otherConfig
	)
	{
		return new Component[]{
				new TranslatableComponent(Lib.DESC_INFO+"blockSide.facing")
						.append(": ")
						.append(new TranslatableComponent(descBase+thisConfig.getSerializedName())),
				new TranslatableComponent(Lib.DESC_INFO+"blockSide.opposite")
						.append(": ")
						.append(new TranslatableComponent(descBase+otherConfig.getSerializedName()))
		};
	}

	public static Component formatFluidStack(FluidStack fluid)
	{
		String s;
		if(!fluid.isEmpty())
			s = fluid.getDisplayName().getString()+": "+fluid.getAmount()+"mB";
		else
			s = I18n.get(Lib.GUI+"empty");
		return new TextComponent(s);
	}
}
