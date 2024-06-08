/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

public class LogicCircuitHandler
{
	public enum LogicCircuitOperator
	{
		SET(1, args -> args[0], 0),
		NOT(1, args -> !args[0], 1),
		OR(2, args -> args[0]|args[1], 3),
		AND(2, args -> args[0]&args[1], 2),
		XOR(2, args -> args[0]^args[1], 4),
		NOR(2, args -> !(args[0]|args[1]), 4),
		NAND(2, args -> !(args[0]&args[1]), 1),
		XNOR(2, args -> args[0]==args[1], 5),
		IMPLY(2, args -> (!args[0])|args[1], 2),
		NIMPLY(2, args -> args[0]&!args[1], 3);

		public static final Codec<LogicCircuitOperator> CODEC = Codec.intRange(0, values().length-1)
				.xmap(i -> values()[i], LogicCircuitOperator::ordinal);
		public static final StreamCodec<ByteBuf, LogicCircuitOperator> STREAM_CODEC = ByteBufCodecs.INT
				.map(i -> values()[i], LogicCircuitOperator::ordinal);

		private final int argumentCount;
		private final Predicate<boolean[]> operator;
		private final int complexity;

		public static final int TOTAL_MAX_INPUTS = Arrays.stream(values())
				.mapToInt(LogicCircuitOperator::getArgumentCount)
				.max()
				.orElse(1);

		LogicCircuitOperator(int argumentCount, Predicate<boolean[]> operator, int complexity)
		{
			this.argumentCount = argumentCount;
			this.operator = operator;
			this.complexity = complexity;
		}

		public int getArgumentCount()
		{
			return this.argumentCount;
		}

		public int getComplexity()
		{
			return complexity;
		}

		public boolean apply(boolean[] args)
		{
			return this.operator.test(args);
		}

		@Nullable
		public static LogicCircuitOperator getByString(String name)
		{
			if(name==null)
				return null;
			return valueOf(name);
		}

		public Component getDescription()
		{
			return Component.translatable(Lib.DESC_INFO+"operator."+this.name().toLowerCase(Locale.ENGLISH));
		}
	}

	public enum LogicCircuitRegister
	{
		// All 16 dye colors
		WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY,
		LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK,
		// Plus 8 internal storages
		R0, R1, R2, R3, R4, R5, R6, R7;

		public static final Codec<LogicCircuitRegister> CODEC = Codec.intRange(0, values().length-1)
				.xmap(i -> values()[i], LogicCircuitRegister::ordinal);
		public static final StreamCodec<ByteBuf, LogicCircuitRegister> STREAM_CODEC = ByteBufCodecs.INT
				.map(i -> values()[i], LogicCircuitRegister::ordinal);

		public MutableComponent getDescription()
		{
			if(this.ordinal() < 16)
				return Component.translatable("color.minecraft."+DyeColor.byId(this.ordinal()).getName());
			else
				return Component.translatable(Lib.DESC_INFO+"register", this.ordinal()-16);
		}
	}

	public static final class LogicCircuitInstruction
	{
		public static final Codec<LogicCircuitInstruction> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				LogicCircuitOperator.CODEC.fieldOf("operator").forGetter(i -> i.operator),
				LogicCircuitRegister.CODEC.fieldOf("output").forGetter(i -> i.output),
				LogicCircuitRegister.CODEC.listOf()
						.xmap(l -> l.toArray(new LogicCircuitRegister[0]), Arrays::asList)
						.fieldOf("inputs").forGetter(i -> i.inputs)
		).apply(inst, LogicCircuitInstruction::new));

		public static final StreamCodec<ByteBuf, LogicCircuitInstruction> STREAM_CODEC = StreamCodec.composite(
				LogicCircuitOperator.STREAM_CODEC, i -> i.operator,
				LogicCircuitRegister.STREAM_CODEC, i -> i.output,
				LogicCircuitRegister.STREAM_CODEC.apply(ByteBufCodecs.list())
						.map(l -> l.toArray(new LogicCircuitRegister[0]), Arrays::asList), i -> i.inputs,
				LogicCircuitInstruction::new
		);

		private final LogicCircuitOperator operator;
		private final LogicCircuitRegister output;
		private final LogicCircuitRegister[] inputs;

		private final MutableComponent formattedString;

		public LogicCircuitInstruction(LogicCircuitOperator operator, LogicCircuitRegister output, LogicCircuitRegister[] inputs)
		{
			Preconditions.checkArgument(operator.getArgumentCount()==inputs.length, "Logic inputs must match the argument count of the operator");
			this.operator = operator;
			this.output = output;
			this.inputs = inputs;

			this.formattedString = this.output.getDescription();
			this.formattedString.append(" = ");
			this.formattedString.append(this.operator.getDescription());
			for(int i = 0; i < inputs.length; i++)
			{
				this.formattedString.append(i!=0?", ": ": ");
				this.formattedString.append(inputs[i].getDescription());
			}
		}

		public LogicCircuitOperator getOperator()
		{
			return operator;
		}

		public LogicCircuitRegister getOutput()
		{
			return output;
		}

		public LogicCircuitRegister[] getInputs()
		{
			return inputs;
		}

		public void apply(ILogicCircuitHandler handler)
		{
			boolean[] bInputs = new boolean[operator.getArgumentCount()];
			for(int i = 0; i < inputs.length; i++)
				bInputs[i] = handler.getLogicCircuitRegister(inputs[i]);
			handler.setLogicCircuitRegister(output, operator.apply(bInputs));
		}

		public Component getFormattedString()
		{
			return this.formattedString;
		}

		public CompoundTag serialize()
		{
			CompoundTag nbt = new CompoundTag();
			nbt.putString("operator", this.operator.name());
			nbt.putString("output", this.output.name());
			ListTag inputList = new ListTag();
			for(LogicCircuitRegister input : this.inputs)
				inputList.add(StringTag.valueOf(input.name()));
			nbt.put("inputs", inputList);
			return nbt;
		}

		public static LogicCircuitInstruction deserialize(CompoundTag nbt)
		{
			LogicCircuitOperator operator = LogicCircuitOperator.valueOf(nbt.getString("operator"));
			LogicCircuitRegister output = LogicCircuitRegister.valueOf(nbt.getString("output"));
			ListTag inputList = nbt.getList("inputs", Tag.TAG_STRING);
			LogicCircuitRegister[] inputs = new LogicCircuitRegister[inputList.size()];
			for(int i = 0; i < inputs.length; i++)
				inputs[i] = LogicCircuitRegister.valueOf(inputList.getString(i));
			return new LogicCircuitInstruction(operator, output, inputs);
		}
	}

	public interface ILogicCircuitHandler
	{
		boolean getLogicCircuitRegister(LogicCircuitRegister register);

		void setLogicCircuitRegister(LogicCircuitRegister register, boolean state);
	}
}
