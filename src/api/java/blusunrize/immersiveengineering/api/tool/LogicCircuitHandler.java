/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.Arrays;
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
	}

	public enum LogicCircuitRegister
	{
		// All 16 dye colors
		WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY,
		LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK,
		// Plus 8 internal storages
		R0, R1, R2, R3, R4, R5, R6, R7;
	}

	public static final class LogicCircuitInstruction
	{
		private final LogicCircuitOperator operator;
		private final LogicCircuitRegister output;
		private final LogicCircuitRegister[] inputs;

		private final String formattedString;

		public LogicCircuitInstruction(LogicCircuitOperator operator, LogicCircuitRegister output, LogicCircuitRegister[] inputs)
		{
			Preconditions.checkArgument(operator.getArgumentCount()==inputs.length, "Logic inputs must match the argument count of the operator");
			this.operator = operator;
			this.output = output;
			this.inputs = inputs;

			StringBuilder s = new StringBuilder(output.name()+" = "+operator.name());
			for(int i = 0; i < inputs.length; i++)
				s.append(i!=0?", ": ": ").append(inputs[i].toString());
			this.formattedString = s.toString();
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

		public String getFormattedString()
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
