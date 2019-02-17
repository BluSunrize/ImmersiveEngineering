/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil.LightningAnimation;
import blusunrize.immersiveengineering.common.util.Utils;
import elucent.albedo.event.GatherLightsEvent;
import elucent.albedo.lighting.Light;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AlbedoHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{

	}

	@Override
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void gatherLights(GatherLightsEvent event)
	{
		for(LightningAnimation animation : TileEntityTeslaCoil.effectMap.values())
		{
			if(animation.shoudlRecalculateLightning())
				animation.createLightning(Utils.RAND);

			Light.Builder builder = Light.builder();
			builder.radius(2.5f);
			builder.color(77/255f, 74/255f, 152/255f);
			for(Vec3d point : animation.subPoints)
			{
				builder.pos(point);
				event.getLightList().add(builder.build());
			}
		}
	}

	@Override
	public void postInit()
	{
	}
}