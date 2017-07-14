package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler.IESubCommand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Locale;

import static net.minecraft.command.CommandBase.parseInt;

public class CommandWireGrid extends IESubCommand
{
	@Override
	public String getIdent()
	{
		return "genWireGrid";
	}

	@Override
	public void perform(CommandHandler handler, MinecraftServer server, ICommandSender sender, String[] args)
	{
		try
		{
			int minX = parseInt(args[1]);
			int y = parseInt(args[2]);
			int minZ = parseInt(args[3]);
			int maxX = parseInt(args[4]);
			int maxZ = parseInt(args[6]);
			World w = sender.getEntityWorld();
			IBlockState hvRelay = IEContent.blockConnectors.getDefaultState();
			hvRelay = hvRelay.withProperty(IEContent.blockConnectors.property, BlockTypes_Connector.RELAY_HV)
					.withProperty(IEProperties.FACING_ALL, EnumFacing.DOWN);
			for (int x = minX;x<maxX;x++)
				for (int z = minZ;z<maxZ;z++)
				{
					w.setBlockState(new BlockPos(x, y, z), hvRelay);
				}
			for (int x = minX;x<maxX-1;x++)
				for (int z = minZ;z<maxZ-1;z++)
				{
					BlockPos here = new BlockPos(x, y, z);
					ImmersiveNetHandler.INSTANCE.addConnection(w, here, here.add(1, 0, 0), 1, WireType.STEEL);
					ImmersiveNetHandler.INSTANCE.addConnection(w, here, here.add(0, 0, 1), 1, WireType.STEEL);
					ImmersiveNetHandler.INSTANCE.addConnection(w, here, here.add(1, 0, 1), 1, WireType.STEEL);
				}
		} catch (NumberInvalidException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server, ICommandSender sender, String[]args)
	{
		return new ArrayList<>();
	}

	@Override
	public int getPermissionLevel()
	{
		return 4;
	}
}