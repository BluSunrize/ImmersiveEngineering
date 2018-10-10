/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends CommandTreeBase
{
	private final String name;

	public CommandHandler(boolean client)
	{
		if(client)
		{
			addSubcommand(new CommandResetRenders());
			name = "cie";
		}
		else
		{
			addSubcommand(new CommandMineral());
			addSubcommand(new CommandShaders());
			name = "ie";
		}
		addSubcommand(new CommandTreeHelp(this));
	}

	@Nonnull
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return name.equals("cie")?0: 4;
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender)
	{
		return "Use \"/"+name+" help\" for more information";
	}

	private static final String start = "<";
	private static final String end = ">";

	@Nonnull
	@Override
	public List<String> getTabCompletions(@Nullable MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos pos)
	{
		List<String> ret = super.getTabCompletions(server, sender, args, pos);
		for(int i = 0; i < ret.size(); i++)
		{
			String curr = ret.get(i);
			if(curr.indexOf(' ') >= 0)
			{
				ret.set(i, start+curr+end);
			}
		}
		return ret;
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) throws CommandException
	{
		List<String> argsCleaned = new ArrayList<>(args.length);
		String currentPart = null;
		for(String s : args)
		{
			if(s.startsWith(start))
			{
				if(currentPart!=null)
					throw new CommandException("String opens twice (once \""+currentPart+"\", once \""+s+"\")");
				currentPart = s;
			}
			else if(currentPart!=null)
				currentPart += " "+s;
			else
				argsCleaned.add(s);
			if(s.endsWith(end))
			{
				if(currentPart==null)
					throw new CommandException("String closed without being openeed first! (\""+s+"\")");
				if(currentPart.length() >= 2)
					argsCleaned.add(currentPart.substring(1, currentPart.length()-1));
				currentPart = null;
			}
		}
		if(currentPart!=null)
			throw new CommandException("Unclosed string ("+currentPart+")");
		super.execute(server, sender, argsCleaned.toArray(new String[0]));
	}
}