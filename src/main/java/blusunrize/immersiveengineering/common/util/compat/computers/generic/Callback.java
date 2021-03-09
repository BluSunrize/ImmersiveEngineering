package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Callback<T>
{
	private final List<Callback<? super T>> additional = new ArrayList<>();

	public String renameMethod(String javaName)
	{
		return javaName;
	}

	protected final void addAdditional(Callback<? super T> toAdd)
	{
		additional.add(toAdd);
	}

	public final List<Callback<? super T>> getAdditionalCallbacks()
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
}
