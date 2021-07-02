/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CommonProxy
{
	public void onWorldLoad()
	{
	}

	public void resetManual()
	{
	}

	public void handleTileSound(SoundEvent soundEvent, TileEntity tile, boolean tileActive, float volume, float pitch)
	{
	}

	public void stopTileSound(String soundName, TileEntity tile)
	{
	}

	public void spawnFractalFX(World world, double x, double y, double z, Vector3d direction, double scale, int prefixColour, float[][] colour)
	{
		if(prefixColour >= 0)
			colour = prefixColour==1?FractalParticle.COLOUR_ORANGE: prefixColour==2?FractalParticle.COLOUR_RED: FractalParticle.COLOUR_LIGHTNING;
		FractalParticle.Data particle = new FractalParticle.Data(direction, scale, 10, 16, colour[0], colour[1]);
		world.addParticle(particle, x, y, z, 0, 0, 0);
	}

	public World getClientWorld()
	{
		return null;
	}

	public PlayerEntity getClientPlayer()
	{
		return null;
	}

	public void reInitGui()
	{
	}

	public void clearRenderCaches()
	{
	}

	public void startSkyhookSound(SkylineHookEntity hook)
	{
	}

	public void openManual()
	{

	}

	public void openTileScreen(String guiId, TileEntity tileEntity)
	{
	}

	public Item.Properties useIEOBJRenderer(Item.Properties props)
	{
		return props;
	}
}