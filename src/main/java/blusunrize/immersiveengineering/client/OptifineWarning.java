/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.common.util.IELogger;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.ModLoadingStage;
import net.neoforged.fml.ModLoadingWarning;
import net.minecraftforge.forgespi.language.IModInfo;

/**
 * Optifine replaces the PoseStack class, and its replacement doesn't inherit from IForgePoseStack. So the
 * pushTransformation method is missing, which is called indirectly by various parts of IE. This class adds a loading
 * warning when calling pushTransformation produces a NoSuchMethodError and OF is installed.
 */
public class OptifineWarning
{
	public static void warnIfRequired()
	{
		if(detectBadOptifine())
		{
			final IModInfo modInfo = ModLoadingContext.get().getActiveContainer().getModInfo();
			ModLoadingWarning warning = new ModLoadingWarning(
					modInfo, ModLoadingStage.CONSTRUCT, "immersiveengineering.optifinePoseStackWarning"
			);
			ModLoader.get().addWarning(warning);
		}
	}

	private static boolean detectBadOptifine()
	{
		try
		{
			Class.forName("net.optifine.Config");
		} catch(ClassNotFoundException ignored)
		{
			// The call shouldn't fail in this case, if it does we want a proper crash report
			return false;
		}
		try
		{
			PoseStack stack = new PoseStack();
			stack.pushTransformation(new Transformation(null));
			return false;
		} catch(NoSuchMethodError x)
		{
			IELogger.logger.error("Detected bad Optifine version, error is:", x);
			return true;
		}
	}
}
