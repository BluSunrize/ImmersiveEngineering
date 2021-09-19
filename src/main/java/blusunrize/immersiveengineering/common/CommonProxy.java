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

import blusunrize.immersiveengineering.client.fx.IEParticles;
import blusunrize.immersiveengineering.common.blocks.metal.BucketWheelTileEntity;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import blusunrize.immersiveengineering.common.gui.GuiHandler;
import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class CommonProxy
{
	public void modConstruction()
	{
	}

	public void preInit(ParallelDispatchEvent ev)
	{
	}

	public void preInitEnd()
	{
	}

	public void init()
	{
	}

	public void initEnd()
	{
	}

	public void postInit()
	{
	}

	public void postInitEnd()
	{
	}

	public void serverStarting()
	{
	}

	public void onWorldLoad()
	{
	}

	public void resetManual()
	{
	}

	public boolean isSoundPlaying(String key)
	{
		return true;
	}

	public void playTickableSound(SoundEvent soundEvent, SoundSource category, String key, float volume, float pitch, Supplier<Boolean> tickFunction)
	{
	}

	public void handleTileSound(SoundEvent soundEvent, BlockEntity tile, boolean tileActive, float volume, float pitch)
	{
	}

	public void stopTileSound(String soundName, BlockEntity tile)
	{
	}

	public void spawnBucketWheelFX(BucketWheelTileEntity tile, ItemStack stack)
	{
	}

	public void spawnSparkFX(Level world, double x, double y, double z, double mx, double my, double mz)
	{
		world.addParticle(IEParticles.SPARKS, x, y, z, mx, my, mz);
	}

	public void spawnRedstoneFX(Level world, double x, double y, double z, double mx, double my, double mz, float size, float r, float g, float b)
	{
	}

	public void spawnFluidSplashFX(Level world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
	}

	public void spawnBubbleFX(Level world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
	}

	public void spawnFractalFX(Level world, double x, double y, double z, Vec3 direction, double scale, int prefixColour, float[][] colour)
	{
	}

	public Level getClientWorld()
	{
		return null;
	}

	public Player getClientPlayer()
	{
		return null;
	}

	public String getNameFromUUID(String uuid)
	{
		return ServerLifecycleHooks.getCurrentServer().getSessionService()
				.fillProfileProperties(new GameProfile(UUID.fromString(uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), null), false).getName();
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

	public void openTileScreen(ResourceLocation guiId, BlockEntity tileEntity)
	{
	}

	public void registerContainersAndScreens()
	{
		GuiHandler.commonInit();
	}

	public Item.Properties useIEOBJRenderer(Item.Properties props)
	{
		return props;
	}
}