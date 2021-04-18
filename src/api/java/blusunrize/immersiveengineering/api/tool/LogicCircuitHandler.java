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
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants.NBT;

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
		XNOR(2, args -> args[0]==args[1], 5);

		private final int argumentCount;
		private final Predicate<boolean[]> operator;
		private final int complexity;

		public static final int TOTAL_MAX_INPUTS = Arrays.stream(values())
				.map(LogicCircuitOperator::getArgumentCount).max(Integer::compareTo).orElse(1);

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

		public TextComponent getDescription()
		{
			return new TranslationTextComponent(Lib.DESC_INFO+"operator."+this.name().toLowerCase(Locale.ENGLISH));
		}
	}

	public enum LogicCircuitRegister
	{
		// All 16 dye colors
		WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY,
		LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK,
		// Plus 8 internal storages
		R0, R1, R2, R3, R4, R5, R6, R7;

		public TextComponent getDescription()
		{
			if(this.ordinal() < 16)
				return new TranslationTextComponent("color.minecraft."+DyeColor.byId(this.ordinal()).getTranslationKey());
			else
				return new TranslationTextComponent(Lib.DESC_INFO+"register", this.ordinal()-16);
		}
	}

	public static final class LogicCircuitInstruction
	{
		private final LogicCircuitOperator operator;
		private final LogicCircuitRegister output;
		private final LogicCircuitRegister[] inputs;

		private final TextComponent formattedString;

		public LogicCircuitInstruction(LogicCircuitOperator operator, LogicCircuitRegister output, LogicCircuitRegister[] inputs)
		{
			Preconditions.checkArgument(operator.getArgumentCount()==inputs.length, "Logic inputs must match the argument count of the operator");
			this.operator = operator;
			this.output = output;
			this.inputs = inputs;

			this.formattedString = this.output.getDescription();
			this.formattedString.appendString(" = ");
			this.formattedString.appendSibling(this.operator.getDescription());
			for(int i = 0; i < inputs.length; i++)
			{
				this.formattedString.appendString(i!=0?", ": ": ");
				this.formattedString.appendSibling(inputs[i].getDescription());
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

		public TextComponent getFormattedString()
		{
			return this.formattedString;
		}

		public CompoundNBT serialize()
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.putString("operator", this.operator.name());
			nbt.putString("output", this.output.name());
			ListNBT inputList = new ListNBT();
			for(LogicCircuitRegister input : this.inputs)
				inputList.add(StringNBT.valueOf(input.name()));
			nbt.put("inputs", inputList);
			return nbt;
		}

		public static LogicCircuitInstruction deserialize(CompoundNBT nbt)
		{
			LogicCircuitOperator operator = LogicCircuitOperator.valueOf(nbt.getString("operator"));
			LogicCircuitRegister output = LogicCircuitRegister.valueOf(nbt.getString("output"));
			ListNBT inputList = nbt.getList("inputs", NBT.TAG_STRING);
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
