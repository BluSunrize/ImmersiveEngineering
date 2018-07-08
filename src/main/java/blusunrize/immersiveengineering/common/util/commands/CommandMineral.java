/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.common.IESaveData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommandMineral extends CommandTreeBase
{
	{
		addSubcommand(new CommandMineralList());
		addSubcommand(new CommandMineralGet());
		addSubcommand(new CommandMineralSet());
		addSubcommand(new CommandMineralSetDepletion());
		addSubcommand(new CommandTreeHelp(this));
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "mineral";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender)
	{
		return "Use \"/ie mineral help\" for more information";
	}

	@Nonnull
	@Override
	public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args,
										  BlockPos pos)
	{
		ArrayList<String> list = new ArrayList<>();
		// subcommand argument autocomplete
		if(args.length > 1)
		{
			switch(args[0])
			{
				case "set":
					if(args.length > 2)
						break;
					for(MineralMix mineralMix : ExcavatorHandler.mineralList.keySet())
						if(args[1].isEmpty()||mineralMix.name.toLowerCase(Locale.ENGLISH).startsWith(args[1].toLowerCase(Locale.ENGLISH)))
							list.add(mineralMix.name);
					break;
			}
			return list;
		}
		return super.getTabCompletions(server, sender, args, pos);
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	private class CommandMineralList extends CommandBase
	{
		@Nonnull
		@Override
		public String getName()
		{
			return "list";
		}

		@Nonnull
		@Override
		public String getUsage(@Nonnull ICommandSender sender)
		{
			return "/mineral list";
		}

		@Override
		public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args)
		{
			StringBuilder s = new StringBuilder();
			int i = 0;
			for(MineralMix mm : ExcavatorHandler.mineralList.keySet())
				s.append((i++) > 0?", ": "").append(mm.name);
			sender.sendMessage(new TextComponentString(s.toString()));
		}
	}

	private class CommandMineralGet extends CommandBase
	{
		@Nonnull
		@Override
		public String getName()
		{
			return "get";
		}

		@Nonnull
		@Override
		public String getUsage(@Nonnull ICommandSender sender)
		{
			return "/mineral get";
		}

		@Override
		public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args)
		{
			MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getEntityWorld(),
					sender.getPosition().getX() >> 4, sender.getPosition().getZ() >> 4);
			sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+
					CommandMineral.this.getName()+".get",
					TextFormatting.GOLD+(info.mineral!=null?info.mineral.name: "null")+TextFormatting.RESET,
					TextFormatting.GOLD+(info.mineralOverride!=null?info.mineralOverride.name: "null")+TextFormatting.RESET,
					TextFormatting.GOLD+(""+info.depletion)+TextFormatting.RESET));
		}
	}

	private class CommandMineralSet extends CommandBase
	{
		@Nonnull
		@Override
		public String getName()
		{
			return "set";
		}

		@Nonnull
		@Override
		public String getUsage(@Nonnull ICommandSender sender)
		{
			return "/mineral set <mineral name> (surround the name in <angle brackets> if it contains a space)";
		}

		@Override
		public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
		{

			MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getEntityWorld(),
					sender.getPosition().getX() >> 4, sender.getPosition().getZ() >> 4);
			if(args.length < 1)
				throw new CommandException("Need exactly one parameter");

			StringBuilder name = new StringBuilder();
			for(int i = 0; i < args.length; i++)
			{
				name.append(args[i]);
				if(i < args.length-1)
					name.append(" ");
			}
			MineralMix mineral = null;
			for(MineralMix mm : ExcavatorHandler.mineralList.keySet())
				if(mm.name.equalsIgnoreCase(name.toString()))
					mineral = mm;
			if(mineral==null)
				throw new CommandException(Lib.CHAT_COMMAND+
						CommandMineral.this.getName()+".set.invalidMineral", name.toString());
			info.mineralOverride = mineral;
			sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+
					CommandMineral.this.getName()+".set.sucess", mineral.name));
			IESaveData.setDirty(sender.getEntityWorld().provider.getDimension());
		}
	}

	private class CommandMineralSetDepletion extends CommandBase
	{
		@Nonnull
		@Override
		public String getName()
		{
			return "setDepletion";
		}

		@Nonnull
		@Override
		public String getUsage(@Nonnull ICommandSender sender)
		{
			return "/mineral setDepletion <depletion>";
		}

		@Override
		public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
		{

			MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getEntityWorld(),
					sender.getPosition().getX() >> 4, sender.getPosition().getZ() >> 4);
			if(args.length!=1)
				throw new CommandException("Need exactly one parameter");
			int depl = parseInt(args[0].trim());
			info.depletion = depl;
			sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+CommandMineral.this.getName()+".setDepletion.sucess", (depl < 0?I18n.translateToLocal(Lib.CHAT_INFO+"coreDrill.infinite"): Integer.toString(depl))));
			IESaveData.setDirty(sender.getEntityWorld().provider.getDimension());
		}
	}
}