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
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.excavator.MineralWorldInfo;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.Utils;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.Pair;

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
				.then(putMineral())
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
			getMineral(command, new ColumnPos(player.getPosition()));
			return Command.SINGLE_SUCCESS;
		}).then(
				Commands.argument("location", ColumnPosArgument.columnPos())
						.executes(command -> {
							ColumnPos pos = ColumnPosArgument.func_218101_a(command, "location");
							getMineral(command, pos);
							return Command.SINGLE_SUCCESS;
						})
		);
		return get;
	}

	private static void getMineral(CommandContext<CommandSource> context, ColumnPos pos)
	{
		CommandSource sender = context.getSource();
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getWorld(), pos);
		StringTextComponent ret = new StringTextComponent("");
		if(info==null||info.getTotalWeight()==0)
			ret.appendSibling(new TranslationTextComponent(Lib.CHAT_COMMAND+"mineral.get.none", pos.x, pos.z));
		else
		{
			ret.appendSibling(new TranslationTextComponent(Lib.CHAT_COMMAND+"mineral.get", pos.x, pos.z));
			for(Pair<MineralVein, Integer> pair : info.getAllVeins())
			{
				MineralVein vein = pair.getLeft();
				double percentage = pair.getRight()/(double)info.getTotalWeight();
				ITextComponent component = new StringTextComponent("\n "+Utils.formatDouble(percentage*100, "0.00")+"% ");
				component.appendSibling(new TranslationTextComponent(vein.getActualMineral().getTranslationKey()));
				ret.appendSibling(component.applyTextStyle(TextFormatting.GRAY));
				component = new StringTextComponent("\n  ");
				component.appendSibling(new TranslationTextComponent(Lib.CHAT_COMMAND+"mineral.get.pos", vein.getPos().x, vein.getPos().z));
				component.appendText("\n  ");
				if(ExcavatorHandler.mineralVeinYield==0)
					component.appendSibling(new TranslationTextComponent(Lib.DESC_INFO+"coresample.infinite"));
				else
					component.appendSibling(new TranslationTextComponent(Lib.DESC_INFO+"coresample.yield",
							ExcavatorHandler.mineralVeinYield-vein.getDepletion()));
				ret.appendSibling(component.applyTextStyle(TextFormatting.DARK_GRAY));
			}
		}
		sender.sendFeedback(ret, true);
	}

	private static LiteralArgumentBuilder<CommandSource> putMineral()
	{
		LiteralArgumentBuilder<CommandSource> set = Commands.literal("put");
		set.requires(source -> source.hasPermissionLevel(2))
				.then(Commands.argument("mineral", new MineralArgument())
						.then(Commands.argument("radius", IntegerArgumentType.integer(8, 64))
								.then(Commands.argument("pos", ColumnPosArgument.columnPos())
										.executes(ctx -> {
											putMineral(ctx, ColumnPosArgument.func_218101_a(ctx, "pos"));
											return Command.SINGLE_SUCCESS;
										}))
								.executes(ctx -> {
									putMineral(ctx, columnPos(ctx.getSource().getPos()));
									return Command.SINGLE_SUCCESS;
								})
						)
				);
		return set;
	}

	private static void putMineral(CommandContext<CommandSource> context, ColumnPos pos)
	{
		CommandSource sender = context.getSource();
		MineralMix mineral = context.getArgument("mineral", MineralMix.class);
		int radius = IntegerArgumentType.getInteger(context, "radius");
		if(mineral!=null)
		{
			MineralVein vein = new MineralVein(pos, mineral, radius);
			ExcavatorHandler.getMineralVeinList().put(sender.getWorld().getDimension().getType(), vein);
			IESaveData.setDirty();
			sender.sendFeedback(new TranslationTextComponent(Lib.CHAT_COMMAND+
					"mineral.put.success", mineral.getId(), radius, pos.x, pos.z), true);
		}
		else
			sender.sendFeedback(new TranslationTextComponent(Lib.CHAT_COMMAND+
					"mineral.put.invalidMineral", mineral.getId()), true);
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
		// TODO
//		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getWorld(), xChunk, zChunk);
//		int depl = context.getArgument("depletion", Integer.class);
//		info.depletion = depl;
		//sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+CommandMineral.this.getName()+".setDepletion.sucess",
		//TODO localization on the server?		(depl < 0?I18n.translateToLocal(Lib.CHAT_INFO+"coreDrill.infinite"): Integer.toString(depl))));
	}

	private static ColumnPos columnPos(Vec3d vec)
	{
		return new ColumnPos((int)vec.x, (int)vec.z);
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