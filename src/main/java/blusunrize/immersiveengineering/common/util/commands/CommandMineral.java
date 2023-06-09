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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class CommandMineral
{
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
			for(MineralMix mm : MineralMix.RECIPES.getRecipes(command.getSource().getLevel()))
				s.append((i++) > 0?", ": "").append(mm.getId());
			command.getSource().sendSuccess(() -> Component.literal(s.toString()), true);
			return Command.SINGLE_SUCCESS;
		});
		return list;
	}

	private static LiteralArgumentBuilder<CommandSourceStack> getMineral()
	{
		LiteralArgumentBuilder<CommandSourceStack> get = Commands.literal("get");
		get.requires(source -> source.hasPermission(2)).executes(command -> {
			ServerPlayer player = command.getSource().getPlayerOrException();
			BlockPos playerPos = player.blockPosition();
			getMineral(command, new ColumnPos(playerPos.getX(), playerPos.getZ()));
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
		MutableComponent ret = Component.literal("");
		if(info==null||info.getTotalWeight()==0)
			ret.append(Component.translatable(Lib.CHAT_COMMAND+"mineral.get.none", pos.x(), pos.z()));
		else
		{
			ret.append(Component.translatable(Lib.CHAT_COMMAND+"mineral.get", pos.x(), pos.z()));
			for(Pair<MineralVein, Integer> pair : info.getAllVeins())
			{
				MineralVein vein = pair.getFirst();
				double percentage = pair.getSecond()/(double)info.getTotalWeight();
				MutableComponent component = Component.literal("\n "+Utils.formatDouble(percentage*100, "0.00")+"% ");
				component.append(Component.translatable(vein.getMineral(context.getSource().getLevel()).getTranslationKey()));
				ret.append(component.withStyle(ChatFormatting.GRAY));
				component = Component.literal("\n  ");
				component.append(Component.translatable(Lib.CHAT_COMMAND+"mineral.get.pos",
						vein.getPos().x(), vein.getPos().z(), vein.getRadius()));
				component.append("\n  ");
				if(ExcavatorHandler.mineralVeinYield==0)
					component.append(Component.translatable(Lib.DESC_INFO+"coresample.infinite"));
				else
					component.append(Component.translatable(Lib.DESC_INFO+"coresample.yield",
							ExcavatorHandler.mineralVeinYield-vein.getDepletion()));
				ret.append(component.withStyle(ChatFormatting.GRAY));
			}
		}
		sender.sendSuccess(() -> ret, true);
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
			sender.sendSuccess(() -> Component.translatable(Lib.CHAT_COMMAND+
					"mineral.put.success", mineral.getId(), radius, pos.x(), pos.z()), true);
		}
		else
			sender.sendSuccess(() -> Component.translatable(Lib.CHAT_COMMAND+
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
				pair.getFirst().setDepletion(depletion);
			IESaveData.markInstanceDirty();
			sender.sendSuccess(() -> Component.translatable(Lib.CHAT_COMMAND+
					"mineral.set_depletion.success", depletion), true);
		}
		else
			sender.sendSuccess(() -> Component.translatable(Lib.CHAT_COMMAND+
					"mineral.set_depletion.no_mineral", pos.x(), pos.z()), true);
	}

	private static ColumnPos columnPos(Vec3 vec)
	{
		return new ColumnPos((int)vec.x, (int)vec.z);
	}

}
