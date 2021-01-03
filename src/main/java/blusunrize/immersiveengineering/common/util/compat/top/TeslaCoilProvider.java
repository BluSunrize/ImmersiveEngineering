package blusunrize.immersiveengineering.common.util.compat.top;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class TeslaCoilProvider implements IProbeInfoProvider
{

	@Override
	public String getID()
	{
		return ImmersiveEngineering.MODID+":"+"TeslaCoilInfo";
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world,
		BlockState blockState, IProbeHitData data)
	{
		TileEntity tileEntity = world.getTileEntity(data.getPos());
		
		if(tileEntity instanceof TeslaCoilTileEntity)
		{
			TeslaCoilTileEntity teslaCoil = (TeslaCoilTileEntity) tileEntity;
			if(teslaCoil.isDummy())
			{
				tileEntity = world.getTileEntity(data.getPos().offset(teslaCoil.getFacing(), -1));

				if(tileEntity instanceof TeslaCoilTileEntity)
				{
					teslaCoil = (TeslaCoilTileEntity) tileEntity;
				}
				else
				{
					probeInfo.text(new StringTextComponent("<ERROR>"));
					return;
				}
			}

			probeInfo.text(new TranslationTextComponent(
				Lib.CHAT_INFO+"rsControl." + 
				(teslaCoil.redstoneControlInverted?"invertedOn": "invertedOff")
			));

			probeInfo.text(new TranslationTextComponent(
				Lib.CHAT_INFO+"tesla." + 
				(teslaCoil.lowPower?"lowPower": "highPower")
			));

		}
	}
}
