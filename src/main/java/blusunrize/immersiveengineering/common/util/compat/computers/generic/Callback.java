/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Callback<T>
{
	private final List<RemappedCallback<T, ?>> additional = new ArrayList<>();

	public String renameMethod(String javaName)
	{
		return javaName;
	}

	protected final <T2>
	void addAdditional(Callback<T2> toAdd, Function<T, T2> remap)
	{
		additional.add(new RemappedCallback<>(toAdd, remap));
	}

	protected final void addAdditional(Callback<? super T> toAdd)
	{
		addAdditional(toAdd, t -> t);
	}

	public final List<RemappedCallback<? super T, ?>> getAdditionalCallbacks()
	{
		return Collections.unmodifiableList(this.additional);
	}

	protected static String capitalize(String desc)
	{
		StringBuilder result = new StringBuilder("");
		while(!desc.isEmpty())
		{
			int space = desc.indexOf(" ");
			String word;
			if(space < 0)
			{
				word = desc;
				desc = "";
			}
			else
			{
				word = desc.substring(0, space);
				desc = desc.substring(space+1);
			}
			result.append(Character.toUpperCase(word.charAt(0)));
			result.append(word.substring(1));
		}
		return result.toString();
	}

	public record RemappedCallback<OuterT, InnerT>(
			Callback<InnerT> inner, Function<OuterT, InnerT> remap
	)
	{
		@Nonnull
		public <T0>
		Collection<ComputerCallback<? super T0>> getCallbacks(
				LuaTypeConverter converters, Function<T0, OuterT> remap0
		) throws IllegalAccessException
		{
			return ComputerCallback.getInClass(inner, converters, remap0.andThen(remap));
		}
	}
}
