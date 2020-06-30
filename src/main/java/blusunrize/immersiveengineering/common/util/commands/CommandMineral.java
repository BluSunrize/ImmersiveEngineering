/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralWorldInfo;
import blusunrize.immersiveengineering.common.IESaveData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandMineral
{
	public static LiteralArgumentBuilder<CommandSource> create()
	{
		LiteralArgumentBuilder<CommandSource> main = Commands.literal("mineral");
		main
				.then(listMineral())
				.then(getMineral())
				.then(setMineral())
				.then(setMineralDepletion());
		return main;
	}

	private static LiteralArgumentBuilder<CommandSource> listMineral()
	{
		LiteralArgumentBuilder<CommandSource> list = Commands.literal("list");
		list.executes(command -> {
			StringBuilder s = new StringBuilder();
			int i = 0;
			for(MineralMix mm : MineralMix.mineralList.values())
				s.append((i++) > 0?", ": "").append(mm.getId());
			command.getSource().sendFeedback(new StringTextComponent(s.toString()), true);
			return Command.SINGLE_SUCCESS;
		});
		return list;
	}

	private static LiteralArgumentBuilder<CommandSource> getMineral()
	{
		LiteralArgumentBuilder<CommandSource> get = Commands.literal("get");
		get.requires(source -> source.hasPermissionLevel(2)).executes(command -> {
			ServerPlayerEntity player = command.getSource().asPlayer();
			getMineral(command, player.getPosition().getX() >> 4, player.getPosition().getZ() >> 4);
			return Command.SINGLE_SUCCESS;
		}).then(
				Commands.argument("location", ColumnPosArgument.columnPos())
						.executes(command -> {
							ColumnPos pos = ColumnPosArgument.func_218101_a(command, "location");
							getMineral(command, pos.x, pos.z);
							return Command.SINGLE_SUCCESS;
						})
		);
		return get;
	}

	private static void getMineral(CommandContext<CommandSource> context, int xChunk, int zChunk)
	{
		CommandSource sender = context.getSource();
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getWorld(),
				xChunk, zChunk);
		sender.sendFeedback(new TranslationTextComponent(Lib.CHAT_COMMAND+"mineral.get",
				TextFormatting.GOLD+(info.mineral!=null?info.mineral.getId().toString(): "null")+TextFormatting.RESET,
				TextFormatting.GOLD+(info.mineralOverride!=null?info.mineralOverride.getId().toString(): "null")+TextFormatting.RESET,
				TextFormatting.GOLD+(""+info.depletion)+TextFormatting.RESET), true);
	}

	private static LiteralArgumentBuilder<CommandSource> setMineral()
	{
		LiteralArgumentBuilder<CommandSource> set = Commands.literal("set");
		RequiredArgumentBuilder<CommandSource, MineralMix> mineralArg = Commands.argument("mineral", new MineralArgument());
		mineralArg.requires(source -> source.hasPermissionLevel(2)).executes(command -> {
			ServerPlayerEntity player = command.getSource().asPlayer();
			setMineral(command, player.getPosition().getX() >> 4, player.getPosition().getZ() >> 4);
			return Command.SINGLE_SUCCESS;
		}).then(
				Commands.argument("location", ColumnPosArgument.columnPos())
						.executes(command -> {
							ColumnPos pos = ColumnPosArgument.func_218101_a(command, "location");
							setMineral(command, pos.x, pos.z);
							return Command.SINGLE_SUCCESS;
						})
		);
		set.then(mineralArg);
		return set;
	}

	private static void setMineral(CommandContext<CommandSource> context, int xChunk, int zChunk)
	{
		CommandSource sender = context.getSource();
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getWorld(),
				xChunk, zChunk);
		MineralMix mineral = context.getArgument("mineral", MineralMix.class);
		info.mineralOverride = mineral;
		IESaveData.setDirty();
		sender.sendFeedback(new TranslationTextComponent(Lib.CHAT_COMMAND+
				"mineral.set.sucess", mineral.getId()), true);
	}

	private static LiteralArgumentBuilder<CommandSource> setMineralDepletion()
	{
		LiteralArgumentBuilder<CommandSource> setDepletion = Commands.literal("setDepletion");
		RequiredArgumentBuilder<CommandSource, Integer> mineralArg = Commands.argument("depletion",
				IntegerArgumentType.integer(-1, 100));
		mineralArg.requires(source -> source.hasPermissionLevel(2)).executes(command -> {
			ServerPlayerEntity player = command.getSource().asPlayer();
			setMineralDepletion(command, player.getPosition().getX() >> 4, player.getPosition().getZ() >> 4);
			return Command.SINGLE_SUCCESS;
		}).then(
				Commands.argument("location", ColumnPosArgument.columnPos())
						.executes(command -> {
							ColumnPos pos = ColumnPosArgument.func_218101_a(command, "location");
							setMineralDepletion(command, pos.x, pos.z);
							return Command.SINGLE_SUCCESS;
						})
		);
		setDepletion.then(mineralArg);
		return setDepletion;
	}

	private static void setMineralDepletion(CommandContext<CommandSource> context, int xChunk, int zChunk)
	{
		CommandSource sender = context.getSource();
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getWorld(), xChunk, zChunk);
		int depl = context.getArgument("depletion", Integer.class);
		info.depletion = depl;
		//sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+CommandMineral.this.getName()+".setDepletion.sucess",
		//TODO localization on the server?		(depl < 0?I18n.translateToLocal(Lib.CHAT_INFO+"coreDrill.infinite"): Integer.toString(depl))));
	}

	private static class MineralArgument implements ArgumentType<MineralMix>
	{
		public static final DynamicCommandExceptionType invalidVein = new DynamicCommandExceptionType(
				(input) -> new TranslationTextComponent(Lib.CHAT_COMMAND+"mineral.invalid", input));

		@Override
		public MineralMix parse(StringReader reader) throws CommandSyntaxException
		{
			String name = reader.readQuotedString();//TODO does this work properly?
			for(MineralMix mm : MineralMix.mineralList.values())
				if(mm.getId().toString().equalsIgnoreCase(name))
					return mm;
			throw invalidVein.create(name);
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
		{
			return ISuggestionProvider.suggest(MineralMix.mineralList.values().stream().map(mix -> "\""+mix.getId()+"\""), builder);
		}

		@Override
		public Collection<String> getExamples()
		{
			List<String> ret = new ArrayList<>();
			for(MineralMix mix : MineralMix.mineralList.values())
			{
				ret.add("\""+mix.getId()+"\"");
				if(ret.size() > 5)
					break;
			}
			return ret;
		}
	}
}