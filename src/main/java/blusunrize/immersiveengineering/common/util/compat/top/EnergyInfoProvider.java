package blusunrize.immersiveengineering.common.util.compat.top;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class EnergyInfoProvider implements IProbeInfoProvider, IProbeConfigProvider
{

	@Override
	public String getID()
	{
		return ImmersiveEngineering.MODID+":"+"EnergyInfo";
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world,
		BlockState blockState, IProbeHitData data)
	{
		TileEntity tileEntity = world.getTileEntity(data.getPos());
		int cur = 0;
		int max = 0;
		if (tileEntity instanceof IFluxReceiver)
		{
			cur = ((IFluxReceiver) tileEntity).getEnergyStored(null);
			max = ((IFluxReceiver) tileEntity).getMaxEnergyStored(null);
		}
		else if (tileEntity instanceof IFluxProvider)
		{
			cur = ((IFluxProvider) tileEntity).getEnergyStored(null);
			max = ((IFluxProvider) tileEntity).getMaxEnergyStored(null);
		}
		if (max > 0)
		{
			probeInfo.progress(cur, max,
					probeInfo.defaultProgressStyle()
							.suffix("IF")
							.filledColor(Lib.COLOUR_I_ImmersiveOrange)
							.alternateFilledColor(0xff994f20)
							.borderColor(Lib.COLOUR_I_ImmersiveOrangeShadow)
							.numberFormat(NumberFormat.COMPACT));
		}
	}

	@Override
	public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world, Entity entity,
		IProbeHitEntityData data)
	{
	}

	@Override
	public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world,
		BlockState blockState, IProbeHitData data)
	{
		TileEntity tileEntity = world.getTileEntity(data.getPos());
		if(tileEntity instanceof IFluxReceiver||tileEntity instanceof IFluxProvider)
		{
			config.setRFMode(0);				
		}
	}
}
