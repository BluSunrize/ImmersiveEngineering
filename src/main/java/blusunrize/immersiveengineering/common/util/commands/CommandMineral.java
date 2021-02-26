/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.ImmersiveEngineering;
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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
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
	public static void registerArguments()
	{
		ArgumentTypes.register(ImmersiveEngineering.MODID+":mineral", MineralArgument.class,
				new ArgumentSerializer<>(MineralArgument::new));
	}

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
							ColumnPos pos = ColumnPosArgument.fromBlockPos(command, "location");
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
			ret.append(new TranslationTextComponent(Lib.CHAT_COMMAND+"mineral.get.none", pos.x, pos.z));
		else
		{
			ret.append(new TranslationTextComponent(Lib.CHAT_COMMAND+"mineral.get", pos.x, pos.z));
			for(Pair<MineralVein, Integer> pair : info.getAllVeins())
			{
				MineralVein vein = pair.getLeft();
				double percentage = pair.getRight()/(double)info.getTotalWeight();
				IFormattableTextComponent component = new StringTextComponent("\n "+Utils.formatDouble(percentage*100, "0.00")+"% ");
				component.append(new TranslationTextComponent(vein.getMineral().getTranslationKey()));
				ret.append(component.mergeStyle(TextFormatting.GRAY));
				component = new StringTextComponent("\n  ");
				component.append(new TranslationTextComponent(Lib.CHAT_COMMAND+"mineral.get.pos",
						vein.getPos().x, vein.getPos().z, vein.getRadius()));
				component.appendString("\n  ");
				if(ExcavatorHandler.mineralVeinYield==0)
					component.append(new TranslationTextComponent(Lib.DESC_INFO+"coresample.infinite"));
				else
					component.append(new TranslationTextComponent(Lib.DESC_INFO+"coresample.yield",
							ExcavatorHandler.mineralVeinYield-vein.getDepletion()));
				ret.append(component.mergeStyle(TextFormatting.GRAY));
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
											putMineral(ctx, ColumnPosArgument.fromBlockPos(ctx, "pos"));
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
			MineralVein vein = new MineralVein(pos, mineral.getId(), radius);
			ExcavatorHandler.addVein(sender.getWorld().getDimensionKey(), vein);
			IESaveData.setDirty();
			sender.sendFeedback(new TranslationTextComponent(Lib.CHAT_COMMAND+
					"mineral.put.success", mineral.getId(), radius, pos.x, pos.z), true);
		}
		else
			sender.sendFeedback(new TranslationTextComponent(Lib.CHAT_COMMAND+
					"mineral.put.invalid_mineral", mineral.getId()), true);
	}

	private static LiteralArgumentBuilder<CommandSource> setMineralDepletion()
	{
		LiteralArgumentBuilder<CommandSource> setDepletion = Commands.literal("setDepletion");
		setDepletion.requires(source -> source.hasPermissionLevel(2))
				.then(Commands.argument("depletion", IntegerArgumentType.integer(0, ExcavatorHandler.mineralVeinYield))
						.then(Commands.argument("pos", ColumnPosArgument.columnPos())
								.executes(ctx -> {
									setMineralDepletion(ctx, ColumnPosArgument.fromBlockPos(ctx, "pos"));
									return Command.SINGLE_SUCCESS;
								}))
						.executes(ctx -> {
							setMineralDepletion(ctx, columnPos(ctx.getSource().getPos()));
							return Command.SINGLE_SUCCESS;
						})
				);

		return setDepletion;
	}

	private static void setMineralDepletion(CommandContext<CommandSource> context, ColumnPos pos)
	{
		CommandSource sender = context.getSource();
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getWorld(), pos);
		if(info!=null&&info.getTotalWeight() > 0)
		{
			int depletion = IntegerArgumentType.getInteger(context, "depletion");
			for(Pair<MineralVein, Integer> pair : info.getAllVeins())
				pair.getLeft().setDepletion(depletion);
			IESaveData.setDirty();
			sender.sendFeedback(new TranslationTextComponent(Lib.CHAT_COMMAND+
					"mineral.set_depletion.success", depletion), true);
		}
		else
			sender.sendFeedback(new TranslationTextComponent(Lib.CHAT_COMMAND+
					"mineral.set_depletion.no_mineral", pos.x, pos.z), true);
	}

	private static ColumnPos columnPos(Vector3d vec)
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