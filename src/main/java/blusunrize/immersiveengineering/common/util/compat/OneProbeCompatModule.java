/*
 * BluSunrize, RobustProgram
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.SheetmetalTankTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilTileEntity;
import com.google.common.base.Function;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.InterModComms;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @author BluSunrize, Robustprogram - 3.1.2020
 */
public class OneProbeCompatModule extends IECompatModule implements Function<ITheOneProbe, Void>
{
	@Override
	public void preInit()
	{
		Supplier<Function<ITheOneProbe, Void>> supplier = () -> this;
		InterModComms.sendTo("theoneprobe", "getTheOneProbe", supplier);
	}

	@Override
	public void registerRecipes() { }

	@Override
	public void init() { }

	@Override
	public void postInit() { }

	@Nullable
	@Override
	public Void apply(@Nullable ITheOneProbe input)
	{
		EnergyInfoProvider energyInfo = new EnergyInfoProvider();
		input.registerProvider(energyInfo);
		input.registerProbeConfigProvider(energyInfo);
		input.registerProvider(new ProcessProvider());
		input.registerProvider(new TeslaCoilProvider());
		input.registerProvider(new SideConfigProvider());
		input.registerProvider(new FluidInfoProvider());
		input.registerBlockDisplayOverride(new MultiblockDisplayOverride());
		return null;
	}

	public static class FluidInfoProvider implements IProbeInfoProvider
	{

		@Override
		public String getID()
		{
			return ImmersiveEngineering.MODID+":"+"FluidInfo";
		}

		@Override
		public void addProbeInfo(
			ProbeMode mode,
			IProbeInfo probeInfo,
			PlayerEntity player,
			World world,
			BlockState blockState,
			IProbeHitData data
		)
		{
			TileEntity tileEntity = world.getTileEntity(data.getPos());
			if(tileEntity instanceof SheetmetalTankTileEntity)
			{
				SheetmetalTankTileEntity master = ((SheetmetalTankTileEntity) tileEntity).master();
				int current = master.tank.getFluidAmount();
				int max = master.tank.getCapacity();

				if(current > 0)
				{
					probeInfo.progress(current, max,
							probeInfo.defaultProgressStyle()
									.suffix("mB")
									.numberFormat(NumberFormat.COMPACT));
				}
			}
		}
	}


	public static class EnergyInfoProvider implements IProbeInfoProvider, IProbeConfigProvider
	{

		@Override
		public String getID()
		{
			return ImmersiveEngineering.MODID+":"+"EnergyInfo";
		}

		@Override
		public void addProbeInfo(
			ProbeMode mode,
			IProbeInfo probeInfo,
			PlayerEntity player,
			World world,
			BlockState blockState,
			IProbeHitData data
		)
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
		public void getProbeConfig(
			IProbeConfig config,
			PlayerEntity player,
			World world,
			Entity entity,
			IProbeHitEntityData data
		)
		{
		}

		@Override
		public void getProbeConfig(
			IProbeConfig config,
			PlayerEntity player,
			World world,
			BlockState blockState,
			IProbeHitData data
		)
		{
			TileEntity tileEntity = world.getTileEntity(data.getPos());
			if(tileEntity instanceof IFluxReceiver||tileEntity instanceof IFluxProvider)
			{
				config.setRFMode(0);				
			}
		}
	}

	public static class ProcessProvider implements IProbeInfoProvider
	{

		@Override
		public String getID()
		{
			return ImmersiveEngineering.MODID+":"+"ProcessInfo";
		}

		@Override
		public void addProbeInfo(
			ProbeMode mode,
			IProbeInfo probeInfo,
			PlayerEntity player,
			World world,
			BlockState blockState,
			IProbeHitData data
		)
		{
			TileEntity tileEntity = world.getTileEntity(data.getPos());
			if(tileEntity instanceof IEBlockInterfaces.IProcessTile)
			{
				int[] curTicks = ((IEBlockInterfaces.IProcessTile) tileEntity).getCurrentProcessesStep();
				int[] maxTicks = ((IEBlockInterfaces.IProcessTile) tileEntity).getCurrentProcessesMax();
				int h = Math.max(4, (int)Math.ceil(12/(float)curTicks.length));
	
				for(int i = 0; i < curTicks.length; i++)
				{
					if(maxTicks[i] > 0)
					{
						float current = curTicks[i]/(float)maxTicks[i]*100;
						probeInfo.progress(
							(int)current,
							100,
							probeInfo.defaultProgressStyle().showText(h >= 10).suffix("%").height(h)
						);
					}
				}
			}
		}
	}

	public static class TeslaCoilProvider implements IProbeInfoProvider
	{

		@Override
		public String getID()
		{
			return ImmersiveEngineering.MODID+":"+"TeslaCoilInfo";
		}

		@Override
		public void addProbeInfo(
			ProbeMode mode,
			IProbeInfo probeInfo,
			PlayerEntity player,
			World world,
			BlockState blockState,
			IProbeHitData data
		)
		{
			TileEntity tileEntity = world.getTileEntity(data.getPos());
			
			if(tileEntity instanceof TeslaCoilTileEntity)
			{
				TeslaCoilTileEntity teslaCoil = (TeslaCoilTileEntity) tileEntity;
				if(teslaCoil.isDummy())
				{
					tileEntity = world.getTileEntity(
							data.getPos().offset(teslaCoil.getFacing(), -1));

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
	
	public static class SideConfigProvider implements IProbeInfoProvider
	{

		@Override
		public String getID()
		{
			return ImmersiveEngineering.MODID+":"+"SideConfigInfo";
		}

		@Override
		public void addProbeInfo(
			ProbeMode mode,
			IProbeInfo probeInfo,
			PlayerEntity player,
			World world,
			BlockState blockState,
			IProbeHitData data
		)
		{
			TileEntity te = world.getTileEntity(data.getPos());
			if(te instanceof IEBlockInterfaces.IConfigurableSides&&data.getSideHit()!=null)
			{
				boolean flip = player.isSneaking();
				Direction side = flip ? data.getSideHit().getOpposite() : data.getSideHit();
				IOSideConfig config = ((IEBlockInterfaces.IConfigurableSides)te).getSideConfig(side);
				
				StringTextComponent combined = new StringTextComponent("");
				TranslationTextComponent direction =
						new TranslationTextComponent(Lib.DESC_INFO+"blockSide." + (flip?"opposite": "facing"));
				TranslationTextComponent connection = 
						new TranslationTextComponent(Lib.DESC_INFO+"blockSide.io." + config.getString());
				
				combined.append(direction);
				combined.appendString(": ");
				combined.append(connection);
				
				probeInfo.text(combined);
			}
		}
	}

	public static class MultiblockDisplayOverride implements IBlockDisplayOverride
	{
		@Override
		public boolean overrideStandardInfo(
			ProbeMode mode,
			IProbeInfo probeInfo,
			PlayerEntity player,
			World world,
			BlockState blockState,
			IProbeHitData data
		)
		{
			TileEntity te = world.getTileEntity(data.getPos());
			if(te instanceof MultiblockPartTileEntity)
			{
				ItemStack stack = new ItemStack(blockState.getBlock(), 1);
				if(Tools.show(mode, Config.getRealConfig().getShowModName()))
				{
					probeInfo.horizontal()
							.item(stack)
							.vertical()
							.itemLabel(stack)
							.text(new StringTextComponent(TextStyleClass.MODNAME+ImmersiveEngineering.MODNAME));
				}
				else
				{
					probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
							.item(stack)
							.itemLabel(stack);
				}
				return true;
			}
			return false;
		}
	}
}