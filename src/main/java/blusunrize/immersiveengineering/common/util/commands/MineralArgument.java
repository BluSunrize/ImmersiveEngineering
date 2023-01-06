/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// TODO fix static level access hacks
public class MineralArgument implements ArgumentType<MineralMix>
{
	public static final DynamicCommandExceptionType invalidVein = new DynamicCommandExceptionType(
			(input) -> Component.translatable(Lib.CHAT_COMMAND+"mineral.invalid", input));

	@Override
	public MineralMix parse(StringReader reader) throws CommandSyntaxException
	{
		String name = reader.readQuotedString();//TODO does this work properly?
		for(MineralMix mm : getStaticMinerals())
			if(mm.getId().toString().equalsIgnoreCase(name))
				return mm;
		throw invalidVein.create(name);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(getStaticMinerals().stream().map(mix -> "\""+mix.getId()+"\""), builder);
	}

	@Override
	public Collection<String> getExamples()
	{
		List<String> ret = new ArrayList<>();
		for(MineralMix mix : getStaticMinerals())
		{
			ret.add("\""+mix.getId()+"\"");
			if(ret.size() > 5)
				break;
		}
		return ret;
	}

	private Collection<MineralMix> getStaticMinerals()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		Level level = server==null?ImmersiveEngineering.proxy.getClientWorld(): server.overworld();
		return MineralMix.RECIPES.getRecipes(level);
	}
}
