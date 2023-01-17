/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.client.fx.FractalOptions;
import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class CommonProxy
{
	public void onWorldLoad()
	{
	}

	public void resetManual()
	{
	}

	public void handleTileSound(Supplier<SoundEvent> soundEvent, BlockEntity tile, boolean tileActive, float volume, float pitch)
	{
	}

	public void spawnFractalFX(Level world, double x, double y, double z, Vec3 direction, double scale, int prefixColour, float[][] colour)
	{
		if(prefixColour >= 0)
			colour = prefixColour==1?FractalParticle.COLOUR_ORANGE: prefixColour==2?FractalParticle.COLOUR_RED: FractalParticle.COLOUR_LIGHTNING;
		FractalOptions particle = new FractalOptions(direction, scale, 10, 16, colour[0], colour[1]);
		world.addParticle(particle, x, y, z, 0, 0, 0);
	}

	public Level getClientWorld()
	{
		return null;
	}

	public Player getClientPlayer()
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

	public void openTileScreen(String guiId, BlockEntity tileEntity)
	{
	}
}