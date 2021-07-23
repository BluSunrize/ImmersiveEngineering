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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
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
				new EmptyArgumentSerializer<>(MineralArgument::new));
	}

	public static LiteralArgumentBuilder<CommandSourceStack> create()
	{
		LiteralArgumentBuilder<CommandSourceStack> main = Commands.literal("mineral");
		main
				.then(listMineral())
				.then(getMineral())
				.then(putMineral())
				.then(setMineralDepletion());
		return main;
	}

	private static LiteralArgumentBuilder<CommandSourceStack> listMineral()
	{
		LiteralArgumentBuilder<CommandSourceStack> list = Commands.literal("list");
		list.executes(command -> {
			StringBuilder s = new StringBuilder();
			int i = 0;
			for(MineralMix mm : MineralMix.mineralList.values())
				s.append((i++) > 0?", ": "").append(mm.getId());
			command.getSource().sendSuccess(new TextComponent(s.toString()), true);
			return Command.SINGLE_SUCCESS;
		});
		return list;
	}

	private static LiteralArgumentBuilder<CommandSourceStack> getMineral()
	{
		LiteralArgumentBuilder<CommandSourceStack> get = Commands.literal("get");
		get.requires(source -> source.hasPermission(2)).executes(command -> {
			ServerPlayer player = command.getSource().getPlayerOrException();
			getMineral(command, new ColumnPos(player.blockPosition()));
			return Command.SINGLE_SUCCESS;
		}).then(
				Commands.argument("location", ColumnPosArgument.columnPos())
						.executes(command -> {
							ColumnPos pos = ColumnPosArgument.getColumnPos(command, "location");
							getMineral(command, pos);
							return Command.SINGLE_SUCCESS;
						})
		);
		return get;
	}

	private static void getMineral(CommandContext<CommandSourceStack> context, ColumnPos pos)
	{
		CommandSourceStack sender = context.getSource();
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getLevel(), pos);
		TextComponent ret = new TextComponent("");
		if(info==null||info.getTotalWeight()==0)
			ret.append(new TranslatableComponent(Lib.CHAT_COMMAND+"mineral.get.none", pos.x, pos.z));
		else
		{
			ret.append(new TranslatableComponent(Lib.CHAT_COMMAND+"mineral.get", pos.x, pos.z));
			for(Pair<MineralVein, Integer> pair : info.getAllVeins())
			{
				MineralVein vein = pair.getLeft();
				double percentage = pair.getRight()/(double)info.getTotalWeight();
				MutableComponent component = new TextComponent("\n "+Utils.formatDouble(percentage*100, "0.00")+"% ");
				component.append(new TranslatableComponent(vein.getMineral().getTranslationKey()));
				ret.append(component.withStyle(ChatFormatting.GRAY));
				component = new TextComponent("\n  ");
				component.append(new TranslatableComponent(Lib.CHAT_COMMAND+"mineral.get.pos",
						vein.getPos().x, vein.getPos().z, vein.getRadius()));
				component.append("\n  ");
				if(ExcavatorHandler.mineralVeinYield==0)
					component.append(new TranslatableComponent(Lib.DESC_INFO+"coresample.infinite"));
				else
					component.append(new TranslatableComponent(Lib.DESC_INFO+"coresample.yield",
							ExcavatorHandler.mineralVeinYield-vein.getDepletion()));
				ret.append(component.withStyle(ChatFormatting.GRAY));
			}
		}
		sender.sendSuccess(ret, true);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> putMineral()
	{
		LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("put");
		set.requires(source -> source.hasPermission(2))
				.then(Commands.argument("mineral", new MineralArgument())
						.then(Commands.argument("radius", IntegerArgumentType.integer(8, 64))
								.then(Commands.argument("pos", ColumnPosArgument.columnPos())
										.executes(ctx -> {
											putMineral(ctx, ColumnPosArgument.getColumnPos(ctx, "pos"));
											return Command.SINGLE_SUCCESS;
										}))
								.executes(ctx -> {
									putMineral(ctx, columnPos(ctx.getSource().getPosition()));
									return Command.SINGLE_SUCCESS;
								})
						)
				);
		return set;
	}

	private static void putMineral(CommandContext<CommandSourceStack> context, ColumnPos pos)
	{
		CommandSourceStack sender = context.getSource();
		MineralMix mineral = context.getArgument("mineral", MineralMix.class);
		int radius = IntegerArgumentType.getInteger(context, "radius");
		if(mineral!=null)
		{
			MineralVein vein = new MineralVein(pos, mineral.getId(), radius);
			ExcavatorHandler.addVein(sender.getLevel().dimension(), vein);
			IESaveData.markInstanceDirty();
			sender.sendSuccess(new TranslatableComponent(Lib.CHAT_COMMAND+
					"mineral.put.success", mineral.getId(), radius, pos.x, pos.z), true);
		}
		else
			sender.sendSuccess(new TranslatableComponent(Lib.CHAT_COMMAND+
					"mineral.put.invalid_mineral", mineral.getId()), true);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> setMineralDepletion()
	{
		LiteralArgumentBuilder<CommandSourceStack> setDepletion = Commands.literal("setDepletion");
		setDepletion.requires(source -> source.hasPermission(2))
				.then(Commands.argument("depletion", IntegerArgumentType.integer(0, ExcavatorHandler.mineralVeinYield))
						.then(Commands.argument("pos", ColumnPosArgument.columnPos())
								.executes(ctx -> {
									setMineralDepletion(ctx, ColumnPosArgument.getColumnPos(ctx, "pos"));
									return Command.SINGLE_SUCCESS;
								}))
						.executes(ctx -> {
							setMineralDepletion(ctx, columnPos(ctx.getSource().getPosition()));
							return Command.SINGLE_SUCCESS;
						})
				);

		return setDepletion;
	}

	private static void setMineralDepletion(CommandContext<CommandSourceStack> context, ColumnPos pos)
	{
		CommandSourceStack sender = context.getSource();
		MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getLevel(), pos);
		if(info!=null&&info.getTotalWeight() > 0)
		{
			int depletion = IntegerArgumentType.getInteger(context, "depletion");
			for(Pair<MineralVein, Integer> pair : info.getAllVeins())
				pair.getLeft().setDepletion(depletion);
			IESaveData.markInstanceDirty();
			sender.sendSuccess(new TranslatableComponent(Lib.CHAT_COMMAND+
					"mineral.set_depletion.success", depletion), true);
		}
		else
			sender.sendSuccess(new TranslatableComponent(Lib.CHAT_COMMAND+
					"mineral.set_depletion.no_mineral", pos.x, pos.z), true);
	}

	private static ColumnPos columnPos(Vec3 vec)
	{
		return new ColumnPos((int)vec.x, (int)vec.z);
	}

	private static class MineralArgument implements ArgumentType<MineralMix>
	{
		public static final DynamicCommandExceptionType invalidVein = new DynamicCommandExceptionType(
				(input) -> new TranslatableComponent(Lib.CHAT_COMMAND+"mineral.invalid", input));

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
			return SharedSuggestionProvider.suggest(MineralMix.mineralList.values().stream().map(mix -> "\""+mix.getId()+"\""), builder);
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
