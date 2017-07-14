package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CommandHandler extends CommandBase
{
	ArrayList<IESubCommand> commands = new ArrayList<>();
	final String name;
	public CommandHandler(boolean client)
	{
		commands.add(new CommandHelp());
		if (client)
		{
			commands.add(new CommandResetRenders());
			name = "cie";
		}
		else
		{
			commands.add(new CommandMineral());
			commands.add(new CommandShaders());
			commands.add(new CommandWireGrid());
			name = "ie";
		}
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	@Override
	public List<String> getAliases()
	{
		return Collections.emptyList();
	}

//	/**
//	 * Check if the given ICommandSender has permission to execute this command
//	 */
//	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
//	{
//	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		ArrayList<String> list = new ArrayList<String>();
		if(args.length>0)
			for(IESubCommand sub : commands)
			{
				if(args.length==1)
				{
					if(args[0].isEmpty() || sub.getIdent().startsWith(args[0].toLowerCase(Locale.ENGLISH)))
						list.add(sub.getIdent());
				}
				else if(sub.getIdent().equalsIgnoreCase(args[0]))
				{
					String[] redArgs = new String[args.length-1];
					System.arraycopy(args,1, redArgs,0, redArgs.length);
					ArrayList<String> subCommands = sub.getSubCommands(this, server, sender, redArgs);
					if(subCommands!=null)
						list.addAll(subCommands);
				}
			}
		return list;
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		String sub = "";
		int i=0;
		for(IESubCommand com : commands)
			sub += ((i++)>0?"|":"")+com.getIdent();
		return "/"+name+" <"+sub+">";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws NumberInvalidException
	{
		if(args.length>0)
		{
			if ("genWireGrid".equalsIgnoreCase(args[0]))
			{
				int minX = parseInt(args[1]);
				int y = parseInt(args[2]);
				int minZ = parseInt(args[3]);
				int maxX = parseInt(args[4]);
				int maxZ = parseInt(args[5]);
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
			}
			else
			{
				for(IESubCommand com : commands)
				{
					if(com.getIdent().equalsIgnoreCase(args[0]))
					{
						if(!sender.canUseCommand(com.getPermissionLevel(), this.getName()))
						{
							TextComponentTranslation msg = new TextComponentTranslation("commands.generic.permission");
							msg.getStyle().setColor(TextFormatting.RED);
							sender.sendMessage(msg);
						}
						else
							com.perform(this, server, sender, args);
					}
				}
			}
		}
		else
		{
			String sub = "";
			int i=0;
			for(IESubCommand com : commands)
				sub += ((i++)>0?", ":"")+com.getIdent();
			sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+"available",sub));
		}
	}

	public abstract static class IESubCommand
	{
		public abstract String getIdent();
		public abstract void perform(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args);
		public String getHelp(String subIdent)
		{
			return Lib.CHAT_COMMAND+getIdent()+subIdent+".help";
		}
		public abstract ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server,  ICommandSender sender, String[] args);
		public abstract int getPermissionLevel();
	}
}