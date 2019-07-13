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

import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.common.blocks.metal.BucketWheelTileEntity;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.UUID;

public class CommonProxy
{
	public void preInit()
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

	public void addFailedConnection(Connection connection, BlockPos reason, PlayerEntity player)
	{
	}

	public void reloadManual()
	{
	}

	public void handleTileSound(SoundEvent soundEvent, TileEntity tile, boolean tileActive, float volume, float pitch)
	{
	}

	public void stopTileSound(String soundName, TileEntity tile)
	{
	}

	public void spawnBucketWheelFX(BucketWheelTileEntity tile, ItemStack stack)
	{
	}

	public void spawnSparkFX(World world, double x, double y, double z, double mx, double my, double mz)
	{
	}

	public void spawnRedstoneFX(World world, double x, double y, double z, double mx, double my, double mz, float size, float r, float g, float b)
	{
	}

	public void spawnFluidSplashFX(World world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
	}

	public void spawnBubbleFX(World world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
	}

	public void spawnFractalFX(World world, double x, double y, double z, Vec3d direction, double scale, int prefixColour, float[][] colour)
	{
	}

	public void draw3DBlockCauldron()
	{
	}

	public void drawSpecificFluidPipe(String configuration)
	{
	}

	public boolean armorHasCustomModel(ItemStack stack)
	{
		return false;
	}

	public boolean drawConveyorInGui(String conveyor, Direction facing)
	{
		return false;
	}

	public void drawFluidPumpTop()
	{
	}

	public String[] splitStringOnWidth(String s, int w)
	{
		return new String[]{s};
	}

	public World getClientWorld()
	{
		return null;
	}

	public PlayerEntity getClientPlayer()
	{
		return null;
	}

	public String getNameFromUUID(String uuid)
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().getMinecraftSessionService().fillProfileProperties(new GameProfile(UUID.fromString(uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), null), false).getName();
	}

	public void reInitGui()
	{
	}

	public void removeStateFromSmartModelCache(IExtendedBlockState state)
	{
	}

	public void removeStateFromConnectionModelCache(IExtendedBlockState state)
	{
	}

	public void clearConnectionModelCache()
	{
	}

	public void clearRenderCaches()
	{
	}

	public void startSkyhookSound(EntitySkylineHook hook)
	{
	}
}